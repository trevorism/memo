import axios from 'axios'
import { mapImage, rawImageUrl } from './galleryApi'

const FOLDER_BASE = '/api/folder'

function mapFolder(raw) {
  if (!raw || !raw.id) {
    return null
  }

  const coverImageId = raw.coverImageId || null
  return {
    id: raw.id,
    name: raw.name || 'Untitled',
    username: raw.username || 'Unknown',
    imageCount: raw.imageCount ?? (Array.isArray(raw.imageIds) ? raw.imageIds.length : 0),
    coverImageId,
    coverUrl: coverImageId ? rawImageUrl(coverImageId) : null,
    createdDate: raw.createdDate || null
  }
}

async function listFolders() {
  const response = await axios.get(`${FOLDER_BASE}/`)
  const folders = Array.isArray(response.data) ? response.data : []
  return folders.map(mapFolder).filter(Boolean)
}

async function getFolder(folderId) {
  const response = await axios.get(`${FOLDER_BASE}/${encodeURIComponent(folderId)}`)
  const folder = mapFolder(response.data)
  if (!folder) {
    throw new Error('not_found')
  }
  return folder
}

async function listFolderImages(folderId) {
  const response = await axios.get(`${FOLDER_BASE}/${encodeURIComponent(folderId)}/image`)
  const images = Array.isArray(response.data) ? response.data : []
  return images.map(mapImage).filter(Boolean)
}

async function listFoldersForImage(imageId) {
  const response = await axios.get(`${FOLDER_BASE}/forImage/${encodeURIComponent(imageId)}`)
  const folders = Array.isArray(response.data) ? response.data : []
  return folders.map(mapFolder).filter(Boolean)
}

async function createFolder(name) {
  const trimmed = (name || '').trim()
  if (!trimmed) {
    throw new Error('name_required')
  }
  const response = await axios.post(`${FOLDER_BASE}/`, { name: trimmed })
  const folder = mapFolder(response.data)
  if (!folder) {
    throw new Error('create_failed')
  }
  return folder
}

async function uploadAlbumZip(file) {
  if (!file) {
    throw new Error('file_required')
  }

  const formData = new FormData()
  formData.append('file', file)

  const response = await axios.post(`${FOLDER_BASE}/zip`, formData)
  const folder = mapFolder(response.data)
  if (!folder) {
    throw new Error('create_failed')
  }
  return folder
}

async function renameFolder(folderId, name) {
  const trimmed = (name || '').trim()
  if (!trimmed) {
    throw new Error('name_required')
  }
  const response = await axios.put(`${FOLDER_BASE}/${encodeURIComponent(folderId)}`, { name: trimmed })
  return mapFolder(response.data)
}

async function deleteFolder(folderId) {
  await axios.delete(`${FOLDER_BASE}/${encodeURIComponent(folderId)}`)
  return true
}

async function addImageToFolder(folderId, imageId) {
  const response = await axios.post(
    `${FOLDER_BASE}/${encodeURIComponent(folderId)}/image/${encodeURIComponent(imageId)}`
  )
  return mapFolder(response.data)
}

async function removeImageFromFolder(folderId, imageId) {
  const response = await axios.delete(
    `${FOLDER_BASE}/${encodeURIComponent(folderId)}/image/${encodeURIComponent(imageId)}`
  )
  return mapFolder(response.data)
}

export {
  listFolders,
  getFolder,
  listFolderImages,
  listFoldersForImage,
  createFolder,
  uploadAlbumZip,
  renameFolder,
  deleteFolder,
  addImageToFolder,
  removeImageFromFolder
}
