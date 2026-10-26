import { http } from './http'
import type { PlatformApp } from '@/types/api'

export async function listPlatformApps(): Promise<PlatformApp[]> {
  const { data } = await http.get<PlatformApp[]>('/api/apps')
  return data
}

export async function getPlatformApp(code: string): Promise<PlatformApp> {
  const { data } = await http.get<PlatformApp>(`/api/apps/${code}`)
  return data
}
