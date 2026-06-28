// Display formatting helpers. Kept pure so components share one place to
// handle null/invalid dates instead of rendering "Invalid Date".

function toDate(value) {
  if (!value) {
    return null
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

// Date only, e.g. gallery cards. Empty string for missing/invalid input.
export function formatDate(value) {
  const date = toDate(value)
  return date ? date.toLocaleDateString() : ''
}

// Date and time, e.g. photo detail and comments.
export function formatDateTime(value) {
  const date = toDate(value)
  return date ? date.toLocaleString() : ''
}

// Epoch millis for sorting; missing/invalid sorts oldest.
export function toTimestamp(value) {
  const date = toDate(value)
  return date ? date.getTime() : 0
}
