export interface DemoApp {
  id: string
  name: string
  tagline: string
  description: string
}

/** 平台内示例 AI 应用（非平台品牌名） */
export const DEMO_APPS: DemoApp[] = [
  {
    id: 'health',
    name: '健康科普顾问',
    tagline: '示例应用',
    description:
      '居家营养与健康科普场景：本地知识库问答、引用溯源与资料不足拒答。用于练习 RAG、会话与评测链路。',
  },
]

export function findDemoApp(appId: string): DemoApp | undefined {
  return DEMO_APPS.find((item) => item.id === appId)
}
