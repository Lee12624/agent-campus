export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

export interface ChatHistory {
  id: string
  title: string
  /** 首条用户消息预览；侧栏小字展示，与 title（多为最近一条）区分 */
  firstPreview?: string
  timestamp: number
}

export interface SSEConfig {
  endpoint: string
  userMessage: string
  chatId: string
  onMessage: (data: string) => void
  onError: (error: Event) => void
  onComplete?: () => void | Promise<void>
}

export interface ConversationListItem {
  conversationId: string
  /** 首条用户消息预览，列表主标题（与豆包等产品一致） */
  firstUserMessagePreview?: string | null
  lastUserMessagePreview?: string | null
  lastMessageTimestamp: number
  messageCount: number
}

export interface ChatHistoryResponse {
  conversationId: string
  messages: MessageDTO[]
  total: number
}

export interface MessageDTO {
  content: string
  messageType: string
  timestamp?: number
}
