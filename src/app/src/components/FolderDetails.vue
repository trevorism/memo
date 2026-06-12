<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton, VaBadge, VaModal } from 'vuestic-ui'
import { canManageFolder } from '../utils/auth'
import { listImages } from '../utils/galleryApi'
import { getFolder, listFolderImages, removeImageFromFolder, addImageToFolder, deleteFolder } from '../utils/folderApi'

const props = defineProps({
  folderId: { type: String, required: true }
})

const router = useRouter()
const folder = ref(null)
const images = ref([])
const loading = ref(true)
const error = ref(null)
const failedImageIds = ref({})
const removingIds = ref({})

const showRemoveModal = ref(false)
const removeTarget = ref(null)

const showDeleteFolderModal = ref(false)
const deletingFolder = ref(false)

const showAddModal = ref(false)
const loadingAll = ref(false)
const addError = ref(null)
const allImages = ref([])
const addingIds = ref({})
const failedAddIds = ref({})

const hasImages = computed(() => images.value.length > 0)

const candidateImages = computed(() => {
  const present = new Set(images.value.map((i) => i.id))
  return allImages.value.filter((i) => !present.has(i.id))
})

const canManage = computed(() => !!folder.value && canManageFolder(folder.value))

onMounted(async () => {
  await fetchData()
})

async function fetchData() {
  loading.value = true
  error.value = null
  try {
    const [folderData, imageData] = await Promise.all([
      getFolder(props.folderId),
      listFolderImages(props.folderId)
    ])
    folder.value = folderData
    images.value = imageData
  } catch (err) {
    error.value = err.response?.status === 404
      ? 'Folder not found'
      : err.message || 'Failed to load folder'
    console.error('Error fetching folder:', err)
  } finally {
    loading.value = false
  }
}

function navigateToPhoto(imageId) {
  router.push({ name: 'PhotoDetails', params: { imageId } })
}

function goBack() {
  router.back()
}

function requestRemove(image) {
  if (removingIds.value[image.id]) return
  removeTarget.value = image
  showRemoveModal.value = true
}

async function performRemove() {
  const image = removeTarget.value
  if (!image) return

  removingIds.value = { ...removingIds.value, [image.id]: true }
  try {
    await removeImageFromFolder(props.folderId, image.id)
    images.value = images.value.filter((item) => item.id !== image.id)
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to remove photo'
    console.error('Error removing image from folder:', err)
  } finally {
    const next = { ...removingIds.value }
    delete next[image.id]
    removingIds.value = next
    removeTarget.value = null
  }
}

function markImageFailed(imageId) {
  failedImageIds.value = { ...failedImageIds.value, [imageId]: true }
}

async function openAddModal() {
  showAddModal.value = true
  loadingAll.value = true
  addError.value = null
  try {
    allImages.value = await listImages({ visibility: 'all', limit: 200 })
  } catch (err) {
    addError.value = err.message || 'Failed to load photos'
    console.error('Error loading photos:', err)
  } finally {
    loadingAll.value = false
  }
}

async function addToFolder(image) {
  if (addingIds.value[image.id]) return
  addingIds.value = { ...addingIds.value, [image.id]: true }
  try {
    await addImageToFolder(props.folderId, image.id)
    images.value = [image, ...images.value]
    if (folder.value) {
      folder.value = { ...folder.value, imageCount: folder.value.imageCount + 1 }
    }
  } catch (err) {
    addError.value = err.response?.data?.message || err.message || 'Failed to add photo'
    console.error('Error adding image to folder:', err)
  } finally {
    const next = { ...addingIds.value }
    delete next[image.id]
    addingIds.value = next
  }
}

function markAddFailed(imageId) {
  failedAddIds.value = { ...failedAddIds.value, [imageId]: true }
}

async function performDeleteFolder() {
  if (deletingFolder.value) return
  deletingFolder.value = true
  error.value = null
  try {
    await deleteFolder(props.folderId)
    router.push({ name: 'Home' })
  } catch (err) {
    error.value = err.response?.status === 403
      ? 'Only the folder creator can delete this album.'
      : err.response?.data?.message || err.message || 'Failed to delete album'
    console.error('Error deleting folder:', err)
  } finally {
    deletingFolder.value = false
    showDeleteFolderModal.value = false
  }
}
</script>

