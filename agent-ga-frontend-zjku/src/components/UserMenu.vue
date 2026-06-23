<template>
  <div class="user-menu" :class="variant">
    <template v-if="loggedIn">
      <div class="user-info">
        <span class="user-avatar" aria-hidden="true">{{ avatarChar }}</span>
        <span class="user-name">{{ displayName }}</span>
      </div>
      <button type="button" class="btn-logout" @click="handleLogout" title="退出登录">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" />
        </svg>
        <span v-if="variant !== 'topbar'" class="logout-text">退出</span>
      </button>
    </template>
    <span v-else class="guest-badge">未登录</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { isLoggedIn, clearAuth, useAuth } from '@/composables/useAuth'

defineProps<{
  variant?: 'sidebar' | 'topbar'
}>()

const router = useRouter()
const { user } = useAuth()

const loggedIn = computed(() => isLoggedIn())
const displayName = computed(() => user.value?.username || '用户')
const avatarChar = computed(() => (displayName.value.charAt(0) || 'U').toUpperCase())

function handleLogout() {
  clearAuth()
  router.push('/')
}
</script>

<style scoped>
.user-menu {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* ---- sidebar (default) ---- */
.user-menu.sidebar {
  flex-direction: row;
  justify-content: space-between;
  width: 100%;
}

.user-menu.topbar {
  flex-direction: row;
  gap: 0.5rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  min-width: 0;
}

.user-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
  font-size: 0.8125rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(29, 78, 216, 0.3);
}

.user-name {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-logout {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  border: 2px solid #fecaca;
  background: #fef2f2;
  color: #dc2626;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.4rem 0.65rem;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
  flex-shrink: 0;
}

.btn-logout:hover {
  background: #fee2e2;
  border-color: #dc2626;
}

.guest-badge {
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  padding: 0.35rem 0.65rem;
  border: 2px solid #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
}

/* ---- topbar variant (narrow screen on home page) ---- */
.user-menu.topbar .user-avatar {
  width: 28px;
  height: 28px;
}

.user-menu.topbar .user-name {
  display: none;
}

.user-menu.topbar .logout-text {
  display: none;
}

.user-menu.topbar .btn-logout {
  padding: 0.35rem 0.5rem;
}
</style>
