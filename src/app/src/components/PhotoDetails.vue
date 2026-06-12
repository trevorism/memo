<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VaButton, VaBadge, VaModal } from 'vuestic-ui'
import { getCurrentUserName } from '../utils/auth'
import { getImage, listComments, addComment, updateCaption, deleteImage } from '../utils/galleryApi'

const route = useRoute()
const router = useRouter()

const image = ref(null)
const comments = ref([])
const loading = ref(true)
const loadingComments = ref(false)
const error = ref(null)
const commentsError = ref(null)
const submittingComment = ref(false)
const newComment = ref('')
const imageLoadFailed = ref(false)
const deleting = ref(false)
const showDeleteModal = ref(false)
const editingCaption = ref(false)
const captionDraft = ref('')
const savingCaption = ref(false)

const isOwner = computed(() => {
  const me = (getCurrentUserName() || '').trim().toLowerCase()
  return !!me && (image.value?.uploadedBy || '').toLowerCase() === me
})

const canDelete = isOwner
const canEditCaption = isOwner

const sortedComments = computed(() => {
  return [...comments.value].sort((a, b) => {
    const aTime = a?.createdDate ? new Date(a.createdDate).getTime() : 0
    const bTime = b?.createdDate ? new Date(b.createdDate).getTime() : 0
    return bTime - aTime
  })
})

const commentCount = computed(() => comments.value.length)

onMounted(async () => {
  await Promise.all([fetchImage(), loadComments()])
})