<template>
  <div class="folder-container px-4 py-8">
    <div class="mb-6">
      <VaButton preset="plain" color="secondary" @click="goBack" class="mb-4">← Back</VaButton>
      <div class="flex flex-col md:flex-row md:items-end justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">{{ folder ? folder.name : 'Folder' }}</h2>
          <p v-if="folder" class="text-gray-500 text-sm mt-1">
            {{ folder.imageCount }} {{ folder.imageCount === 1 ? 'photo' : 'photos' }} · by {{ folder.username }}
          </p>
        </div>
        <div v-if="folder" class="flex items-center gap-2">
          <VaButton color="primary" @click="openAddModal">Add photos</VaButton>
          <VaButton
            v-if="canManage"
            preset="secondary"
            color="danger"
            :loading="deletingFolder"
            @click="showDeleteFolderModal = true"
          >
            Delete album
          </VaButton>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12">
      <p class="text-gray-500">Loading photos...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <p class="text-red-500">{{ error }}</p>
      <VaButton class="mt-4" @click="fetchData" color="warning">Try Again</VaButton>
    </div>

    <div v-else-if="!hasImages" class="text-center py-12">
      <p class="text-gray-500 text-lg mb-4">No photos in this folder</p>
      <p class="text-gray-400 mb-6">Add photos to start filling this album.</p>
      <VaButton color="primary" size="large" @click="openAddModal">Add Photos</VaButton>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <article
        v-for="image in images"
        :key="image.id"
        class="bg-white rounded-md border border-gray-200 overflow-hidden shadow-sm hover:shadow-lg transition-shadow"
      >
        <button
          type="button"
          class="relative w-full h-40 bg-gray-200 overflow-hidden text-left"
          @click="navigateToPhoto(image.id)"
          :title="`Open photo from ${image.uploadedBy}`"
        >
          <div
            v-if="failedImageIds[image.id]"
            class="w-full h-full flex items-center justify-center text-xs text-gray-600 px-2 text-center"
          >
            Image unavailable (open details)
          </div>
          <img
            v-else
            :src="image.thumbnailUrl || image.url"
            :alt="'Photo from ' + image.uploadedBy"
            class="w-full h-full object-cover"
            @error="markImageFailed(image.id)"
          />
        </button>
        <div class="p-3">
          <p
            v-if="image.caption"
            class="text-sm text-gray-700 mb-2 line-clamp-2"
            :title="image.caption"
          >
            {{ image.caption }}
          </p>
          <div class="flex items-center justify-between gap-2">
            <div class="min-w-0">
              <p class="font-semibold text-sm truncate" :title="image.uploadedBy">{{ image.uploadedBy }}</p>
              <span v-if="image.uploadedDate" class="text-xs text-gray-500">
                {{ new Date(image.uploadedDate).toLocaleDateString() }}
              </span>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <VaBadge
                :text="`${image.commentCount} ${image.commentCount === 1 ? 'comment' : 'comments'}`"
                color="info"
              />
              <VaButton
                preset="secondary"
                color="danger"
                size="small"
                :loading="!!removingIds[image.id]"
                @click="requestRemove(image)"
              >
                Remove
              </VaButton>
            </div>
          </div>
        </div>
      </article>
    </div>

    <VaModal
      v-model="showRemoveModal"
      title="Remove from folder?"
      ok-text="Remove"
      cancel-text="Cancel"
      @ok="performRemove"
    >
      <p class="text-gray-700">
        This removes the photo from this folder. The photo itself is not deleted and
        stays in the gallery.
      </p>
    </VaModal>

    <VaModal
      v-model="showDeleteFolderModal"
      title="Delete album?"
      ok-text="Delete"
      cancel-text="Cancel"
      @ok="performDeleteFolder"
    >
      <p class="text-gray-700">
        This will delete the album<span v-if="folder"> "{{ folder.name }}"</span>.
        The photos inside it are not deleted and remain in the gallery.
      </p>
    </VaModal>

    <VaModal v-model="showAddModal" title="Add photos to album" hide-default-actions>
      <div class="min-w-[300px]">
        <div v-if="addError" class="bg-red-100 border border-red-400 text-red-700 px-3 py-2 rounded mb-3 text-sm">
          {{ addError }}
        </div>

        <div v-if="loadingAll" class="text-gray-500 py-6 text-center text-sm">Loading photos...</div>

        <div v-else>
          <p v-if="!candidateImages.length" class="text-gray-500 text-sm py-4 text-center">
            All photos are already in this album.
          </p>
          <div v-else class="grid grid-cols-3 sm:grid-cols-4 gap-2 max-h-[60vh] overflow-y-auto">
            <button
              v-for="image in candidateImages"
              :key="image.id"
              type="button"
              class="relative aspect-square bg-gray-200 rounded overflow-hidden disabled:opacity-50"
              :disabled="!!addingIds[image.id]"
              :title="image.caption || `Add photo from ${image.uploadedBy}`"
              @click="addToFolder(image)"
            >
              <div
                v-if="failedAddIds[image.id]"
                class="w-full h-full flex items-center justify-center text-[10px] text-gray-600 px-1 text-center"
              >
                Unavailable
              </div>
              <img
                v-else
                :src="image.thumbnailUrl || image.url"
                :alt="'Photo from ' + image.uploadedBy"
                class="w-full h-full object-cover"
                @error="markAddFailed(image.id)"
              />
              <div
                v-if="addingIds[image.id]"
                class="absolute inset-0 flex items-center justify-center bg-white/60 text-xs"
              >
                Adding...
              </div>
              <div class="absolute bottom-1 right-1 bg-blue-600 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs shadow">＋</div>
            </button>
          </div>
        </div>

        <div class="flex justify-end mt-5">
          <VaButton preset="secondary" @click="showAddModal = false">Done</VaButton>
        </div>
      </div>
    </VaModal>
  </div>
</template>

<style scoped>
.folder-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
