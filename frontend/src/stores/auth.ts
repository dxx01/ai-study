import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import type { LoginResponse, MenuItem } from '@/types/api'

const TOKEN_KEY = 'ai_study_token'
const USER_KEY = 'ai_study_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const username = ref<string | null>(null)
  const permissions = ref<string[]>([])
  const roles = ref<string[]>([])
  const menus = ref<MenuItem[]>([])

  const isLoggedIn = computed(() => !!token.value)

  function applyLogin(data: LoginResponse) {
    token.value = data.token
    username.value = data.username
    permissions.value = data.permissions ?? []
    roles.value = data.roles ?? []
    menus.value = data.menus ?? []
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(
      USER_KEY,
      JSON.stringify({
        username: data.username,
        permissions: data.permissions,
        roles: data.roles,
        menus: data.menus,
      }),
    )
  }

  function restoreFromStorage() {
    const raw = localStorage.getItem(USER_KEY)
    if (raw) {
      try {
        const parsed = JSON.parse(raw) as {
          username: string
          permissions: string[]
          roles: string[]
          menus?: MenuItem[]
        }
        username.value = parsed.username
        permissions.value = parsed.permissions ?? []
        roles.value = parsed.roles ?? []
        menus.value = parsed.menus ?? []
      } catch {
        /* ignore */
      }
    }
  }

  function clearSession() {
    token.value = null
    username.value = null
    permissions.value = []
    roles.value = []
    menus.value = []
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function hasPermission(code: string): boolean {
    return permissions.value.includes(code)
  }

  async function login(usernameInput: string, password: string) {
    const data = await authApi.login(usernameInput, password)
    applyLogin(data)
  }

  async function register(usernameInput: string, password: string) {
    const data = await authApi.register(usernameInput, password)
    applyLogin(data)
  }

  async function logout() {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } finally {
      clearSession()
    }
  }

  async function refreshProfile() {
    if (!token.value) {
      return
    }
    const me = await authApi.fetchMe()
    username.value = me.username
    permissions.value = me.permissions
    roles.value = me.roles
    menus.value = me.menus ?? []
  }

  restoreFromStorage()

  return {
    token,
    username,
    permissions,
    roles,
    menus,
    isLoggedIn,
    hasPermission,
    login,
    register,
    logout,
    refreshProfile,
    clearSession,
  }
})
