import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAll, create, update, deleteNote, togglePin, updatePosition } from '@/services/noteService'
import type { Note, NoteRequest } from '@/types/NoteType'
import { noteSocket } from '@/services/websocket/noteSocket'
import { socketService } from '@/services/websocket/socketService'

export const useNoteStore = defineStore('notes', () => {
  const notes = ref<Note[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const searchQuery = ref('')
  const currentSpaceId = ref<string | null>(null)

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

  function addNoteToList(note: any) {
    notes.value.unshift(note)
  }

  async function fetchNotes(spaceId: string) {
    if (currentSpaceId.value) {
      noteSocket.unsubscribeAll(currentSpaceId.value)
    }
    currentSpaceId.value = spaceId
    loading.value = true
    error.value = null
    notes.value = []
    try {
      const res = await getAll(spaceId)
      notes.value = Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : [])
      await connectSocket(spaceId)
    } catch (e) {
      error.value = 'Không thể tải ghi chú'
      console.error(e)
    } finally {
      loading.value = false
    }
  }

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
      if (idx !== -1) notes.value[idx] = payload
    })
    noteSocket.subscribetogglePin(spaceId, (payload) => {
      const idx = notes.value.findIndex(n => n.id === payload.id)
      if (idx !== -1) notes.value[idx] = payload
      notes.value.sort((a, b) => Number(b.pinned) - Number(a.pinned))
    })
  }

  function disconnectSocket(spaceId: string) {
    noteSocket.unsubscribeAll(spaceId)
  }

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

  async function updateNote(spaceId: string, id: string, data: NoteRequest) {
    try {
      await update(spaceId, id, data)
    } catch (e) {
      error.value = 'Không thể cập nhật ghi chú'
      console.error(e)
      return null
    }
  }

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

  async function changePinStatus(spaceId: string, id: string): Promise<void> {
    try {
      await togglePin(spaceId, id)
    } catch (e) {
      error.value = 'Không thể ghim ghi chú'
      console.error(e)
    }
  }

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

  return {
    notes, loading, error, searchQuery,
    filteredNotes, pinnedNotes, unpinnedNotes,
    fetchNotes, createNote, updateNote,
    deleteNote: deletedNote, changePinStatus,
    disconnectSocket, updateNotePosition
  }
})