<script setup lang="js">
import { ref, onMounted, onBeforeUnmount } from 'vue'

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
  <canvas ref="canvasRef" aria-hidden="true" class="corner-canvas left" />
  <canvas ref="rightCanvasRef" aria-hidden="true" class="corner-canvas right" />
  <div class="top-bar" role="banner">
    <div class="title-block">
      <header class="header">Memowand</header>
      <div class="sub-heading">Private Gallery</div>
    </div>
  </div>
</template>

<style scoped>
.corner-canvas {
  position: fixed;
  top: 5px; /* centered inside the 50px top band ( (50 - 40) / 2 ) */
  width: 40px; /* CSS size matches JS `size` */
  height: 40px;
  z-index: 10001; /* above the top bar */
  pointer-events: none; /* do not block clicks */
  opacity: 0.98;
  filter: drop-shadow(0 2px 6px rgba(0,0,0,0.25));
  border-radius: 8px;
  background: transparent;
}

.corner-canvas.left { left: 12px; }
.corner-canvas.right { right: 12px; }

/* full-width top bar that reserves the top 50px */
.top-bar {
  position: fixed;
  top: 0; /* start at the very top */
  left: 0;
  right: 0;
  height: 50px; /* reserve 50px so other content won't collide */
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  pointer-events: auto;
  user-select: none;
  background: transparent;
}

/* keep content below this component from overlapping:
   consumers can also add `padding-top: 50px` to `main` if desired */
.title-block {
  text-align: center;
  line-height: 1;
}

.header {
  font-size: 1.05rem;
  font-weight: 700;
  margin: 0;
  color: #111;
}

.sub-heading {
  font-size: 0.75rem;
  color: #666;
  margin-top: 2px;
}

#header {
  position: relative;
  z-index: 1;
}
</style>