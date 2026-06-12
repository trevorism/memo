<script setup>
import axios from 'axios'
import { computed, ref } from 'vue'
import { VaButton } from 'vuestic-ui'
import { getCurrentUserName } from '../utils/auth'
import Gallery from './Gallery.vue'
import Albums from './Albums.vue'

const loggingOut = ref(false)
const view = ref('photos')
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
    // Hard navigation so the app re-reads cookies and renders the SplashPage.
    window.location.assign('/')
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
    <div class="flex justify-center gap-2 mt-2 mb-2 px-4">
      <VaButton
        :preset="view === 'photos' ? undefined : 'secondary'"
        @click="view = 'photos'"
      >
        All Photos
      </VaButton>
      <VaButton
        :preset="view === 'albums' ? undefined : 'secondary'"
        @click="view = 'albums'"
      >
        Albums
      </VaButton>
    </div>
    <Gallery v-if="view === 'photos'" />
    <Albums v-else />
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

