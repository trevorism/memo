<template>
  <div class="dashboard">
    <h1>Dashboard</h1>
    <div class="dashboard-stats">
      <div class="stat-card">
        <div class="stat-number">{{ albums.length }}</div>
        <div class="stat-label">Albums</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ totalImages }}</div>
        <div class="stat-label">Photos</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ totalComments }}</div>
        <div class="stat-label">Comments</div>
      </div>
    </div>
    <div class="dashboard-actions">
      <router-link to="/albums" class="btn btn-primary">View Albums</router-link>
      <button @click="showCreateAlbum = true" class="btn btn-secondary">Create Album</button>
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
import { ref, computed, onMounted } from 'vue'

const albums = ref([])
const showCreateAlbum = ref(false)
const creating = ref(false)
const newAlbum = ref({ name: '', description: '' })

const totalImages = computed(() => albums.value.reduce((sum, a) => sum + (a.imageCount || 0), 0))
const totalComments = computed(() => albums.value.reduce((sum, a) => sum + (a.commentCount || 0), 0))

const fetchAlbums = async () => {
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

onMounted(fetchAlbums)
</script>

<style scoped>
.dashboard {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard h1 {
  color: #2d3748;
  margin-bottom: 2rem;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.07);
}

.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  color: #718096;
  margin-top: 0.5rem;
}

.dashboard-actions {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
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

.btn-secondary:hover {
  background: #667eea;
  color: white;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
