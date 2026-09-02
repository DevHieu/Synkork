import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { toast } from 'vue-sonner'
import type { Note, NoteRequest } from '@/features/note/types/NoteType'

export interface ConflictInfo {
  type: 'update' | 'delete'
  currentNote: Note
  pendingData?: NoteRequest
}

export const useNoteStore = defineStore('notes', () => {
  // ── STATE ──────────────────────────────────────────────
  const notes = ref<Note[]>([])
  const archivedNotes = ref<Note[]>([])
  const loading = ref(false)
  const loadingArchived = ref(false)
  const error = ref<string | null>(null)
  const searchQuery = ref('')
  const currentSpaceId = ref<string | null>(null)
  const conflict = ref<ConflictInfo | null>(null)

  // ── GETTERS ────────────────────────────────────────────
  const filteredNotes = computed(() => {
    if (!notes.value?.length) return []
    if (!searchQuery.value.trim()) return notes.value
    const q = searchQuery.value.toLowerCase()
    return notes.value.filter(
      n => n.title?.toLowerCase().includes(q) || n.note?.toLowerCase().includes(q)
    )
  })

  const pinnedNotes = computed(() => filteredNotes.value?.filter(n => n.pinned) ?? [])
  const unpinnedNotes = computed(() => filteredNotes.value?.filter(n => !n.pinned) ?? [])

  // ── MUTATIONS — chỉ set/sửa state, KHÔNG gọi API ────────
  // Toàn bộ logic gọi API nằm ở composable useNoteActions().
  function setNotes(list: Note[]) { notes.value = list }
  function addNote(note: Note) { notes.value.unshift(note) }
  function removeNoteFromList(id: string) { notes.value = notes.value.filter(n => n.id !== id) }
  function replaceNote(note: Note) {
    const idx = notes.value.findIndex(n => n.id === note.id)
    if (idx !== -1) notes.value[idx] = note
  }
  function sortByPinned() {
    notes.value.sort((a, b) => Number(b.pinned) - Number(a.pinned))
  }
  function setArchivedNotes(list: Note[]) { archivedNotes.value = list }
  function removeArchivedNote(id: string) {
    archivedNotes.value = archivedNotes.value.filter(n => n.id !== id)
  }
  function setCurrentSpaceId(id: string | null) { currentSpaceId.value = id }
  function setLoading(value: boolean) { loading.value = value }
  function setLoadingArchived(value: boolean) { loadingArchived.value = value }
  function setError(msg: string | null) {
    error.value = msg
    if (msg) {
      toast.error(msg)
    }
  }
  function setConflict(c: ConflictInfo | null) { conflict.value = c }
  function clearConflict() { conflict.value = null }

  return {
    // state
    notes, archivedNotes, loading, loadingArchived, error, searchQuery, currentSpaceId, conflict,
    // getters
    filteredNotes, pinnedNotes, unpinnedNotes,
    // mutations
    setNotes, addNote, removeNoteFromList, replaceNote, sortByPinned,
    setArchivedNotes, removeArchivedNote, setCurrentSpaceId,
    setLoading, setLoadingArchived, setError, setConflict, clearConflict,
  }
})