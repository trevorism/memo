<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { VaButton } from 'vuestic-ui'
import Gallery from './Gallery.vue'
import Albums from './Albums.vue'

const router = useRouter()

// Single source of truth for "what am I looking at". The first three values are
// photo-uploader filters passed straight to the Gallery; 'albums' swaps the view.
const filter = ref('all')
const albumsRef = ref(null)

const filters = [
  { value: 'all', label: 'All Photos' },
  { value: 'mine', label: 'Mine' },
  { value: 'others', label: "Others'" },
  { value: 'albums', label: 'Albums' }
]

const isAlbums = computed(() => filter.value === 'albums')

function onAction() {
  if (isAlbums.value) {
    albumsRef.value?.openCreateModal()
  } else {
    router.push({ name: 'Upload' })
  }
}
</script>

<template>
  <div class="w-full">
    <div class="toolbar">
      <div class="segmented" role="tablist">
        <button
          v-for="f in filters"
          :key="f.value"
          type="button"
          class="segmented-btn"
          :class="{ 'is-active': filter === f.value }"
          role="tab"
          :aria-selected="filter === f.value"
          @click="filter = f.value"
        >
          {{ f.label }}
        </button>
      </div>

      <VaButton
        color="primary"
        gradient
        round
        :icon="isAlbums ? 'create_new_folder' : 'add_a_photo'"
        @click="onAction"
      >
        {{ isAlbums ? 'New Folder' : 'Upload Photo' }}
      </VaButton>
    </div>

    <Albums v-if="isAlbums" ref="albumsRef" />
    <Gallery v-else :visibility="filter" />
  </div>
</template>

<style scoped>
.toolbar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.25rem 1rem 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

/* Pill segmented control: filters by content type / uploader */
.segmented {
  display: inline-flex;
  max-width: 100%;
  padding: 4px;
  gap: 4px;
  border-radius: 9999px;
  background: var(--c-surface-2);
  border: 1px solid var(--c-subtle);
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.segmented::-webkit-scrollbar {
  display: none;
}

.segmented-btn {
  appearance: none;
  border: 0;
  cursor: pointer;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 600;
  white-space: nowrap;
  color: var(--c-muted);
  background: transparent;
  transition: color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.segmented-btn:hover {
  color: var(--c-ink);
}

.segmented-btn.is-active {
  color: var(--c-accent-contrast);
  background-image: linear-gradient(120deg, var(--c-accent), var(--c-accent-2));
  box-shadow: 0 6px 16px -8px color-mix(in srgb, var(--c-accent) 70%, transparent);
}
</style>
