export interface Note {
  id: string        // ← đổi từ number sang string vì backend dùng UUID
  title: string
  note: string | null     // ← đổi từ content sang note
  color: string | null
  pinned: boolean
  allowEditAll: boolean
  createdAt: string
  updatedAt: string
}

export interface NoteRequest {
  title: string
  note?: string        // ← đổi từ content sang note
  color?: string
  pinned?: boolean
  allowEditAll?: boolean
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}