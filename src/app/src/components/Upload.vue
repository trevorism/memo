<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton } from 'vuestic-ui'
import { getCurrentUserName } from '../utils/auth'
import { uploadImage as saveImage } from '../utils/galleryApi'

const router = useRouter()
const uploading = ref(false)
const error = ref(null)
const success = ref(false)
const selectedFile = ref(null)
const preview = ref(null)

function handleFileSelect(event) {
  const files = event.target.files
  if (files && files.length > 0) {
    const file = files[0]

    // Validate file type
    if (!file.type.startsWith('image/')) {
      error.value = 'Please select an image file'
      selectedFile.value = null
      preview.value = null
      return
    }

    // Validate file size (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      error.value = 'File size must be less than 10MB'
      selectedFile.value = null
      preview.value = null
      return
    }

    selectedFile.value = file
    error.value = null

    // Create preview
    const reader = new FileReader()
    reader.onload = (e) => {
      preview.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

async function uploadImage() {
  if (!selectedFile.value) {
    error.value = 'Please select an image'
    return
  }

  uploading.value = true
  error.value = null

  try {
    const response = await saveImage(selectedFile.value, getCurrentUserName() || 'Unknown')

    if (response?.id) {
      success.value = true
      // Redirect back to welcome page after 2 seconds
      setTimeout(() => {
        router.push({ name: 'Home' })
      }, 2000)
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to upload image'
    console.error('Error uploading image:', err)
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
        <h1 class="text-3xl font-bold">Upload Photo</h1>
        <p class="text-gray-600 mt-2">Share a photo with the gallery</p>
      </div>

      <!-- Success Message -->
      <div v-if="success" class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-6">
        Photo uploaded successfully! Redirecting...
      </div>

      <!-- Error Message -->
      <div v-if="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-6">
        {{ error }}
      </div>

      <!-- Upload Form -->
      <div v-if="!success" class="bg-white rounded-lg shadow-md p-8">
        <!-- Preview -->
        <div v-if="preview" class="mb-6">
          <p class="text-sm font-semibold text-gray-700 mb-2">Preview</p>
          <img :src="preview" alt="Preview" class="max-w-full h-auto max-h-96 rounded-lg" />
        </div>

        <!-- File Input -->
        <div class="mb-6">
          <label class="block text-sm font-semibold text-gray-700 mb-2"> Select Image </label>
          <input
            type="file"
            accept="image/*"
            @change="handleFileSelect"
            :disabled="uploading"
            class="block w-full text-sm text-gray-500
              file:mr-4 file:py-2 file:px-4
              file:rounded-full file:border-0
              file:text-sm file:font-semibold
              file:bg-blue-50 file:text-blue-700
              hover:file:bg-blue-100
              disabled:opacity-50"
          />
          <p class="text-xs text-gray-500 mt-2">Supported formats: JPG, PNG, GIF, WebP (max 10MB)</p>
        </div>

        <!-- Selected File Info -->
        <div v-if="selectedFile" class="mb-6 p-4 bg-gray-50 rounded">
          <p class="text-sm text-gray-600">
            <span class="font-semibold">File:</span> {{ selectedFile.name }}
          </p>
          <p class="text-sm text-gray-600">
            <span class="font-semibold">Size:</span>
            {{ (selectedFile.size / 1024 / 1024).toFixed(2) }} MB
          </p>
        </div>

        <!-- Action Buttons -->
        <div class="flex gap-3 justify-end">
          <VaButton preset="secondary" @click="clearFile" :disabled="uploading">
            Clear
          </VaButton>
          <VaButton
            :disabled="!selectedFile || uploading"
            :loading="uploading"
            @click="uploadImage"
            color="primary"
            size="medium"
          >
            {{ uploading ? 'Uploading...' : 'Upload Photo' }}
          </VaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.upload-container {
  min-height: calc(100vh - 80px);
  background-color: #f7f7f7;
}
</style>



