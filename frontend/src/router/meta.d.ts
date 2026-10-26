import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    guest?: boolean
    requiresAuth?: boolean
    permission?: string
    /** 顶栏显示的模块名 */
    module?: string
    /** 页面卡片 / 子页标题 */
    title?: string
    /** 占位页说明（PlaceholderView） */
    placeholderDesc?: string
  }
}

export {}
