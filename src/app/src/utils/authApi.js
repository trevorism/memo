import axios from 'axios'
import { logout as endSession } from '@trevorism/ui-auth'

const TENANT_GUID = '606db07c-3733-4697-88de-bb159773ea94'

async function login(username, password) {
  await axios.post(`/api/login/${TENANT_GUID}`, { username, password })
}

async function logout() {
  await endSession()
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

export { login, logout, register, forgotPassword, getOAuthRedirectUrl }
