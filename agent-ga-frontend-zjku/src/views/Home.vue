<template>
  <div class="home">
    <div class="home-wrap">
      <aside class="home-rail" aria-label="站点与账户">
        <div class="home-rail-head">
          <h1 class="home-rail-title" :aria-label="railTitle">
            <span v-for="(ch, i) in railTitleChars" :key="i" class="home-rail-title-char">{{ ch }}</span>
          </h1>
        </div>
        <div class="home-rail-actions">
          <UserMenu :variant="narrowScreen ? 'topbar' : 'sidebar'" />
          <router-link v-if="!loggedIn" to="/login" class="home-login-link">登录</router-link>
        </div>
      </aside>
      <div class="home-content">
    <div class="shell">
      <header class="hero">
        <img src="/assets/images/zjklogo.jpg" alt="" class="logo" />
        <h1 class="title">张家口学院明湖校区</h1>
        <p class="tagline">校园信息咨询</p>
        <p class="intro">
          面向师生的校内信息查询入口：支持教务与办事流程类问题；可在对话页开启<strong>高级模式</strong>，启用工具能力（含联网检索、网页与文件相关能力等，具体以服务端配置为准）。
        </p>
        <div class="cta-row">
          <button type="button" class="btn-main" @click="goToChat">进入咨询</button>
        </div>
      </header>

      <section class="features" aria-label="功能说明">
        <article class="card">
          <h2 class="card-title">普通模式</h2>
          <p class="card-text">优先依据校内知识库与检索增强回答，适合课程、流程、制度等相对稳定的问题。</p>
        </article>
        <article class="card">
          <h2 class="card-title">高级模式</h2>
          <p class="card-text">在普通能力基础上挂载工具集，可按需调用联网搜索等；响应时间可能更长，请按需开启。</p>
        </article>
        <article class="card">
          <h2 class="card-title">会话与历史</h2>
          <p class="card-text">支持多轮对话、会话列表、加载与删除；数据保存在本地部署的数据库中。</p>
        </article>
      </section>

      <footer class="page-foot">
        <span>© 张家口学院 · 2026</span>
      </footer>
    </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import UserMenu from '@/components/UserMenu.vue'
import { isLoggedIn } from '@/composables/useAuth'

const router = useRouter()
const loggedIn = computed(() => isLoggedIn())

const railTitle = '专属于张家口学院明湖校区的智能助手'
const railTitleChars = computed(() => Array.from(railTitle))

/** 窄屏下边栏改为一行，用户区用横向胶囊更省高 */
const narrowScreen = ref(false)
let mq: MediaQueryList | null = null
const syncNarrow = () => {
  narrowScreen.value = mq?.matches ?? false
}
onMounted(() => {
  mq = window.matchMedia('(max-width: 640px)')
  syncNarrow()
  mq.addEventListener('change', syncNarrow)
})
onUnmounted(() => {
  mq?.removeEventListener('change', syncNarrow)
})

const goToChat = () => {
  router.push('/chat')
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  padding: clamp(0.65rem, 2vw, 1rem) clamp(0.75rem, 2.5vw, 1.5rem);
  box-sizing: border-box;
}

.home-wrap {
  display: flex;
  align-items: stretch;
  gap: clamp(0.75rem, 2vw, 1.25rem);
  width: 100%;
  max-width: min(72rem, 100%);
  margin: 0 auto;
  flex: 1;
  min-height: 0;
}

