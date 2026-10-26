<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppPageCard from '@/components/app/AppPageCard.vue'
import { useRoutePageTitle } from '@/composables/useRoutePageTitle'

const route = useRoute()
const pageTitle = useRoutePageTitle()

const description = computed(
  () => (route.meta.placeholderDesc as string) || '该能力正在与后端联调，菜单先占位，避免误以为已完整上线。',
)
</script>

<template>
  <AppPageCard :title="pageTitle" :list-layout="false">
    <el-empty :description="description">
      <el-text type="info">实现检索 trace、评测集与运行记录后，将在此展示可观测数据。</el-text>
    </el-empty>
  </AppPageCard>
</template>
