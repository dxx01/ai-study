<script setup lang="ts">
import { useRouter } from 'vue-router'
import AppPageCard from '@/components/app/AppPageCard.vue'
import { usePlatformApps } from '@/composables/usePlatformApps'
import { useRoutePageTitle } from '@/composables/useRoutePageTitle'

const pageTitle = useRoutePageTitle()
const router = useRouter()
const { apps, ensureLoaded } = usePlatformApps()
void ensureLoaded()

function openApp(appId: string) {
  void router.push({ name: 'app-detail', params: { appId } })
}
</script>

<template>
  <AppPageCard :title="pageTitle" :list-layout="false">
    <p class="apps-lead">平台可托管多个 AI 应用。当前为学习演示，仅内置下列示例应用。</p>
    <el-row :gutter="16">
      <el-col v-for="app in apps" :key="app.code" :xs="24" :sm="12" :lg="8">
        <el-card shadow="hover" class="app-card" @click="openApp(app.code)">
          <el-tag size="small" type="success" class="app-card__tag">{{ app.tagline }}</el-tag>
          <h3 class="app-card__title">{{ app.name }}</h3>
          <p class="app-card__desc">{{ app.description }}</p>
          <el-button type="primary" link>进入应用 →</el-button>
        </el-card>
      </el-col>
    </el-row>
  </AppPageCard>
</template>

<style scoped>
.apps-lead {
  margin: 0 0 20px;
  color: #6b7280;
  font-size: 14px;
}

.app-card {
  border-radius: 12px;
  margin-bottom: 16px;
  cursor: pointer;
}

.app-card__tag {
  margin-bottom: 8px;
}

.app-card__title {
  margin: 0 0 8px;
  font-size: 18px;
  color: #111827;
}

.app-card__desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  min-height: 3em;
}
</style>
