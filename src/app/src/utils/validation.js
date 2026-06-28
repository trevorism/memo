// Shared form-validation rules so the auth screens agree on what's valid.
// These mirror the backend's minimums; keep them in sync if the API changes.

export const MIN_USERNAME_LENGTH = 3
export const MIN_PASSWORD_LENGTH = 6

export function isValidUsername(value) {
  return (value || '').trim().length >= MIN_USERNAME_LENGTH
}

// Pragmatic check: local@domain.tld with no whitespace. Not RFC-exhaustive,
// but rejects the obvious garbage the old `includes('@')` check let through.
export function isValidEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((value || '').trim())
}

export function isValidPassword(value) {
  return (value || '').length >= MIN_PASSWORD_LENGTH
}
