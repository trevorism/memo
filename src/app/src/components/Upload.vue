<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton } from 'vuestic-ui'
import { getCurrentUserName } from '../utils/auth'
import { uploadImage as saveImage } from '../utils/galleryApi'
import { uploadAlbumZip } from '../utils/folderApi'
import { compressImage } from '../utils/imageCompression'

const router = useRouter()
const uploading = ref(false)
const error = ref(null)
const success = ref(false)
const selectedFile = ref(null)
const preview = ref(null)
const caption = ref('')

const CAPTION_MAX = 280
const MAX_IMAGE_MB = 10
const MAX_ZIP_MB = 32

function isZipFile(file) {
  if (!file) return false
  const type = (file.type || '').toLowerCase()
  return (
    type === 'application/zip' ||
    type === 'application/x-zip-compressed' ||
    type === 'application/x-zip' ||
    /\.zip$/i.test(file.name || '')
  )
}

const isZip = computed(() => isZipFile(selectedFile.value))

function handleFileSelect(event) {
  const files = event.target.files
  if (!files || files.length === 0) return

  const file = files[0]
  const zip = isZipFile(file)

  // Validate file type (single image or a .zip of images)
  if (!zip && !file.type.startsWith('image/')) {
    error.value = 'Please select an image or a .zip file'
    selectedFile.value = null
    preview.value = null
    return
  }

  // Validate file size
  const maxMb = zip ? MAX_ZIP_MB : MAX_IMAGE_MB
  if (file.size > maxMb * 1024 * 1024) {
    error.value = `File size must be less than ${maxMb}MB`
    selectedFile.value = null
    preview.value = null
    return
  }

  selectedFile.value = file
  error.value = null

  // Zips have no image preview
  if (zip) {
    preview.value = null
    return
  }

  // Create preview for single images
  const reader = new FileReader()
  reader.onload = (e) => {
    preview.value = e.target.result
  }
  reader.readAsDataURL(file)
}

async function uploadImage() {
  if (!selectedFile.value) {
    error.value = 'Please select an image or a .zip file'
    return
  }

  uploading.value = true
  error.value = null

  try {
    if (isZip.value) {
      const folder = await uploadAlbumZip(selectedFile.value, getCurrentUserName() || 'Unknown')
      if (folder?.id) {
        success.value = true
        // Open the newly created album
        setTimeout(() => {
          router.push({ name: 'FolderDetails', params: { folderId: folder.id } })
        }, 1200)
      }
    } else {
      const fileToUpload = await compressImage(selectedFile.value)
      const response = await saveImage(fileToUpload, getCurrentUserName() || 'Unknown', caption.value)

      if (response?.id) {
        success.value = true
        // Redirect back to welcome page after 2 seconds
        setTimeout(() => {
          router.push({ name: 'Home' })
        }, 2000)
      }
    }
  } catch (err) {
    error.value =
      err.response?.data?.error ||
      err.response?.data?.message ||
      err.message ||
      'Failed to upload'
    console.error('Error uploading:', err)
  } finally {
    uploading.value = false
  }
}

function goBack() {
  router.back()
}

function clearFile() {
  selectedFile.value = null
  preview.value = null
  caption.value = ''
  error.value = null
}
</script>

<template>
  <div class="upload-container px-4 py-8">
    <div class="max-w-2xl mx-auto">
      <!-- Header -->
      <div class="mb-6">
        <VaButton preset="plain" color="secondary" @click="goBack" class="mb-4">
          ← Back
        </VaButton>
        <h1 class="text-3xl font-extrabold tracking-tight text-ink">{{ isZip ? 'Create Album from Zip' : 'Upload Photo' }}</h1>
        <p class="text-muted mt-2">
          Share a photo with the gallery, or upload a .zip to create an album from its images.
        </p>
      </div>

      <!-- Success Message -->
      <div v-if="success" class="bg-green-500/10 border border-green-500/40 text-green-600 px-4 py-3 rounded-xl mb-6">
        {{ isZip ? 'Album created! Opening it now...' : 'Photo uploaded successfully! Redirecting...' }}
      </div>

      <!-- Error Message -->
      <div v-if="error" class="bg-red-500/10 border border-red-500/40 text-red-500 px-4 py-3 rounded-xl mb-6">
        {{ error }}
      </div>

      <!-- Upload Form -->
      <div v-if="!success" class="app-card p-8">
        <!-- Preview -->
        <div v-if="preview" class="mb-6">
          <p class="text-sm font-semibold text-body mb-2">Preview</p>
          <img :src="preview" alt="Preview" class="max-w-full h-auto max-h-96 rounded-xl border border-subtle" />
        </div>

        <!-- Zip notice -->
        <div v-else-if="isZip" class="mb-6 p-4 bg-accent/10 border border-accent/30 rounded-xl text-sm text-body">
          📦 This will create an album named <strong class="text-ink">{{ selectedFile.name.replace(/\.zip$/i, '') }}</strong>
          from the images inside the zip.
        </div>

        <!-- File Input -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-body mb-2"> Select Image or Zip </label>
          <input
            type="file"
            accept="image/*,.zip"
            @change="handleFileSelect"
            :disabled="uploading"
            class="block w-full text-sm text-muted
              file:mr-4 file:py-2 file:px-4
              file:rounded-full file:border-0
              file:text-sm file:font-semibold
              file:bg-surface-2 file:text-accent
              hover:file:bg-accent/10
              disabled:opacity-50 cursor-pointer"
          />
          <p class="text-xs text-muted mt-2">
            Images: JPG, PNG, GIF, WebP (max {{ MAX_IMAGE_MB }}MB) · Album zip: .zip (max {{ MAX_ZIP_MB }}MB)
          </p>
        </div>

        <!-- Selected File Info -->
        <div v-if="selectedFile" class="mb-6 p-4 bg-surface-2 rounded-xl">
          <p class="text-sm text-body">
            <span class="font-semibold text-ink">File:</span> {{ selectedFile.name }}
          </p>
          <p class="text-sm text-body">
            <span class="font-semibold text-ink">Size:</span>
            {{ (selectedFile.size / 1024 / 1024).toFixed(2) }} MB
          </p>
        </div>

        <!-- Caption -->
        <div v-if="!isZip" class="mb-6">
          <label for="caption" class="block text-sm font-semibold text-body mb-2">
            Caption <span class="font-normal text-muted">(optional)</span>
          </label>
          <textarea
            id="caption"
            v-model="caption"
            rows="2"
            :maxlength="CAPTION_MAX"
            :disabled="uploading"
            placeholder="Add a caption for this photo..."
            class="app-input"
          ></textarea>
          <p class="text-xs text-muted mt-1 text-right">{{ caption.length }}/{{ CAPTION_MAX }}</p>
        </div>

        <!-- Action Buttons -->
        <div class="flex gap-3 justify-end">
          <VaButton preset="secondary" round @click="clearFile" :disabled="uploading">
            Clear
          </VaButton>
          <VaButton
            :disabled="!selectedFile || uploading"
            :loading="uploading"
            @click="uploadImage"
            color="primary"
            gradient
            round
            size="medium"
          >
            {{ uploading ? (isZip ? 'Creating album...' : 'Uploading...') : (isZip ? 'Create Album' : 'Upload Photo') }}
          </VaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.upload-container {
  min-height: calc(100vh - var(--header-h));
  background-color: var(--c-canvas);
}
</style>



