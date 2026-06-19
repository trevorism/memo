<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton, VaBadge, VaModal } from 'vuestic-ui'
import { canManageFolder } from '../utils/auth'
import { listFolders, createFolder, deleteFolder } from '../utils/folderApi'

const router = useRouter()
const folders = ref([])
const loading = ref(true)
const error = ref(null)
const failedCoverIds = ref({})
const deletingIds = ref({})

const showCreateModal = ref(false)
const newFolderName = ref('')
const creating = ref(false)

const showDeleteModal = ref(false)
const deleteTarget = ref(null)

const hasFolders = computed(() => folders.value.length > 0)

onMounted(async () => {
  await fetchFolders()
})

async function fetchFolders() {
  loading.value = true
  error.value = null
  try {
    folders.value = await listFolders()
  } catch (err) {
    error.value = err.message || 'Failed to load folders'
    console.error('Error fetching folders:', err)
  } finally {
    loading.value = false
  }
}

function openFolder(folderId) {
  router.push({ name: 'FolderDetails', params: { folderId } })
}

async function createNewFolder() {
  const name = newFolderName.value.trim()
  if (!name || creating.value) return

  creating.value = true
  error.value = null
  try {
    const folder = await createFolder(name)
    folders.value = [folder, ...folders.value]
    newFolderName.value = ''
    showCreateModal.value = false
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to create folder'
    console.error('Error creating folder:', err)
  } finally {
    creating.value = false
  }
}

function requestDelete(folder) {
  if (deletingIds.value[folder.id]) return
  deleteTarget.value = folder
  showDeleteModal.value = true
}

async function performDelete() {
  const folder = deleteTarget.value
  if (!folder) return

  deletingIds.value = { ...deletingIds.value, [folder.id]: true }
  try {
    await deleteFolder(folder.id)
    folders.value = folders.value.filter((f) => f.id !== folder.id)
  } catch (err) {
    error.value = err.response?.status === 403
      ? 'Only the folder creator can delete this folder.'
      : err.response?.data?.message || err.message || 'Failed to delete folder'
    console.error('Error deleting folder:', err)
  } finally {
    const next = { ...deletingIds.value }
    delete next[folder.id]
    deletingIds.value = next
    deleteTarget.value = null
  }
}

function markCoverFailed(folderId) {
  failedCoverIds.value = { ...failedCoverIds.value, [folderId]: true }
}

// Lets the unified toolbar in Welcome.vue trigger folder creation.
function openCreateModal() {
  showCreateModal.value = true
}

defineExpose({ openCreateModal })
</script>

<template>
  <div class="albums-container px-4 pt-4 pb-10">
    <div v-if="loading" class="grid grid-cols-2 md:grid-cols-3 gap-5">
      <div v-for="n in 6" :key="n" class="app-card overflow-hidden">
        <div class="skeleton aspect-[4/3]"></div>
        <div class="p-4"><div class="skeleton h-4 w-2/3 rounded"></div></div>
      </div>
    </div>

    <div v-else-if="error" class="text-center py-16">
      <p class="text-red-500">{{ error }}</p>
      <VaButton class="mt-4" @click="fetchFolders" color="warning" round>Try Again</VaButton>
    </div>

    <div v-else-if="!hasFolders" class="text-center py-16">
      <div class="empty-icon accent-gradient">
        <span class="material-icons">folder_open</span>
      </div>
      <p class="text-ink text-lg font-semibold mb-1">No folders yet</p>
      <p class="text-muted mb-6">Create a folder to start organizing photos.</p>
      <VaButton color="primary" gradient round @click="showCreateModal = true" size="large">
        Create Your First Folder
      </VaButton>
    </div>

    <div v-else class="grid grid-cols-2 md:grid-cols-3 gap-5">
      <article
        v-for="folder in folders"
        :key="folder.id"
        class="app-card-interactive group"
      >
        <button
          type="button"
          class="relative w-full aspect-[4/3] bg-surface-2 overflow-hidden text-left flex items-center justify-center"
          @click="openFolder(folder.id)"
          :title="`Open ${folder.name}`"
        >
          <img
            v-if="folder.coverUrl && !failedCoverIds[folder.id]"
            :src="folder.coverUrl"
            :alt="folder.name"
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            @error="markCoverFailed(folder.id)"
          />
          <span v-else class="material-icons text-5xl text-muted">folder</span>
        </button>
        <div class="p-4">
          <div class="flex items-center justify-between gap-2">
            <div class="min-w-0">
              <p class="font-semibold text-sm text-ink truncate" :title="folder.name">{{ folder.name }}</p>
              <span class="text-xs text-muted">by {{ folder.username }}</span>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <VaBadge
                :text="`${folder.imageCount} ${folder.imageCount === 1 ? 'photo' : 'photos'}`"
                color="info"
              />
              <VaButton
                v-if="canManageFolder(folder)"
                preset="secondary"
                color="danger"
                size="small"
                :loading="!!deletingIds[folder.id]"
                @click="requestDelete(folder)"
              >
                Delete
              </VaButton>
            </div>
          </div>
        </div>
      </article>
    </div>

    <VaModal
      v-model="showCreateModal"
      title="New folder"
      ok-text="Create"
      cancel-text="Cancel"
      :ok-disabled="!newFolderName.trim() || creating"
      @ok="createNewFolder"
    >
      <input
        v-model="newFolderName"
        type="text"
        placeholder="Folder name"
        :disabled="creating"
        @keyup.enter="createNewFolder"
        class="app-input"
      />
    </VaModal>

    <VaModal
      v-model="showDeleteModal"
      title="Delete folder?"
      ok-text="Delete"
      cancel-text="Cancel"
      @ok="performDelete"
    >
      <p class="text-gray-700">
        This will delete the folder<span v-if="deleteTarget"> "{{ deleteTarget.name }}"</span>.
        The photos inside it are not deleted and remain in the gallery.
      </p>
    </VaModal>
  </div>
</template>

<style scoped>
.albums-container {
  max-width: 1200px;
  margin: 0 auto;
}

.empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 9999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
  box-shadow: 0 12px 28px -12px color-mix(in srgb, var(--c-accent) 70%, transparent);
}
.empty-icon .material-icons {
  color: var(--c-accent-contrast);
  font-size: 34px;
}
</style>
