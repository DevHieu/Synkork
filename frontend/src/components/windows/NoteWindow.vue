<template>
  <div class="min-h-screen bg-background">
    <header class="sticky top-0 z-40 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div class="max-w-6xl mx-auto px-4 h-14 flex items-center gap-4">
        <div class="flex items-center gap-2 font-bold text-lg shrink-0">
          <NotebookPen :size="20" class="text-primary" />
          <span>NoteApp</span>
        </div>
        <div class="flex-1 relative max-w-sm">
          <Search :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            v-model="store.searchQuery"
            placeholder="Tìm kiếm ghi chú..."
            class="w-full pl-8 pr-3 py-1.5 text-sm rounded-lg border bg-muted/50 focus:outline-none focus:ring-2 focus:ring-ring transition-all"
          />
          <button v-if="store.searchQuery" @click="store.searchQuery = ''" class="absolute right-2 top-1/2 -translate-y-1/2">
            <X :size="12" class="text-muted-foreground" />
          </button>
        </div>
        <div class="ml-auto flex items-center gap-2">
          <span class="text-xs text-muted-foreground hidden sm:block">{{ store.notes.length }} ghi chú</span>
          <button
            @click="openCreate"
            class="flex items-center gap-1.5 px-3 py-1.5 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
          >
            <Plus :size="16" />
            <span class="hidden sm:block">Tạo mới</span>
          </button>
        </div>
      </div>
    </header>

    <main class="max-w-6xl mx-auto px-4 py-6">
      <!-- Loading -->
      <div v-if="store.loading && store.notes.length === 0" class="flex items-center justify-center py-20">
        <div class="flex flex-col items-center gap-3 text-muted-foreground">
          <Loader2 :size="28" class="animate-spin" />
          <span class="text-sm">Đang tải ghi chú...</span>
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="store.error" class="flex items-center justify-center py-20">
        <div class="text-center">
          <AlertCircle :size="40" class="mx-auto text-destructive mb-3" />
          <p class="text-sm text-muted-foreground">{{ store.error }}</p>
          <button @click="store.fetchNotes(spaceId)" class="mt-3 text-sm text-primary hover:underline">Thử lại</button>
        </div>
      </div>

      <template v-else>
        <!-- Empty state -->
        <div v-if="store.filteredNotes.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <NotebookPen :size="48" class="mb-4 opacity-20" />
          <p class="text-sm">{{ store.searchQuery ? 'Không tìm thấy ghi chú nào' : 'Chưa có ghi chú nào. Hãy tạo mới!' }}</p>
          <button v-if="!store.searchQuery" @click="openCreate" class="mt-3 text-sm text-primary hover:underline">
            Tạo ghi chú đầu tiên
          </button>
        </div>

        <template v-else>
          <!-- Pinned notes: giữ grid thường, không kéo thả -->
          <section v-if="store.pinnedNotes.length > 0" class="mb-6">
            <h2 class="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <Pin :size="12" /> Đã ghim
            </h2>
            <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
              <NoteCard
                v-for="note in store.pinnedNotes"
                :key="note.id"
                :note="note"
                @view="openDetail"
                @edit="openEdit"
                @delete="confirmDelete"
                @pin="handleTogglePin"
              />
            </div>
          </section>

          <!-- Unpinned notes: dùng GridLayout kéo thả -->
          <section v-if="store.unpinnedNotes.length > 0">
            <h2 v-if="store.pinnedNotes.length > 0" class="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">
              Khác
            </h2>
            <GridLayout
              v-model:layout="layout"
              :col-num="12"
              :row-height="100"
              :is-draggable="true"
              :is-resizable="true"
              :margin="[12, 12]"
              :use-css-transforms="true"
              :responsive="false"
              @layout-updated="onLayoutUpdated"
            >
              <GridItem
                v-for="item in layout"
                :key="item.i"
                :x="item.x"
                :y="item.y"
                :w="item.w"
                :h="item.h"
                :i="item.i"
                drag-allow-from=".drag-handle"
                drag-ignore-from=".no-drag"
              >
                <NoteCard
                  v-if="getNoteById(item.i)"
                  :note="getNoteById(item.i)!"
                  @view="openDetail"
                  @edit="openEdit"
                  @delete="confirmDelete"
                  @pin="handleTogglePin"
                />
              </GridItem>
            </GridLayout>
          </section>
        </template>
      </template>
    </main>

    <!-- Detail Dialog -->
    <NoteDetailDialog
      :open="detailOpen"
      :note="selectedNote"
      @close="detailOpen = false"
      @edit="openEditFromDetail"
      @delete="confirmDelete"
    />

    <!-- Edit/Create Dialog -->
    <NoteDialog
      :open="dialogOpen"
      :note="selectedNote"
      @close="dialogOpen = false"
      @submit="handleSubmit"
    />

    <!-- Confirm Delete -->
    <ConfirmDialog
      :open="confirmOpen"
      @confirm="handleDelete"
      @cancel="confirmOpen = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NotebookPen, Plus, Search, X, Pin, Loader2, AlertCircle } from 'lucide-vue-next'
