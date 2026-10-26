import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/workspace' },
        {
          path: 'workspace',
          name: 'workspace',
          component: () => import('@/views/WorkspaceView.vue'),
          meta: { module: '工作台', title: '工作台' },
        },
        {
          path: 'apps',
          name: 'apps',
          component: () => import('@/views/AppsView.vue'),
          meta: { module: '应用', title: '应用列表' },
        },
        {
          path: 'apps/:appId',
          name: 'app-detail',
          component: () => import('@/views/AppDetailView.vue'),
          meta: { module: '应用', title: '应用详情' },
        },
        {
          path: 'apps/:appId/chat',
          name: 'app-chat',
          component: () => import('@/views/ChatView.vue'),
          meta: { permission: 'ai:chat', module: '应用', title: '健康科普顾问 · 对话调试' },
        },
        {
          path: 'chat',
          redirect: '/apps/health/chat',
        },
        {
          path: 'apps/:appId/knowledge',
          name: 'app-knowledge',
          component: () => import('@/views/KnowledgeView.vue'),
          meta: { permission: 'ai:knowledge:read', module: '应用', title: '知识库' },
        },
        {
          path: 'knowledge',
          redirect: '/apps/health/knowledge',
        },
        {
          path: 'platform/models',
          name: 'platform-models',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: {
            permission: 'sys:user:manage',
            module: '模型与提示词',
            title: '模型与提示词',
            placeholderDesc: '将展示各应用绑定的模型、系统提示词与参数；当前仍使用服务默认配置。',
          },
        },
        {
          path: 'platform/evals',
          name: 'platform-evals',
          component: () => import('@/views/PlaceholderView.vue'),
          meta: {
            permission: 'sys:user:manage',
            module: '评测与运行记录',
            title: '评测与运行记录',
            placeholderDesc: '将展示金标评测结果、单次对话的 token/耗时与引用片段；能力接入前仅占位。',
          },
        },
        {
          path: 'platform/users',
          name: 'platform-users',
          component: () => import('@/views/SystemView.vue'),
          meta: { permission: 'sys:user:manage', module: '平台管理', title: '用户管理' },
        },
        {
          path: 'platform/roles',
          name: 'platform-roles',
          component: () => import('@/views/SystemView.vue'),
          meta: { permission: 'sys:role:manage', module: '平台管理', title: '角色管理' },
        },
        {
          path: 'platform/menus',
          name: 'platform-menus',
          component: () => import('@/views/SystemView.vue'),
          meta: { permission: 'sys:menu:manage', module: '平台管理', title: '菜单管理' },
        },
        { path: 'platform', redirect: '/platform/users' },
        { path: 'system/users', redirect: '/platform/users' },
        { path: 'system/roles', redirect: '/platform/roles' },
        { path: 'system/menus', redirect: '/platform/menus' },
        { path: 'system', redirect: '/platform/users' },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.guest) {
    if (auth.isLoggedIn && (to.name === 'login' || to.name === 'register')) {
      return { path: '/workspace' }
    }
    return true
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  const perm = to.meta.permission as string | undefined
  if (perm && !auth.hasPermission(perm)) {
    return { path: '/workspace' }
  }
  return true
})

export default router
