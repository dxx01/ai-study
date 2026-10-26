<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { listProviders, postChat } from '@/api/expert'
import type { ChatMode, ChatProviderInfo, ChatResponse } from '@/types/api'
import AppPageCard from '@/components/app/AppPageCard.vue'
import { useApiFeedback } from '@/composables/useApiFeedback'
import { useRoutePageTitle } from '@/composables/useRoutePageTitle'
import { renderMarkdown } from '@/utils/markdown'

const route = useRoute()
const pageTitle = useRoutePageTitle()
const { showError } = useApiFeedback()
const appId = computed(() => String(route.params.appId ?? 'health'))

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  meta?: ChatResponse
}

const sessionId = ref<string | null>(null)
const mode = ref<ChatMode>('AUTO')
const provider = ref<string | undefined>()
const providers = ref<ChatProviderInfo[]>([])
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
  void loadProviders()
})

async function loadProviders() {
  try {
    providers.value = await listProviders()
    provider.value =
      providers.value.find((item) => item.defaultProvider)?.id ?? providers.value[0]?.id
  } catch {
    // 模型列表不可用时使用后端默认提供方
  }
}

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
      provider: provider.value,
      appId: appId.value,
    })
    sessionId.value = resp.sessionId
    messages.value.push({ role: 'assistant', content: resp.answer, meta: resp })
  } catch (e) {
    showError(e, '发送失败')
  } finally {
    loading.value = false
    await scrollBottom()
  }
}
</script>

<template>
  <AppPageCard
    :list-layout="false"
    page-class="app-page--chat"
    card-class="app-page-card--chat"
    :title="pageTitle"
  >
    <template #actions>
      <el-select v-if="providers.length" v-model="provider" class="model-select" placeholder="模型">
        <el-option v-for="item in providers" :key="item.id" :label="item.model" :value="item.id" />
      </el-select>
      <el-radio-group v-model="mode">
        <el-radio-button value="AUTO">自动</el-radio-button>
        <el-radio-button value="LOCAL">仅本地</el-radio-button>
        <el-radio-button value="WEB">联网</el-radio-button>
      </el-radio-group>
    </template>

    <div ref="listRef" class="message-list">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="message-row"
        :class="msg.role"
      >
        <div class="bubble">
          <div
            v-if="msg.role === 'assistant'"
            class="content markdown-body"
            v-html="renderMarkdown(msg.content)"
          ></div>
          <div v-else class="content">{{ msg.content }}</div>
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
  </AppPageCard>
</template>

<style scoped>
.model-select {
  width: 180px;
}

.message-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
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
  flex-shrink: 0;
}

.loading-hint {
  color: #6b7280;
  font-size: 13px;
  padding: 8px;
}
</style>
