import { describe, it, expect, beforeEach } from 'vitest'
import {
  getCookieValue,
  getCurrentUserName,
  isLoggedIn,
  isAdmin,
  canManageFolder
} from '../src/utils/auth'

function clearCookies() {
  document.cookie.split(';').forEach((c) => {
    const name = c.split('=')[0].trim()
    if (name) {
      document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`
    }
  })
}

describe('auth', () => {
  beforeEach(() => {
    clearCookies()
  })

  it('reads a cookie value and url-decodes it', () => {
    document.cookie = 'user_name=' + encodeURIComponent('Alice Smith')
    expect(getCookieValue('user_name')).toBe('Alice Smith')
  })

  it('returns empty string for a missing cookie', () => {
    expect(getCookieValue('nope')).toBe('')
  })

  it('isLoggedIn reflects presence of a non-blank user_name', () => {
    expect(isLoggedIn()).toBe(false)
    document.cookie = 'user_name=' + encodeURIComponent('bob')
    expect(getCurrentUserName()).toBe('bob')
    expect(isLoggedIn()).toBe(true)
  })

  it('isAdmin is true only for admin=true (case-insensitive)', () => {
    expect(isAdmin()).toBe(false)
    document.cookie = 'admin=TRUE'
    expect(isAdmin()).toBe(true)
  })

  it('isAdmin is false for other admin values', () => {
    document.cookie = 'admin=false'
    expect(isAdmin()).toBe(false)
  })

  it('canManageFolder allows an admin regardless of owner', () => {
    document.cookie = 'admin=true'
    document.cookie = 'user_name=' + encodeURIComponent('someoneElse')
    expect(canManageFolder({ username: 'notme' })).toBe(true)
  })

  it('canManageFolder allows the folder creator (case-insensitive)', () => {
    document.cookie = 'user_name=' + encodeURIComponent('Alice')
    expect(canManageFolder({ username: 'alice' })).toBe(true)
  })

  it('canManageFolder denies a non-owner non-admin', () => {
    document.cookie = 'user_name=' + encodeURIComponent('alice')
    expect(canManageFolder({ username: 'bob' })).toBe(false)
  })

  it('canManageFolder denies when nobody is logged in', () => {
    expect(canManageFolder({ username: 'alice' })).toBe(false)
  })
})
