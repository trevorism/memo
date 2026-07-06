import { describe, it, expect } from 'vitest'
import { compressImage } from './imageCompression'

describe('compressImage', () => {
  it('returns the input unchanged when there is no file', async () => {
    expect(await compressImage(null)).toBe(null)
  })

  it('skips non-compressible content types', async () => {
    const file = new File(['plain text'], 'notes.txt', { type: 'text/plain' })
    expect(await compressImage(file)).toBe(file)
  })

  it('skips images already below the size threshold', async () => {
    // Small jpeg (well under the 1MB minimum) is returned as-is, no canvas work.
    const file = new File(['tiny'], 'small.jpg', { type: 'image/jpeg' })
    expect(await compressImage(file)).toBe(file)
  })
})
