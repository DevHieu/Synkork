import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

import {
  getAll,
  create,
  update,
  deleteNote,
  togglePin,
  updatePosition,
  setReminder,
  archiveNote
} from '@/services/noteService'

import type { Note, NoteRequest } from '@/types/NoteType'

import { noteSocket } from '@/services/websocket/noteSocket'
import { socketService } from '@/services/websocket/socketService'

export const useNoteStore = defineStore('notes', () => {

  const notes = ref<Note[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const searchQuery = ref('')
  const currentSpaceId = ref<string | null>(null)

  // FILTER
  const filteredNotes = computed(() => {
    if (!notes.value?.length) return []
    if (!searchQuery.value.trim()) return notes.value
    const q = searchQuery.value.toLowerCase()
    return notes.value.filter(
      n =>
        n.title?.toLowerCase().includes(q) ||
        n.note?.toLowerCase().includes(q)
    )
  })

  const pinnedNotes = computed(() =>
    filteredNotes.value?.filter(n => n.pinned) ?? []
  )

  const unpinnedNotes = computed(() =>
    filteredNotes.value?.filter(n => !n.pinned) ?? []
  )

  // HELPERS
  function addNoteToList(note: any) {
    notes.value.unshift(note)
  }

  // ARCHIVE
async function archiveNoteStore(
  spaceId: string,
  id: string
): Promise<void> {
  try {
    await archiveNote(spaceId, id)

    // remove khỏi UI luôn
    notes.value = notes.value.filter(
      n => n.id !== id
    )
  } catch (e) {
    error.value = 'Không thể lưu trữ ghi chú'
    console.error(e)
  }
}
  // FETCH
  async function fetchNotes(spaceId: string) {
    // ── Guard: không fetch lại nếu đang xem cùng space và đã có notes
    if (currentSpaceId.value === spaceId && notes.value.length > 0) return

    if (currentSpaceId.value && currentSpaceId.value !== spaceId) {
      noteSocket.unsubscribeAll(currentSpaceId.value)
    }

    currentSpaceId.value = spaceId
    loading.value = true
    error.value = null
    notes.value = []

    try {
      const res = await getAll(spaceId)
      notes.value = Array.isArray(res)
        ? res
        : (Array.isArray(res?.data) ? res.data : [])

      await connectSocket(spaceId)
    } catch (e) {
      error.value = 'Không thể tải ghi chú'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  // SOCKET
  async function connectSocket(spaceId: string) {
    await socketService.connect()

    noteSocket.subscribeCreateNote(spaceId, (payload) => {
      addNoteToList(payload)
    })

    noteSocket.subscribeDeleteNote(spaceId, (payload) => {
      notes.value = notes.value.filter(n => n.id !== payload)
    })

    noteSocket.subscribeUpdateNote(spaceId, (payload) => {
      const idx = notes.value.findIndex(n => n.id === payload.id)
      if (idx !== -1) {
        notes.value[idx] = payload
      }
    })

    noteSocket.subscribetogglePin(spaceId, (payload) => {
      const idx = notes.value.findIndex(n => n.id === payload.id)
      if (idx !== -1) {
        notes.value[idx] = payload
      }
      notes.value.sort(
        (a, b) => Number(b.pinned) - Number(a.pinned)
      )
    })
  }

  // DISCONNECT
  function disconnectSocket(spaceId: string) {
    noteSocket.unsubscribeAll(spaceId)
  }

  // CREATE
  async function createNote(spaceId: string, data: NoteRequest): Promise<Note | null> {
    try {
      const res = await create(spaceId, data)
      return res
    } catch (e) {
      error.value = 'Không thể tạo ghi chú'
      console.error(e)
      return null
    }
  }

  // UPDATE
  async function updateNote(spaceId: string, id: string, data: NoteRequest) {
    try {
      await update(spaceId, id, data)
    } catch (e) {
      error.value = 'Không thể cập nhật ghi chú'
      console.error(e)
      return null
    }
  }

  // DELETE
  async function deletedNote(spaceId: string, id: string): Promise<boolean> {
    try {
      await deleteNote(spaceId, id)
      return true
    } catch (e) {
      error.value = 'Không thể xóa ghi chú'
      console.error(e)
      return false
    }
  }

  // PIN
  async function changePinStatus(spaceId: string, id: string): Promise<void> {
    try {
      await togglePin(spaceId, id)
    } catch (e) {
      error.value = 'Không thể ghim ghi chú'
      console.error(e)
    }
  }

  // POSITION
  async function updateNotePosition(
    spaceId: string,
    id: string,
    pos: { posX: number; posY: number; width: number; height: number }
  ): Promise<void> {
    try {
      await updatePosition(spaceId, id, pos)
    } catch (e) {
      error.value = 'Không thể cập nhật vị trí'
      console.error(e)
    }
  }

  // REMINDER
  async function setNoteReminder(
    spaceId: string,
    id: string,
    reminderAt: string | null
  ): Promise<void> {
    try {
      await setReminder(spaceId, id, reminderAt)
    } catch (e) {
      error.value = 'Không thể đặt nhắc nhở'
      console.error(e)
    }
  }

  return {
    notes,
    loading,
    error,
    searchQuery,
  
    filteredNotes,
    pinnedNotes,
    unpinnedNotes,
  
    fetchNotes,
    createNote,
    updateNote,
  
    deleteNote: deletedNote,
  
    changePinStatus,
    disconnectSocket,
  
    updateNotePosition,
    setNoteReminder,
  
    archiveNote: archiveNoteStore
  }
})