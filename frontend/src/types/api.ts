export type ChatMode = 'LOCAL' | 'WEB' | 'AUTO'

export interface LoginResponse {
  token: string
  tokenType: string
  loginId: string
  username: string
  roles: string[]
  permissions: string[]
}

export interface UserInfoResponse {
  loginId: string
  username: string
  roles: string[]
  permissions: string[]
}

export interface SourceCitation {
  type: string
  documentId?: string
  title: string
  excerpt: string
  url?: string
  score?: number
}

export interface ChatResponse {
  sessionId: string
  expertId: string
  expertName: string
  question: string
  answer: string
  modeUsed: ChatMode
  webSearchUsed: boolean
  sources: SourceCitation[]
  disclaimer: string
}

export interface ChatRequest {
  message: string
  sessionId?: string | null
  mode?: ChatMode
  provider?: string
}

export interface ChatProviderInfo {
  id: string
  model: string
  defaultProvider: boolean
}

export interface DocumentSummary {
  documentId: string
  fileName: string
  sizeBytes: number
  uploadedAt: string
  chunkCount: number
}