async function fetchImage() {
  loading.value = true
  error.value = null
  imageLoadFailed.value = false

  try {
    image.value = await getImage(route.params.imageId)
  } catch (err) {
    const notFound = err?.response?.status === 404 || err?.message === 'not_found'
    error.value = notFound ? 'Photo not found.' : 'Unable to load photo.'
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  if (loadingComments.value) return

  loadingComments.value = true
  commentsError.value = null

  try {
    comments.value = await listComments(route.params.imageId)
  } catch (err) {
    commentsError.value = 'Unable to load comments.'
  } finally {
    loadingComments.value = false
  }
}

function goBack() {
  router.push({ name: 'Home' })
}

function startEditCaption() {
  captionDraft.value = image.value?.caption || ''
  editingCaption.value = true
}

function cancelEditCaption() {
  editingCaption.value = false
  captionDraft.value = ''
}

async function saveCaption() {
  if (savingCaption.value) return

  savingCaption.value = true
  error.value = null

  try {
    const updated = await updateCaption(route.params.imageId, captionDraft.value)
    image.value = { ...image.value, caption: updated.caption }
    editingCaption.value = false
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Unable to update caption.'
    console.error('Error updating caption:', err)
  } finally {
    savingCaption.value = false
  }
}

function requestDelete() {
  if (deleting.value) return
  showDeleteModal.value = true
}

async function removeImage() {
  deleting.value = true
  error.value = null

  try {
    await deleteImage(route.params.imageId)
    router.push({ name: 'Home' })
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Unable to delete photo.'
    console.error('Error deleting image:', err)
  } finally {
    deleting.value = false
  }
}

async function submitComment() {
  if (!newComment.value.trim() || submittingComment.value) return

  submittingComment.value = true
  commentsError.value = null

  try {
    const created = await addComment(route.params.imageId, {
      author: getCurrentUserName() || 'Anonymous',
      text: newComment.value.trim()
    })

    comments.value = [created, ...comments.value]
    newComment.value = ''
  } catch (err) {
    commentsError.value = 'Unable to submit comment.'
  } finally {
    submittingComment.value = false
  }
}
</script>

<template>
  <div class="photo-page px-4 py-8">
    <div class="max-w-5xl mx-auto">
      <VaButton preset="plain" color="secondary" class="mb-4" @click="goBack">Back to gallery</VaButton>

      <div v-if="loading" class="text-center py-12 text-gray-500">Loading photo...</div>

      <div v-else-if="error" class="text-center py-12">
        <p class="text-red-500 mb-4">{{ error }}</p>
        <VaButton color="primary" @click="goBack">Return to Gallery</VaButton>
      </div>

      <div v-else-if="image" class="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        <div class="w-full bg-gray-100">
          <div v-if="imageLoadFailed" class="w-full h-80 flex items-center justify-center text-gray-600">
            Image unavailable
          </div>
          <img
            v-else
            :src="image.url || image.thumbnailUrl"
            :alt="`Photo uploaded by ${image.uploadedBy}`"
            class="w-full max-h-[70vh] object-contain"
            @error="imageLoadFailed = true"
          />
        </div>

        <div class="p-5">
          <div v-if="editingCaption" class="mb-4">
            <textarea
              v-model="captionDraft"
              rows="3"
              class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
              placeholder="Add a caption..."
            ></textarea>
            <div class="mt-2 flex justify-end gap-2">
              <VaButton preset="secondary" color="secondary" :disabled="savingCaption" @click="cancelEditCaption">
                Cancel
              </VaButton>
              <VaButton color="primary" :loading="savingCaption" @click="saveCaption">
                Save
              </VaButton>
            </div>
          </div>
          <div v-else class="mb-4 flex items-start justify-between gap-3">
            <p v-if="image.caption" class="text-base text-gray-800 whitespace-pre-wrap">
              {{ image.caption }}
            </p>
            <p v-else-if="canEditCaption" class="text-base text-gray-400 italic">No caption yet.</p>
            <VaButton
              v-if="canEditCaption"
              preset="plain"
              color="primary"
              size="small"
              class="shrink-0"
              @click="startEditCaption"
            >
              {{ image.caption ? 'Edit' : 'Add caption' }}
            </VaButton>
          </div>
          <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div>
              <p class="text-sm text-gray-500">Uploaded by</p>
              <p class="font-semibold">{{ image.uploadedBy || 'Unknown' }}</p>
            </div>
            <div class="flex items-center gap-3">
              <VaBadge :text="`${commentCount} comments`" color="info" />
              <span v-if="image.uploadedDate" class="text-sm text-gray-500">
                {{ new Date(image.uploadedDate).toLocaleString() }}
              </span>
              <VaButton
                v-if="canDelete"
                preset="secondary"
                color="danger"
                size="small"
                :loading="deleting"
                @click="requestDelete"
              >
                Delete
              </VaButton>
            </div>
          </div>
        </div>

        <section class="px-5 pb-5 border-t border-gray-100">
          <div class="pt-4">
            <h3 class="text-xl font-semibold mb-4">Comments</h3>

            <div class="mb-4">
              <textarea
                v-model="newComment"
                rows="3"
                class="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                placeholder="Write a comment..."
              ></textarea>
              <div class="mt-2 flex justify-end">
                <VaButton color="primary" :disabled="!newComment.trim()" :loading="submittingComment" @click="submitComment">
                  Post Comment
                </VaButton>
              </div>
            </div>

            <p v-if="loadingComments" class="text-gray-500">Loading comments...</p>
            <p v-else-if="commentsError" class="text-red-500">{{ commentsError }}</p>
            <p v-else-if="sortedComments.length === 0" class="text-gray-500">No comments yet.</p>

            <div v-else class="space-y-3">
              <article v-for="comment in sortedComments" :key="comment.id" class="p-3 border border-gray-200 rounded-md">
                <div class="flex items-center justify-between gap-3 mb-1">
                  <p class="font-semibold text-sm">{{ comment.author || 'Anonymous' }}</p>
                  <p v-if="comment.createdDate" class="text-xs text-gray-500">
                    {{ new Date(comment.createdDate).toLocaleString() }}
                  </p>
                </div>
                <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ comment.text }}</p>
              </article>
            </div>
          </div>
        </section>
      </div>
    </div>

    <VaModal
      v-model="showDeleteModal"
      title="Delete photo?"
      ok-text="Delete"
      cancel-text="Cancel"
      @ok="removeImage"
    >
      <p class="text-gray-700">
        This will permanently delete this photo and its comments. This action cannot be undone.
      </p>
    </VaModal>
  </div>
</template>

<style scoped>
.photo-page {
  min-height: calc(100vh - 80px);
  background-color: #f7f7f7;
}
</style>




