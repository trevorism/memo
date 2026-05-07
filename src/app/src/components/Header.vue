<template>
  <nav class="header-nav">
    <div class="nav-brand">
      <router-link to="/">📸 Memowand</router-link>
    </div>
    <div class="nav-links">
      <template v-if="user">
        <router-link to="/dashboard" class="nav-link">Dashboard</router-link>
        <router-link to="/albums" class="nav-link">Albums</router-link>
        <span class="nav-user">{{ user.name || user.email }}</span>
        <button @click="handleLogout" class="nav-btn-logout">Logout</button>
      </template>
      <template v-else>
        <router-link to="/login" class="nav-link">Login</router-link>
        <router-link to="/register" class="nav-link">Register</router-link>
      </template>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const user = ref(null)

onMounted(() => {
  const stored = localStorage.getItem('memo_user')
  if (stored) {
    try {
      user.value = JSON.parse(stored)
    } catch (e) {
      user.value = null
    }
  }
})

const handleLogout = () => {
  localStorage.removeItem('memo_user')
  localStorage.removeItem('memo_token')
  user.value = null
  router.push('/login')
}
</script>

<style scoped>
.header-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.nav-brand a {
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
  text-decoration: none;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.nav-link {
  color: rgba(255,255,255,0.85);
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all 0.2s;
}

.nav-link:hover {
  color: white;
  background: rgba(255,255,255,0.15);
}

.nav-user {
  color: rgba(255,255,255,0.9);
  font-size: 0.9rem;
}

.nav-btn-logout {
  background: rgba(255,255,255,0.2);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn-logout:hover {
  background: rgba(255,255,255,0.3);
}
</style>
