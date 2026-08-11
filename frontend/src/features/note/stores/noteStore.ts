import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Note, NoteRequest } from '@/features/note/types/NoteType'

import {
  getAll, create, update, deleteNote as deleteNoteApi, togglePin,
  updatePosition, setReminder, archiveNote as archiveNoteApi, copyToPersonal,
  getArchivedNotes, restoreNote as restoreNoteApi
} from '@/features/note/services/noteService'

import { noteSocket } from '@/features/note/services/noteSocket'
import { socketService } from '@/services/socketService'

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

  // ── MUTATIONS — chỉ set/sửa state, không gọi API ────────
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
  function setError(msg: string | null) { error.value = msg }
  function setConflict(c: ConflictInfo | null) { conflict.value = c }
  function clearConflict() { conflict.value = null }

  // ══════════════ ACTIONS — gọi API thật, có xử lý loading/error ══════════════

  // FETCH danh sách note + kết nối socket
  async function fetchNotes(spaceId: string) {
    if (currentSpaceId.value === spaceId && notes.value.length > 0) return

    if (currentSpaceId.value && currentSpaceId.value !== spaceId) {
      noteSocket.unsubscribeAll(currentSpaceId.value)
    }

    setCurrentSpaceId(spaceId)
    loading.value = true
    setError(null)
    setNotes([])

    try {
      const res = await getAll(spaceId)
      setNotes(Array.isArray(res) ? res : (res?.data ?? []))
      await connectSocket(spaceId)
    } catch (e) {
      setError('Không thể tải ghi chú')
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  async function connectSocket(spaceId: string) {
    await socketService.connect()

    noteSocket.subscribeCreateNote(spaceId, (note: Note) => addNote(note))
    noteSocket.subscribeDeleteNote(spaceId, (id: string) => removeNoteFromList(id))
    noteSocket.subscribeUpdateNote(spaceId, (payload: Note) => replaceNote(payload))
    noteSocket.subscribetogglePin(spaceId, (payload: Note) => {
      replaceNote(payload)
      sortByPinned()
    })
  }

  function disconnectSocket(spaceId: string) {
    noteSocket.unsubscribeAll(spaceId)
  }

  // CREATE
  async function createNote(spaceId: string, data: NoteRequest): Promise<Note | null> {
    try {
      return await create(spaceId, data)
    } catch (e) {
      setError('Không thể tạo ghi chú')
      console.error(e)
      return null
    }
  }

  // UPDATE — có check xung đột version (409)
  async function updateNote(spaceId: string, id: string, data: NoteRequest): Promise<boolean> {
    try {
      await update(spaceId, id, data)
      return true
    } catch (e: any) {
      if (e?.response?.status === 409) {
        setConflict({
          type: 'update',
          currentNote: e.response.data.currentNote,
          pendingData: data
        })
      } else {
        setError('Không thể cập nhật ghi chú')
        console.error(e)
      }
      return false
    }
  }

  // DELETE — có check quyền (403) + xung đột version (409)
  async function removeNote(spaceId: string, id: string, version?: number): Promise<boolean> {
    try {
      await deleteNoteApi(spaceId, id, version)
      return true
    } catch (e: any) {
      const status = e?.response?.status
      if (status === 409) {
        setConflict({ type: 'delete', currentNote: e.response.data.currentNote })
      } else if (status === 403) {
        setError('Chỉ Owner hoặc Admin mới được xóa ghi chú')
      } else {
        setError('Không thể xóa ghi chú')
        console.error(e)
      }
      return false
    }
  }

  // PIN
  async function changePinStatus(spaceId: string, id: string) {
    try {
      await togglePin(spaceId, id)
    } catch (e) {
      setError('Không thể ghim ghi chú')
      console.error(e)
    }
  }

  // VỊ TRÍ (kéo thả)
  async function updateNotePosition(
    spaceId: string, id: string,
    pos: { posX: number; posY: number; width: number; height: number }
  ) {
    try {
      await updatePosition(spaceId, id, pos)
    } catch (e) {
      setError('Không thể cập nhật vị trí')
      console.error(e)
    }
  }

  // NHẮC NHỞ
  async function setNoteReminder(spaceId: string, id: string, reminderAt: string | null) {
    try {
      await setReminder(spaceId, id, reminderAt)
    } catch (e) {
      setError('Không thể đặt nhắc nhở')
      console.error(e)
    }
  }

  // LƯU TRỮ / KHÔI PHỤC
  async function archiveNote(spaceId: string, id: string) {
    try {
      await archiveNoteApi(spaceId, id)
      removeNoteFromList(id)
    } catch (e) {
      setError('Không thể lưu trữ ghi chú')
      console.error(e)
    }
  }

  async function fetchArchivedNotes(spaceId: string) {
    loadingArchived.value = true
    try {
      const res = await getArchivedNotes(spaceId)
      setArchivedNotes(Array.isArray(res) ? res : (res?.data ?? []))
    } catch (e) {
      setError('Không thể tải ghi chú đã lưu trữ')
      console.error(e)
    } finally {
      loadingArchived.value = false
    }
  }

  async function restoreNote(spaceId: string, id: string) {
    try {
      const restored = await restoreNoteApi(spaceId, id)
      removeArchivedNote(id)
      if (currentSpaceId.value === spaceId) addNote(restored)
    } catch (e) {
      setError('Không thể khôi phục ghi chú')
      console.error(e)
    }
  }

  // LƯU VỀ KHÔNG GIAN CÁ NHÂN
  async function copyNoteToPersonal(spaceId: string, id: string): Promise<Note> {
    try {
      return await copyToPersonal(spaceId, id)
    } catch (e) {
      setError('Không thể lưu ghi chú vào không gian cá nhân')
      console.error(e)
      throw e
    }
  }

  return {
    // state
    notes, archivedNotes, loading, loadingArchived, error, searchQuery, currentSpaceId, conflict,
  
    // getters
    filteredNotes, pinnedNotes, unpinnedNotes,
  
    // mutations (đang bị thiếu — thêm các dòng này)
    setNotes,
    addNote,
    removeNoteFromList,
    replaceNote,
    sortByPinned,
    setArchivedNotes,
    removeArchivedNote,
    setCurrentSpaceId,
    setError,
    setConflict,
    removeNote,
    clearConflict,
  
    // actions
    fetchNotes, disconnectSocket,
    createNote, updateNote,
    deleteNote: removeNote,
    changePinStatus, updateNotePosition, setNoteReminder,
    archiveNote, fetchArchivedNotes, restoreNote,
    copyNoteToPersonal,
  }
})