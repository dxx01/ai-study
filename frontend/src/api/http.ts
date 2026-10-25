import axios, { type AxiosError } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const baseURL = import.meta.env.VITE_API_BASE ?? ''

export const http = axios.create({
  baseURL,
  timeout: 120_000,
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ detail?: string; title?: string }>) => {
    const status = error.response?.status
    const detail =
      error.response?.data?.detail ??
      error.response?.data?.title ??
      error.message

    if (status === 401) {
      const auth = useAuthStore()
      auth.clearSession()
      if (router.currentRoute.value.path !== '/login') {
        void router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    }

    return Promise.reject(new Error(detail || '请求失败'))
  },
)
