import axios from 'axios'
import { isLoggedIn } from './auth'

// Session tokens live 15 minutes; refresh a couple of minutes early so an active
// tab effectively never hits an expired token, with the 401 interceptor as a net.
const PROACTIVE_INTERVAL_MS = 13 * 60 * 1000

const REFRESH_URL = '/api/refresh/'

// Endpoints that must never trigger a refresh-and-retry (auth flow itself).
const AUTH_PATHS = ['/api/refresh', '/api/login', '/api/logout', '/api/authWarmup']

// Single-flight guard: concurrent 401s (and the proactive timer) share one
// in-flight refresh instead of stampeding the backend.
let refreshPromise = null

function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = axios.post(REFRESH_URL).finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

function isAuthPath(url) {
  return !!url && AUTH_PATHS.some((path) => url.includes(path))
}

function redirectToLogin() {
  // Cookies were cleared by a failed refresh; a hard nav re-reads them and shows
  // the splash/login page.
  window.location.assign('/')
}

// Registers the response interceptor on the global axios instance every component
// already imports. Safe to call once at startup.
export function installAuthRefresh() {
  axios.interceptors.response.use(
    (response) => response,
    async (error) => {
      const { response, config } = error

      const shouldTryRefresh =
        response &&
        response.status === 401 &&
        config &&
        !config._retried &&
        (config.url || '').includes('/api/') &&
        !isAuthPath(config.url)

      if (!shouldTryRefresh) {
        return Promise.reject(error)
      }

      config._retried = true
      try {
        await refreshSession()
      } catch {
        redirectToLogin()
        return Promise.reject(error)
      }

      // New session cookie is set; replay the original request.
      return axios(config)
    }
  )
}

// Proactively renews the session on a timer while the user is logged in. Errors
// are swallowed here — the 401 interceptor handles a genuinely dead session.
export function startProactiveRefresh() {
  setInterval(() => {
    if (isLoggedIn()) {
      refreshSession().catch(() => {})
    }
  }, PROACTIVE_INTERVAL_MS)
}
