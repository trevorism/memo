const COMPRESSIBLE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp'])

const DEFAULTS = {
  maxDimension: 1920,
  quality: 0.85,
  // Below this, the network cost is already small; skip the work.
  minBytesToCompress: 1024 * 1024
}

async function loadBitmap(file) {
  if (typeof createImageBitmap === 'function') {
    return createImageBitmap(file)
  }
  // Fallback for browsers without createImageBitmap.
  const url = URL.createObjectURL(file)
  try {
    const img = await new Promise((resolve, reject) => {
      const el = new Image()
      el.onload = () => resolve(el)
      el.onerror = reject
      el.src = url
    })
    return { width: img.naturalWidth, height: img.naturalHeight, image: img, close() {} }
  } finally {
    // Revoked after draw by the caller-friendly wrapper below.
    setTimeout(() => URL.revokeObjectURL(url), 0)
  }
}

function targetDimensions(width, height, maxDimension) {
  const largest = Math.max(width, height)
  if (largest <= maxDimension) {
    return { width, height }
  }
  const scale = maxDimension / largest
  return {
    width: Math.round(width * scale),
    height: Math.round(height * scale)
  }
}

function canvasToBlob(canvas, type, quality) {
  return new Promise((resolve) => {
    canvas.toBlob((blob) => resolve(blob), type, quality)
  })
}

async function compressImage(file, options = {}) {
  const { maxDimension, quality, minBytesToCompress } = { ...DEFAULTS, ...options }

  if (!file || !COMPRESSIBLE_TYPES.has(file.type) || file.size < minBytesToCompress) {
    return file
  }

  try {
    const bitmap = await loadBitmap(file)
    const srcWidth = bitmap.width
    const srcHeight = bitmap.height
    const { width, height } = targetDimensions(srcWidth, srcHeight, maxDimension)

    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const ctx = canvas.getContext('2d')
    ctx.drawImage(bitmap.image ?? bitmap, 0, 0, width, height)
    bitmap.close?.()

    // PNGs ignore quality and stay large; re-encode photos to JPEG.
    const outputType = file.type === 'image/webp' ? 'image/webp' : 'image/jpeg'
    const blob = await canvasToBlob(canvas, outputType, quality)

    // Bail out if compression didn't actually help (e.g. already-tiny images).
    if (!blob || blob.size >= file.size) {
      return file
    }

    const extension = outputType === 'image/webp' ? 'webp' : 'jpg'
    const newName = file.name.replace(/\.[^./\\]+$/, '') + '.' + extension
    return new File([blob], newName, { type: outputType, lastModified: Date.now() })
  } catch (err) {
    console.warn('Image compression failed; uploading original file.', err)
    return file
  }
}

export { compressImage }
