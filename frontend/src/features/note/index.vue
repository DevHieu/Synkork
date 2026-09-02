<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useNoteStore } from '@/features/note/stores/noteStore.ts'
import { useNoteActions } from '@/features/note/composable/UseNoteActions'
import { useUserStore } from '@/features/users/stores/userStore'
import { GridLayout, GridItem } from 'vue3-grid-layout-next'
import { NotebookPen, Plus, Search, X, Pin, Loader2, AlertCircle, Hash, Archive } from 'lucide-vue-next'

import NoteCard from '@/features/note/components/NoteCard.vue'
import NoteDialog from '@/features/note/components/dialog/NoteDialog.vue'
import NoteDetailDialog from '@/features/note/components/dialog/NoteDetailDialog.vue'
import ConfirmDialog from '@/features/note/components/dialog/ConfirmDialog.vue'
import ReminderDialog from '@/features/note/components/dialog/ReminderDialog.vue'
import ArchivedNotesDialog from '@/features/note/components/dialog/ArchivedNotesDialog.vue'
import ConflictDialog from '@/features/note/components/dialog/ConflictDialog.vue'

import type { Note, NoteRequest } from '@/features/note/types/NoteType'
import { useSpaceStore } from '@/features/spaces/stores/spaceStore'
import { storeToRefs } from 'pinia'
import SidebarTrigger from '@/components/ui/sidebar/SidebarTrigger.vue'

const route = useRoute()
const spaceId = computed(() => route.params.spaceId as string)
const spaceStore = useSpaceStore()

// ── State (đọc) ── và ── Actions (gọi API/side-effect) tách riêng ──
const store = useNoteStore()
const actions = useNoteActions()

const { currentSpace, isPersonalSpace } = storeToRefs(spaceStore)

// ── User store (để lấy personalNoteId cho nút "Lưu cá nhân") ──
const userStore = useUserStore()
const { userPersonalSpace } = storeToRefs(userStore)

const dialogOpen = ref(false)
const detailOpen = ref(false)
const selectedNote = ref<Note | null>(null)
const confirmOpen = ref(false)
const deleteTargetId = ref<string | null>(null)
const reminderOpen = ref(false)
const reminderNote = ref<Note | null>(null)
const layout = ref<any[]>([])
const archivedOpen = ref(false)
const deleteTargetVersion = ref<number | undefined>(undefined)

let layoutUpdateCount = 0
let debounceTimer: ReturnType<typeof setTimeout>

// ── SINGLE watch on ID only ───────────────────────────────
watch(
  () => currentSpace.value?.id,
  (newId, oldId) => {
    if (!newId) return
    if (newId === oldId) return
    if (oldId) actions.disconnectSocket(oldId)
    store.setNotes([])
    layout.value = []
    layoutUpdateCount = 0
    actions.fetchNotes(newId)
  },
  { immediate: true }
)

watch(
  () => store.unpinnedNotes,
  (newNotes) => {
    const existingIds = new Set(layout.value.map((l) => l.i))

    newNotes.forEach((note) => {
      const id = String(note.id)
      if (existingIds.has(id)) return
      const hasSavedPosition =
        note.posX != null && note.posY != null &&
        (note.posX !== 0 || note.posY !== 0)
      const slotIndex = layout.value.length

      layout.value.push({
        i: id,
        x: hasSavedPosition ? note.posX : (slotIndex % 4) * 3,
        y: hasSavedPosition ? note.posY : Math.floor(slotIndex / 4) * 2,
        w: note.width || 3,
        h: note.height || 2,
      })
    })

    const noteIds = new Set(newNotes.map((n) => String(n.id)))
    layout.value = layout.value.filter((l) => noteIds.has(l.i))
    nextTick(() => { layoutUpdateCount = 0 })
  },
  { immediate: true, deep: true }
)

function getNoteById(id: string): Note | undefined {
  return store.unpinnedNotes.find((n) => String(n.id) === id)
}

function onLayoutUpdated(newLayout: any[]) {
  if (layoutUpdateCount < 1) { layoutUpdateCount++; return }
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    newLayout.forEach((item) => {
      actions.updateNotePosition(spaceId.value, item.i, {
        posX: item.x, posY: item.y, width: item.w, height: item.h,
      })
    })
  }, 600)
}

