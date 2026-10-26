<script setup lang="ts">
import { computed, ref } from 'vue'

export interface CheckboxPanelOption {
  value: string
  title: string
  description?: string
  tag?: string
}

const model = defineModel<string[]>({ default: () => [] })

const props = withDefaults(
  defineProps<{
    options: CheckboxPanelOption[]
    placeholder?: string
    emptyText?: string
    maxHeight?: string
  }>(),
  {
    placeholder: '搜索名称或编码',
    emptyText: '无匹配项',
    maxHeight: '360px',
  },
)

const keyword = ref('')

const filteredOptions = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  if (!q) {
    return props.options
  }
  return props.options.filter((opt) => {
    const hay = `${opt.value} ${opt.title} ${opt.description ?? ''} ${opt.tag ?? ''}`.toLowerCase()
    return hay.includes(q)
  })
})
</script>

<template>
  <div class="checkbox-panel">
    <el-input v-model="keyword" clearable :placeholder="placeholder" class="checkbox-panel-search" />
    <el-scrollbar :max-height="maxHeight">
      <el-checkbox-group v-if="filteredOptions.length" v-model="model" class="checkbox-panel-group">
        <el-checkbox
          v-for="opt in filteredOptions"
          :key="opt.value"
          :value="opt.value"
          class="app-dialog-check-row checkbox-panel-item"
        >
          <span class="checkbox-panel-title">{{ opt.title }}</span>
          <el-tag v-if="opt.tag" size="small" type="info" class="app-tag-gap">{{ opt.tag }}</el-tag>
          <span v-if="opt.description" class="checkbox-panel-desc">{{ opt.description }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-else :description="emptyText" :image-size="64" />
    </el-scrollbar>
  </div>
</template>

<style scoped>
.checkbox-panel-search {
  margin-bottom: 12px;
}

.checkbox-panel-group {
  display: flex;
  flex-direction: column;
  padding-right: 8px;
}

.checkbox-panel-item {
  align-items: flex-start;
  height: auto;
  margin-bottom: 8px;
}

.checkbox-panel-title {
  margin-right: 4px;
}

.checkbox-panel-desc {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}
</style>
