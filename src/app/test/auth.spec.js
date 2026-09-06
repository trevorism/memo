import { describe, it, expect, beforeEach, vi } from 'vitest'
import { canManageFolder, getCurrentUserName, isAdmin, isLoggedIn } from '../src/utils/auth'

const session = vi.hoisted(() => ({ state: null }))

vi.mock('@trevorism/ui-auth', async () => {
  const { reactive, computed } = await import('vue')
  session.state = reactive({ authenticated: false, admin: false, username: null })
  return {
    user: computed(() =>
      session.state.authenticated ? { username: session.state.username } : null
    ),
    isAuthenticated: computed(() => session.state.authenticated),
    isAdmin: computed(() => session.state.authenticated && session.state.admin)
  }
})

function signIn({ username = 'alice', admin = false } = {}) {
  session.state.authenticated = true
  session.state.username = username
  session.state.admin = admin
}

describe('auth', () => {
  beforeEach(() => {
    session.state.authenticated = false
    session.state.username = null
    session.state.admin = false
  })

  it('reports nobody signed in before the session loads', () => {
    expect(isLoggedIn()).toBe(false)
    expect(getCurrentUserName()).toBe('')
    expect(isAdmin()).toBe(false)
  })

  it('reads the username from the session', () => {
    signIn({ username: 'alice' })

    expect(isLoggedIn()).toBe(true)
    expect(getCurrentUserName()).toBe('alice')
  })

  it('flags administrators only when they are signed in', () => {
    session.state.admin = true
    expect(isAdmin()).toBe(false)

    signIn({ admin: true })
    expect(isAdmin()).toBe(true)
  })

  it('canManageFolder allows an admin regardless of owner', () => {
    signIn({ username: 'alice', admin: true })

    expect(canManageFolder({ username: 'bob' })).toBe(true)
  })

  it('canManageFolder allows the folder creator, case insensitively', () => {
    signIn({ username: 'Alice' })

    expect(canManageFolder({ username: 'alice' })).toBe(true)
    expect(canManageFolder({ username: 'bob' })).toBe(false)
  })

  it('canManageFolder refuses a signed out visitor', () => {
    expect(canManageFolder({ username: 'alice' })).toBe(false)
    expect(canManageFolder(null)).toBe(false)
  })
})
