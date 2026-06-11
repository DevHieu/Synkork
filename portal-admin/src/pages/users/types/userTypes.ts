export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED'
export type UserPlan = 'FREE' | 'TEAM' | 'BUSINESS'

export interface User {
  id: string
  displayName: string
  username: string
  email: string
  status: UserStatus
  plan: UserPlan
  avatarUrl: string | null
  provider: string
  createdAt: string
}

export interface UserParams {
  search?: string
  status?: UserStatus
  plan?: UserPlan
  page?: number
  size?: number
  dateFrom?: string
  dateTo?: string
}
