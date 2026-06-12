import axios from 'axios'

const IMAGE_BASE = '/api/image'
const COMMENT_BASE = '/api/comment'

function rawImageUrl(id) {
  return `${IMAGE_BASE}/${encodeURIComponent(id)}/raw`
}

function mapImage(raw) {
  if (!raw || !raw.id) {
    return null
  }

  const url = rawImageUrl(raw.id)
  return {
    id: raw.id,
    url,
    thumbnailUrl: url,
    uploadedBy: raw.username || raw.uploadedBy || 'Unknown',
    caption: raw.caption || '',
    commentCount: raw.commentCount ?? 0,
    uploadedDate: raw.createdDate || raw.uploadedDate || null
  }
}

function mapComment(raw) {
  if (!raw) {
    return raw
  }

  return {
    id: raw.id,
    imageId: raw.imageId,
    author: raw.author || 'Anonymous',
    text: raw.text || '',
    createdDate: raw.createdDate || null
  }
}

function listEndpoint(visibility, uploadedBy) {
  const me = (uploadedBy || '').trim()
  if (me && visibility === 'mine') {
    return `${IMAGE_BASE}/mine/${encodeURIComponent(me)}`
  }
  if (me && visibility === 'others') {
    return `${IMAGE_BASE}/others/${encodeURIComponent(me)}`
  }
  return `${IMAGE_BASE}/`
}

async function listImages({ visibility = 'all', uploadedBy = '', limit = 50 } = {}) {
  const response = await axios.get(listEndpoint(visibility, uploadedBy))
  const images = Array.isArray(response.data) ? response.data : []
  const mapped = images.map(mapImage).filter(Boolean)
  return mapped.slice(0, Math.max(0, Math.min(limit, 200)))
}

async function getImage(imageId) {
  const response = await axios.get(`${IMAGE_BASE}/${encodeURIComponent(imageId)}`)
  const image = mapImage(response.data)
  if (!image) {
    throw new Error('not_found')
  }
  return image
}

async function listComments(imageId) {
  const response = await axios.get(`${COMMENT_BASE}/${encodeURIComponent(imageId)}/comments`)
  const comments = Array.isArray(response.data) ? response.data : []
  return comments.map(mapComment)
}

async function addComment(imageId, commentPayload) {
  const text = (commentPayload?.text || '').trim()
  if (!text) {
    throw new Error('invalid_comment')
  }

  const body = {
    imageId,
    author: (commentPayload?.author || '').trim() || 'Anonymous',
    text
  }

  const response = await axios.post(
    `${COMMENT_BASE}/${encodeURIComponent(imageId)}/comments`,
    body
  )
  return mapComment(response.data)
}

async function uploadImage(file, uploadedBy, caption = '') {
  if (!file) {
    throw new Error('file_required')
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append('uploadedBy', (uploadedBy || '').trim() || 'Unknown')
  formData.append('caption', (caption || '').trim())

  const response = await axios.post(`${IMAGE_BASE}/`, formData)
  return mapImage(response.data)
}

async function deleteComment(imageId, commentId) {
  await axios.delete(
    `${COMMENT_BASE}/${encodeURIComponent(imageId)}/comments/${encodeURIComponent(commentId)}`
  )
  return true
}

async function updateCaption(imageId, caption) {
  const response = await axios.put(`${IMAGE_BASE}/${encodeURIComponent(imageId)}`, {
    caption: (caption || '').trim()
  })
  const image = mapImage(response.data)
  if (!image) {
    throw new Error('not_found')
  }
  return image
}

async function deleteImage(imageId) {
  await axios.delete(`${IMAGE_BASE}/${encodeURIComponent(imageId)}`)
  return true
}

export { listImages, getImage, listComments, addComment, deleteComment, uploadImage, updateCaption, deleteImage }
