export type ManagerStatus = 'active' | 'inactive' | 'banned'

export interface ManagerAccount {
  id: string
  username: string
  displayName: string
  email: string
  avatarUrl: string | null
  role: 'manager'
  status: ManagerStatus
  provider: string
  createdAt: string
  updatedAt: string
}

export interface ManagerParams {
  keyword?: string
  status?: ManagerStatus
  page?: number
  size?: number
}

export interface CreateManagerPayload {
  displayName: string
  username: string
  email: string
  status: ManagerStatus
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
