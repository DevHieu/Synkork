export type ManagerStatus = 'ACTIVE' | 'BANNED' | 'NOT_VERIFIED'
export type ManagementRole = 'USER' | 'MANAGER' | 'ADMIN'
export type ManagerPlan = 'FREE' | 'TEAM' | 'BUSINESS'

export interface ManagerAccount {
  id: string
  username: string
  displayName: string
  email: string
  avatarUrl: string | null
  role: ManagementRole
  status: ManagerStatus
  plan?: ManagerPlan | null
  provider: string
  createdAt: string
  updatedAt: string
}

export interface ManagerParams {
  search?: string
  status?: ManagerStatus
  role?: ManagementRole
  page?: number
  size?: number
  dateFrom?: string
  dateTo?: string
}

export interface CreateManagerPayload {
  displayName: string
  username: string
  email: string
  status: ManagerStatus
  role: Exclude<ManagementRole, 'user'>
}

export type UpdateManagerPayload = Partial<Omit<CreateManagerPayload, 'username' | 'role'>>
  & {
    role?: ManagementRole
    plan?: ManagerPlan
  }
