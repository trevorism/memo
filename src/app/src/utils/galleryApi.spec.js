import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import { listImages, getImage, listComments, addComment, uploadImage, deleteImage } from './galleryApi'

vi.mock('axios')

beforeEach(() => {
  vi.clearAllMocks()
})

describe('galleryApi', () => {
  it('maps backend images to the gallery shape', async () => {
    axios.get.mockResolvedValueOnce({
      data: [
        { id: 'abc', username: 'trevor', bucketPath: 'a.jpg', createdDate: '2026-01-01T00:00:00Z', commentCount: 3 }
      ]
    })

    const images = await listImages({ visibility: 'all' })

    expect(axios.get).toHaveBeenCalledWith('/api/image/')
    expect(images).toEqual([
      {
        id: 'abc',
        url: '/api/image/abc/raw',
        thumbnailUrl: '/api/image/abc/raw',
        uploadedBy: 'trevor',
        caption: '',
        commentCount: 3,
        uploadedDate: '2026-01-01T00:00:00Z'
      }
    ])
  })

  it('uses the mine endpoint when filtering by uploader', async () => {
    axios.get.mockResolvedValueOnce({ data: [] })

    await listImages({ visibility: 'mine', uploadedBy: 'trevor' })

    expect(axios.get).toHaveBeenCalledWith('/api/image/mine/trevor')
  })

  it('uses the others endpoint for everyone else', async () => {
    axios.get.mockResolvedValueOnce({ data: [] })

    await listImages({ visibility: 'others', uploadedBy: 'trevor' })

    expect(axios.get).toHaveBeenCalledWith('/api/image/others/trevor')
  })

  it('rejects with not_found when an image is missing', async () => {
    axios.get.mockResolvedValueOnce({ data: null })

    await expect(getImage('missing')).rejects.toThrow('not_found')
  })

  it('lists comments mapped to the comment shape', async () => {
    axios.get.mockResolvedValueOnce({
      data: [{ id: 'c1', imageId: 'abc', author: 'alex', text: 'Nice', createdDate: '2026-01-02T00:00:00Z' }]
    })

    const comments = await listComments('abc')

    expect(axios.get).toHaveBeenCalledWith('/api/comment/abc/comments')
    expect(comments[0]).toEqual({
      id: 'c1',
      imageId: 'abc',
      author: 'alex',
      text: 'Nice',
      createdDate: '2026-01-02T00:00:00Z'
    })
  })

  it('rejects empty comments before calling the backend', async () => {
    await expect(addComment('abc', { author: 'trevor', text: '   ' })).rejects.toThrow('invalid_comment')
    expect(axios.post).not.toHaveBeenCalled()
  })

  it('posts a comment with the image id and trimmed values', async () => {
    axios.post.mockResolvedValueOnce({
      data: { id: 'c2', imageId: 'abc', author: 'trevor', text: 'Hello', createdDate: '2026-01-03T00:00:00Z' }
    })

    const created = await addComment('abc', { author: 'trevor', text: '  Hello  ' })

    expect(axios.post).toHaveBeenCalledWith('/api/comment/abc/comments', {
      imageId: 'abc',
      author: 'trevor',
      text: 'Hello'
    })
    expect(created.text).toBe('Hello')
  })

  it('uploads an image as multipart form data', async () => {
    axios.post.mockResolvedValueOnce({
      data: { id: 'new', username: 'trevor', bucketPath: 'b.jpg', createdDate: '2026-01-04T00:00:00Z' }
    })

    const file = new File(['data'], 'b.jpg', { type: 'image/jpeg' })
    const created = await uploadImage(file, 'trevor')

    expect(axios.post).toHaveBeenCalledTimes(1)
    const [url, body] = axios.post.mock.calls[0]
    expect(url).toBe('/api/image/')
    expect(body).toBeInstanceOf(FormData)
    expect(body.get('file')).toBe(file)
    expect(body.get('uploadedBy')).toBe('trevor')
    expect(created.id).toBe('new')
  })

  it('deletes an image by id', async () => {
    axios.delete.mockResolvedValueOnce({})

    const result = await deleteImage('abc')

    expect(axios.delete).toHaveBeenCalledWith('/api/image/abc')
    expect(result).toBe(true)
  })
})

