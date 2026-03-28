<script setup>
import { computed, onMounted, ref } from 'vue'
import { VaButton } from 'vuestic-ui'

const userName = ref('')

const displayName = computed(() => {
  const trimmed = userName.value?.trim()
  return trimmed ? trimmed : 'back'
})

function getCookieValue(name) {
  const cookiePrefix = `${name}=`
  const cookies = document.cookie ? document.cookie.split('; ') : []

  for (const cookie of cookies) {
    if (cookie.startsWith(cookiePrefix)) {
      try {
        return decodeURIComponent(cookie.substring(cookiePrefix.length))
      } catch {
        return ''
      }
    }
  }

  return ''
}

function refreshUser() {
  userName.value = getCookieValue('user_name')
}

onMounted(() => {
  refreshUser()
})
</script>

<template>
  <div class="w-full">
    <main class="hero pt-6 md:pt-8 text-center px-4">
      <h1 class="text-3xl font-extrabold mb-2">Welcome {{ displayName }}</h1>
      <p class="text-lg text-gray-600 mb-6 mx-auto max-w-2xl">
        You are signed in to Memowand and ready to continue preserving memories together.
      </p>

      <div class="flex justify-center gap-3 flex-wrap">
        <VaButton color="primary" class="px-6" to="/">Go to home</VaButton>
        <VaButton color="secondary" class="px-6" to="/login">Switch account</VaButton>
      </div>
    </main>
  </div>
</template>

<style scoped>
.hero { padding: 1rem 1rem; }
</style>

