import { describe, it, expect } from 'vitest'
import { isValidUsername, isValidEmail, isValidPassword } from './validation'

describe('validation', () => {
  it('requires usernames of at least 3 characters, ignoring surrounding space', () => {
    expect(isValidUsername('ab')).toBe(false)
    expect(isValidUsername('  ab  ')).toBe(false)
    expect(isValidUsername('abc')).toBe(true)
    expect(isValidUsername(null)).toBe(false)
  })

  it('accepts well-formed emails and rejects the obvious garbage', () => {
    expect(isValidEmail('user@example.com')).toBe(true)
    expect(isValidEmail('  user@example.com  ')).toBe(true)
    expect(isValidEmail('user@example')).toBe(false)
    expect(isValidEmail('userexample.com')).toBe(false)
    expect(isValidEmail('a @b.com')).toBe(false)
    expect(isValidEmail('')).toBe(false)
  })

  it('requires passwords of at least 6 characters', () => {
    expect(isValidPassword('12345')).toBe(false)
    expect(isValidPassword('123456')).toBe(true)
    expect(isValidPassword(null)).toBe(false)
  })
})
