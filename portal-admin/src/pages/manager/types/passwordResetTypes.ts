export type PasswordResetStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'NOT_VERIFIED'

export interface PasswordResetRequest {
  id: string
  username: string
  displayName: string
  email: string
  role: string
  status: PasswordResetStatus
  createdAt: string
  updatedAt: string
}

export interface PasswordResetParams {
  search?: string
  status?: PasswordResetStatus
  page?: number
  size?: number
  dateFrom?: string
  dateTo?: string
}
