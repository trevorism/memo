<template>
  <div class="album-detail">
    <div class="album-header">
      <button @click="goBack" class="btn-back">← Back</button>
      <div class="album-title">
        <h1>{{ album?.name || 'Album' }}</h1>
        <p>{{ album?.description || '' }}</p>
      </div>
      <button @click="showUpload = true" class="btn btn-primary">+ Upload Photos</button>
    </div>
    
    <div v-if="loading" class="loading">Loading photos...</div>
    
    <div v-else-if="images.length === 0" class="empty-state">
      <p>No photos in this album yet. Upload some!</p>
    </div>
    
    <div v-else class="photos-grid">
      <div
        v-for="image in images"
        :key="image.id"
        class="photo-card"
      >
        <img
          :src="getImageUrl(image.id)"
          :alt="image.fileName"
          class="photo-image"
          @click="viewImage(image)"
        />
        <div class="photo-actions">
          <button @click="toggleComments(image)" class="btn-comment">
            💬 {{ image.commentCount || 0 }}
          </button>
          <button @click="deleteImage(image.id)" class="btn-delete">🗑️</button>
        </div>
        
        <div v-if="showCommentsFor === image.id" class="comments-section">
          <div class="comments-list">
            <div
              v-for="comment in image.comments"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-header">
                <strong>{{ comment.userName || 'Anonymous' }}</strong>
                <span class="comment-date">{{ new Date(comment.createdAt).toLocaleDateString() }}</span>
              </div>
              <p class="comment-text">{{ comment.text }}</p>
            </div>
          </div>
          <form @submit.prevent="addComment(image)" class="comment-form">
            <input
              v-model="newCommentText"
              placeholder="Add a comment..."
              class="comment-input"
            />
            <button type="submit" class="btn btn-primary btn-small">Send</button>
          </form>
        </div>
      </div>
    </div>
    
    <div v-if="showUpload" class="modal-overlay" @click.self="showUpload = false">
      <div class="modal">
        <h3>Upload Photos</h3>
        <form @submit.prevent="handleUpload">
          <div class="form-group">
            <label>Select Photos</label>
            <input
              type="file"
              multiple
              accept="image/*"
              @change="handleFileSelect"
              required
            />
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" :disabled="uploading">
              {{ uploading ? 'Uploading...' : 'Upload' }}
            </button>
            <button type="button" class="btn btn-secondary" @click="showUpload = false">Cancel</button>
          </div>
        </form>
      </div>
    </div>
    
    <div v-if="selectedImage" class="image-modal" @click.self="selectedImage = null">
      <div class="image-modal-content">
        <img :src="getImageUrl(selectedImage.id)" :alt="selectedImage.fileName" />
        <button @click="selectedImage = null" class="btn-close">✕</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const album = ref(null)
const images = ref([])
const loading = ref(false)
const showUpload = ref(false)
const uploading = ref(false)
const selectedImage = ref(null)
const showCommentsFor = ref(null)
const newCommentText = ref('')
const selectedFiles = ref([])

const albumId = route.params.id

const fetchAlbum = async () => {
  try {
    const token = localStorage.getItem('memo_token')
    const response = await fetch(`/api/album/${albumId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (response.ok) {
      album.value = await response.json()
    }
  } catch (e) {
    console.error('Failed to fetch album:', e)
  }
}

const fetchImages = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('memo_token')
    const response = await fetch(`/api/image/album/${albumId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (response.ok) {
      images.value = await response.json()
    }
  } catch (e) {
    console.error('Failed to fetch images:', e)
  } finally {
    loading.value = false
  }
}

const getImageUrl = (imageId) => `/api/image/${imageId}/raw`

const handleFileSelect = (event) => {
  selectedFiles.value = Array.from(event.target.files)
}

const handleUpload = async () => {
  uploading.value = true
  try {
    const token = localStorage.getItem('memo_token')
    for (const file of selectedFiles.value) {
      const formData = new FormData()
      formData.append('file', file)
      
      await fetch(`/api/image/album/${albumId}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
      })
    }
    showUpload.value = false
    selectedFiles.value = []
    await fetchImages()
  } catch (e) {
    console.error('Failed to upload:', e)
  } finally {
    uploading.value = false
  }
}

const toggleComments = (image) => {
  showCommentsFor.value = showCommentsFor.value === image.id ? null : image.id
}

const addComment = async (image) => {
  if (!newCommentText.value.trim()) return
  
  try {
    const token = localStorage.getItem('memo_token')
    const response = await fetch(`/api/comment/image/${image.id}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ text: newCommentText.value })
    })
    if (response.ok) {
      newCommentText.value = ''
      await fetchImages()
    }
  } catch (e) {
    console.error('Failed to add comment:', e)
  }
}

const deleteImage = async (imageId) => {
  if (!confirm('Delete this photo?')) return
  
  try {
    const token = localStorage.getItem('memo_token')
    await fetch(`/api/image/${imageId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    await fetchImages()
  } catch (e) {
    console.error('Failed to delete image:', e)
  }
}

const viewImage = (image) => {
  selectedImage.value = image
}

const goBack = () => {
  router.push('/albums')
}

onMounted(() => {
  fetchAlbum()
  fetchImages()
})
</script>

<style scoped>
.album-detail {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.album-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.btn-back {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #667eea;
}

.album-title h1 {
  color: #2d3748;
  margin: 0;
}

.album-title p {
  color: #718096;
  margin: 0.5rem 0 0;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-small {
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  color: #718096;
  padding: 3rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #718096;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
}

.photo-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.07);
}

.photo-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s;
}

.photo-image:hover {
  transform: scale(1.02);
}

.photo-actions {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem;
}

.btn-comment,
.btn-delete {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-comment:hover,
.btn-delete:hover {
  background: #f7fafc;
}

.comments-section {
  border-top: 1px solid #e2e8f0;
  padding: 1rem;
}

.comments-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 1rem;
}

.comment-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid #f7fafc;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  margin-bottom: 0.25rem;
}

.comment-date {
  color: #a0aec0;
}

.comment-text {
  color: #4a5568;
  margin: 0;
}

.comment-form {
  display: flex;
  gap: 0.5rem;
}

.comment-input {
  flex: 1;
  padding: 0.5rem;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
}

.modal h3 {
  margin-bottom: 1.5rem;
  color: #2d3748;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-weight: 500;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.modal-actions .btn {
  flex: 1;
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
}

.image-modal-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.image-modal-content img {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
}

.btn-close {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
}
</style>
