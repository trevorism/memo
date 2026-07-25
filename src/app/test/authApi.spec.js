import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import { warmup, login, logout, register, forgotPassword, getOAuthRedirectUrl } from '../src/utils/authApi'

vi.mock('axios', () => ({
  default: { get: vi.fn(), post: vi.fn() }
}))

const TENANT_GUID = '606db07c-3733-4697-88de-bb159773ea94'

describe('authApi', () => {
  beforeEach(() => {
    axios.get.mockReset()
    axios.post.mockReset()
    axios.get.mockResolvedValue({ data: '' })
    axios.post.mockResolvedValue({})
  })

  it('warmup hits the warmup endpoint', async () => {
    await warmup()
    expect(axios.get).toHaveBeenCalledWith('/api/authWarmup')
  })

  it('login posts credentials to the tenant login endpoint', async () => {
    await login('alice', 'secret1')
    expect(axios.post).toHaveBeenCalledWith(`/api/login/${TENANT_GUID}`, {
      username: 'alice',
      password: 'secret1'
    })
  })

  it('logout posts to the logout endpoint', async () => {
    await logout()
    expect(axios.post).toHaveBeenCalledWith('/api/logout/')
  })

  it('register posts the new-user payload', async () => {
    await register({ username: 'alice', email: 'a@b.com', password: 'secret1' })
    expect(axios.post).toHaveBeenCalledWith('/api/user', {
      username: 'alice',
      email: 'a@b.com',
      password: 'secret1'
    })
  })

  it('forgotPassword posts the email with the tenant id', async () => {
    await forgotPassword('a@b.com')
    expect(axios.post).toHaveBeenCalledWith('/api/login/forgot', {
      email: 'a@b.com',
      tenantId: TENANT_GUID
    })
  })

  it('getOAuthRedirectUrl builds the provider url and returns the redirect', async () => {
    axios.get.mockResolvedValueOnce({ data: 'https://provider/redirect' })
    const url = await getOAuthRedirectUrl('google')
    expect(axios.get).toHaveBeenCalledWith(`/api/google/${TENANT_GUID}`)
    expect(url).toBe('https://provider/redirect')
  })

  it('getOAuthRedirectUrl appends an encoded return_url', async () => {
    axios.get.mockResolvedValueOnce({ data: 'x' })
    await getOAuthRedirectUrl('microsoft', 'https://ret.com/a?b=1')
    expect(axios.get).toHaveBeenCalledWith(
      `/api/microsoft/${TENANT_GUID}?return_url=${encodeURIComponent('https://ret.com/a?b=1')}`
    )
  })
})
