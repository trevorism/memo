<script setup>
import Header from "./components/Header.vue";
import { useTheme } from "./composables/useTheme";

useTheme().init();
</script>

<template>
  <div class="min-h-screen bg-canvas text-body">
    <Header />
    <main class="app-main">
      <router-view></router-view>
    </main>
  </div>
</template>

<style scoped>
.app-main {
  padding-top: var(--header-h);
}
</style>
