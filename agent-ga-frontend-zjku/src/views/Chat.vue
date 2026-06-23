<template>
  <div class="layout">
    <aside class="sidebar" :class="{ collapsed: !sidebarVisible }" aria-label="会话列表">
      <div class="sidebar-top">
        <div class="sidebar-brand">
          <span class="brand-mark" aria-hidden="true" />
          <span class="brand-text">会话</span>
        </div>
        <div class="sidebar-actions">
          <button type="button" class="icon-btn danger" title="清空全部会话" @click="confirmClearAll">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m3 0v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6h14zM10 11v6M14 11v6" />
            </svg>
          </button>
          <button type="button" class="btn-primary" @click="startNewChatAndCloseSidebar">新建会话</button>
        </div>
      </div>
      <div class="sidebar-list">
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="session-row"
          :class="{ active: chat.id === currentChatId }"
        >
          <button type="button" class="session-main" @click="loadChatAndCloseSidebar(chat.id)">
            <span class="session-title">{{ chat.title }}</span>
            <span class="session-meta">
              <span
                v-if="chat.firstPreview && chat.firstPreview !== chat.title"
                class="session-meta-first"
                title="首条提问"
              >{{ chat.firstPreview }}</span>
              <time class="session-meta-time" :datetime="String(chat.timestamp)">{{ formatTime(chat.timestamp) }}</time>
            </span>
          </button>
          <button type="button" class="icon-btn ghost" title="删除此会话" @click.stop="confirmDelete(chat.id)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2m3 0v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6h14z" />
            </svg>
          </button>
        </div>
      </div>
      <div class="sidebar-footer">
        <UserMenu variant="sidebar" />
      </div>
    </aside>

    <div class="main">
      <header class="topbar">
        <div class="topbar-left">
          <button type="button" class="icon-btn" title="展开/收起侧栏" @click="toggleSidebar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <div class="titles">
            <h1 class="title">张家口学院明湖校区 · 校园信息咨询</h1>
            <p class="subtitle">教务、办事流程与校园常见问题；高级模式启用工具集（含联网检索等）</p>
          </div>
        </div>
        <div class="topbar-right">
          <button
            type="button"
            class="toggle-pill"
            :class="{ on: isAdvancedMode }"
            @click="toggleMode"
          >
            <span class="toggle-dot" aria-hidden="true" />
            {{ isAdvancedMode ? '高级模式：开' : '高级模式：关' }}
          </button>
        </div>
      </header>

      <main class="thread" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty">
          <div class="empty-card">
            <img src="/assets/images/zjklogo.jpg" alt="" class="empty-logo" />
            <h2 class="empty-heading">开始一次咨询</h2>
            <p class="empty-lead">
              在下方输入问题即可。<strong>普通模式</strong>优先依据校内知识库与检索增强回答；开启<strong>高级模式</strong>后，系统可调用已注册工具（如联网搜索、网页与文件相关能力等），耗时会略长，请按需使用。
            </p>
            <ul class="empty-list">
              <li>课程、学分、选课与考试相关说明</li>
              <li>奖助、办事流程与校园服务指引</li>
              <li>需要外部公开信息或工具协助时，请开启高级模式并注意信息来源与时效</li>
            </ul>
          </div>
        </div>

        <div
          v-for="(message, index) in messages"
          :key="message.id"
          class="bubble-row"
          :class="message.role === 'user' ? 'from-user' : 'from-assistant'"
        >
          <div class="bubble-avatar" aria-hidden="true">
            <img
              v-if="message.role === 'user'"
              src="/assets/images/user.jpg"
              alt=""
              class="bubble-avatar-img"
            />
            <img
              v-else
              src="/assets/images/zjklogo.jpg"
              alt=""
              class="bubble-avatar-img"
            />
          </div>
          <div class="bubble">
            <div
              v-if="message.role === 'assistant' && index === messages.length - 1 && isTyping && message.content.trim() === ''"
              class="bubble-body waiting"
            >
              <div class="dots" aria-live="polite">
                <span /><span /><span />
              </div>
              <span class="waiting-text">正在生成回复…</span>
            </div>
            <div v-else class="bubble-body">
              <div class="bubble-text">{{ message.content }}</div>
              <time class="bubble-time">{{ formatTime(message.timestamp) }}</time>
            </div>
          </div>
        </div>
      </main>

      <footer class="composer">
        <div class="composer-inner">
          <textarea
            v-model="inputMessage"
            class="composer-input"
            @keydown.enter.exact.prevent="sendMessage"
            @keydown.enter.shift.exact="addNewLine"
            placeholder="请输入您的问题…"
            :disabled="isTyping"
            ref="messageInput"
            rows="1"
            aria-label="问题输入"
          />
          <button
            type="button"
            class="btn-send"
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isTyping"
          >
            发送
          </button>
        </div>
        <div class="composer-hint">
          <span>{{ isAdvancedMode ? '当前：高级模式（工具可用）' : '当前：普通模式（校内知识优先）' }}</span>
          <span>Enter 发送 · Shift+Enter 换行</span>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useChat } from '@/composables/useChat'
