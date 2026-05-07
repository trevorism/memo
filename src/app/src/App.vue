<template>
  <div id="app">
    <Header />
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import Header from './components/Header.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background: #f7fafc;
  color: #2d3748;
}

.app-main {
  min-height: calc(100vh - 80px);
}
</style>
