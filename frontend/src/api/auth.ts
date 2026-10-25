import { http } from './http'
import type { LoginResponse, UserInfoResponse } from '@/types/api'

export async function login(username: string, password: string): Promise<LoginResponse> {
  const { data } = await http.post<LoginResponse>('/auth/login', { username, password })
  return data
}

export async function register(username: string, password: string): Promise<LoginResponse> {
  const { data } = await http.post<LoginResponse>('/auth/register', { username, password })
  return data
}

export async function logout(): Promise<void> {
  await http.post('/auth/logout')
}

export async function fetchMe(): Promise<UserInfoResponse> {
  const { data } = await http.get<UserInfoResponse>('/auth/me')
  return data
}
