<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listDocuments } from '@/api/expert'
import AppPageCard from '@/components/app/AppPageCard.vue'
import { usePlatformApps } from '@/composables/usePlatformApps'
import { useRoutePageTitle } from '@/composables/useRoutePageTitle'

const pageTitle = useRoutePageTitle()
const router = useRouter()
const { apps, ensureLoaded } = usePlatformApps()
const docCount = ref<number | null>(null)

onMounted(async () => {
  await ensureLoaded()
  try {
    const docs = await listDocuments('health')
    docCount.value = docs.length
  } catch {
    docCount.value = null
  }
})

function openApp(appId: string) {
  void router.push({ name: 'app-detail', params: { appId } })
}
</script>

<template>
  <AppPageCard :title="pageTitle" :list-layout="false">
    <p class="workspace-lead">
      AI 应用工程学习与实践平台：在可运行的微服务与 RAG 链路上，观察引用、模型与评测指标，而不是只做后台表单。
    </p>
    <el-row :gutter="16" class="workspace-grid">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="workspace-card">
          <template #header>
            <span>示例应用</span>
          </template>
          <div v-for="app in apps" :key="app.code" class="workspace-app">
            <div>
              <div class="workspace-app__name">{{ app.name }}</div>
              <div class="workspace-app__tag">{{ app.tagline }}</div>
              <p class="workspace-app__desc">{{ app.description }}</p>
            </div>
            <el-button type="primary" @click="openApp(app.code)">进入应用</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="workspace-card">
          <template #header>
            <span>平台概览</span>
          </template>
          <ul class="workspace-stats">
            <li>
              <span>知识库文档</span>
              <strong>{{ docCount === null ? '—' : docCount }}</strong>
            </li>
            <li>
              <span>金标评测</span>
              <strong>规划中</strong>
            </li>
            <li>
              <span>运行记录</span>
              <strong>规划中</strong>
            </li>
          </ul>
          <el-text type="info" size="small">
            评测与 token 耗时等能力接入后，将在此汇总最近一次结果。
          </el-text>
        </el-card>
      </el-col>
    </el-row>
  </AppPageCard>
</template>

<style scoped>
.workspace-lead {
  margin: 0 0 20px;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.workspace-grid {
  margin-top: 4px;
}

.workspace-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.workspace-app {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.workspace-app__name {
  font-weight: 600;
  font-size: 16px;
  color: #111827;
}

.workspace-app__tag {
  margin-top: 4px;
  font-size: 12px;
  color: #0f766e;
}

.workspace-app__desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
}

.workspace-stats {
  margin: 0 0 12px;
  padding: 0;
  list-style: none;
}

.workspace-stats li {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
  color: #374151;
}

.workspace-stats strong {
  color: #111827;
}
</style>
