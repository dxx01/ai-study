<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppPageCard from '@/components/app/AppPageCard.vue'
import { usePlatformApps } from '@/composables/usePlatformApps'

const route = useRoute()
const router = useRouter()
const { findApp, ensureLoaded } = usePlatformApps()

void ensureLoaded()

const app = computed(() => findApp(String(route.params.appId)))

function goChat() {
  if (!app.value) {
    return
  }
  void router.push({ name: 'app-chat', params: { appId: app.value.code } })
}

function goKnowledge() {
  if (!app.value) {
    return
  }
  void router.push({ name: 'app-knowledge', params: { appId: app.value.code } })
}
</script>

<template>
  <AppPageCard v-if="app" :title="app.name" :list-layout="false">
    <template #title>
      <span>{{ app.name }}</span>
      <el-tag size="small" type="info" class="app-detail__tag">{{ app.tagline }}</el-tag>
    </template>
    <p class="app-detail__desc">{{ app.description }}</p>
    <div class="app-detail__actions">
      <el-button type="primary" @click="goChat">对话调试</el-button>
      <el-button @click="goKnowledge">知识库</el-button>
    </div>
    <el-alert
      class="app-detail__hint"
      type="info"
      :closable="false"
      show-icon
      title="学习目标"
      description="在对话页查看回答、引用与（后续）检索片段、模型与耗时，便于对比不同配置下的效果。"
    />
  </AppPageCard>
  <AppPageCard v-else title="应用不存在" :list-layout="false">
    <el-empty description="未找到该应用，请从「应用」列表进入。" />
  </AppPageCard>
</template>

<style scoped>
.app-detail__tag {
  margin-left: 8px;
  vertical-align: middle;
}

.app-detail__desc {
  margin: 0 0 20px;
  color: #4b5563;
  line-height: 1.6;
}

.app-detail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.app-detail__hint {
  max-width: 720px;
}
</style>
