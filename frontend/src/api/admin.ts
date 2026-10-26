import { http } from './http'
import type {
  AdminPermissionItem,
  AdminRoleItem,
  AdminUserItem,
  MenuItem,
  MenuPayload,
} from '@/types/api'

export async function listAdminUsers(): Promise<AdminUserItem[]> {
  const { data } = await http.get<AdminUserItem[]>('/api/admin/users')
  return data
}

export async function listAdminRoles(): Promise<AdminRoleItem[]> {
  const { data } = await http.get<AdminRoleItem[]>('/api/admin/roles')
  return data
}

export async function listAdminMenus(): Promise<MenuItem[]> {
  const { data } = await http.get<MenuItem[]>('/api/admin/menus')
  return data
}

export async function listAdminPermissions(): Promise<AdminPermissionItem[]> {
  const { data } = await http.get<AdminPermissionItem[]>('/api/admin/permissions')
  return data
}

export async function updateUserStatus(id: number, status: number): Promise<void> {
  await http.put(`/api/admin/users/${id}/status`, { status })
}

export async function updateUserRoles(id: number, roles: string[]): Promise<void> {
  await http.put(`/api/admin/users/${id}/roles`, { roles })
}

export async function updateRolePermissions(id: number, permissions: string[]): Promise<void> {
  await http.put(`/api/admin/roles/${id}/permissions`, { permissions })
}

export async function createMenu(payload: MenuPayload): Promise<void> {
  await http.post('/api/admin/menus', payload)
}

export async function updateMenu(id: number, payload: MenuPayload): Promise<void> {
  await http.put(`/api/admin/menus/${id}`, payload)
}

export async function deleteMenu(id: number): Promise<void> {
  await http.delete(`/api/admin/menus/${id}`)
}
