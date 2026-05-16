import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

import {
  getAll,
  create,
  update,
  deleteNote,
  togglePin,
  updatePosition,
  setReminder
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

  const reminderQueue = ref<any[]>([])

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

  function removeReminder(id: string) {
    reminderQueue.value =
      reminderQueue.value.filter(r => r.id !== id)
  }

  // BROWSER NOTIFICATION
  function _fireNotification(note: Note) {
    const n = new Notification(`🔔 ${note.title}`, {
      body: note.note
        ? note.note.slice(0, 100)
        : 'Bạn có một nhắc nhở mới',
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      tag: `reminder-${note.id}`,
      requireInteraction: true,
    })

    n.onclick = () => {
      window.focus()
      n.close()
    }
  }

  function showBrowserNotification(note: Note) {
    if (Notification.permission === 'default') {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          _fireNotification(note)
        }
      })
      return
    }

    if (Notification.permission === 'granted') {
      _fireNotification(note)
    }
  }

  // FETCH
  async function fetchNotes(spaceId: string) {
    if (Notification.permission === 'default') {
      await Notification.requestPermission()
    }

    if (currentSpaceId.value) {
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

    // CREATE
    noteSocket.subscribeCreateNote(spaceId, (payload) => {
      addNoteToList(payload)
    })

    // DELETE
    noteSocket.subscribeDeleteNote(spaceId, (payload) => {
      notes.value = notes.value.filter(n => n.id !== payload)
    })

    // UPDATE
    noteSocket.subscribeUpdateNote(spaceId, (payload) => {
      const idx = notes.value.findIndex(n => n.id === payload.id)
      if (idx !== -1) {
        notes.value[idx] = payload
      }
    })

    // PIN
    noteSocket.subscribetogglePin(spaceId, (payload) => {
      const idx = notes.value.findIndex(n => n.id === payload.id)
      if (idx !== -1) {
        notes.value[idx] = payload
      }
      notes.value.sort(
        (a, b) => Number(b.pinned) - Number(a.pinned)
      )
    })

    // REMINDER
    noteSocket.subscribeReminder(spaceId, (payload) => {
      console.log('🔔 Reminder received:', payload) 
      // 1. Browser notification
      showBrowserNotification(payload)

      // 2. Sound
      const audio = new Audio('/notification.mp3')
      audio.volume = 0.5
      audio.play().catch(() => {})

      // 3. Toast popup
      reminderQueue.value.unshift({
        ...payload,
        visible: true
      })

      // 4. Auto remove toast sau 15s
      setTimeout(() => {
        removeReminder(payload.id)
      }, 15000)

      // 5. Update note trong store (reminderSent = true từ server)
      const idx = notes.value.findIndex(n => n.id === payload.id)
      if (idx !== -1) {
        notes.value[idx] = payload
      }
    })
  }

  // DISCONNECT
  function disconnectSocket(spaceId: string) {
    noteSocket.unsubscribeAll(spaceId)
  }

  // CREATE
  async function createNote(
    spaceId: string,
    data: NoteRequest
  ): Promise<Note | null> {
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
  async function updateNote(
    spaceId: string,
    id: string,
    data: NoteRequest
  ) {
    try {
      await update(spaceId, id, data)
    } catch (e) {
      error.value = 'Không thể cập nhật ghi chú'
      console.error(e)
      return null
    }
  }

  // DELETE
  async function deletedNote(
    spaceId: string,
    id: string
  ): Promise<boolean> {
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
  async function changePinStatus(
    spaceId: string,
    id: string
  ): Promise<void> {
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
    pos: {
      posX: number
      posY: number
      width: number
      height: number
    }
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

    reminderQueue,

    fetchNotes,
    createNote,
    updateNote,

    deleteNote: deletedNote,

    changePinStatus,
    disconnectSocket,

    updateNotePosition,
    setNoteReminder,

    removeReminder,
    showBrowserNotification,
  }
})