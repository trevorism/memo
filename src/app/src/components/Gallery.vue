<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton, VaBadge, VaModal } from 'vuestic-ui'
import { getCurrentUserName } from '../utils/auth'
import { listImages, deleteImage } from '../utils/galleryApi'

const router = useRouter()
const images = ref([])
const loading = ref(true)
const error = ref(null)
const selectedFilter = ref('all')
const currentUserName = computed(() => getCurrentUserName()?.trim() || '')
const failedImageIds = ref({})
const deletingImageIds = ref({})
const showDeleteModal = ref(false)
const deleteTarget = ref(null)

function canDelete(image) {
  const me = currentUserName.value.toLowerCase()
  return !!me && (image.uploadedBy || '').toLowerCase() === me
}

function requestDelete(image) {
  if (deletingImageIds.value[image.id]) return
  deleteTarget.value = image
  showDeleteModal.value = true
}

async function performDelete() {
  const image = deleteTarget.value
  if (!image) return

  deletingImageIds.value = { ...deletingImageIds.value, [image.id]: true }

  try {
    await deleteImage(image.id)
    images.value = images.value.filter((item) => item.id !== image.id)
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to delete photo'
    console.error('Error deleting image:', err)
  } finally {
    const next = { ...deletingImageIds.value }
    delete next[image.id]
    deletingImageIds.value = next
    deleteTarget.value = null
  }
}

const hasImages = computed(() => images.value.length > 0)

onMounted(async () => {
  await fetchImages()
})

async function fetchImages() {
  loading.value = true
  error.value = null

  try {
    images.value = await listImages({
      visibility: selectedFilter.value,
      uploadedBy: currentUserName.value,
      limit: 50
    })
  } catch (err) {
    error.value = err.message || 'Failed to load images'
    console.error('Error fetching images:', err)
  } finally {
    loading.value = false
  }
}

function navigateToUpload() {
  router.push({ name: 'Upload' })
}

function navigateToPhoto(imageId) {
  router.push({ name: 'PhotoDetails', params: { imageId } })
}

async function onFilterChange() {
  await fetchImages()
}

function markImageFailed(imageId) {
  failedImageIds.value = { ...failedImageIds.value, [imageId]: true }
}
</script>

<template>
  <div class="gallery-container px-4 py-8">
    <div class="flex flex-col md:flex-row justify-between md:items-center gap-3 mb-6">
      <h2 class="text-2xl font-bold">Photo Gallery</h2>
      <div class="flex items-center gap-3">
        <label for="gallery-filter" class="text-sm text-gray-600">Show</label>
        <select
          id="gallery-filter"
          v-model="selectedFilter"
          @change="onFilterChange"
          class="px-3 py-2 border border-gray-300 rounded-md text-sm"
        >
          <option value="all">All uploads</option>
          <option value="mine">My uploads</option>
          <option value="others">Others' uploads</option>
        </select>
        <VaButton color="primary" @click="navigateToUpload" size="medium">
          Upload Photo
        </VaButton>
      </div>
    </div>

    <div v-if="loading" class="text-center py-12">
      <p class="text-gray-500">Loading photos...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <p class="text-red-500">{{ error }}</p>
      <VaButton class="mt-4" @click="fetchImages" color="warning">
        Try Again
      </VaButton>
    </div>

    <div v-else-if="!hasImages" class="text-center py-12">
      <p class="text-gray-500 text-lg mb-4">No photos yet</p>
      <p class="text-gray-400 mb-6">
        {{ selectedFilter === 'all' ? 'Be the first to upload a photo!' : 'Try a different filter.' }}
      </p>
      <VaButton color="primary" @click="navigateToUpload" size="large">
        Upload Your First Photo
      </VaButton>
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
                  v-if="canDelete(image)"
                  preset="secondary"
                  color="danger"
                  size="small"
                  :loading="!!deletingImageIds[image.id]"
                  @click="requestDelete(image)"
                >
                  Delete
                </VaButton>
              </div>
            </div>
          </div>
      </article>
    </div>

    <VaModal
      v-model="showDeleteModal"
      title="Delete photo?"
      ok-text="Delete"
      cancel-text="Cancel"
      @ok="performDelete"
    >
      <p class="text-gray-700">
        This will permanently delete this photo<span v-if="deleteTarget"> uploaded by {{ deleteTarget.uploadedBy }}</span>
        and its comments. This action cannot be undone.
      </p>
    </VaModal>
  </div>
</template>

<style scoped>
.gallery-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>

