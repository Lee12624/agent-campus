export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

export interface ChatHistory {
  id: string
  title: string
  timestamp: number
}

export interface SSEConfig {
  endpoint: string
  userMessage: string
  chatId: string
  onMessage: (data: string) => void
  onError: (error: Event) => void
  onComplete?: () => void
}

export interface ConversationListItem {
  conversationId: string
  lastUserMessagePreview: string
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
