import { ElMessage } from 'element-plus'

/** 从异常或字符串解析用户可读文案 */
export function resolveErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof Error && error.message.trim()) {
    return error.message
  }
  if (typeof error === 'string' && error.trim()) {
    return error
  }
  return fallback
}

export function useApiFeedback() {
  function showError(error: unknown, fallback = '操作失败') {
    ElMessage.error(resolveErrorMessage(error, fallback))
  }

  function showSuccess(message: string) {
    ElMessage.success(message)
  }

  function showWarning(message: string) {
    ElMessage.warning(message)
  }

  /**
   * 执行异步请求；失败时统一提示，不抛出（返回 undefined）。
   */
  async function runAsync<T>(
    fn: () => Promise<T>,
    options?: { success?: string; error?: string },
  ): Promise<T | undefined> {
    try {
      const result = await fn()
      if (options?.success) {
        showSuccess(options.success)
      }
      return result
    } catch (error) {
      showError(error, options?.error ?? '操作失败')
      return undefined
    }
  }

  return {
    resolveErrorMessage,
    showError,
    showSuccess,
    showWarning,
    runAsync,
  }
}