import { ChatService } from '@/api/chat'
import UserMenu from '@/components/UserMenu.vue'

const {
  messages,
  isTyping,
  currentChatId,
  chatHistory,
  addMessage,
  sendToAI,
  startNewChat,
  loadChat,
  loadChatHistory,
  deleteChatHistory,
  clearAllChatHistory,
  formatTime
} = useChat()

const inputMessage = ref('')
const isAdvancedMode = ref(false)
const sidebarVisible = ref(true)

const messagesContainer = ref<HTMLElement>()
const messageInput = ref<HTMLTextAreaElement>()

onMounted(async () => {
  const isMobile = window.innerWidth <= 768
  sidebarVisible.value = !isMobile
  startNewChat()
  await loadChatHistory()
})

onUnmounted(() => {
  ChatService.closeConnection()
})

const toggleSidebar = () => {
  sidebarVisible.value = !sidebarVisible.value
}

const toggleMode = () => {
  isAdvancedMode.value = !isAdvancedMode.value
}

const loadChatAndCloseSidebar = async (chatId: string) => {
  // 在移动端自动关闭侧边栏
  const isMobile = window.innerWidth <= 768
  if (isMobile) {
    sidebarVisible.value = false
  }
  await loadChat(chatId)
}

const startNewChatAndCloseSidebar = () => {
  // 在移动端自动关闭侧边栏
  const isMobile = window.innerWidth <= 768
  if (isMobile) {
    sidebarVisible.value = false
  }
  startNewChat()
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return
  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  addMessage('user', userMessage)
  await sendToAI(userMessage, isAdvancedMode.value)
}

const addNewLine = () => {
  inputMessage.value += '\n'
}

const confirmDelete = (chatId: string) => {
  if (confirm('确定删除该会话？此操作不可恢复。')) {
    deleteChatHistory(chatId)
  }
}

const confirmClearAll = () => {
  if (confirm('确定清空全部会话记录？此操作不可恢复。')) {
    clearAllChatHistory()
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const adjustTextareaHeight = () => {
  if (messageInput.value) {
    messageInput.value.style.height = 'auto'
    messageInput.value.style.height = messageInput.value.scrollHeight + 'px'
  }
}

watch(inputMessage, adjustTextareaHeight)

watch(
  messages,
  () => {
    nextTick(() => scrollToBottom())
  },
  { deep: true }
)
</script>

<style scoped>
.layout {
  --c-page: #e8eef7;
  --c-bg: #f1f5f9;
  --c-surface: #ffffff;
  --c-border: #94a3b8;
  --c-border-light: #cbd5e1;
  --c-text: #0f172a;
  --c-muted: #64748b;
  --c-primary: #1d4ed8;
  --c-primary-hover: #1e40af;
  --c-primary-soft: #dbeafe;
  --c-primary-ring: rgba(29, 78, 216, 0.22);
  --c-danger: #dc2626;
  --c-danger-bg: #fef2f2;
  --sidebar-w: min(300px, 88vw);
  display: flex;
  height: 100vh;
  height: 100dvh;
  max-height: 100vh;
  max-height: 100dvh;
  overflow: hidden;
  background: var(--c-page);
  color: var(--c-text);
}

.sidebar {
  width: var(--sidebar-w);
  flex-shrink: 0;
  background: var(--c-surface);
  border-right: 2px solid var(--c-border-light);
  display: flex;
  flex-direction: column;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  z-index: 20;
  box-shadow: 2px 0 12px rgba(15, 23, 42, 0.06);
}

.sidebar.collapsed {
  transform: translateX(-100%);
  position: absolute;
  height: 100%;
  height: 100dvh;
  box-shadow: 4px 0 24px rgba(15, 23, 42, 0.12);
}

.sidebar-top {
  padding: 1rem 1rem 0.875rem;
  border-bottom: 2px solid var(--c-border-light);
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.brand-mark {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  background: var(--c-primary);
}

.brand-text {
  font-size: 0.8125rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: #475569;
  text-transform: uppercase;
}

.sidebar-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  flex: 1;
  border: none;
  background: linear-gradient(180deg, #2563eb 0%, var(--c-primary) 100%);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 700;
  padding: 0.55rem 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  box-shadow: 0 2px 6px rgba(29, 78, 216, 0.35);
}

.btn-primary:hover {
  background: linear-gradient(180deg, var(--c-primary) 0%, var(--c-primary-hover) 100%);
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 2px solid var(--c-border-light);
  border-radius: 8px;
  background: var(--c-surface);
  color: #475569;
  cursor: pointer;
  font-family: inherit;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}

.icon-btn:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
  background: var(--c-primary-soft);
}

.icon-btn.danger {
  border-color: #fecaca;
  background: var(--c-danger-bg);
  color: var(--c-danger);
}

.icon-btn.danger:hover {
  border-color: var(--c-danger);
  background: #fee2e2;
}

.icon-btn.ghost {
  border: 2px solid transparent;
  background: transparent;
  width: 36px;
  height: 36px;
  color: #64748b;
}

.icon-btn.ghost:hover {
  border-color: var(--c-border-light);
  color: var(--c-danger);
  background: var(--c-danger-bg);
}

.sidebar-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0.5rem;
  overscroll-behavior: contain;
}

.sidebar-footer {
  flex-shrink: 0;
  padding: 0.5rem 0.65rem 0.65rem;
  border-top: 2px solid var(--c-border-light);
  background: linear-gradient(0deg, #f1f5f9 0%, #fff 100%);
}

.session-row {
  display: flex;
  align-items: stretch;
  gap: 0.25rem;
  border-radius: 10px;
  margin-bottom: 6px;
  border: 2px solid transparent;
}

.session-row:hover {
  background: var(--c-bg);
}

.session-row.active {
  background: var(--c-primary-soft);
  border-color: #93c5fd;
  box-shadow: inset 3px 0 0 var(--c-primary);
}

.session-main {
  flex: 1;
  text-align: left;
  border: none;
  background: transparent;
  padding: 0.7rem 0.5rem 0.7rem 0.65rem;
  cursor: pointer;
  font-family: inherit;
  min-width: 0;
}

.session-title {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--c-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.2rem;
  margin-top: 0.2rem;
  min-width: 0;
}

.session-meta-first {
  width: 100%;
  font-size: 0.72rem;
  font-weight: 500;
  color: #64748b;
  line-height: 1.35;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: normal;
}

.session-meta-time {
  display: block;
  font-size: 0.7rem;
  color: var(--c-muted);
}

.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background: var(--c-surface);
}

.topbar {
  position: relative;
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.55rem clamp(0.75rem, 2vw, 1.25rem) 0.55rem;
  padding-top: calc(0.55rem + 3px);
  border-bottom: 2px solid var(--c-border-light);
  background: linear-gradient(180deg, #fff 0%, #f8fafc 100%);
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.04);
}

.topbar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #1d4ed8, #3b82f6);
  pointer-events: none;
}

