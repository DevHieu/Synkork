export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED'
export type UserPlan = 'FREE' | 'TEAM' | 'BUSINESS'

export interface User {
  id: string
  displayName: string
  username: string
  email: string
  status: UserStatus
  plan: UserPlan
  role: string
  avatarUrl: string | null
  provider: string
  createdAt: string
}

export interface UserParams {
  keyword?: string
  status?: UserStatus
  plan?: UserPlan
  page?: number
  size?: number
  fromDate?: string
  toDate?: string
}


