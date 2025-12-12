<template>
  <div class="chat-container">
    <!-- 左侧边栏 - 历史对话 -->
    <div class="sidebar" :class="{ collapsed: !sidebarVisible }">
      <div class="sidebar-header">
        <h3>历史对话</h3>
        <div class="header-buttons">
          <button class="clear-all-btn" @click="confirmClearAll" title="清除所有历史记录">
            🗑️
          </button>
          <button class="new-chat-btn" @click="startNewChat">
            <span>+</span> 新对话
          </button>
        </div>
      </div>
      <div class="chat-history">
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="history-item"
          :class="{ active: chat.id === currentChatId }"
        >
          <div class="history-content" @click="loadChat(chat.id)">
            <div class="history-title">{{ chat.title }}</div>
            <div class="history-time">{{ formatTime(chat.timestamp) }}</div>
          </div>
          <button
            class="delete-btn"
            @click="confirmDelete(chat.id)"
            title="删除对话"
          >
            🗑️
          </button>
        </div>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="main-chat" :style="mainChatStyle">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="header-left">
          <button class="sidebar-toggle" @click="toggleSidebar">
            <span>☰</span>
          </button>
          <h2>张家口学院明湖校区校园智能问答助手</h2>
        </div>
        <div class="header-right">
          <button
            class="mode-toggle"
            :class="{ active: isAdvancedMode }"
            @click="toggleMode"
          >
            高级模式
          </button>
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="messages-container" ref="messagesContainer">
        <!-- 欢迎页面 -->
        <div v-if="messages.length === 0" class="welcome-screen">
          <div class="welcome-content">
            <div class="welcome-logo">
              <img src="/assets/images/zjklogo.jpg" alt="张家口学院Logo" class="welcome-logo-image" />
            </div>
            <div class="welcome-text">
              <h2>欢迎使用张家口学院明湖校区校园智能问答助手</h2>
              <p>我是您的专属校园AI助手，可以为您解答各类校园问题</p>
              <div class="welcome-features">
                <div class="welcome-feature">
                  <span class="welcome-feature-icon">📚</span>
                  <span>课程信息查询</span>
                </div>
                <div class="welcome-feature">
                  <span class="welcome-feature-icon">🏫</span>
                  <span>校园服务指南</span>
                </div>
                <div class="welcome-feature">
                  <span class="welcome-feature-icon">🤝</span>
                  <span>智能问答</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div
          v-for="(message, index) in messages"
          :key="message.id"
          class="message"
          :class="{ 'message-user': message.role === 'user', 'message-assistant': message.role === 'assistant' }"
        >
          <div class="message-avatar">
            <img
              v-if="message.role === 'user'"
              src="/assets/images/user.jpg"
              alt="用户头像"
              class="avatar-image"
            />
            <img
              v-else-if="message.role === 'assistant'"
              src="/assets/images/zjklogo.jpg"
              alt="AI助手头像"
              class="avatar-image"
            />
          </div>
          <div class="message-content">
            <!-- 如果是最后一条AI消息且正在输入且内容为空，则显示正在输入状态 -->
            <div v-if="message.role === 'assistant' && index === messages.length - 1 && isTyping && message.content.trim() === ''">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div class="message-text">正在输入...</div>
            </div>
            <!-- 否则显示正常消息内容 -->
            <div v-else>
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-container">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact="addNewLine"
            placeholder="输入您的问题..."
            :disabled="isTyping"
            ref="messageInput"
            rows="1"
          ></textarea>
          <button
            class="send-btn"
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isTyping"
          >
            <span>发送</span>
          </button>
        </div>
        <div class="input-footer">
          <span class="mode-indicator">
            当前模式: {{ isAdvancedMode ? '高级模式' : '普通模式' }}
          </span>
          <span class="hint">Enter 发送，Shift+Enter 换行</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, watch, computed } from 'vue'
import { useChat } from '@/composables/useChat'
import { ChatService } from '@/api/chat'

// 使用组合式函数
const {
  messages,
  isTyping,
  currentChatId,
  chatHistory,
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
} = useChat()

