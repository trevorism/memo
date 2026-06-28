import axios from 'axios'

// Single source for auth/account HTTP calls, mirroring the galleryApi/folderApi
// pattern so components don't reach for axios directly.

const TENANT_GUID = '606db07c-3733-4697-88de-bb159773ea94'

// Primes auth cookies before the login screen decides where to send the user.
async function warmup() {
  await axios.get('/api/authWarmup')
}

async function login(username, password) {
  await axios.post(`/api/login/${TENANT_GUID}`, { username, password })
}

async function logout() {
  await axios.post('/api/logout/')
}

async function register({ username, email, password }) {
  await axios.post('/api/user', { username, email, password })
}

async function forgotPassword(email) {
  await axios.post('/api/login/forgot', { email, tenantId: TENANT_GUID })
}

// Returns the provider's hosted redirect URL for the caller to navigate to.
async function getOAuthRedirectUrl(provider, returnUrl = '') {
  let url = `/api/${provider}/${TENANT_GUID}`
  if (returnUrl) {
    url += `?return_url=${encodeURIComponent(returnUrl)}`
  }
  const response = await axios.get(url)
  return response.data
}

export { warmup, login, logout, register, forgotPassword, getOAuthRedirectUrl }
