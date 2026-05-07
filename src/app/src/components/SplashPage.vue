<template>
  <div class="splash-page">
    <div class="splash-content">
      <h1 class="splash-title">Welcome to Memowand</h1>
      <p class="splash-subtitle">Share your family moments with photos and comments</p>
      <div class="splash-actions">
        <router-link to="/register" class="btn btn-primary">Get Started</router-link>
        <router-link to="/login" class="btn btn-secondary">Sign In</router-link>
      </div>
    </div>
    <div class="splash-features">
      <div class="feature-card">
        <div class="feature-icon">📷</div>
        <h3>Upload Photos</h3>
        <p>Share your favorite family moments with albums</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">💬</div>
        <h3>Comment</h3>
        <p>Leave comments on photos and keep the conversation going</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">👨‍👩‍👧‍👦</div>
        <h3>Family Only</h3>
        <p>Your memories stay private with your family</p>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.splash-page {
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.splash-content {
  text-align: center;
  margin-bottom: 4rem;
}

.splash-title {
  font-size: 3rem;
  color: #2d3748;
  margin-bottom: 1rem;
}

.splash-subtitle {
  font-size: 1.25rem;
  color: #718096;
  margin-bottom: 2rem;
}

.splash-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn {
  padding: 0.75rem 2rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #667eea;
  color: white;
}

.splash-features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  max-width: 900px;
  width: 100%;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.07);
  transition: transform 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  color: #2d3748;
  margin-bottom: 0.5rem;
}

.feature-card p {
  color: #718096;
}

@media (max-width: 768px) {
  .splash-features {
    grid-template-columns: 1fr;
  }
  
  .splash-title {
    font-size: 2rem;
  }
}
</style>
