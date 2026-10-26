<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarMenu from '@/components/SidebarMenu.vue'
import { useRouteModuleName } from '@/composables/useRoutePageTitle'
import { useAuthStore } from '@/stores/auth'
import type { MenuItem } from '@/types/api'

const route = useRoute()
const moduleName = useRouteModuleName()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => route.path)

/** 未从后端拉到菜单时的平台导航（与 deploy/sql/03-seed-data.sql 对齐） */
const fallbackMenus = computed<MenuItem[]>(() => {
  const platformChildren: MenuItem[] = [
    {
      id: 91,
      parentId: 90,
      name: '用户管理',
      path: '/platform/users',
      icon: 'User',
      permissionCode: 'sys:user:manage',
      sortOrder: 1,
      children: [],
    },
    {
      id: 92,
      parentId: 90,
      name: '角色管理',
      path: '/platform/roles',
      icon: 'UserFilled',
      permissionCode: 'sys:role:manage',
      sortOrder: 2,
      children: [],
    },
    {
      id: 93,
      parentId: 90,
      name: '菜单管理',
      path: '/platform/menus',
      icon: 'Menu',
      permissionCode: 'sys:menu:manage',
      sortOrder: 3,
      children: [],
    },
  ].filter((item) => !item.permissionCode || auth.hasPermission(item.permissionCode))

  const items: MenuItem[] = [
    {
      id: 10,
      parentId: 0,
      name: '工作台',
      path: '/workspace',
      icon: 'Odometer',
      sortOrder: 1,
      children: [],
    },
    {
      id: 11,
      parentId: 0,
      name: '应用',
      path: '/apps',
      icon: 'Grid',
      permissionCode: 'ai:chat',
      sortOrder: 2,
      children: [],
    },
    {
      id: 12,
      parentId: 0,
      name: '知识库',
      path: '/knowledge',
      icon: 'FolderOpened',
      permissionCode: 'ai:knowledge:read',
      sortOrder: 3,
      children: [],
    },
  ].filter((item) => !item.permissionCode || auth.hasPermission(item.permissionCode))

  if (auth.hasPermission('sys:user:manage')) {
    items.push({
      id: 13,
      parentId: 0,
      name: '模型与提示词',
      path: '/platform/models',
      icon: 'Cpu',
      permissionCode: 'sys:user:manage',
      sortOrder: 4,
      children: [],
    })
    items.push({
      id: 14,
      parentId: 0,
      name: '评测与运行记录',
      path: '/platform/evals',
      icon: 'DataLine',
      permissionCode: 'sys:user:manage',
      sortOrder: 5,
      children: [],
    })
  }

  if (platformChildren.length) {
    items.push({
      id: 90,
      parentId: 0,
      name: '平台管理',
      path: '/platform',
      icon: 'Setting',
      sortOrder: 9,
      children: platformChildren,
    })
  }

  return items
})

const displayMenus = computed(() => (auth.menus.length ? auth.menus : fallbackMenus.value))

onMounted(() => {
  if (auth.isLoggedIn) {
    void auth.refreshProfile().catch(() => undefined)
  }
})

async function onLogout() {
  await auth.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <el-container class="layout-shell">
    <el-aside width="240px" class="layout-shell__aside aside">
      <div class="brand">
        <el-icon><Monitor /></el-icon>
        <div class="brand-text">
          <span class="brand-title">AI 应用平台</span>
          <span class="brand-sub">学习与实践</span>
        </div>
      </div>
      <el-menu :default-active="active" router class="layout-shell__menu menu">
        <SidebarMenu :items="displayMenus" />
      </el-menu>
    </el-aside>
    <el-container class="layout-shell__column" direction="vertical">
      <el-header class="layout-shell__header header">
        <div class="header-title">{{ moduleName }}</div>
        <div class="header-user">
          <el-tag size="small" type="info">{{ auth.username }}</el-tag>
          <el-button link type="primary" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="layout-shell__main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.aside {
  background: linear-gradient(180deg, #1e3a5f 0%, #172554 100%);
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 16px;
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.brand-title {
  font-weight: 600;
  font-size: 15px;
  line-height: 1.2;
}

.brand-sub {
  font-size: 11px;
  opacity: 0.75;
}

.menu {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(255, 255, 255, 0.88);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.1);
  --el-menu-active-color: #fff;
  background-color: transparent !important;
  border-right: none;
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.88);
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.menu :deep(.el-menu--inline) {
  background-color: transparent !important;
}

.menu :deep(.el-sub-menu .el-menu-item) {
  padding-left: 48px !important;
  color: rgba(255, 255, 255, 0.8);
}

.menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.menu :deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.75);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
