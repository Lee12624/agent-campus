import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Login from '../views/Login.vue'
import { isLoggedIn } from '@/composables/useAuth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { public: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: Chat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const pub = to.meta.public === true
  if (pub) {
    if (to.path === '/login' && isLoggedIn()) {
      next('/chat')
      return
    }
    next()
    return
  }
  if (!isLoggedIn()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router