.topbar-left {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  min-width: 0;
}

.topbar-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 0.65rem;
  flex-shrink: 0;
}

.titles {
  min-width: 0;
}

.title {
  margin: 0;
  font-size: clamp(0.95rem, 2.2vw, 1.2rem);
  font-weight: 700;
  line-height: 1.3;
  letter-spacing: -0.02em;
  color: var(--c-text);
}

.subtitle {
  margin: 0.35rem 0 0;
  font-size: clamp(0.75rem, 1.8vw, 0.875rem);
  color: var(--c-muted);
  line-height: 1.45;
}

.toggle-pill {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  border: 2px solid #64748b;
  background: #fff;
  color: var(--c-text);
  font-size: clamp(0.75rem, 1.8vw, 0.8125rem);
  font-weight: 700;
  padding: 0.5rem 1rem;
  border-radius: 999px;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

.toggle-pill:hover {
  border-color: var(--c-primary);
  color: var(--c-primary);
}

.toggle-pill.on {
  background: linear-gradient(180deg, #2563eb 0%, var(--c-primary) 100%);
  border-color: var(--c-primary-hover);
  color: #fff;
  box-shadow: 0 2px 10px rgba(29, 78, 216, 0.4);
}

.toggle-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;
}

.toggle-pill.on .toggle-dot {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.5);
}

.thread {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: clamp(0.5rem, 1.5vw, 1rem) clamp(0.75rem, 3vw, 2rem);
  background: var(--c-bg);
  display: flex;
  flex-direction: column;
  overscroll-behavior: contain;
}

.empty {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(0.5rem, 2vw, 1rem);
}

.empty-card {
  width: 100%;
  max-width: min(42rem, 100%);
  max-height: 100%;
  overflow-y: auto;
  background: var(--c-surface);
  border: 2px solid var(--c-border-light);
  border-radius: clamp(12px, 2vw, 16px);
  padding: clamp(1rem, 3vw, 1.75rem) clamp(1rem, 3vw, 2rem);
  box-shadow:
    0 4px 6px -1px rgba(15, 23, 42, 0.08),
    0 12px 28px -6px rgba(15, 23, 42, 0.12);
}

.empty-logo {
  width: clamp(72px, 16vw, 96px);
  height: clamp(72px, 16vw, 96px);
  border-radius: 14px;
  object-fit: cover;
  display: block;
  margin: 0 auto clamp(0.65rem, 2vw, 1rem);
  border: 2px solid var(--c-border-light);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.1);
}

