import { formatDateTime } from '@/utils/format'

/** @deprecated 优先直接 import formatDateTime from '@/utils/format' */
export function useFormatDate() {
  return { formatDateTime }
}

export { formatDateTime } from '@/utils/format'
