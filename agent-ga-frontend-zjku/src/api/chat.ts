import type { SSEConfig, ConversationListItem, ChatHistoryResponse, MessageDTO } from './types'
import { authHeaders, getToken } from '@/composables/useAuth'

const BASE_URL = '/api'

/** 与 SSE 一致：附带 access_token，避免仅依赖 Authorization 时跨域预检/浏览器策略导致鉴权失败 */
function withAccessToken(url: string): string {
  const token = getToken()
  if (!token) return url
  const q = `access_token=${encodeURIComponent(token)}`
  return url.includes('?') ? `${url}&${q}` : `${url}?${q}`
}

export class ChatService {
  private static eventSource: EventSource | null = null

  /**
   * 发送普通模式聊天请求
   */
  static sendNormalChat(config: Omit<SSEConfig, 'endpoint'>) {
    const endpoint = `${BASE_URL}/ai/campus_app/chat/rag/stream`
    this.sendSSEChat({ ...config, endpoint })
  }

  /**
   * 发送高级模式聊天请求
   */
  static sendAdvancedChat(config: Omit<SSEConfig, 'endpoint'>) {
    const endpoint = `${BASE_URL}/ai/campus_app/chat/tool/stream`
    this.sendSSEChat({ ...config, endpoint })
  }

  /**
   * 通用SSE聊天方法
   */
  private static sendSSEChat(config: SSEConfig) {
    // 关闭之前的连接
    if (this.eventSource) {
      this.eventSource.close()
    }

    const token = getToken()
    let url = `${config.endpoint}?userMessage=${encodeURIComponent(config.userMessage)}&chatId=${encodeURIComponent(config.chatId)}`
    if (token) {
      url += `&access_token=${encodeURIComponent(token)}`
    }

    this.eventSource = new EventSource(url)
    let streamCompleted = false // 标记流是否已正常完成

    this.eventSource.onmessage = (event) => {
      const rawData = event.data.trim()

      // 开发环境下的调试日志
      if (import.meta.env.DEV) {
        console.log('SSE received:', rawData)
      }

      // 去除SSE格式前缀 "data: "
      const data = rawData.startsWith('data: ') ? rawData.substring(6) : rawData

      // 检查是否是结束信号或错误
      if (data === '' || data === '[DONE]') {
        if (import.meta.env.DEV) {
          console.log('SSE stream completed normally')
        }
        streamCompleted = true
        const done = config.onComplete?.()
        if (done != null && typeof (done as Promise<void>).then === 'function') {
          void (done as Promise<void>).catch((err) => console.error('SSE onComplete 失败:', err))
        }
        this.eventSource?.close()
        this.eventSource = null
        return
      }

      // 检查是否包含错误信息
      if (data.includes('服务异常')) {
        console.error('SSE service error:', data)
        streamCompleted = true // 标记为已处理
        config.onError(new Event('error'))
        this.eventSource?.close()
        this.eventSource = null
        return
      }

      // 正常数据
      if (import.meta.env.DEV) {
        console.log('SSE normal data:', data)
      }
      config.onMessage(data)
    }

    this.eventSource.onerror = (error) => {
      // 如果流已经正常完成，则忽略这个错误（服务器正常关闭连接）
      if (streamCompleted) {
        if (import.meta.env.DEV) {
          console.log('SSE connection closed normally after stream completion')
        }
        return
      }

      // 只有在流未完成时才报告错误
      console.error('SSE connection error:', error)
      config.onError(error)
      this.eventSource?.close()
      this.eventSource = null
    }

    this.eventSource.onopen = () => {
      if (import.meta.env.DEV) {
        console.log('SSE connection opened successfully')
      }
    }
  }

  /**
   * 关闭SSE连接
   */
  static closeConnection() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
  }

  /**
   * 获取对话列表
   */
  static async getConversationList(): Promise<ConversationListItem[]> {
    const response = await fetch(withAccessToken(`${BASE_URL}/ai/chat/history/database/list`), {
      headers: { ...authHeaders() }
    })
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED')
    }
    if (!response.ok) {
      throw new Error('获取对话列表失败')
    }
    return response.json()
  }

  /**
   * 获取指定对话的历史记录
   */
  static async getChatHistory(conversationId: string): Promise<ChatHistoryResponse> {
    const response = await fetch(
      withAccessToken(
        `${BASE_URL}/ai/chat/history/database?conversationId=${encodeURIComponent(conversationId)}`
      ),
      { headers: { ...authHeaders() } }
    )
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED')
    }
    if (!response.ok) {
      throw new Error('获取历史对话失败')
    }
    return response.json()
  }

  /**
   * 添加消息到历史记录
   */
  static async addMessage(conversationId: string, message: MessageDTO): Promise<{ success: boolean; message: string }> {
    const response = await fetch(
      withAccessToken(
        `${BASE_URL}/ai/chat/history/database/add?conversationId=${encodeURIComponent(conversationId)}`
      ),
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...authHeaders()
        },
        body: JSON.stringify(message)
      }
    )
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED')
    }
    return response.json()
  }

  /**
   * 清除指定对话的历史记录
   */
  static async clearChatHistory(conversationId: string): Promise<{ success: boolean; message: string }> {
    const response = await fetch(
      withAccessToken(
        `${BASE_URL}/ai/chat/history/database?conversationId=${encodeURIComponent(conversationId)}`
      ),
      {
        method: 'DELETE',
        headers: { ...authHeaders() }
      }
    )
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED')
    }
    return response.json()
  }

  /**
   * 清除所有对话的历史记录
   */
  static async clearAllChatHistory(): Promise<{ success: boolean; message: string }> {
    const response = await fetch(withAccessToken(`${BASE_URL}/ai/chat/history/database/clearAll`), {
      method: 'DELETE',
      headers: { ...authHeaders() }
    })
    if (response.status === 401) {
      throw new Error('UNAUTHORIZED')
    }
    return response.json()
  }
}
