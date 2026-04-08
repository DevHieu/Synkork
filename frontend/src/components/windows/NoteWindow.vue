<template>
  <div class="min-h-screen bg-background">
    <header class="sticky top-0 z-40 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div class="max-w-5xl mx-auto px-4 h-14 flex items-center gap-4">
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

    <main class="max-w-5xl mx-auto px-4 py-6">
      <div v-if="store.loading && store.notes.length === 0" class="flex items-center justify-center py-20">
        <div class="flex flex-col items-center gap-3 text-muted-foreground">
          <Loader2 :size="28" class="animate-spin" />
          <span class="text-sm">Đang tải ghi chú...</span>
        </div>
      </div>

      <div v-else-if="store.error" class="flex items-center justify-center py-20">
        <div class="text-center">
          <AlertCircle :size="40" class="mx-auto text-destructive mb-3" />
          <p class="text-sm text-muted-foreground">{{ store.error }}</p>
          <button @click="store.fetchNotes(spaceId)" class="mt-3 text-sm text-primary hover:underline">Thử lại</button>
        </div>
      </div>

      <template v-else>
        <div v-if="store.filteredNotes.length === 0" class="flex flex-col items-center justify-center py-20 text-muted-foreground">
          <NotebookPen :size="48" class="mb-4 opacity-20" />
          <p class="text-sm">{{ store.searchQuery ? 'Không tìm thấy ghi chú nào' : 'Chưa có ghi chú nào. Hãy tạo mới!' }}</p>
          <button v-if="!store.searchQuery" @click="openCreate" class="mt-3 text-sm text-primary hover:underline">
            Tạo ghi chú đầu tiên
          </button>
        </div>

        <template v-else>
          <section v-if="store.pinnedNotes.length > 0" class="mb-6">
            <h2 class="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <Pin :size="12" /> Đã ghim
            </h2>
            <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
              <NoteCard
                v-for="note in store.pinnedNotes" :key="note.id" :note="note"
                @view="openDetail"
                @edit="openEdit"
                @delete="confirmDelete"
                @pin="handleTogglePin"
              />
            </div>
          </section>

          <section v-if="store.unpinnedNotes.length > 0">
            <h2 v-if="store.pinnedNotes.length > 0" class="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-3">
              Khác
            </h2>
            <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
              <NoteCard
                v-for="note in store.unpinnedNotes" :key="note.id" :note="note"
                @view="openDetail"
                @edit="openEdit"
                @delete="confirmDelete"
                @pin="handleTogglePin"
              />
            </div>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from "vue-router";
import { NotebookPen, Plus, Search, X, Pin, Loader2, AlertCircle } from 'lucide-vue-next'
import { useNoteStore } from '@/stores/noteStore'
import NoteCard from '@/components/note/NoteCard.vue'
import NoteDialog from '@/components/dialog/NoteDialog/NoteDialog.vue'
import NoteDetailDialog from '@/components/dialog/NoteDialog/NoteDetailDialog.vue'
import ConfirmDialog from '@/components/dialog/NoteDialog/ConfirmDialog.vue'
import type { Note, NoteRequest } from '@/types/NoteType'

const route = useRoute();
const spaceId = route.params.spaceId as string;

const store = useNoteStore()
const dialogOpen = ref(false)
const detailOpen = ref(false)
const selectedNote = ref<Note | null>(null)
const confirmOpen = ref(false)
const deleteTargetId = ref<string | null>(null)


onMounted(() => {
  store.fetchNotes(spaceId)

})

onUnmounted(() => {
  
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
  if (id) await store.updateNote(spaceId, id, data)
  else await store.createNote(spaceId, data)
  dialogOpen.value = false
}

async function handleDelete() {
  if (deleteTargetId.value != null) {
    await store.deleteNote(spaceId, deleteTargetId.value)
    confirmOpen.value = false
    deleteTargetId.value = null
  }
}

async function handleTogglePin(id: string) {
  await store.changePinStatus(spaceId, id);
}
</script>