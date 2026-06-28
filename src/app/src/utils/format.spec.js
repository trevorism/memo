import { describe, it, expect } from 'vitest'
import { formatDate, formatDateTime, toTimestamp } from './format'

describe('format', () => {
  it('returns empty string for missing or invalid dates', () => {
    expect(formatDate(null)).toBe('')
    expect(formatDate('')).toBe('')
    expect(formatDate('not-a-date')).toBe('')
    expect(formatDateTime(undefined)).toBe('')
    expect(formatDateTime('nonsense')).toBe('')
  })

  it('formats valid dates without throwing', () => {
    const iso = '2026-01-02T03:04:05Z'
    expect(formatDate(iso)).toBe(new Date(iso).toLocaleDateString())
    expect(formatDateTime(iso)).toBe(new Date(iso).toLocaleString())
  })

  it('converts dates to a sortable timestamp, 0 for invalid', () => {
    const iso = '2026-01-02T03:04:05Z'
    expect(toTimestamp(iso)).toBe(new Date(iso).getTime())
    expect(toTimestamp(null)).toBe(0)
    expect(toTimestamp('garbage')).toBe(0)
  })
})
