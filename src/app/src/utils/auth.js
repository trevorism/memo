import { isAdmin as sessionIsAdmin, isAuthenticated, ready, user } from '@trevorism/ui-auth'

export function sessionSettled() {
  return ready
}

export function getCurrentUserName() {
  return user.value?.username ?? ''
}

export function isLoggedIn() {
  return isAuthenticated.value
}

export function isAdmin() {
  return sessionIsAdmin.value
}

// Mirrors the backend's creator-or-admin check used to authorize folder deletion.
export function canManageFolder(folder) {
  if (isAdmin()) {
    return true
  }
  const me = getCurrentUserName()?.trim().toLowerCase()
  return !!me && (folder?.username || '').toLowerCase() === me
}
