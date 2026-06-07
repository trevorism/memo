<script setup>
import axios from 'axios'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton } from 'vuestic-ui'
import { getCurrentUserName } from '../utils/auth'
import Gallery from './Gallery.vue'

const router = useRouter()
const loggingOut = ref(false)
const userName = computed(() => getCurrentUserName())

const displayName = computed(() => {
  const trimmed = userName.value?.trim()
  return trimmed ? trimmed : 'back'
})

async function logout() {
  if (loggingOut.value) return

  loggingOut.value = true
  try {
    await axios.post('/api/logout/')
  } finally {
    loggingOut.value = false
    await router.push({ name: 'Home' })
  }
}
</script>

<template>
  <div class="w-full relative">
    <VaButton class="logout-corner" color="danger" :disabled="loggingOut" @click="logout">
      {{ loggingOut ? 'Logging out...' : 'Logout' }}
    </VaButton>
    <main class="hero pt-6 md:pt-8 text-center px-4">
      <h1 class="text-3xl font-extrabold mb-2">Welcome {{ displayName }}</h1>
    </main>
    <Gallery />
  </div>
</template>

<style scoped>
.hero { padding: 1rem 1rem; }

.logout-corner {
  position: absolute;
  top: 12px;
  right: 16px;
}
</style>

