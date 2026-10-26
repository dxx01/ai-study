<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const PAGE_SIZES = [10, 20, 50, 100] as const

/**
 * 业务列表标准表格封装。
 * 树形表（tree=true）全量展示，不做分页（见 frontend-standards §4.2.1）。
 */
const props = withDefaults(
  defineProps<{
    data?: unknown[]
    loading?: boolean
    emptyText?: string
    /** 树形表格（如菜单树），与 row-key / tree-props 配合 */
    tree?: boolean
    /** 在列表布局下撑满卡片剩余高度，减少主区灰底留白 */
    fill?: boolean
    /** 扁平列表默认 true；树表强制关闭 */
    pagination?: boolean
    /** 后端分页总数；不传则在当前 `data` 上做前端分页（过渡期，待 API 迁 PageResult） */
    total?: number
  }>(),
  {
    data: () => [],
    loading: false,
    emptyText: '暂无数据',
    tree: false,
    fill: true,
    pagination: undefined,
    total: undefined,
  },
)

const emit = defineEmits<{
  'page-change': [payload: { pageNum: number; pageSize: number }]
}>()

const pageNum = defineModel<number>('pageNum', { default: 1 })
const pageSize = defineModel<number>('pageSize', { default: 20 })

const paginationEnabled = computed(() => {
  if (props.tree) {
    return false
  }
  return props.pagination ?? true
})

const isServerPagination = computed(() => props.total !== undefined)

const totalCount = computed(() => {
  if (isServerPagination.value) {
    return props.total ?? 0
  }
  return props.data?.length ?? 0
})

const tableData = computed(() => {
  if (!paginationEnabled.value || isServerPagination.value) {
    return props.data
  }
  const start = (pageNum.value - 1) * pageSize.value
  return props.data.slice(start, start + pageSize.value)
})

const tableBodyRef = ref<HTMLElement | null>(null)
const tableHeight = ref<number>()

let resizeObserver: ResizeObserver | undefined

function bindTableHeight() {
  resizeObserver?.disconnect()
  resizeObserver = undefined
  tableHeight.value = undefined

  if (!props.fill || !tableBodyRef.value) {
    return
  }

  resizeObserver = new ResizeObserver((entries) => {
    const height = entries[0]?.contentRect.height
    if (height > 0) {
      tableHeight.value = Math.floor(height)
    }
  })
  resizeObserver.observe(tableBodyRef.value)
}

function emitPageChange() {
  emit('page-change', { pageNum: pageNum.value, pageSize: pageSize.value })
}

function onPageNumChange(value: number) {
  pageNum.value = value
  emitPageChange()
}

function onPageSizeChange(value: number) {
  pageSize.value = value
  pageNum.value = 1
  emitPageChange()
}

watch(
  () => [props.data?.length, pageSize.value, paginationEnabled.value, isServerPagination.value],
  () => {
    if (!paginationEnabled.value || isServerPagination.value) {
      return
    }
    const maxPage = Math.max(1, Math.ceil(totalCount.value / pageSize.value))
    if (pageNum.value > maxPage) {
      pageNum.value = maxPage
    }
  },
)

onMounted(() => bindTableHeight())
onBeforeUnmount(() => resizeObserver?.disconnect())
watch(() => props.fill, () => bindTableHeight())
</script>

<template>
  <div
    class="app-data-table"
    :class="{
      'app-data-table--fill': fill,
      'app-data-table--tree': tree,
      'app-data-table--paginated': paginationEnabled,
    }"
  >
    <div ref="tableBodyRef" class="app-data-table__body">
      <el-table
        v-bind="$attrs"
        class="app-data-table__inner"
        :data="tableData"
        border
        stripe
        :height="fill ? tableHeight : undefined"
        :empty-text="emptyText"
        v-loading="loading"
      >
        <slot />
      </el-table>
    </div>
    <div v-if="paginationEnabled" class="app-list-pagination app-data-table__pagination">
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="totalCount"
        :page-sizes="PAGE_SIZES"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @update:current-page="onPageNumChange"
        @update:page-size="onPageSizeChange"
      />
    </div>
  </div>
</template>