.empty-heading {
  margin: 0 0 0.45rem;
  font-size: clamp(1.05rem, 2.8vw, 1.35rem);
  font-weight: 700;
  text-align: center;
  color: var(--c-text);
}

.empty-lead {
  margin: 0 0 0.75rem;
  font-size: clamp(0.8125rem, 2vw, 0.9375rem);
  color: #475569;
  line-height: 1.55;
  text-align: left;
}

.empty-lead strong {
  color: var(--c-text);
}

.empty-list {
  margin: 0;
  padding-left: 1.25rem;
  font-size: clamp(0.75rem, 1.8vw, 0.875rem);
  color: var(--c-text);
  line-height: 1.5;
}

.empty-list li {
  margin-bottom: 0.45rem;
}

.bubble-row {
  display: flex;
  gap: 0.65rem;
  max-width: min(760px, 94%);
  margin-bottom: 1rem;
}

.bubble-row.from-user {
  align-self: flex-end;
  flex-direction: row-reverse;
  margin-left: auto;
}

.bubble-row.from-assistant {
  align-self: flex-start;
}

.bubble-avatar {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid var(--c-border-light);
  background: var(--c-surface);
}

.bubble-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bubble {
  min-width: 0;
}

.bubble-body {
  background: var(--c-surface);
  border: 2px solid var(--c-border-light);
  border-radius: 12px;
  padding: 0.8rem 1.05rem;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.05);
}

.from-user .bubble-body {
  background: linear-gradient(180deg, #2563eb 0%, var(--c-primary) 100%);
  border-color: var(--c-primary-hover);
  color: #fff;
  box-shadow: 0 2px 12px rgba(29, 78, 216, 0.28);
}

.from-user .bubble-time {
  color: rgba(255, 255, 255, 0.9);
}

.bubble-text {
  font-size: 0.9375rem;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble-time {
  display: block;
  font-size: 0.75rem;
  margin-top: 0.5rem;
  color: var(--c-muted);
}

.bubble-body.waiting {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.dots {
  display: flex;
  gap: 4px;
}

.dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--c-primary);
  animation: dot 1.2s ease-in-out infinite;
}

.dots span:nth-child(2) {
  animation-delay: 0.15s;
}

.dots span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes dot {
  0%,
  80%,
  100% {
    opacity: 0.35;
    transform: scale(0.85);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

.waiting-text {
  font-size: 0.875rem;
  color: var(--c-muted);
}

.composer {
  flex-shrink: 0;
  border-top: 2px solid var(--c-border-light);
  background: var(--c-surface);
  padding: 0.55rem clamp(0.75rem, 2.5vw, 1.25rem) 0.5rem;
  box-shadow: 0 -4px 16px rgba(15, 23, 42, 0.06);
}

.composer-inner {
  display: flex;
  gap: 0.65rem;
  align-items: flex-end;
}

.composer-input {
  flex: 1;
  min-height: 48px;
  max-height: 160px;
  padding: 0.7rem 1rem;
  border: 2px solid var(--c-border-light);
  border-radius: 10px;
  font-size: 0.9375rem;
  font-family: inherit;
  line-height: 1.45;
  resize: none;
  background: #f8fafc;
  color: var(--c-text);
}

.composer-input:focus {
  outline: none;
  border-color: var(--c-primary);
  background: #fff;
  box-shadow: 0 0 0 4px var(--c-primary-ring);
}

.composer-input:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-send {
  flex-shrink: 0;
  min-height: 48px;
  min-width: 88px;
  padding: 0 1.35rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(180deg, #2563eb 0%, var(--c-primary) 100%);
  color: #fff;
  font-size: 0.9375rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  box-shadow: 0 2px 8px rgba(29, 78, 216, 0.4);
}

.btn-send:hover:not(:disabled) {
  background: linear-gradient(180deg, var(--c-primary) 0%, var(--c-primary-hover) 100%);
}

.btn-send:disabled {
  background: #cbd5e1;
  box-shadow: none;
  cursor: not-allowed;
  color: #94a3b8;
}

.composer-hint {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.35rem;
  margin-top: 0.55rem;
  font-size: 0.75rem;
  font-weight: 500;
  color: #475569;
}

@media (min-width: 769px) and (max-width: 1024px) {
  .layout {
    --sidebar-w: 260px;
  }

  .empty-card {
    max-width: 38rem;
  }
}

@media (max-width: 768px) {
  .sidebar {
    position: absolute;
    height: 100%;
    box-shadow: 4px 0 24px rgba(15, 23, 42, 0.15);
  }

  .subtitle {
    display: none;
  }

  .bubble-row {
    max-width: 96%;
  }

  .toggle-pill {
    padding: 0.45rem 0.75rem;
  }
}
</style>
