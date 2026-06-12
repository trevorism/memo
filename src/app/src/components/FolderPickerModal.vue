<script setup>
import { ref, watch } from 'vue'
import { VaModal, VaButton } from 'vuestic-ui'
import {
  listFolders,
  listFoldersForImage,
  createFolder,
  addImageToFolder,
  removeImageFromFolder
} from '../utils/folderApi'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  imageId: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'changed'])

const loading = ref(false)
const error = ref(null)
const folders = ref([])
const memberIds = ref({}) // folderId -> true if image is in folder
const busyIds = ref({})
const newFolderName = ref('')
const creating = ref(false)

watch(
  () => props.modelValue,
  async (open) => {
    if (open && props.imageId) {
      await load()
    }
  },
  { immediate: true }
)

async function load() {
  loading.value = true
  error.value = null
  try {
    const [all, mine] = await Promise.all([
      listFolders(),
      listFoldersForImage(props.imageId)
    ])
    folders.value = all
    const map = {}
    mine.forEach((f) => {
      map[f.id] = true
    })
    memberIds.value = map
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to load folders'
    console.error('Error loading folders:', err)
  } finally {
    loading.value = false
  }
}

async function toggleFolder(folder) {
  if (busyIds.value[folder.id]) return
  busyIds.value = { ...busyIds.value, [folder.id]: true }
  const wasMember = !!memberIds.value[folder.id]

  try {
    if (wasMember) {
      await removeImageFromFolder(folder.id, props.imageId)
      const next = { ...memberIds.value }
      delete next[folder.id]
      memberIds.value = next
    } else {
      await addImageToFolder(folder.id, props.imageId)
      memberIds.value = { ...memberIds.value, [folder.id]: true }
    }
    emit('changed')
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to update folder'
    console.error('Error updating folder membership:', err)
  } finally {
    const next = { ...busyIds.value }
    delete next[folder.id]
    busyIds.value = next
  }
}

async function createAndAdd() {
  const name = newFolderName.value.trim()
  if (!name || creating.value) return

  creating.value = true
  error.value = null
  try {
    const folder = await createFolder(name)
    await addImageToFolder(folder.id, props.imageId)
    folders.value = [folder, ...folders.value]
    memberIds.value = { ...memberIds.value, [folder.id]: true }
    newFolderName.value = ''
    emit('changed')
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Failed to create folder'
    console.error('Error creating folder:', err)
  } finally {
    creating.value = false
  }
}

function close() {
  emit('update:modelValue', false)
}
</script>

<template>
  <VaModal
    :model-value="modelValue"
    title="Add to folder"
    hide-default-actions
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="min-w-[280px]">
      <div v-if="error" class="bg-red-100 border border-red-400 text-red-700 px-3 py-2 rounded mb-3 text-sm">
        {{ error }}
      </div>

      <div v-if="loading" class="text-gray-500 py-6 text-center text-sm">Loading folders...</div>

      <div v-else>
        <p v-if="!folders.length" class="text-gray-500 text-sm mb-3">
          No folders yet. Create one below.
        </p>
        <ul v-else class="mb-4 max-h-64 overflow-y-auto divide-y divide-gray-100">
          <li
            v-for="folder in folders"
            :key="folder.id"
            class="flex items-center gap-3 py-2"
          >
            <input
              type="checkbox"
              :id="`folder-${folder.id}`"
              :checked="!!memberIds[folder.id]"
              :disabled="!!busyIds[folder.id]"
              @change="toggleFolder(folder)"
              class="h-4 w-4"
            />
            <label :for="`folder-${folder.id}`" class="flex-1 text-sm cursor-pointer truncate">
              {{ folder.name }}
            </label>
            <span class="text-xs text-gray-400 shrink-0">{{ folder.imageCount }}</span>
          </li>
        </ul>

        <div class="flex items-center gap-2 border-t border-gray-100 pt-3">
          <input
            v-model="newFolderName"
            type="text"
            placeholder="New folder name"
            :disabled="creating"
            @keyup.enter="createAndAdd"
            class="flex-1 border border-gray-300 rounded-md px-3 py-2 text-sm disabled:opacity-50"
          />
          <VaButton
            size="small"
            color="primary"
            :disabled="!newFolderName.trim() || creating"
            :loading="creating"
            @click="createAndAdd"
          >
            Create
          </VaButton>
        </div>
      </div>

      <div class="flex justify-end mt-5">
        <VaButton preset="secondary" @click="close">Done</VaButton>
      </div>
    </div>
  </VaModal>
</template>
