import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import {
  listFolders,
  getFolder,
  createFolder,
  uploadAlbumZip,
  deleteFolder
} from './folderApi'

vi.mock('axios', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

describe('folderApi', () => {
  beforeEach(() => {
    axios.get.mockReset()
    axios.post.mockReset()
    axios.put.mockReset()
    axios.delete.mockReset()
  })

  it('listFolders maps raw folders, applies defaults, and drops invalid ones', async () => {
    axios.get.mockResolvedValueOnce({
      data: [
        { id: 'f1', imageIds: ['a', 'b'] },
        { id: 'f2', name: 'Trip', username: 'alice', coverImageId: 'c9' },
        { name: 'no id' }
      ]
    })

    const folders = await listFolders()

    expect(folders.length).toBe(2)
    expect(folders[0]).toMatchObject({ id: 'f1', name: 'Untitled', username: 'Unknown', imageCount: 2 })
    expect(folders[1].coverUrl).toBe('/api/image/c9/thumb')
  })

  it('listFolders tolerates a non-array payload', async () => {
    axios.get.mockResolvedValueOnce({ data: null })
    expect(await listFolders()).toEqual([])
  })

  it('getFolder throws not_found when the payload has no id', async () => {
    axios.get.mockResolvedValueOnce({ data: {} })
    await expect(getFolder('x')).rejects.toThrow('not_found')
  })

  it('getFolder returns a mapped folder', async () => {
    axios.get.mockResolvedValueOnce({ data: { id: 'f1', name: 'A' } })
    const folder = await getFolder('f1')
    expect(folder).toMatchObject({ id: 'f1', name: 'A' })
  })

  it('createFolder rejects a blank name without calling the api', async () => {
    await expect(createFolder('   ')).rejects.toThrow('name_required')
    expect(axios.post).not.toHaveBeenCalled()
  })

  it('createFolder trims the name and posts it', async () => {
    axios.post.mockResolvedValueOnce({ data: { id: 'f1', name: 'Trip' } })
    const folder = await createFolder('  Trip  ')
    expect(axios.post).toHaveBeenCalledWith('/api/folder/', { name: 'Trip' })
    expect(folder.id).toBe('f1')
  })

  it('uploadAlbumZip requires a file', async () => {
    await expect(uploadAlbumZip(null)).rejects.toThrow('file_required')
    expect(axios.post).not.toHaveBeenCalled()
  })

  it('deleteFolder issues a DELETE and returns true', async () => {
    axios.delete.mockResolvedValueOnce({})
    expect(await deleteFolder('f1')).toBe(true)
    expect(axios.delete).toHaveBeenCalledWith('/api/folder/f1')
  })
})
