<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    loading?: boolean
    /** 列表/管理页：铺满主区宽高（默认开启）；对话等特殊页传 false */
    listLayout?: boolean
    /** 追加到 .app-page，如 app-page--chat、app-page--wide（宽表） */
    pageClass?: string
    /** 追加到 .app-page-card */
    cardClass?: string
  }>(),
  { listLayout: true },
)
</script>

<template>
  <div class="app-page" :class="[pageClass, { 'app-page--list': listLayout }]">
    <el-card
      shadow="never"
      class="app-page-card"
      :class="[cardClass, { 'app-page-card--list': listLayout }]"
      v-loading="loading"
    >
      <template v-if="title || $slots.title || $slots.actions" #header>
        <div class="app-page-header">
          <span class="app-page-title">
            <slot name="title">{{ title }}</slot>
          </span>
          <div v-if="$slots.actions" class="app-page-actions">
            <slot name="actions" />
          </div>
        </div>
      </template>
      <slot />
    </el-card>
  </div>
</template>
