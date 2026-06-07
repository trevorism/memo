<script setup>
import { computed } from 'vue'
import SplashPage from './SplashPage.vue'
import Welcome from './Welcome.vue'
import { isLoggedIn as getIsLoggedIn } from '../utils/auth'

const isLoggedIn = computed(() => getIsLoggedIn())
</script>

<template>
  <Welcome v-if="isLoggedIn" />
  <SplashPage v-else />
</template>

