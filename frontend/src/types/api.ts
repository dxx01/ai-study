export type ChatMode = 'LOCAL' | 'WEB' | 'AUTO'

/**
 * 列表分页请求参数（与后端 DTO 对齐；新增分页接口时统一使用）。
 */
export interface PageQuery {
  pageNum: number
  pageSize: number
}

/**
 * 分页响应。`records` / `total` 含义须与后端一致；若后端字段不同，在此扩展或映射，禁止各页面自定义同名结构。
 */
export interface PageResult<T> {
  records: T[]
  total: number
}

/** 启用状态：0=禁用，1=启用（见 constants/dicts.ts） */
export type EnableStatusCode = 0 | 1

export interface MenuItem {
  id: number
  parentId: number
  name: string
  path: string
  icon?: string | null
  permissionCode?: string | null
  sortOrder: number
  visible?: boolean
  status?: EnableStatusCode
  children: MenuItem[]
}

export interface LoginResponse {
  token: string
  tokenType: string
  loginId: string
  username: string
  roles: string[]
  permissions: string[]
  menus: MenuItem[]
}

export interface UserInfoResponse {
  loginId: string
  username: string
  roles: string[]
  permissions: string[]
  menus: MenuItem[]
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
  appId?: string
}

export interface ChatProviderInfo {
  id: string
  model: string
  defaultProvider: boolean
}

export interface AdminUserItem {
  id: number
  username: string
  status: EnableStatusCode
  roles: string[]
  lastLoginAt?: string | null
  createdAt?: string | null
}

export interface AdminRoleItem {
  id: number
  code: string
  name: string
  status: EnableStatusCode
  sortOrder: number
  permissions: string[]
}

export interface AdminPermissionItem {
  id: number
  code: string
  name: string
  groupName?: string | null
}

export interface MenuPayload {
  parentId: number
  name: string
  path: string
  icon?: string | null
  permissionCode?: string | null
  sortOrder: number
  visible: boolean
  status: EnableStatusCode
}

export interface PlatformApp {
  code: string
  name: string
  tagline?: string | null
  description?: string | null
  expertId?: string | null
}

export interface DocumentSummary {
  documentId: string
  appId: string
  category: number
  fileName: string
  sizeBytes: number
  uploadedAt: string
  chunkCount: number
}
