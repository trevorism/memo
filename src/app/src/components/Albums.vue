<template>
  <div class="albums-page">
    <div class="page-header">
      <h1>Albums</h1>
      <button @click="showCreateAlbum = true" class="btn btn-primary">+ New Album</button>
    </div>
    
    <div v-if="loading" class="loading">Loading albums...</div>
    
    <div v-else-if="albums.length === 0" class="empty-state">
      <p>No albums yet. Create your first album!</p>
    </div>
    
    <div v-else class="albums-grid">
      <div
        v-for="album in albums"
        :key="album.id"
        class="album-card"
        @click="viewAlbum(album.id)"
      >
        <div class="album-cover">
          <img
            v-if="album.coverImage"
            :src="album.coverImage"
            :alt="album.name"
          />
          <div v-else class="album-placeholder">📷</div>
        </div>
        <div class="album-info">
          <h3>{{ album.name }}</h3>
          <p>{{ album.description || 'No description' }}</p>
          <div class="album-meta">
            <span>{{ album.imageCount || 0 }} photos</span>
            <span>{{ album.createdAt ? new Date(album.createdAt).toLocaleDateString() : 'N/A' }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="showCreateAlbum" class="modal-overlay" @click.self="showCreateAlbum = false">
      <div class="modal">
        <h3>Create New Album</h3>
        <form @submit.prevent="createAlbum">
          <div class="form-group">
            <label>Album Name</label>
            <input v-model="newAlbum.name" required placeholder="Summer Vacation 2024" />
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea v-model="newAlbum.description" placeholder="A description of this album..."></textarea>
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" :disabled="creating">Create</button>
            <button type="button" class="btn btn-secondary" @click="showCreateAlbum = false">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const albums = ref([])
const loading = ref(false)
const showCreateAlbum = ref(false)
const creating = ref(false)
const newAlbum = ref({ name: '', description: '' })

const fetchAlbums = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('memo_token')
    const response = await fetch('/api/album/', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (response.ok) {
      albums.value = await response.json()
    }
  } catch (e) {
    console.error('Failed to fetch albums:', e)
  } finally {
    loading.value = false
  }
}

const createAlbum = async () => {
  creating.value = true
  try {
    const token = localStorage.getItem('memo_token')
    const response = await fetch('/api/album/', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(newAlbum.value)
    })
    if (response.ok) {
      showCreateAlbum.value = false
      newAlbum.value = { name: '', description: '' }
      await fetchAlbums()
    }
  } catch (e) {
    console.error('Failed to create album:', e)
  } finally {
    creating.value = false
  }
}

const viewAlbum = (albumId) => {
  router.push(`/album/${albumId}`)
}

onMounted(fetchAlbums)
</script>

<style scoped>
.albums-page {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  color: #2d3748;
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

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
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

.albums-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.album-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.07);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.album-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 15px rgba(0,0,0,0.1);
}

.album-cover {
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-placeholder {
  font-size: 4rem;
}

.album-info {
  padding: 1.5rem;
}

.album-info h3 {
  color: #2d3748;
  margin-bottom: 0.5rem;
}

.album-info p {
  color: #718096;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.album-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: #a0aec0;
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

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.modal-actions .btn {
  flex: 1;
}
</style>
