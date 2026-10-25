import { http } from './http'
import type { ChatRequest, ChatResponse, DocumentSummary } from '@/types/api'

export async function postChat(body: ChatRequest): Promise<ChatResponse> {
  const { data } = await http.post<ChatResponse>('/api/chat', body)
  return data
}

export async function listDocuments(): Promise<DocumentSummary[]> {
  const { data } = await http.get<DocumentSummary[]>('/api/knowledge/documents')
  return data
}

export async function uploadDocument(file: File): Promise<DocumentSummary> {
  const form = new FormData()
  form.append('file', file)
  const { data } = await http.post<DocumentSummary>('/api/knowledge/documents', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return data
}

export async function reloadKnowledge(): Promise<{ reloaded: boolean; documentCount: number }> {
  const { data } = await http.post<{ reloaded: boolean; documentCount: number }>(
    '/api/knowledge/reload',
  )
  return data
}
