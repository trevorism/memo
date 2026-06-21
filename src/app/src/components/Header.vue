<script setup lang="js">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'
import mixpanel from 'mixpanel-browser'
import { VaButton, VaDropdown, VaDropdownContent, VaIcon } from 'vuestic-ui'
import { useTheme } from '../composables/useTheme'
import { isLoggedIn, getCurrentUserName } from '../utils/auth'

const { theme, toggleTheme } = useTheme()

const loggedIn = isLoggedIn()
const userName = getCurrentUserName()?.trim() || ''
const loggingOut = ref(false)

async function logout() {
  if (loggingOut.value) return

  loggingOut.value = true
  try {
    await axios.post('/api/logout/')
  } finally {
    try {
      mixpanel.reset()
    } catch {
      // Analytics is best-effort; never block logout on it.
    }
    window.location.assign('/')
  }
}

const canvasRef = ref(null)
const rightCanvasRef = ref(null)
const size = 40 // CSS size in px (square canvas) — fits inside 50px top band

let resizeObserver = null
let rafId = null
let randomPhase = Math.random() * Math.PI * 2

function lerp(a, b, t) { return a + (b - a) * t }

function drawCandle(ctx, width, height, time = 0) {
  ctx.clearRect(0, 0, width, height)
  const cx = width / 2
  const cy = height / 2

  // Candle body
  const bodyW = Math.min(width, height) * 0.28
  const bodyH = Math.min(width, height) * 0.5
  const bodyX = cx - bodyW / 2
  const bodyY = cy - bodyH / 6

  ctx.fillStyle = '#e6dcd3'
  ctx.beginPath()
  ctx.roundRect(bodyX, bodyY, bodyW, bodyH, 6)
  ctx.fill()

  // Wax highlights
  ctx.fillStyle = 'rgba(255,255,255,0.55)'
  ctx.beginPath()
  ctx.moveTo(bodyX + bodyW * 0.15, bodyY + bodyH * 0.12)
  ctx.quadraticCurveTo(bodyX + bodyW * 0.25, bodyY + bodyH * 0.22, bodyX + bodyW * 0.15, bodyY + bodyH * 0.6)
  ctx.lineTo(bodyX + bodyW * 0.3, bodyY + bodyH * 0.6)
  ctx.quadraticCurveTo(bodyX + bodyW * 0.4, bodyY + bodyH * 0.4, bodyX + bodyW * 0.35, bodyY + bodyH * 0.18)
  ctx.closePath()
  ctx.fill()

  // Wick
  const wickX = cx
  const wickY1 = bodyY - 4
  const wickY2 = bodyY + bodyH * 0.06
  ctx.strokeStyle = '#2b2b2b'
  ctx.lineWidth = Math.max(1, width * 0.012)
  ctx.beginPath()
  ctx.moveTo(wickX, wickY1)
  ctx.lineTo(wickX, wickY2)
  ctx.stroke()

  // --- Flickering flame parameters (vary with time) ---
  const t = time + randomPhase
  const smallJitter = (Math.random() - 0.5) * 0.02
  const pulse = 1 + Math.sin(t * 12) * 0.06 + smallJitter // overall size pulse
  const flameRadius = Math.min(width, height) * 0.14 * pulse
  const flameX = cx + Math.sin(t * 9) * (Math.min(width, height) * 0.006) // subtle horizontal wobble
  const flameY = bodyY - (bodyH * 0.35) + Math.cos(t * 14) * (Math.min(width, height) * 0.008) // vertical wobble

  // Color interpolation between yellow and orange
  const colorMix = (Math.sin(t * 18) * 0.5 + 0.5) * (0.6 + Math.random() * 0.4)
  const r0 = Math.round(lerp(255, 255, colorMix))
  const g0 = Math.round(lerp(235, 170, colorMix))
  const b0 = Math.round(lerp(130, 60, colorMix))
  const aOuter = lerp(0.85, 0.95, Math.random())

  const g = ctx.createRadialGradient(flameX, flameY, flameRadius * 0.08, flameX, flameY, flameRadius)
  g.addColorStop(0, `rgba(${r0},${g0},${b0},0.98)`)
  g.addColorStop(0.5, `rgba(${Math.round(lerp(255,255,colorMix))},${Math.round(lerp(200,140,colorMix))},${Math.round(lerp(80,40,colorMix))},0.92)`)
  g.addColorStop(1, `rgba(${Math.round(lerp(255,255,colorMix))},${Math.round(lerp(160,110,colorMix))},${Math.round(lerp(45,15,colorMix))},${aOuter})`)

  ctx.fillStyle = g
  ctx.beginPath()
  ctx.ellipse(flameX, flameY, flameRadius * 0.8, flameRadius * 1.15, 0, 0, Math.PI * 2)
  ctx.fill()

  // Flame core (white/yellow)
  ctx.fillStyle = '#fff6d1'
  ctx.beginPath()
  const coreWobble = Math.sin(t * 20) * flameRadius * 0.12
  ctx.moveTo(flameX + coreWobble, flameY - flameRadius * 0.45)
  ctx.quadraticCurveTo(flameX + flameRadius * 0.25 + coreWobble, flameY, flameX, flameY + flameRadius * 0.45)
  ctx.quadraticCurveTo(flameX - flameRadius * 0.25 + coreWobble, flameY, flameX + coreWobble, flameY - flameRadius * 0.45)
  ctx.fill()
}

