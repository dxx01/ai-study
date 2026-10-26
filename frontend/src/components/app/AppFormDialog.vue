<script setup lang="ts">
/**
 * 标准表单弹窗：footer、destroy-on-close、长内容 body 滚动（.app-form-dialog）。
 * 业务 el-form、rules、validate、API 由页面在 @confirm 中处理。
 */
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    width?: 'narrow' | 'medium' | 'wide' | string
    confirmLoading?: boolean
    confirmText?: string
    cancelText?: string
  }>(),
  {
    width: 'medium',
    confirmLoading: false,
    confirmText: '保存',
    cancelText: '取消',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
  cancel: []
}>()

const widthMap: Record<string, string> = {
  narrow: '420px',
  medium: '560px',
  wide: '720px',
}

const dialogWidth = computed(() => widthMap[props.width] ?? props.width)

function close() {
  emit('update:modelValue', false)
  emit('cancel')
}

function onConfirm() {
  emit('confirm')
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="dialogWidth"
    class="app-form-dialog"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <slot />
    <template #footer>
      <el-button @click="close">{{ cancelText }}</el-button>
      <el-button type="primary" :loading="confirmLoading" @click="onConfirm">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>
