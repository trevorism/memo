<template>
  <div class="welcome-page">
    <div class="welcome-card">
      <h2>Welcome to Memowand!</h2>
      <p>Your account has been created successfully.</p>
      <router-link to="/dashboard" class="btn btn-primary">Go to Dashboard</router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.welcome-page {
  min-height: calc(100vh - 80px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.welcome-card {
  background: white;
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  text-align: center;
  max-width: 400px;
}

.welcome-card h2 {
  color: #2d3748;
  margin-bottom: 1rem;
}

.welcome-card p {
  color: #718096;
  margin-bottom: 2rem;
}

.btn {
  display: inline-block;
  padding: 0.75rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>
