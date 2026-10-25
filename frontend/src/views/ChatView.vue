<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { postChat } from '@/api/expert'
import type { ChatMode, ChatResponse } from '@/types/api'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  meta?: ChatResponse
}

const sessionId = ref<string | null>(null)
const mode = ref<ChatMode>('AUTO')
const input = ref('')
const loading = ref(false)
const messages = ref<ChatMessage[]>([])
const listRef = ref<HTMLElement | null>(null)

onMounted(() => {
  messages.value.push({
    role: 'assistant',
    content:
      '你好，我是居家营养与健康科普顾问。我可以结合本地知识库回答膳食、运动与常见健康科普问题；涉及诊疗请及时就医。',
  })
})

async function scrollBottom() {
  await nextTick()
  listRef.value?.scrollTo({ top: listRef.value.scrollHeight, behavior: 'smooth' })
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) {
    return
  }
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await scrollBottom()
  try {
    const resp = await postChat({
      message: text,
      sessionId: sessionId.value,
      mode: mode.value,
    })
    sessionId.value = resp.sessionId
    messages.value.push({ role: 'assistant', content: resp.answer, meta: resp })
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '发送失败')
  } finally {
    loading.value = false
    await scrollBottom()
  }
}
</script>

<template>
  <div class="chat-page">
    <el-card shadow="never" class="chat-card">
      <template #header>
        <div class="chat-header">
          <span>智能对话</span>
          <el-radio-group v-model="mode" size="small">
            <el-radio-button label="AUTO">自动</el-radio-button>
            <el-radio-button label="LOCAL">仅本地</el-radio-button>
            <el-radio-button label="WEB">联网</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div ref="listRef" class="message-list">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="message-row"
          :class="msg.role"
        >
          <div class="bubble">
            <div class="content">{{ msg.content }}</div>
            <template v-if="msg.meta">
              <el-divider />
              <p v-if="msg.meta.disclaimer" class="disclaimer">{{ msg.meta.disclaimer }}</p>
              <div v-if="msg.meta.sources?.length" class="sources">
                <div class="sources-title">参考依据</div>
                <el-collapse>
                  <el-collapse-item
                    v-for="(s, i) in msg.meta.sources"
                    :key="i"
                    :title="s.title"
                  >
                    <p class="source-type">{{ s.type === 'WEB_SEARCH' ? '联网检索' : '本地知识库' }}</p>
                    <p>{{ s.excerpt }}</p>
                    <p v-if="s.documentId && s.type === 'WEB_SEARCH'" class="source-link">
                      {{ s.documentId }}
                    </p>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </template>
          </div>
        </div>
        <div v-if="loading" class="loading-hint">正在思考…</div>
      </div>

      <div class="composer">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          maxlength="4000"
          show-word-limit
          placeholder="请输入您的健康问题或科普疑问…"
          @keydown.ctrl.enter="send"
        />
        <el-button type="primary" :loading="loading" @click="send">发送 (Ctrl+Enter)</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.chat-page {
  max-width: 960px;
  margin: 0 auto;
}

.chat-card {
  border-radius: 12px;
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  max-height: calc(100vh - 280px);
  padding: 8px 4px 16px;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: min(720px, 88%);
  padding: 12px 14px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  white-space: pre-wrap;
}

.message-row.user .bubble {
  background: #ecfdf5;
  border-color: #a7f3d0;
}

.disclaimer {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}

.sources-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}

.source-type {
  font-size: 12px;
  color: #0f766e;
  margin: 0 0 4px;
}

.source-link {
  font-size: 12px;
  color: #2563eb;
  word-break: break-all;
}

.composer {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.loading-hint {
  color: #6b7280;
  font-size: 13px;
  padding: 8px;
}
</style>
