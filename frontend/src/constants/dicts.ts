/** 通用启用/停用（与库表 TINYINT、EnableStatus 一致） */
export const EnableStatus = {
  Disabled: 0,
  Enabled: 1,
} as const

export type EnableStatusCode = (typeof EnableStatus)[keyof typeof EnableStatus]

export const ENABLE_STATUS_OPTIONS: { value: EnableStatusCode; label: string }[] = [
  { value: EnableStatus.Enabled, label: '启用' },
  { value: EnableStatus.Disabled, label: '禁用' },
]

const ENABLE_STATUS_LABEL: Record<EnableStatusCode, string> = {
  [EnableStatus.Disabled]: '禁用',
  [EnableStatus.Enabled]: '启用',
}

/** 兼容历史字符串 ACTIVE/DISABLED，统一为数字字典码 */
export function normalizeEnableStatus(raw: number | string | null | undefined): EnableStatusCode {
  if (raw === EnableStatus.Disabled || raw === EnableStatus.Enabled) {
    return raw
  }
  if (raw === 'DISABLED' || raw === '0') {
    return EnableStatus.Disabled
  }
  return EnableStatus.Enabled
}

export function labelEnableStatus(raw: number | string | null | undefined): string {
  return ENABLE_STATUS_LABEL[normalizeEnableStatus(raw)]
}

export function isEnableStatusEnabled(raw: number | string | null | undefined): boolean {
  return normalizeEnableStatus(raw) === EnableStatus.Enabled
}

export function enableStatusTagType(raw: number | string | null | undefined): 'success' | 'info' {
  return isEnableStatusEnabled(raw) ? 'success' : 'info'
}
