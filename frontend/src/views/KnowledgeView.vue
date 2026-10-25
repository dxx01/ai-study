<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDocuments, reloadKnowledge, uploadDocument } from '@/api/expert'
import type { DocumentSummary } from '@/types/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const loading = ref(false)
const documents = ref<DocumentSummary[]>([])

async function refresh() {
  loading.value = true
  try {
    documents.value = await listDocuments()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function onUpload(file: File) {
  if (!auth.hasPermission('ai:knowledge:write')) {
    ElMessage.warning('无上传权限')
    return false
  }
  try {
    await uploadDocument(file)
    ElMessage.success('上传并索引成功')
    await refresh()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '上传失败')
  }
  return false
}

async function onReload() {
  try {
    const result = await reloadKnowledge()
    ElMessage.success(`索引已重建，共 ${result.documentCount} 篇`)
    await refresh()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '重建失败')
  }
}

function formatSize(bytes: number) {
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

onMounted(refresh)
</script>

<template>
  <div class="knowledge-page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="panel-header">
          <span>知识库文档</span>
          <div class="actions">
            <el-upload
              v-if="auth.hasPermission('ai:knowledge:write')"
              :show-file-list="false"
              :before-upload="onUpload"
              accept=".pdf,.doc,.docx,.txt,.md,.html,.htm,.ppt,.pptx"
            >
              <el-button type="primary">上传文档</el-button>
            </el-upload>
            <el-button
              v-if="auth.hasPermission('ai:knowledge:admin')"
              @click="onReload"
            >
              重建索引
            </el-button>
            <el-button :loading="loading" @click="refresh">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="documents" stripe empty-text="暂无文档，请上传 PDF/Word/TXT 等">
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column label="大小" width="120">
          <template #default="{ row }">{{ formatSize(row.sizeBytes) }}</template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="分块数" width="100" />
        <el-table-column prop="uploadedAt" label="上传时间" min-width="180" />
        <el-table-column prop="documentId" label="文档 ID" min-width="220" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.knowledge-page {
  max-width: 1100px;
  margin: 0 auto;
}

.panel {
  border-radius: 12px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
