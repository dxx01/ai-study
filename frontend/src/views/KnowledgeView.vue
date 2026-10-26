<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { listDocuments, reloadKnowledge, uploadDocument } from '@/api/expert'
import AppDataTable from '@/components/app/AppDataTable.vue'
import AppPageCard from '@/components/app/AppPageCard.vue'
import { useApiFeedback } from '@/composables/useApiFeedback'
import { useRoutePageTitle } from '@/composables/useRoutePageTitle'
import type { DocumentSummary } from '@/types/api'
import { formatDateTime, formatFileSize } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const pageTitle = useRoutePageTitle()
const appId = computed(() => String(route.params.appId ?? 'health'))
const uploadCategory = ref(0)
const { showError, showSuccess, showWarning } = useApiFeedback()
const loading = ref(false)
const documents = ref<DocumentSummary[]>([])

async function refresh() {
  loading.value = true
  try {
    documents.value = await listDocuments(appId.value)
  } catch (e) {
    showError(e, '加载失败')
  } finally {
    loading.value = false
  }
}

async function onUpload(file: File) {
  if (!auth.hasPermission('ai:knowledge:write')) {
    showWarning('无上传权限')
    return false
  }
  try {
    await uploadDocument(file, appId.value, uploadCategory.value)
    showSuccess('上传并索引成功')
    await refresh()
  } catch (e) {
    showError(e, '上传失败')
  }
  return false
}

async function onReload() {
  try {
    const result = await reloadKnowledge()
    showSuccess(`索引已重建，共 ${result.documentCount} 篇`)
    await refresh()
  } catch (e) {
    showError(e, '重建失败')
  }
}

watch(appId, () => {
  void refresh()
})

onMounted(refresh)
</script>

<template>
  <AppPageCard :title="pageTitle" :loading="loading">
    <template #actions>
      <el-select
        v-if="auth.hasPermission('ai:knowledge:write')"
        v-model="uploadCategory"
        class="knowledge-category"
        placeholder="资料类型"
      >
        <el-option label="未分类" :value="0" />
        <el-option label="食物营养" :value="1" />
        <el-option label="疾病科普" :value="2" />
        <el-option label="其他" :value="9" />
      </el-select>
      <el-upload
        v-if="auth.hasPermission('ai:knowledge:write')"
        :show-file-list="false"
        :before-upload="onUpload"
        accept=".pdf,.doc,.docx,.txt,.md,.html,.htm,.ppt,.pptx"
      >
        <el-button type="primary">上传文档</el-button>
      </el-upload>
      <el-button v-if="auth.hasPermission('ai:knowledge:admin')" @click="onReload">重建索引</el-button>
      <el-button @click="refresh">刷新</el-button>
    </template>

    <div class="app-page-list-body">
    <AppDataTable :data="documents" empty-text="暂无文档，请上传 PDF/Word/TXT 等">
      <el-table-column prop="fileName" label="文件名" min-width="200" />
      <el-table-column label="大小" width="120">
        <template #default="{ row }">{{ formatFileSize(row.sizeBytes) }}</template>
      </el-table-column>
      <el-table-column prop="chunkCount" label="分块数" width="100" />
      <el-table-column label="上传时间" min-width="180">
        <template #default="{ row }">{{ formatDateTime(row.uploadedAt) }}</template>
      </el-table-column>
      <el-table-column prop="documentId" label="文档 ID" min-width="220" show-overflow-tooltip />
    </AppDataTable>
    </div>
  </AppPageCard>
</template>
