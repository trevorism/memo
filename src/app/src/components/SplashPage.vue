<script setup>
import { VaButton } from 'vuestic-ui'
</script>

<template>
  <div class="w-full">
    <main class="hero pt-6 md:pt-8 text-center">
      <h1 class="text-3xl font-extrabold mb-2">Welcome to Memowand</h1>
      <p class="text-lg text-gray-600 mb-6 mx-auto max-w-2xl">
        Memowand is an invitation-only gallery for families and groups — a private place to store, share, and talk about your photos and videos.
      </p>
      <p class="text-lg text-gray-600 mb-6 mx-auto max-w-2xl">
        Sign in or register to join and start preserving memories together.
      </p>

      <div class="flex justify-center gap-3">
        <VaButton color="primary" class="px-6" to="/login">Sign in</VaButton>
        <VaButton color="secondary" class="px-6" to="/register">Register</VaButton>
      </div>
    </main>
  </div>
</template>

<style scoped>
.hero { padding: 1rem 1rem; }
</style>
