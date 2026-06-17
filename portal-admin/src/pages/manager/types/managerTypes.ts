export type ManagerStatus = 'active' | 'inactive' | 'banned'
export type ManagementRole = 'manager' | 'admin'

export interface ManagerAccount {
  id: string
  username: string
  displayName: string
  email: string
  avatarUrl: string | null
  role: ManagementRole
  status: ManagerStatus
  provider: string
  createdAt: string
  updatedAt: string
}

export interface ManagerParams {
  keyword?: string
  status?: ManagerStatus
  role?: ManagementRole
  page?: number
  size?: number
}

export interface CreateManagerPayload {
  displayName: string
  username: string
  email: string
  status: ManagerStatus
  role: ManagementRole
}

export type UpdateManagerPayload = Partial<Omit<CreateManagerPayload, 'username'>>

export interface ManagerPage {
  content: ManagerAccount[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}
