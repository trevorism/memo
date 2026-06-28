<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton, VaBadge, VaModal } from 'vuestic-ui'
import { getCurrentUserName } from '../utils/auth'
import { listImages, deleteImage } from '../utils/galleryApi'
import { formatDate } from '../utils/format'

// `visibility` is driven by the unified toolbar in Welcome.vue.
const props = defineProps({
  visibility: { type: String, default: 'all' }
})

const router = useRouter()
const images = ref([])
const loading = ref(true)
const error = ref(null)
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

// Refetch whenever the toolbar switches between all / mine / others.
watch(() => props.visibility, fetchImages)

async function fetchImages() {
  loading.value = true
  error.value = null

  try {
    images.value = await listImages({
      visibility: props.visibility,
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

function navigateToComments(imageId) {
  router.push({ name: 'PhotoDetails', params: { imageId }, hash: '#comments' })
}

function markImageFailed(imageId) {
  failedImageIds.value = { ...failedImageIds.value, [imageId]: true }
}
</script>

<template>
  <div class="gallery-container px-4 pt-4 pb-10">
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <div v-for="n in 6" :key="n" class="app-card overflow-hidden">
        <div class="skeleton aspect-[4/3]"></div>
        <div class="p-4 space-y-2">
          <div class="skeleton h-4 w-2/3 rounded"></div>
          <div class="skeleton h-3 w-1/3 rounded"></div>
        </div>
      </div>
    </div>

    <div v-else-if="error" class="text-center py-16">
      <p class="text-red-500">{{ error }}</p>
      <VaButton class="mt-4" @click="fetchImages" color="warning" round> Try Again </VaButton>
    </div>

    <div v-else-if="!hasImages" class="text-center py-16">
      <div class="empty-icon accent-gradient">
        <span class="material-icons">photo_library</span>
      </div>
      <p class="text-ink text-lg font-semibold mb-1">No photos yet</p>
      <p class="text-muted mb-6">
        {{ props.visibility === 'all' ? 'Be the first to upload a photo!' : 'Try a different filter.' }}
      </p>
      <VaButton color="primary" gradient round @click="navigateToUpload" size="large">
        Upload Your First Photo
      </VaButton>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
      <article v-for="image in images" :key="image.id" class="app-card-interactive group">
        <button
          type="button"
          class="relative w-full aspect-[4/3] bg-surface-2 overflow-hidden text-left"
          @click="navigateToPhoto(image.id)"
          :title="`Open photo from ${image.uploadedBy}`"
        >
          <div
            v-if="failedImageIds[image.id]"
            class="w-full h-full flex items-center justify-center text-xs text-muted px-2 text-center"
          >
            Image unavailable (open details)
          </div>
          <img
            v-else
            :src="image.thumbnailUrl || image.url"
            :alt="'Photo from ' + image.uploadedBy"
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            @error="markImageFailed(image.id)"
          />
        </button>
        <div class="p-4">
          <p v-if="image.caption" class="text-sm text-body mb-2 line-clamp-2" :title="image.caption">
            {{ image.caption }}
          </p>
          <div class="flex items-center justify-between gap-2">
            <div class="min-w-0">
              <p class="font-semibold text-sm text-ink truncate" :title="image.uploadedBy">{{ image.uploadedBy }}</p>
              <span v-if="image.uploadedDate" class="text-xs text-muted">
                {{ formatDate(image.uploadedDate) }}
              </span>
            </div>
            <div class="flex items-center gap-2 shrink-0">
              <VaBadge
                :text="`${image.commentCount} ${image.commentCount === 1 ? 'comment' : 'comments'}`"
                @click="navigateToComments(image.id)"
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

    <VaModal v-model="showDeleteModal" title="Delete photo?" ok-text="Delete" cancel-text="Cancel" @ok="performDelete">
      <p class="text-gray-700">
        This will permanently delete this photo<span v-if="deleteTarget">
          uploaded by {{ deleteTarget.uploadedBy }}</span
        >
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
