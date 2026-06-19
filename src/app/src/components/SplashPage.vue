<script setup>
import { VaButton } from 'vuestic-ui'
</script>

<template>
  <div class="splash">
    <div class="splash-glow" aria-hidden="true"></div>
    <main class="hero">
      <p class="eyebrow">Invitation-only · Private</p>
      <h1 class="splash-title">
        Welcome to <span class="brand-wordmark">Memowand</span>
      </h1>
      <p class="lede">
        A private gallery for families and groups — a calm, beautiful place to store,
        share, and talk about your photos and videos.
      </p>
      <p class="sub-lede">Sign in or register to join and start preserving memories together.</p>

      <div class="cta-row">
        <VaButton color="primary" gradient round size="large" class="px-7" to="/login">Sign in</VaButton>
        <VaButton preset="secondary" round size="large" class="px-7" to="/register">Register</VaButton>
      </div>
    </main>
  </div>
</template>

<style scoped>
.splash {
  position: relative;
  min-height: calc(100vh - var(--header-h));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 2rem 1rem;
}

/* Soft teal→cyan radial mesh behind the hero */
.splash-glow {
  position: absolute;
  inset: -20% -10% auto -10%;
  height: 70%;
  background:
    radial-gradient(40% 60% at 25% 30%, color-mix(in srgb, var(--c-accent) 28%, transparent), transparent 70%),
    radial-gradient(45% 55% at 80% 20%, color-mix(in srgb, var(--c-accent-2) 26%, transparent), transparent 70%);
  filter: blur(20px);
  pointer-events: none;
  z-index: 0;
}

.hero {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 44rem;
}

.eyebrow {
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--c-accent);
  margin-bottom: 0.75rem;
}

.splash-title {
  font-size: clamp(2.25rem, 6vw, 3.5rem);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--c-ink);
  line-height: 1.05;
  margin-bottom: 1.25rem;
}

.lede {
  font-size: 1.15rem;
  color: var(--c-body);
  margin: 0 auto 0.75rem;
  max-width: 36rem;
}

.sub-lede {
  color: var(--c-muted);
  margin: 0 auto 2rem;
  max-width: 32rem;
}

.cta-row {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}
</style>