import { GridLayout, GridItem } from 'vue3-grid-layout-next'
import 'vue3-grid-layout-next/dist/style.css'
import { useNoteStore } from '@/stores/noteStore'
import NoteCard from '@/components/note/NoteCard.vue'
import NoteDialog from '@/components/dialog/NoteDialog/NoteDialog.vue'
import NoteDetailDialog from '@/components/dialog/NoteDialog/NoteDetailDialog.vue'
import ConfirmDialog from '@/components/dialog/NoteDialog/ConfirmDialog.vue'
import type { Note, NoteRequest } from '@/types/NoteType'

const route = useRoute()
const spaceId = computed(() => route.params.spaceId as string)

const store = useNoteStore()
const dialogOpen = ref(false)
const detailOpen = ref(false)
const selectedNote = ref<Note | null>(null)
const confirmOpen = ref(false)
const deleteTargetId = ref<string | null>(null)

// Layout cho GridLayout - sync từ unpinnedNotes
const layout = ref<{ i: string; x: number; y: number; w: number; h: number }[]>([])

// Mỗi khi unpinnedNotes thay đổi (thêm/xóa note), sync lại layout
watch(
  () => store.unpinnedNotes,
  (newNotes) => {
    const existingIds = new Set(layout.value.map(l => l.i))

    // Thêm note mới vào layout
    newNotes.forEach((note, index) => {
      if (!existingIds.has(note.id)) {
        layout.value.push({
          i: note.id,
          x: note.posX ?? (index * 3) % 12,
          y: note.posY ?? 9999, // để xuống dưới cùng
          w: note.width  ?? 3,
          h: note.height ?? 2,
        })
      }
    })

    // Xóa note đã bị remove khỏi layout
    const noteIds = new Set(newNotes.map(n => n.id))
    layout.value = layout.value.filter(l => noteIds.has(l.i))
  },
  { immediate: true, deep: true }
)

function getNoteById(id: string): Note | undefined {
  return store.unpinnedNotes.find(n => n.id === id)
}

// Debounce tránh spam API khi đang kéo
let debounceTimer: ReturnType<typeof setTimeout>

function onLayoutUpdated(newLayout: { i: string; x: number; y: number; w: number; h: number }[]) {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(async () => {
    const promises = newLayout.map(item =>
      store.updateNotePosition(spaceId.value, item.i, {
        posX: item.x,
        posY: item.y,
        width: item.w,
        height: item.h,
      })
    )
    await Promise.all(promises)
  }, 600)
}

onMounted(() => {
  store.fetchNotes(spaceId.value)
})

watch(spaceId, (newId, oldId) => {
  if (oldId) store.disconnectSocket(oldId)
  layout.value = []
  store.fetchNotes(newId)
})

onUnmounted(() => {
  store.disconnectSocket(spaceId.value)
  store.notes = []
})

function openCreate() {
  selectedNote.value = null
  dialogOpen.value = true
}

function openDetail(note: Note) {
  selectedNote.value = note
  detailOpen.value = true
}

function openEdit(note: Note) {
  selectedNote.value = note
  dialogOpen.value = true
}

function openEditFromDetail(note: Note) {
  detailOpen.value = false
  selectedNote.value = note
  dialogOpen.value = true
}

function confirmDelete(id: string) {
  deleteTargetId.value = id
  detailOpen.value = false
  confirmOpen.value = true
}

async function handleSubmit(data: NoteRequest, id?: string) {
  if (id) await store.updateNote(spaceId.value, id, data)
  else await store.createNote(spaceId.value, data)
  dialogOpen.value = false
}

async function handleDelete() {
  if (deleteTargetId.value != null) {
    await store.deleteNote(spaceId.value, deleteTargetId.value)
    confirmOpen.value = false
    deleteTargetId.value = null
  }
}

async function handleTogglePin(id: string) {
  await store.changePinStatus(spaceId.value, id)
}
</script>