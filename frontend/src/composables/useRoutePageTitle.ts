import { computed } from 'vue'
import { useRoute } from 'vue-router'

/** 页面卡片标题（route.meta.title） */
export function useRoutePageTitle() {
  const route = useRoute()
  return computed(() => (route.meta.title as string | undefined) ?? '')
}

/** 顶栏模块名（route.meta.module，缺省回退 title） */
export function useRouteModuleName() {
  const route = useRoute()
  return computed(
    () => (route.meta.module as string | undefined) ?? (route.meta.title as string | undefined) ?? '工作台',
  )
}
