import { describe, it, expect } from 'vitest'
import { destinationFromQuery } from '../src/utils/redirectTarget'

describe('destinationFromQuery', () => {
  it('honors the next param the auth library sends', () => {
    expect(destinationFromQuery({ next: '/folder/12' })).toBe('/folder/12')
  })

  it('still honors return_url from the oauth buttons', () => {
    expect(destinationFromQuery({ return_url: '/upload' })).toBe('/upload')
  })

  it('prefers next when both are present', () => {
    expect(destinationFromQuery({ next: '/albums', return_url: '/upload' })).toBe('/albums')
  })

  it('falls back to return_url when next is not usable', () => {
    expect(destinationFromQuery({ next: 'https://evil.example', return_url: '/upload' })).toBe(
      '/upload'
    )
  })

  it('rejects an absolute url', () => {
    expect(destinationFromQuery({ next: 'https://evil.example/steal' })).toBe('')
  })

  it('rejects a protocol relative url', () => {
    expect(destinationFromQuery({ next: '//evil.example/steal' })).toBe('')
  })

  it('rejects a backslash escape that browsers treat as protocol relative', () => {
    expect(destinationFromQuery({ next: '/\\evil.example' })).toBe('')
  })

  it('rejects a scheme relative path', () => {
    expect(destinationFromQuery({ next: 'javascript:alert(1)' })).toBe('')
  })

  it('returns empty when nothing was asked for', () => {
    expect(destinationFromQuery({})).toBe('')
    expect(destinationFromQuery()).toBe('')
  })

  it('ignores a repeated param that vue router hands over as an array', () => {
    expect(destinationFromQuery({ next: ['/a', '/b'] })).toBe('')
  })
})