// 本地响应式数据
const inputMessage = ref('')
const isAdvancedMode = ref(false)
const sidebarVisible = ref(true)

// DOM引用
const messagesContainer = ref<HTMLElement>()
const messageInput = ref<HTMLTextAreaElement>()

// 计算属性
const mainChatStyle = computed(() => ({
  marginLeft: sidebarVisible.value ? '0px' : '0px'
}))

// 初始化
onMounted(() => {
  // 在移动端默认隐藏侧边栏
  const isMobile = window.innerWidth <= 768
  sidebarVisible.value = !isMobile

  startNewChat()
  loadChatHistory()
})

// 清理
onUnmounted(() => {
  ChatService.closeConnection()
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
}

// 切换模式
const toggleMode = () => {
  isAdvancedMode.value = !isAdvancedMode.value
}

  // 发送消息
  const sendMessage = async () => {
    if (!inputMessage.value.trim() || isTyping.value) return

    const userMessage = inputMessage.value.trim()
    inputMessage.value = ''

    // 添加用户消息
    addMessage('user', userMessage)

    // 开始AI回复（回复完成后会自动保存历史记录）
    await sendToAI(userMessage, isAdvancedMode.value)
  }

// 添加换行
const addNewLine = () => {
  inputMessage.value += '\n'
}

// 确认删除对话
const confirmDelete = (chatId: string) => {
  if (confirm('确定要删除这个对话吗？此操作不可恢复。')) {
    deleteChatHistory(chatId)
  }
}

// 确认清除所有历史记录
const confirmClearAll = () => {
  if (confirm('确定要清除所有历史记录吗？此操作不可恢复。')) {
    clearAllChatHistory()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 自动调整输入框高度
const adjustTextareaHeight = () => {
  if (messageInput.value) {
    messageInput.value.style.height = 'auto'
    messageInput.value.style.height = messageInput.value.scrollHeight + 'px'
  }
}

// 监听输入框变化
watch(inputMessage, adjustTextareaHeight)

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  background: #f5f5f5;
}

/* 左侧边栏 */
.sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
  position: relative;
}

.sidebar.collapsed {
  transform: translateX(-100%);
}

.sidebar-header {
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.new-chat-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.clear-all-btn {
  background: #dc3545;
  color: white;
  border: none;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.clear-all-btn:hover {
  background: #c82333;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.history-item {
  padding: 0.8rem;
  margin-bottom: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.history-item:hover {
  background: #f8f9fa;
}

.history-item.active {
  background: #e3f2fd;
  border-left: 3px solid #007bff;
}

.history-title {
  font-weight: 500;
  margin-bottom: 0.3rem;
  font-size: 0.9rem;
}

.history-time {
  font-size: 0.8rem;
  color: #666;
}

.history-content {
  flex: 1;
  cursor: pointer;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.3rem;
  border-radius: 4px;
  opacity: 0.7;
  transition: opacity 0.2s, background-color 0.2s;
  font-size: 0.9rem;
}

.delete-btn:hover {
  opacity: 1;
  background: rgba(255, 0, 0, 0.1);
}

/* 主聊天区域 */
.main-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  margin-left: 0;
  transition: margin-left 0.3s ease;
}

/* 聊天头部 */
.chat-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.sidebar-toggle {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
}

.sidebar-toggle:hover {
  background: #f5f5f5;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.mode-toggle {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.mode-toggle.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

/* 消息区域 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 欢迎页面 */
.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  text-align: center;
}

.welcome-content {
  max-width: 600px;
  width: 100%;
}

.welcome-logo {
  margin-bottom: 2rem;
}

.welcome-logo-image {
  width: 150px;
  height: 150px;
  border-radius: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border: 3px solid rgba(255, 107, 107, 0.2);
}

.welcome-text h2 {
  color: #333;
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.welcome-text p {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.welcome-features {
  display: flex;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
}

.welcome-feature {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.8);
  padding: 1.5rem 1rem;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.1);
  min-width: 120px;
  transition: all 0.3s ease;
}

.welcome-feature:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.welcome-feature-icon {
  font-size: 2rem;
}

.message {
  display: flex;
  gap: 0.8rem;
  max-width: 80%;
}

.message-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-user .message-content {
  background: #007bff;
  color: white;
}

.message-assistant {
  align-self: flex-start;
}

.message-assistant .message-content {
  background: #f8f9fa;
  color: #333;
}

.message-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  background: white;
  border: 1px solid #e0e0e0;
  overflow: hidden;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.message-user .message-avatar {
  background: #007bff;
  color: white;
}

.message-content {
  padding: 0.8rem 1rem;
  border-radius: 18px;
  position: relative;
}

.message-text {
  line-height: 1.5;
  word-wrap: break-word;
}

.message-time {
  font-size: 0.8rem;
  opacity: 0.7;
  margin-top: 0.3rem;
}

/* 输入区域 */
.input-container {
  border-top: 1px solid #e0e0e0;
  padding: 1rem 1.5rem;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}

.input-wrapper textarea {
  flex: 1;
  padding: 0.8rem 1rem;
  border: 1px solid #dee2e6;
  border-radius: 20px;
  resize: none;
  font-family: inherit;
  font-size: 0.95rem;
  line-height: 1.4;
  max-height: 120px;
  overflow-y: auto;
}

.input-wrapper textarea:focus {
  outline: none;
  border-color: #007bff;
}

.send-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #0056b3;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: #666;
}

.mode-indicator {
  font-weight: 500;
}

/* 打字机效果 */
.typing-indicator {
  display: flex;
  gap: 0.2rem;
  margin-bottom: 0.5rem;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #666;
  animation: typing 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.4;
    transform: scale(0.8);
  }
  30% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 平板端响应式设计 (768px - 1024px) */
@media (min-width: 769px) and (max-width: 1024px) {
  .sidebar {
    width: 250px;
  }

  .main-chat {
    margin-left: 250px;
  }

  .message {
    max-width: 85%;
  }

  .chat-header h2 {
    font-size: 1.1rem;
  }

  .messages-container {
    padding: 1.2rem 1.8rem;
  }

  .input-container {
    padding: 1.2rem 1.8rem;
  }

  .welcome-logo-image {
    width: 120px;
    height: 120px;
  }

  .welcome-text h2 {
    font-size: 1.5rem;
  }

  .welcome-text p {
    font-size: 1rem;
  }

  .welcome-features {
    gap: 1.5rem;
  }

  .welcome-feature {
    padding: 1.2rem 0.8rem;
    min-width: 100px;
  }
}

/* 移动端响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: absolute;
    z-index: 1000;
    height: 100vh;
    width: 280px;
  }

  .sidebar.collapsed {
    transform: translateX(-100%);
  }

  .message {
    max-width: 90%;
  }

  .chat-header h2 {
    font-size: 1rem;
  }

  .welcome-screen {
    padding: 1rem;
  }

  .welcome-logo-image {
    width: 100px;
    height: 100px;
  }

  .welcome-text h2 {
    font-size: 1.3rem;
  }

  .welcome-text p {
    font-size: 0.95rem;
  }

  .welcome-features {
    gap: 1rem;
  }

  .welcome-feature {
    padding: 1rem 0.8rem;
    min-width: 90px;
  }

  .welcome-feature-icon {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .chat-header {
    padding: 0.8rem 1rem;
  }

  .messages-container {
    padding: 0.8rem 1rem;
  }

  .input-container {
    padding: 0.8rem 1rem;
  }

  .message-content {
    padding: 0.6rem 0.8rem;
  }

  .send-btn {
    padding: 0.6rem 1rem;
  }

  .welcome-screen {
    padding: 0.5rem;
  }

  .welcome-logo-image {
    width: 80px;
    height: 80px;
  }

  .welcome-text h2 {
    font-size: 1.1rem;
  }

  .welcome-text p {
    font-size: 0.9rem;
  }

  .welcome-features {
    flex-direction: column;
    gap: 0.8rem;
  }

  .welcome-feature {
    padding: 0.8rem 0.6rem;
    min-width: auto;
    width: 100%;
    max-width: 200px;
  }
}
</style>