const localSearch = ref('')

watch(localSearch, (val) => {
  store.searchQuery = val
})

onMounted(() => {
  store.searchQuery = ''
  localSearch.value = ''
})

onUnmounted(() => {
  store.searchQuery = ''
  actions.disconnectSocket(spaceId.value)
  clearTimeout(debounceTimer)
})

// ── CRUD ──────────────────────────────────────────────────

function openCreate() {
  selectedNote.value = null
  dialogOpen.value = true
}

function openDetail(note: Note) {
  selectedNote.value = note
  detailOpen.value = true
}

function openEdit(note: Note) {
  detailOpen.value = false
  selectedNote.value = note
  dialogOpen.value = true
}

function confirmDelete(id: string) {
  deleteTargetId.value = id
  const note = store.notes.find(n => n.id === id)
  deleteTargetVersion.value = note?.version
  detailOpen.value = false
  confirmOpen.value = true
}

async function handleArchive(id: string) {
  await actions.archiveNote(spaceId.value, id)
}

async function handleDelete() {
  if (!deleteTargetId.value) return
  await actions.deleteNote(spaceId.value, deleteTargetId.value, deleteTargetVersion.value)
  confirmOpen.value = false
  deleteTargetId.value = null
  deleteTargetVersion.value = undefined
}

async function handleTogglePin(id: string) {
  await actions.changePinStatus(spaceId.value, id)
}

async function handleColorChange(id: string, color: string) {
  await actions.updateNote(spaceId.value, id, { color } as NoteRequest)
}

function openReminder(note: Note) {
  reminderNote.value = note
  reminderOpen.value = true
}

async function handleReminderConfirm(reminderAt: string | null) {
  if (!reminderNote.value) return
  await actions.setNoteReminder(spaceId.value, reminderNote.value.id, reminderAt)

  const idx = store.notes.findIndex(n => n.id === reminderNote.value!.id)
  if (idx !== -1) {
    const updated: Note = { ...(store.notes[idx] as Note), reminderAt, reminderSent: false }
    store.replaceNote(updated)
    reminderNote.value = updated
  }
  reminderOpen.value = false
}
</script>

