import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAll, getById, create, update, deleteNote, togglePin, search } from '@/services/noteService'
import type { Note, NoteRequest } from '@/types/NoteType'

export const useNoteStore = defineStore('notes', () => {
  const notes = ref<Note[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const searchQuery = ref('')

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

  async function fetchNotes(spaceId: string) {
    loading.value = true
    error.value = null
    try {
      const res = await getAll(spaceId)
      console.log('res:', res)
      console.log('Array.isArray:', Array.isArray(res))
      notes.value = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : [])
    } catch (e) {
      error.value = 'Không thể tải ghi chú'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  async function createNote(spaceId: string, data: NoteRequest): Promise<Note | null> {
    try {
      const res = await create(spaceId, data)
      notes.value.unshift(res)
      return res
    } catch (e) {
      error.value = 'Không thể tạo ghi chú'
      console.error(e)
      return null
    }
  }

  async function updateNote(spaceId: string,id: string, data: NoteRequest): Promise<Note | null> {
    try {
      const res = await update(spaceId,id, data)
      const idx = notes.value.findIndex(n => n.id === id)
      if (idx !== -1) notes.value[idx] = res
      return res.data
    } catch (e) {
      error.value = 'Không thể cập nhật ghi chú'
      console.error(e)
      return null
    }
  }

  async function deletedNote(spaceId: string,id: string): Promise<boolean> {
    try {
      await deleteNote(spaceId,id)
      notes.value = notes.value.filter(n => n.id !== id)
      return true
    } catch (e) {
      error.value = 'Không thể xóa ghi chú'
      console.error(e)
      return false
    }
  }

  async function changePinStatus(spaceId: string,id: string): Promise<void> {

      try {
        const updatedNote = await togglePin(spaceId, id)
        const idx = notes.value.findIndex(n => n.id === id)
        if (idx !== -1) notes.value[idx] = updatedNote
        notes.value.sort((a, b) => Number(b.pinned) - Number(a.pinned))
      } catch (e) {
        error.value = 'Không thể ghim ghi chú'
        console.error(e)
      }

  }

  return {
    notes, loading, error, searchQuery,
    filteredNotes, pinnedNotes, unpinnedNotes,
    fetchNotes, createNote, updateNote, deleteNote: deletedNote, changePinStatus,
  }
})