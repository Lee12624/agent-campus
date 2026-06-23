<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-header">
        <img src="/assets/images/zjklogo.jpg" alt="" class="logo" />
        <h1 class="title">校园智能助手</h1>
        <p class="subtitle">{{ isRegisterMode ? '创建账号，开始咨询' : '登录后使用完整功能' }}</p>
      </div>

      <form class="form" @submit.prevent="handleSubmit">
        <label class="field">
          <span class="field-label">用户名</span>
          <input
            v-model="username"
            type="text"
            class="field-input"
            placeholder="请输入用户名"
            autocomplete="username"
            required
            minlength="1"
            maxlength="32"
          />
        </label>

        <label class="field">
          <span class="field-label">密码</span>
          <input
            v-model="password"
            type="password"
            class="field-input"
            placeholder="请输入密码（至少6位）"
            autocomplete="current-password"
            required
            minlength="6"
            maxlength="64"
          />
        </label>

        <p v-if="errorMsg" class="error-msg" role="alert">{{ errorMsg }}</p>

        <button type="submit" class="btn-submit" :disabled="submitting">
          {{ submitting ? '处理中…' : isRegisterMode ? '注册' : '登录' }}
        </button>
      </form>

      <div class="switch-row">
        <span>{{ isRegisterMode ? '已有账号？' : '还没有账号？' }}</span>
        <button type="button" class="btn-switch" @click="toggleMode">
          {{ isRegisterMode ? '去登录' : '去注册' }}
        </button>
      </div>

      <router-link to="/" class="back-link">← 返回首页</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login, register } from '@/composables/useAuth'

const router = useRouter()
const route = useRoute()

const isRegisterMode = ref(false)
const username = ref('')
const password = ref('')
const errorMsg = ref('')
const submitting = ref(false)

function toggleMode() {
  isRegisterMode.value = !isRegisterMode.value
  errorMsg.value = ''
}

async function handleSubmit() {
  errorMsg.value = ''
  if (!username.value.trim() || !password.value.trim()) {
    errorMsg.value = '请填写用户名和密码'
    return
  }
  if (password.value.length < 6) {
    errorMsg.value = '密码长度至少6位'
    return
  }

  submitting.value = true
  try {
    if (isRegisterMode.value) {
      await register(username.value.trim(), password.value)
      // 注册成功后自动登录
      await login(username.value.trim(), password.value)
    } else {
      await login(username.value.trim(), password.value)
    }
    // 登录成功，跳转到重定向页面或聊天页
    const redirect = (route.query.redirect as string) || '/chat'
    router.push(redirect)
  } catch (e: any) {
    errorMsg.value = e.message || '操作失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(1rem, 4vw, 2rem);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border: 2px solid #cbd5e1;
  border-radius: clamp(12px, 2vw, 16px);
  padding: clamp(1.5rem, 4vw, 2.5rem);
  box-shadow:
    0 4px 6px -1px rgba(15, 23, 42, 0.08),
    0 12px 28px -6px rgba(15, 23, 42, 0.12);
}

.card-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.logo {
  width: clamp(72px, 16vw, 88px);
  height: clamp(72px, 16vw, 88px);
  border-radius: 14px;
  object-fit: cover;
  border: 2px solid #e2e8f0;
  margin: 0 auto 0.75rem;
  display: block;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.08);
}

.title {
  margin: 0;
  font-size: clamp(1.15rem, 3vw, 1.35rem);
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.subtitle {
  margin: 0.35rem 0 0;
  font-size: 0.875rem;
  color: #64748b;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.field-label {
  font-size: 0.8125rem;
  font-weight: 700;
  color: #334155;
}

.field-input {
  padding: 0.65rem 0.85rem;
  border: 2px solid #cbd5e1;
  border-radius: 10px;
  font-size: 0.9375rem;
  font-family: inherit;
  line-height: 1.45;
  background: #f8fafc;
  color: #0f172a;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.field-input:focus {
  border-color: #1d4ed8;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(29, 78, 216, 0.2);
}

.field-input::placeholder {
  color: #94a3b8;
}

.error-msg {
  margin: 0;
  font-size: 0.8125rem;
  font-weight: 600;
  color: #dc2626;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 0.55rem 0.75rem;
}

.btn-submit {
  min-height: 48px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(180deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
  font-size: 0.9375rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  margin-top: 0.25rem;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.12),
    0 4px 12px rgba(29, 78, 216, 0.35);
}

.btn-submit:hover:not(:disabled) {
  background: linear-gradient(180deg, #1d4ed8 0%, #1e40af 100%);
  box-shadow:
    0 2px 4px rgba(15, 23, 42, 0.15),
    0 6px 16px rgba(29, 78, 216, 0.4);
}

.btn-submit:disabled {
  background: #cbd5e1;
  box-shadow: none;
  cursor: not-allowed;
  color: #94a3b8;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.25rem;
  font-size: 0.8125rem;
  color: #64748b;
}

.btn-switch {
  border: none;
  background: none;
  color: #1d4ed8;
  font-size: 0.8125rem;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  padding: 0;
}

.btn-switch:hover {
  text-decoration: underline;
}

.back-link {
  display: block;
  text-align: center;
  margin-top: 1rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: #64748b;
  text-decoration: none;
}

.back-link:hover {
  color: #1d4ed8;
}
</style>