<template>
  <div class="h-full flex flex-col overflow-y-auto background">

      <!-- HEADER -->
      <header class="sticky top-0 z-40 border-b background/95 backdrop-blur">
        <div class="mx-auto px-4 h-14 flex items-center gap-4">

          <!-- LEFT -->
          <div class="flex-1 flex items-center gap-3">
            <SidebarTrigger class="-ml-1 shrink-0" />

            <Hash class="w-5 h-5 text-teal-600 shrink-0" />

            <span class="font-semibold shrink-0">
              {{ currentSpace?.name }}
            </span>

            <!-- SEARCH -->
            <div class="relative max-w-xs w-full">
              <!-- icon -->
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground w-3.5 h-3.5 z-10" />

              <!-- fake input chống Gmail autofill -->
              <input type="email" autocomplete="username" tabindex="-1"
                class="absolute opacity-0 pointer-events-none w-0 h-0" />

              <!-- search thật -->
              <input v-model="localSearch" placeholder="Tìm kiếm ghi chú..." type="text" name="note-search-field-981273"
                autocomplete="off" data-form-type="other" autocorrect="off" autocapitalize="off" spellcheck="false"
                inputmode="search" readonly @focus="($event.target as HTMLInputElement)?.removeAttribute('readonly')"
                class="w-full pl-8 pr-8 py-1.5 text-sm rounded-lg border bg-muted/50 focus:outline-none focus:ring-2 focus:ring-ring transition-all" />

              <!-- clear -->
              <button v-if="localSearch" @click="localSearch = ''" class="absolute right-2 top-1/2 -translate-y-1/2">
                <X class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          <!-- RIGHT -->
          <div class="flex items-center gap-3">
            <span class="text-xs hidden sm:block">
              {{ store.notes.length }} ghi chú
            </span>

            <button @click="archivedOpen = true"
              class="px-3 py-1.5 text-sm rounded-lg border flex items-center gap-1 cursor-pointer hover:bg-muted transition-colors">
              <Archive class="w-4 h-4" />
              Lưu trữ
            </button>

            <button @click="openCreate"
              class="px-3 py-1.5 text-sm rounded-lg bg-primary text-white flex items-center gap-1 cursor-pointer hover:bg-primary/90 transition-colors">
              <Plus class="w-4 h-4" />
              Tạo mới
            </button>
          </div>

        </div>
      </header>

      <!-- MAIN -->
      <main class="max-w-6xl w-full mx-auto px-4 py-6 flex-1">

        <div v-if="store.loading && store.notes.length === 0" class="text-center py-20">
          <Loader2 class="animate-spin mx-auto" />
        </div>

        <div v-else-if="store.error && !store.error.includes('vị trí')" class="text-center py-20">
          <AlertCircle class="mx-auto mb-3" />
          <p>{{ store.error }}</p>
        </div>

        <template v-else>

          <div v-if="store.filteredNotes.length === 0 && !store.loading" class="text-center py-20">
            <NotebookPen class="mx-auto mb-3 opacity-20" />

            <p class="text-sm text-muted-foreground">
              {{
                store.searchQuery
                  ? 'Không tìm thấy ghi chú nào'
                  : 'Chưa có ghi chú nào'
              }}
            </p>

            <button v-if="!store.searchQuery" @click="openCreate" class="mt-3 text-sm text-primary hover:underline">
              Tạo ghi chú đầu tiên
            </button>
          </div>

          <template v-else>

            <!-- PINNED -->
            <section v-if="store.pinnedNotes.length > 0" class="mb-6">
              <h2 class="text-xs mb-3 flex items-center gap-1 text-muted-foreground font-semibold uppercase">
                <Pin class="w-3 h-3" />
                Đã ghim
              </h2>

              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <NoteCard v-for="note in store.pinnedNotes" :key="note.id" :note="note" @view="openDetail"
                  @edit="openEdit" @delete="confirmDelete" @pin="handleTogglePin" @reminder="openReminder"
                  @color="handleColorChange" @archive="handleArchive" />
              </div>
            </section>

            <!-- UNPINNED -->
            <section v-if="store.unpinnedNotes.length > 0">
              <h2 v-if="store.pinnedNotes.length > 0"
                class="text-xs mb-3 text-muted-foreground font-semibold uppercase">
                Khác
              </h2>

              <GridLayout v-model:layout="layout" :col-num="12" :row-height="100" :is-draggable="true"
                :is-resizable="true" :margin="[12, 12]" :use-css-transforms="true" @layout-updated="onLayoutUpdated">
                <GridItem v-for="item in layout" :key="item.i" :x="item.x" :y="item.y" :w="item.w" :h="item.h"
                  :i="item.i" drag-allow-from=".drag-handle" drag-ignore-from=".no-drag">
                  <NoteCard v-if="getNoteById(item.i)" :note="getNoteById(item.i)!" @view="openDetail" @edit="openEdit"
                    @delete="confirmDelete" @pin="handleTogglePin" @reminder="openReminder" @color="handleColorChange"
                    @archive="handleArchive" />
                </GridItem>
              </GridLayout>
            </section>

          </template>
        </template>
      </main>

      <!-- DIALOGS -->
      <NoteDialog :space-id="spaceId" :open="dialogOpen" :note="selectedNote" @close="dialogOpen = false" />

      <NoteDetailDialog :open="detailOpen" :note="selectedNote" :space-id="spaceId"
        :personal-space-id="userPersonalSpace.noteId" @close="detailOpen = false" @edit="openEdit"
        @delete="confirmDelete" />

      <ConfirmDialog :open="confirmOpen" @confirm="handleDelete" @cancel="confirmOpen = false" />

      <ReminderDialog :open="reminderOpen" :note="reminderNote" @close="reminderOpen = false"
        @confirm="handleReminderConfirm" />

      <ArchivedNotesDialog :open="archivedOpen" :space-id="spaceId" @close="archivedOpen = false" />

      <ConflictDialog :space-id="spaceId" />

  </div>
</template>