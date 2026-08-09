import { useNoteStore } from '@/features/note/stores/noteStore'
import { useNoteSocket } from '@/features/note/composable/UseNoteSocket'
import {
  getAll, create, update, deleteNote, togglePin, updatePosition,
  setReminder, archiveNote, copyToPersonal, getArchivedNotes, restoreNote
} from '@/features/note/services/noteService'
import type { Note, NoteRequest } from '@/features/note/types/NoteType'

export function useNoteActions() {
  const store = useNoteStore()
  const { connect, disconnect } = useNoteSocket()

  async function fetchNotes(spaceId: string) {
    if (store.currentSpaceId === spaceId && store.notes.length > 0) return
    if (store.currentSpaceId && store.currentSpaceId !== spaceId) {
      disconnect(store.currentSpaceId)
    }

    store.setCurrentSpaceId(spaceId)
    store.loading = true
    store.setError(null)
    store.setNotes([])

    try {
      const res = await getAll(spaceId)
      store.setNotes(Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : []))
      await connect(spaceId)
    } catch (e) {
      store.setError('Không thể tải ghi chú')
      console.error(e)
    } finally {
      store.loading = false
    }
  }

  async function fetchArchivedNotes(spaceId: string) {
    store.loadingArchived = true
    try {
      const res = await getArchivedNotes(spaceId)
      store.setArchivedNotes(Array.isArray(res) ? res : (Array.isArray(res?.data) ? res.data : []))
    } catch (e) {
      store.setError('Không thể tải ghi chú đã lưu trữ')
      console.error(e)
    } finally {
      store.loadingArchived = false
    }
  }

  async function createNote(spaceId: string, data: NoteRequest): Promise<Note | null> {
    try {
      return await create(spaceId, data)
    } catch (e) {
      store.setError('Không thể tạo ghi chú')
      console.error(e)
      return null
    }
  }

  async function updateNote(spaceId: string, id: string, data: NoteRequest): Promise<boolean> {
    try {
      await update(spaceId, id, data)
      return true
    } catch (e: any) {
      if (e?.response?.status === 409) {
        store.setConflict({ type: 'update', currentNote: e.response.data.currentNote, pendingData: data })
      } else {
        store.setError('Không thể cập nhật ghi chú')
        console.error(e)
      }
      return false
    }
  }

  async function deletedNote(spaceId: string, id: string, version?: number): Promise<boolean> {
    try {
      await deleteNote(spaceId, id, version)
      return true
    } catch (e: any) {
      if (e?.response?.status === 409) {
        store.setConflict({ type: 'delete', currentNote: e.response.data.currentNote })
      } else if (e?.response?.status === 403) {
        store.setError('Chỉ Owner hoặc Admin mới được xóa ghi chú')
      } else {
        store.setError('Không thể xóa ghi chú')
        console.error(e)
      }
      return false
    }
  }

  async function changePinStatus(spaceId: string, id: string) {
    try {
      await togglePin(spaceId, id)
    } catch (e) {
      store.setError('Không thể ghim ghi chú')
      console.error(e)
    }
  }

  async function updateNotePosition(
    spaceId: string, id: string,
    pos: { posX: number; posY: number; width: number; height: number }
  ) {
    try {
      await updatePosition(spaceId, id, pos)
    } catch (e) {
      store.setError('Không thể cập nhật vị trí')
      console.error(e)
    }
  }

  async function setNoteReminder(spaceId: string, id: string, reminderAt: string | null) {
    try {
      await setReminder(spaceId, id, reminderAt)
    } catch (e) {
      store.setError('Không thể đặt nhắc nhở')
      console.error(e)
    }
  }

  async function archiveNoteAction(spaceId: string, id: string) {
    try {
      await archiveNote(spaceId, id)
      store.removeNote(id)
    } catch (e) {
      store.setError('Không thể lưu trữ ghi chú')
      console.error(e)
    }
  }

  async function restoreNoteAction(spaceId: string, id: string) {
    try {
      const restored = await restoreNote(spaceId, id)
      store.removeArchivedNote(id)
      if (store.currentSpaceId === spaceId) store.addNote(restored)
    } catch (e) {
      store.setError('Không thể khôi phục ghi chú')
      console.error(e)
    }
  }

  async function copyNoteToPersonal(spaceId: string, id: string): Promise<Note> {
    try {
      return await copyToPersonal(spaceId, id)
    } catch (e) {
      store.setError('Không thể lưu ghi chú vào không gian cá nhân')
      console.error(e)
      throw e
    }
  }

  return {
    fetchNotes, fetchArchivedNotes, createNote, updateNote,
    deleteNote: deletedNote, changePinStatus, updateNotePosition, setNoteReminder,
    archiveNote: archiveNoteAction, restoreNote: restoreNoteAction, copyNoteToPersonal,
    disconnectSocket: disconnect
  }
}