.home-rail {
  width: min(7.5rem, 26vw);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 1rem 0.65rem;
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.home-rail-head {
  flex: 1;
  min-height: 0;
  display: flex;
  justify-content: center;
  align-items: stretch;
}

.home-rail-title {
  margin: 0;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-evenly;
  align-items: center;
  font-size: clamp(0.9375rem, 1.35vw, 1.125rem);
  font-weight: 800;
  line-height: 1;
  color: #334155;
  text-align: center;
}

.home-rail-title-char {
  display: block;
}

.home-rail-actions {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0.65rem;
}

.home-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.home-login-link {
  display: block;
  text-align: center;
  font-size: 0.8125rem;
  font-weight: 700;
  color: #1d4ed8;
  text-decoration: none;
  padding: 0.5rem 0.65rem;
  border-radius: 10px;
  border: 2px solid #93c5fd;
  background: #eff6ff;
}

.home-login-link:hover {
  background: #dbeafe;
}

@media (max-width: 640px) {
  .home-wrap {
    flex-direction: column;
  }

  .home-rail {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
    justify-content: space-between;
    padding: 0.65rem 0.85rem;
  }

  .home-rail-head {
    flex: 1 1 auto;
    min-height: 0;
    min-width: 0;
    justify-content: flex-start;
  }

  .home-rail-title {
    flex: 1 1 auto;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: flex-start;
    align-content: center;
    font-size: 0.875rem;
    min-width: 0;
    text-align: left;
  }

  .home-rail-title-char {
    display: inline;
  }

  .home-rail-actions {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-items: center;
    width: auto;
    gap: 0.5rem;
  }

  .home-login-link {
    display: inline-block;
    padding: 0.35rem 0.65rem;
  }
}

.shell {
  width: 100%;
  flex: 1;
  min-height: 0;
  background: #fff;
  border: 1px solid #94a3b8;
  border-radius: clamp(12px, 2vw, 16px);
  box-shadow:
    0 4px 6px -1px rgba(15, 23, 42, 0.08),
    0 12px 24px -4px rgba(15, 23, 42, 0.12);
  overflow: hidden;
}

.hero {
  padding: clamp(1.5rem, 5vw, 3rem) clamp(1.25rem, 4vw, 3rem);
  text-align: center;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
}

.logo {
  width: clamp(88px, 18vw, 120px);
  height: clamp(88px, 18vw, 120px);
  border-radius: 14px;
  object-fit: cover;
  border: 2px solid #e2e8f0;
  margin: 0 auto clamp(1rem, 3vw, 1.5rem);
  display: block;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.08);
}

.title {
  margin: 0;
  font-size: clamp(1.35rem, 4vw, 1.85rem);
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  line-height: 1.25;
}

.tagline {
  margin: 0.5rem 0 0;
  font-size: clamp(1rem, 2.5vw, 1.125rem);
  font-weight: 600;
  color: #1d4ed8;
}

.intro {
  margin: clamp(1rem, 3vw, 1.5rem) auto 0;
  max-width: 40rem;
  font-size: clamp(0.875rem, 2.2vw, 1rem);
  line-height: 1.65;
  color: #475569;
  text-align: left;
}

.intro strong {
  color: #0f172a;
}

.cta-row {
  margin-top: clamp(1.25rem, 4vw, 2rem);
}

.btn-main {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  min-width: min(100%, 280px);
  padding: 0.75rem 2.5rem;
  font-size: 1rem;
  font-weight: 700;
  font-family: inherit;
  color: #fff;
  background: linear-gradient(180deg, #2563eb 0%, #1d4ed8 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.12),
    0 4px 12px rgba(29, 78, 216, 0.35);
}

.btn-main:hover {
  background: linear-gradient(180deg, #1d4ed8 0%, #1e40af 100%);
  box-shadow:
    0 2px 4px rgba(15, 23, 42, 0.15),
    0 6px 16px rgba(29, 78, 216, 0.4);
}

.btn-main:active {
  transform: translateY(1px);
}

.features {
  display: grid;
  grid-template-columns: 1fr;
  gap: clamp(0.75rem, 2vw, 1rem);
  padding: clamp(1rem, 3vw, 1.5rem);
  background: #f8fafc;
}

@media (min-width: 640px) {
  .features {
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
    padding: 1.25rem 1.5rem 1.5rem;
  }
}

.card {
  background: #fff;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: clamp(1rem, 2.5vw, 1.25rem);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.card-title {
  margin: 0 0 0.5rem;
  font-size: 0.9375rem;
  font-weight: 700;
  color: #1e40af;
}

.card-text {
  margin: 0;
  font-size: 0.8125rem;
  line-height: 1.55;
  color: #475569;
}

.page-foot {
  padding: 1rem 1.5rem 1.25rem;
  text-align: center;
  font-size: 0.75rem;
  color: #64748b;
  border-top: 1px solid #e2e8f0;
  background: #fff;
}
</style>
