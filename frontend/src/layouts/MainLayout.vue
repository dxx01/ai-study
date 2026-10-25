<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const active = computed(() => route.path)

async function onLogout() {
  await auth.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="240px" class="aside">
      <div class="brand">
        <el-icon><FirstAidKit /></el-icon>
        <span>健康科普顾问</span>
      </div>
      <el-menu :default-active="active" router class="menu">
        <el-menu-item v-if="auth.hasPermission('ai:chat')" index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能对话</span>
        </el-menu-item>
        <el-menu-item v-if="auth.hasPermission('ai:knowledge:read')" index="/knowledge">
          <el-icon><FolderOpened /></el-icon>
          <span>知识库</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ route.meta.title ?? '工作台' }}</div>
        <div class="header-user">
          <el-tag size="small" type="info">{{ auth.username }}</el-tag>
          <el-button link type="primary" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}

.aside {
  background: linear-gradient(180deg, #0f766e 0%, #115e59 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 16px;
  font-weight: 600;
  font-size: 16px;
}

.menu {
  border-right: none;
  background: transparent;
  flex: 1;
}

.menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.85);
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
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

.main {
  padding: 20px;
  background: #f3f4f6;
}
</style>
