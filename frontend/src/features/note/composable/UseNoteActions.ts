import {
  useNoteStore,
  type ConflictInfo,
} from "@/features/note/stores/noteStore";
import type { Note, NoteRequest } from "@/features/note/types/NoteType";

import {
  getAll,
  create,
  update,
  deleteNote as deleteNoteApi,
  togglePin,
  updatePosition,
  setReminder,
  archiveNote as archiveNoteApi,
  copyToPersonal,
  getArchivedNotes,
  restoreNote as restoreNoteApi,
} from "@/features/note/services/noteService";

import { noteSocket } from "@/features/note/services/noteSocket";
import { socketService } from "@/services/socketService";

export function useNoteActions() {
  const store = useNoteStore();

  // ── FETCH danh sách note + kết nối socket ────────────────
  async function fetchNotes(spaceId: string) {
    if (store.currentSpaceId === spaceId && store.notes.length > 0) return;

    if (store.currentSpaceId && store.currentSpaceId !== spaceId) {
      noteSocket.unsubscribeAll(store.currentSpaceId);
    }

    store.setCurrentSpaceId(spaceId);
    store.setLoading(true);
    store.setError(null);
    store.setNotes([]);

    try {
      const res = await getAll(spaceId);
      store.setNotes(Array.isArray(res) ? res : (res?.data ?? []));
      await connectSocket(spaceId);
    } catch (e) {
      store.setError("Không thể tải ghi chú");
      console.error(e);
    } finally {
      store.setLoading(false);
    }
  }

  async function connectSocket(spaceId: string) {
    await socketService.connect();

    noteSocket.subscribeCreateNote(spaceId, (note: Note) =>
      store.addNote(note),
    );
    noteSocket.subscribeDeleteNote(spaceId, (id: string) =>
      store.removeNoteFromList(id),
    );
    noteSocket.subscribeUpdateNote(spaceId, (payload: Note) =>
      store.replaceNote(payload),
    );
    noteSocket.subscribetogglePin(spaceId, (payload: Note) => {
      store.replaceNote(payload);
      store.sortByPinned();
    });
  }

  function disconnectSocket(spaceId: string) {
    noteSocket.unsubscribeAll(spaceId);
  }

  // ── CREATE ────────────────────────────────────────────────
  async function createNote(
    spaceId: string,
    data: NoteRequest,
  ): Promise<Note | null> {
    try {
      return await create(spaceId, data);
    } catch (e) {
      store.setError("Không thể tạo ghi chú");
      console.error(e);
      return null;
    }
  }

  // ── UPDATE — có check xung đột version (409) ─────────────
  async function updateNote(
    spaceId: string,
    id: string,
    data: NoteRequest,
  ): Promise<boolean> {
    try {
      await update(spaceId, id, data);
      return true;
    } catch (e: any) {
      if (e?.response?.status === 409) {
        store.setConflict({
          type: "update",
          currentNote: e.response.data.currentNote,
          pendingData: data,
        } as ConflictInfo);
      } else {
        store.setError("Không thể cập nhật ghi chú");
        console.error(e);
      }
      return false;
    }
  }

  // ── DELETE — có check quyền (403) + xung đột version (409) ─
  async function deleteNote(
    spaceId: string,
    id: string,
    version?: number,
  ): Promise<boolean> {
    try {
      await deleteNoteApi(spaceId, id, version);
      return true;
    } catch (e: any) {
      const status = e?.response?.status;
      if (status === 409) {
        store.setConflict({
          type: "delete",
          currentNote: e.response.data.currentNote,
        } as ConflictInfo);
      } else if (status === 403) {
        store.setError("Chỉ Owner hoặc Admin mới được xóa ghi chú");
      } else {
        store.setError("Không thể xóa ghi chú");
        console.error(e);
      }
      return false;
    }
  }

  // ── PIN ───────────────────────────────────────────────────
  async function changePinStatus(spaceId: string, id: string) {
    try {
      await togglePin(spaceId, id);
    } catch (e) {
      store.setError("Không thể ghim ghi chú");
      console.error(e);
    }
  }

  // ── VỊ TRÍ (kéo thả) ──────────────────────────────────────
  async function updateNotePosition(
    spaceId: string,
    id: string,
    pos: { posX: number; posY: number; width: number; height: number },
  ) {
    try {
      await updatePosition(spaceId, id, pos);
    } catch (e) {
      store.setError("Không thể cập nhật vị trí");
      console.error(e);
    }
  }

  // ── NHẮC NHỞ ──────────────────────────────────────────────
  async function setNoteReminder(
    spaceId: string,
    id: string,
    reminderAt: string | null,
  ) {
    try {
      await setReminder(spaceId, id, reminderAt);
    } catch (e) {
      store.setError("Không thể đặt nhắc nhở");
      console.error(e);
    }
  }

  // ── LƯU TRỮ / KHÔI PHỤC ───────────────────────────────────
  async function archiveNote(spaceId: string, id: string) {
    try {
      await archiveNoteApi(spaceId, id);
      store.removeNoteFromList(id);
    } catch (e) {
      store.setError("Không thể lưu trữ ghi chú");
      console.error(e);
    }
  }

  async function fetchArchivedNotes(spaceId: string) {
    store.setLoadingArchived(true);
    try {
      const res = await getArchivedNotes(spaceId);
      store.setArchivedNotes(Array.isArray(res) ? res : (res?.data ?? []));
    } catch (e) {
      store.setError("Không thể tải ghi chú đã lưu trữ");
      console.error(e);
    } finally {
      store.setLoadingArchived(false);
    }
  }

  async function restoreNote(spaceId: string, id: string) {
    try {
      const restored = await restoreNoteApi(spaceId, id);
      store.removeArchivedNote(id);
    } catch (e) {
      store.setError("Không thể khôi phục ghi chú");
      console.error(e);
    }
  }

  // ── LƯU VỀ KHÔNG GIAN CÁ NHÂN ─────────────────────────────
  async function copyNoteToPersonal(
    spaceId: string,
    id: string,
  ): Promise<Note> {
    try {
      return await copyToPersonal(spaceId, id);
    } catch (e) {
      store.setError("Không thể lưu ghi chú vào không gian cá nhân");
      console.error(e);
      throw e;
    }
  }

  return {
    fetchNotes,
    disconnectSocket,
    createNote,
    updateNote,
    deleteNote,
    changePinStatus,
    updateNotePosition,
    setNoteReminder,
    archiveNote,
    fetchArchivedNotes,
    restoreNote,
    copyNoteToPersonal,
  };
}
