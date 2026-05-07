import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Splash', component: () => import('../components/SplashPage.vue') },
  { path: '/login', name: 'Login', component: () => import('../components/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../components/Register.vue') },
  { path: '/welcome', name: 'Welcome', component: () => import('../components/Welcome.vue') },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../components/Dashboard.vue') },
  { path: '/albums', name: 'Albums', component: () => import('../components/Albums.vue') },
  { path: '/album/:id', name: 'AlbumDetail', component: () => import('../components/AlbumDetail.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
