<script setup lang="ts">
import { computed } from 'vue'
import { enableStatusTagType, labelEnableStatus } from '@/constants/dicts'

const props = withDefaults(
  defineProps<{
    /** 字典码；enable 为通用启用/停用 0/1 */
    dict?: 'enable'
    status: number | string | null | undefined
  }>(),
  { dict: 'enable' },
)

const label = computed(() => {
  if (props.dict === 'enable') {
    return labelEnableStatus(props.status)
  }
  return String(props.status ?? '—')
})

const tagType = computed(() => {
  if (props.dict === 'enable') {
    return enableStatusTagType(props.status)
  }
  return 'info'
})
</script>

<template>
  <el-tag :type="tagType" size="small">{{ label }}</el-tag>
</template>
