import { computed, ref } from 'vue'

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

interface UserInfo {
  id: number
  username: string
}

// ========== reactive state ==========
const token = ref<string | null>(localStorage.getItem(TOKEN_KEY) || null)
const user = ref<UserInfo | null>(loadUser())

function loadUser(): UserInfo | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function persistUser(u: UserInfo | null) {
  if (u) {
    localStorage.setItem(USER_KEY, JSON.stringify(u))
  } else {
    localStorage.removeItem(USER_KEY)
  }
  user.value = u
}

// ========== public helpers (already imported by chat.ts / router / views) ==========

export function getToken(): string | null {
  return token.value
}

export function authHeaders(): Record<string, string> {
  const t = token.value
  if (!t) return {}
  return { Authorization: `Bearer ${t}` }
}

export function isLoggedIn(): boolean {
  return !!token.value
}

export function clearAuth() {
  token.value = null
  user.value = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

// ========== login / register ==========

export async function login(username: string, password: string) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error((err as any).message || '登录失败')
  }
  const data = await res.json()
  if (!data.success) throw new Error(data.message || '登录失败')
  token.value = data.token
  localStorage.setItem(TOKEN_KEY, data.token)
  persistUser({ id: data.userId, username: data.username })
  return data
}

export async function register(username: string, password: string) {
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({}))
    throw new Error((err as any).message || '注册失败')
  }
  const data = await res.json()
  if (!data.success) throw new Error(data.message || '注册失败')
  return data
}

export async function fetchMe() {
  const res = await fetch('/api/auth/me', { headers: { ...authHeaders() } })
  if (!res.ok) {
    clearAuth()
    throw new Error('UNAUTHORIZED')
  }
  const data = await res.json()
  persistUser({ id: data.userId, username: data.username })
  return data
}

// ========== reactive convenience ==========
export function useAuth() {
  const loggedIn = computed(() => isLoggedIn())
  return { token, user, loggedIn, login, register, fetchMe, clearAuth }
}
