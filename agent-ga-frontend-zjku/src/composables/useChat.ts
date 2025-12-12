import { ref, nextTick } from 'vue'
import { ChatService } from '@/api/chat'
import type { ChatMessage, ChatHistory, ConversationListItem, MessageDTO } from '@/api/types'

export function useChat() {
  const messages = ref<ChatMessage[]>([])
  const isTyping = ref(false)
  const currentChatId = ref<string | null>(null)
  const chatHistory = ref<ChatHistory[]>([])
  const savedMessageCount = ref(0) // 跟踪已保存的消息数量
  const isSaving = ref(false) // 防止重复保存
  const isSending = ref(false) // 防止重复发送

  // 添加消息
  const addMessage = (role: 'user' | 'assistant', content: string): ChatMessage => {
    // 使用当前时间戳（UTC时间戳），前端格式化时会自动转换为北京时间显示
    const timestamp = Date.now()
    const message: ChatMessage = {
      id: timestamp.toString(),
      role,
      content,
      timestamp: timestamp
    }
    messages.value.push(message)
    return message
  }

  // 更新最后一条AI消息（用于流式输出）
  const updateLastAIMessage = (content: string) => {
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.role === 'assistant') {
      lastMessage.content = content
    }
  }

  // 发送消息到AI
  const sendToAI = async (message: string, isAdvancedMode: boolean) => {
    if (!currentChatId.value || isSending.value) {
      console.error('没有有效的聊天ID或正在发送中')
      return
    }

    isTyping.value = true
    isSending.value = true

    // 开发环境下的调试日志
    if (import.meta.env.DEV) {
      console.log('开始发送消息:', message, '模式:', isAdvancedMode ? '高级' : '普通')
    }

    // 添加空的AI消息用于流式更新
    addMessage('assistant', '')

    let fullContent = ''
    let connectionAttempts = 0
    const maxConnectionAttempts = 1 // 主要重试逻辑在ChatService中处理

    try {
      const config = {
        userMessage: message,
        chatId: currentChatId.value,
        onMessage: (data: string) => {
          if (import.meta.env.DEV) {
            console.log('收到SSE数据:', data)
          }
          // 累积内容
          fullContent += data
          if (import.meta.env.DEV) {
            console.log('累积内容:', fullContent)
          }
          updateLastAIMessage(fullContent)
        },
        onError: (error: Event) => {
          connectionAttempts++
          console.error('SSE连接错误', error)

          // 提供更详细的错误反馈
          const errorMessage = connectionAttempts > maxConnectionAttempts
            ? '抱歉，服务器连接持续失败，请检查网络连接后重试。'
            : '抱歉，连接出现问题，正在尝试重新连接...'

          if (fullContent === '') {
            updateLastAIMessage(errorMessage)
          } else {
            // 如果已经收到部分内容，可以选择追加错误信息
            updateLastAIMessage(fullContent + '\n\n[提示] 由于连接问题，内容可能不完整。')
          }

          // 延迟设置状态为false，以便显示错误消息
          setTimeout(() => {
            isTyping.value = false
            isSending.value = false
          }, 500)
        },
        onComplete: () => {
          if (import.meta.env.DEV) {
            console.log('SSE流完成，最终内容:', fullContent)
          }

          // 对话完成后保存消息到后端
          saveChatHistoryToBackend()

          // 延迟设置状态为false，让用户看到完整的消息
          setTimeout(() => {
            isTyping.value = false
            isSending.value = false
          }, 200)
        }
      }

      if (isAdvancedMode) {
        ChatService.sendAdvancedChat(config)
      } else {
        ChatService.sendNormalChat(config)
      }
    } catch (error) {
      console.error('发送消息失败:', error)

      if (fullContent === '') {
        updateLastAIMessage('抱歉，发送消息失败，请检查网络连接或稍后重试。')
      } else {
        updateLastAIMessage(fullContent + '\n\n[提示] 由于发送过程中断，内容可能不完整。')
      }

      isTyping.value = false
      isSending.value = false
    }
  }

  // 开始新对话
  const startNewChat = () => {
    currentChatId.value = Date.now().toString()
    messages.value = []
    savedMessageCount.value = 0 // 重置已保存消息计数
    // 移除预设的欢迎消息，让欢迎界面显示
  }

  // 加载历史对话
  const loadChat = async (chatId: string) => {
    try {
      currentChatId.value = chatId
      const historyResponse = await ChatService.getChatHistory(chatId)

      // 对后端返回的消息进行去重处理（基于内容和时间戳）
      const uniqueMessages = historyResponse.messages.reduce((acc: MessageDTO[], current: MessageDTO, index: number) => {
        // 检查是否已存在相同内容和时间戳的消息
        const existingIndex = acc.findIndex(msg =>
          msg.content === current.content &&
          msg.messageType === current.messageType &&
          Math.abs((msg.timestamp || 0) - (current.timestamp || 0)) <= 5000 // 5秒内的消息认为是重复的
        )

        if (existingIndex === -1) {
          acc.push(current)
        } else {
          console.log(`发现重复消息，已去重: ${current.content.substring(0, 30)}...`)
        }

        return acc
      }, [])

      // 使用后端返回的实际时间戳
      messages.value = uniqueMessages.map((msg: MessageDTO, index: number) => ({
        id: `${chatId}_${msg.timestamp}_${index}`, // 使用时间戳+索引作为唯一ID
        role: msg.messageType.toLowerCase() as 'user' | 'assistant',
        content: msg.content,
        timestamp: msg.timestamp || Date.now() // 使用后端时间戳，如果没有则使用当前时间
      }))

      savedMessageCount.value = messages.value.length // 设置已保存的消息数量
      // 重置isTyping状态，避免加载历史记录时显示正在输入
      isTyping.value = false

      console.log(`加载历史对话完成，去重后消息数量: ${messages.value.length}`)
    } catch (error) {
      console.error('加载历史对话失败:', error)
      messages.value = []
      savedMessageCount.value = 0
    }
  }

  // 保存聊天历史到后端（只保存新增的消息）
  const saveChatHistoryToBackend = async () => {
    if (!currentChatId.value || isSaving.value) {
      console.log('跳过保存：无对话ID或正在保存中', { currentChatId: currentChatId.value, isSaving: isSaving.value })
      return
    }

    // 防止重复保存
    isSaving.value = true
    console.log('开始保存聊天历史到后端')

    try {
      // 由于MessageChatMemoryAdvisor会自动保存消息，我们采用更保守的策略
      // 只保存最后一条用户消息，确保它被保存（AI消息会自动保存）
      const lastUserMessage = messages.value.slice(savedMessageCount.value).find(msg => msg.role === 'user')

      if (lastUserMessage && lastUserMessage.content.trim() !== '') {
        console.log('保存最后一条用户消息:', lastUserMessage.content.substring(0, 50) + '...')
        const messageDTO: MessageDTO = {
          content: lastUserMessage.content,
          messageType: lastUserMessage.role.toUpperCase(),
          timestamp: lastUserMessage.timestamp
        }
        await ChatService.addMessage(currentChatId.value, messageDTO)
        console.log('用户消息保存完成')
      } else {
        console.log('没有新的用户消息需要保存')
      }

      // 更新已保存的消息数量
      // 注意：这里我们保守地只增加1（用户消息），AI消息应该已经通过自动机制保存了
      if (lastUserMessage) {
        savedMessageCount.value += 1
        console.log(`更新已保存消息数量为: ${savedMessageCount.value}`)
      }

      // 重新加载对话列表
      await loadChatHistory()

      console.log('聊天历史保存完成')
    } catch (error) {
      console.error('保存聊天历史失败:', error)
    } finally {
      // 无论成功还是失败，都重置保存状态
      isSaving.value = false
      console.log('保存状态已重置')
    }
  }

  // 保留原有的saveChatHistory函数用于兼容性（如果需要的话）
  const saveChatHistory = saveChatHistoryToBackend

  // 加载聊天历史列表
  const loadChatHistory = async () => {
    try {
      const conversationList = await ChatService.getConversationList()
      chatHistory.value = conversationList.map((item: ConversationListItem) => ({
        id: item.conversationId,
        title: item.lastUserMessagePreview || '新对话',
        timestamp: item.lastMessageTimestamp
      }))
    } catch (error) {
      console.error('加载聊天历史列表失败:', error)
      chatHistory.value = []
    }
  }

  // 删除指定对话的历史记录
  const deleteChatHistory = async (chatId: string) => {
    try {
      await ChatService.clearChatHistory(chatId)
      // 从列表中移除
      chatHistory.value = chatHistory.value.filter(chat => chat.id !== chatId)
      // 如果删除的是当前对话，清空消息
      if (currentChatId.value === chatId) {
        messages.value = []
        currentChatId.value = null
        savedMessageCount.value = 0
      }
    } catch (error) {
      console.error('删除历史记录失败:', error)
    }
  }

  // 清除所有对话的历史记录
  const clearAllChatHistory = async () => {
    try {
      const result = await ChatService.clearAllChatHistory()
      if (result.success) {
        // 清空前端所有状态
        chatHistory.value = []
        messages.value = []
        currentChatId.value = null
        savedMessageCount.value = 0
        // 重新开始新对话
        startNewChat()
        console.log('所有历史记录已成功清除')
      } else {
        console.error('清除历史记录失败:', result.message)
        throw new Error(result.message || '清除历史记录失败')
      }
    } catch (error) {
      console.error('清除所有历史记录失败:', error)
      // 即使清除失败，也尝试重新加载历史记录以确保状态同步
      await loadChatHistory()
    }
  }

  // 格式化时间 - 显示北京时间
  const formatTime = (timestamp: number): string => {
    try {
      // 处理无效时间戳的情况
      if (!timestamp || timestamp <= 0 || isNaN(timestamp)) {
        console.warn('时间戳无效，使用当前时间:', timestamp)
        return new Date().toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          timeZone: 'Asia/Shanghai'
        })
      }

      // timestamp是UTC毫秒时间戳，直接创建Date对象
      const date = new Date(timestamp)
      const now = new Date()

      // 验证时间戳是否合理（放宽限制）
      // 允许稍微未来的时间（比如时钟偏差），但不允许太久以前的时间
      const oneHourFromNow = now.getTime() + (60 * 60 * 1000) // 允许1小时内的未来时间
      const tenYearsAgo = now.getTime() - (10 * 365 * 24 * 60 * 60 * 1000) // 10年前

      if (timestamp > oneHourFromNow || timestamp < tenYearsAgo) {
        console.warn('时间戳超出合理范围，使用当前时间:', timestamp, '现在时间:', now.getTime())
        return new Date().toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          timeZone: 'Asia/Shanghai'
        })
      }

      // 检查Date对象是否有效
      if (isNaN(date.getTime())) {
        console.warn('创建的Date对象无效，使用当前时间:', timestamp)
        return new Date().toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          timeZone: 'Asia/Shanghai'
        })
      }

      // 计算天数差（基于UTC时间）
      const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))

      // 使用北京时间进行格式化
      if (diffDays === 0) {
        // 今天 - 显示北京时间
        return date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          timeZone: 'Asia/Shanghai'
        })
      } else if (diffDays === 1) {
        // 昨天 - 显示北京时间
        return `昨天 ${date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          timeZone: 'Asia/Shanghai'
        })}`
      } else if (diffDays < 7) {
        // 一周内 - 显示北京时间
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        return `${weekdays[date.getUTCDay()]} ${date.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit',
          timeZone: 'Asia/Shanghai'
        })}`
      } else {
        // 更早 - 显示北京时间
        return date.toLocaleDateString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          timeZone: 'Asia/Shanghai'
        })
      }
    } catch (error) {
      console.error('时间格式化失败:', error, timestamp)
      // 出错时返回当前时间，而不是"时间未知"
      return new Date().toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        timeZone: 'Asia/Shanghai'
      })
    }
  }

  // 格式化消息内容
  const formatMessage = (content: string): string => {
    return content.replace(/\n/g, '<br>')
  }

  return {
    messages,
    isTyping,
    currentChatId,
    chatHistory,
    isSaving,
    isSending,
    addMessage,
    sendToAI,
    startNewChat,
    loadChat,
    saveChatHistory,
    loadChatHistory,
    deleteChatHistory,
    clearAllChatHistory,
    formatTime,
    formatMessage
  }
}