function setupCanvasSize(canvas) {
  const dpr = Math.max(1, window.devicePixelRatio || 1)
  canvas.style.width = `${size}px`
  canvas.style.height = `${size}px`
  canvas.width = Math.round(size * dpr)
  canvas.height = Math.round(size * dpr)
  return dpr
}

function resizeAndDraw() {
  const now = performance.now() / 1000
  ;[canvasRef.value, rightCanvasRef.value].forEach(canvas => {
    if (!canvas) return
    const dpr = setupCanvasSize(canvas)
    const ctx = canvas.getContext('2d')
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    drawCandle(ctx, canvas.width / dpr, canvas.height / dpr, now)
  })
}

function animate(ts) {
  const t = ts / 1000
  ;[canvasRef.value, rightCanvasRef.value].forEach(canvas => {
    if (!canvas) return
    const dpr = Math.max(1, window.devicePixelRatio || 1)
    const ctx = canvas.getContext('2d')
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    drawCandle(ctx, canvas.width / dpr, canvas.height / dpr, t)
  })
  rafId = requestAnimationFrame(animate)
}

onMounted(() => {
  resizeAndDraw()
  window.addEventListener('resize', resizeAndDraw)

  if (window.ResizeObserver) {
    resizeObserver = new ResizeObserver(resizeAndDraw)
    resizeObserver.observe(document.documentElement)
  }

  rafId = requestAnimationFrame(animate)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAndDraw)
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (rafId) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
})
</script>

<template>
  <div class="top-bar glass-bar" role="banner">
    <div class="bar-inner">
      <div v-if="loggedIn" class="bar-user" :title="userName">
        <VaIcon name="account_circle" size="1.1rem" class="user-icon" />
        <span class="user-name">{{ userName }}</span>
      </div>

      <RouterLink to="/" class="brand brand-link" aria-label="Memowand home">
        <canvas ref="canvasRef" aria-hidden="true" class="corner-canvas" />
        <div class="title-block">
          <span class="header brand-wordmark">Memowand</span>
          <span class="sub-heading">Private Gallery</span>
        </div>
        <canvas ref="rightCanvasRef" aria-hidden="true" class="corner-canvas" />
      </RouterLink>

      <!-- Wide screens: inline actions -->
      <div class="bar-actions">
        <VaButton
          v-if="loggedIn"
          preset="secondary"
          color="danger"
          size="small"
          round
          :disabled="loggingOut"
          @click="logout"
        >
          {{ loggingOut ? 'Logging out...' : 'Logout' }}
        </VaButton>
        <VaButton
          class="theme-toggle"
          preset="plain"
          :icon="theme === 'dark' ? 'light_mode' : 'dark_mode'"
          :aria-label="theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'"
          @click="toggleTheme"
        />
      </div>

      <!-- Narrow screens: collapsible menu -->
      <div class="bar-menu">
        <VaDropdown placement="bottom-end" :offset="[16, 0]" stick-to-edges>
          <template #anchor>
            <VaButton preset="plain" icon="menu" aria-label="Open menu" />
          </template>
          <VaDropdownContent class="menu-panel">
            <div v-if="loggedIn" class="menu-user">
              <VaIcon name="account_circle" size="1.2rem" />
              <span>{{ userName }}</span>
            </div>
            <VaButton
              v-if="loggedIn"
              preset="secondary"
              color="danger"
              size="small"
              :disabled="loggingOut"
              class="menu-item"
              @click="logout"
            >
              {{ loggingOut ? 'Logging out...' : 'Logout' }}
            </VaButton>
            <VaButton
              preset="secondary"
              size="small"
              class="menu-item"
              :icon="theme === 'dark' ? 'light_mode' : 'dark_mode'"
              @click="toggleTheme"
            >
              {{ theme === 'dark' ? 'Light mode' : 'Dark mode' }}
            </VaButton>
          </VaDropdownContent>
        </VaDropdown>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* full-width frosted top bar; height tracks the global --header-h token */
.top-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-h);
  z-index: 10000;
  user-select: none;
}

.bar-inner {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 0.75rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.corner-canvas {
  width: 36px;
  height: 36px;
  pointer-events: none;
  opacity: 0.98;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.25));
  border-radius: 8px;
  background: transparent;
  flex: none;
}

.title-block {
  text-align: center;
  line-height: 1.05;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header {
  font-size: 1.15rem;
  margin: 0;
}

.sub-heading {
  font-size: 0.7rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--c-muted);
  margin-top: 3px;
}

/* Username, pinned top-left */
.bar-user {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 0.35rem;
  max-width: 30vw;
  color: var(--c-muted);
}

.user-icon {
  flex: none;
  color: var(--c-accent);
}

.user-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--c-ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-actions {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* The collapsible menu only appears on narrow screens */
.bar-menu {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  display: none;
}

.menu-panel {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 11rem;
  padding: 0.5rem;
  /* Vuestic gives the floating panel an auto z-index of 1; lift it above the
     fixed header (z-index 10000) so it isn't clipped by the top bar. */
  z-index: 10001 !important;
}

.menu-user {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.25rem 0.4rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--c-ink);
  border-bottom: 1px solid var(--c-subtle);
  margin-bottom: 0.15rem;
}

.menu-item {
  justify-content: flex-start;
  width: 100%;
}

/* Mobile: collapse the inline username + actions into the menu */
@media (max-width: 640px) {
  .bar-user,
  .bar-actions {
    display: none;
  }

  .bar-menu {
    display: block;
  }
}
</style>