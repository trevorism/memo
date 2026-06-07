import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import fs from 'node:fs'
import axios from 'axios'

const TENANT_GUID = '606db07c-3733-4697-88de-bb159773ea94'
const SECRETS_PATH = '../main/resources/secrets.properties'
// Trevorism tokens are short-lived (~15 min); refresh well before they expire.
const TOKEN_REFRESH_MS = 10 * 60 * 1000

// Minimal java.util.Properties-style parser. We parse the file directly instead of
// using properties-reader, which silently returned no keys for this file.
function readProperties(path) {
  const out = {}
  const raw = fs.readFileSync(path, 'utf8')
  for (const line of raw.split(/\r?\n/)) {
    const trimmed = line.trim()
    if (!trimmed || trimmed.startsWith('#') || trimmed.startsWith('!')) {
      continue
    }
    const separator = trimmed.indexOf('=')
    if (separator === -1) {
      continue
    }
    out[trimmed.slice(0, separator).trim()] = trimmed.slice(separator + 1).trim()
  }
  return out
}

let token = ''
let credentials = null

try {
  const properties = readProperties(SECRETS_PATH)
  const clientId = properties.localUser
  const clientSecret = properties.localPassword
  if (clientId && clientSecret) {
    credentials = { id: clientId, password: clientSecret, type: 'user', tenantGuid: TENANT_GUID }
  } else {
    console.warn('[proxy-auth] localUser/localPassword missing from secrets.properties; /api calls will 401')
  }
} catch (e) {
  console.warn('[proxy-auth] Could not read secrets.properties; /api calls will 401:', e.message)
}

async function fetchToken() {
  if (!credentials) {
    return ''
  }
  try {
    const response = await axios.post('https://auth.trevorism.com/token', credentials)
    const fetched = typeof response.data === 'string' ? response.data.trim() : ''
    if (!fetched) {
      console.warn('[proxy-auth] Token endpoint returned an empty token')
      return ''
    }
    console.log('[proxy-auth] Obtained fresh session token')
    return fetched
  } catch (err) {
    const status = err.response?.status
    console.warn('[proxy-auth] Failed to obtain token', status ? `(HTTP ${status})` : '', err.message)
    return ''
  }
}

let refreshing = null
async function refreshToken() {
  if (!refreshing) {
    refreshing = fetchToken().then((t) => {
      if (t) token = t
      refreshing = null
      return token
    })
  }
  return refreshing
}

// Bootstraps the session token only when the dev server actually starts. This keeps
// the exported config a plain (mergeable) object so vitest.config.js can mergeConfig it.
function proxyAuthPlugin() {
  return {
    name: 'proxy-auth',
    async configureServer() {
      // Fetch up front so the first /api call is authenticated, then keep it fresh.
      await refreshToken()
      const timer = setInterval(refreshToken, TOKEN_REFRESH_MS)
      timer.unref?.()
    }
  }
}

export default defineConfig({
  plugins: [tailwindcss(), vue(), proxyAuthPlugin()],
  server: {
    host: 'localhost',
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080/',
        changeOrigin: true,
        secure: false,
        configure: (proxy) => {
          proxy.on('error', (err) => {
            console.log('proxy error', err)
          })
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Sending Request to the Target:', req.method, req.url)
            if (token) {
              proxyReq.setHeader('Cookie', `session=${token}`)
            } else {
              console.warn('[proxy-auth] No session token available for', req.url)
              refreshToken()
            }
          })
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('Received Response from the Target:', proxyRes.statusCode, req.url)
            if (proxyRes.statusCode === 401) {
              console.warn('[proxy-auth] Backend returned 401; refreshing token for next request')
              token = ''
              refreshToken()
            }
            const expires = new Date(new Date().getTime() + 60 * 15 * 1000).toUTCString()
            _res.setHeader('Set-Cookie', `user_name=test; Path=/; Expires=${expires}`)
          })
        }
      },
      '/_ah': {
        target: 'http://127.0.0.1:8080/',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
