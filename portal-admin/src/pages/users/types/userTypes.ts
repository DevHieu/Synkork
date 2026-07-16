export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED'
export type UserPlan = 'FREE' | 'TEAM' | 'BUSINESS'
export type UserRole = 'user' | 'manager' | 'admin'

export interface User {
  id: string
  displayName: string
  username: string
  email: string
  role: UserRole
  status: UserStatus
  plan: UserPlan
  avatarUrl: string | null
  provider: string
  createdAt: string
  updatedAt?: string
  warning?: number
  rooms?: UserRoom[]
}

export interface UserRoom {
  membershipId: string
  roomId: string
  name: string | null
  type: 'GROUP' | 'DM' | 'PERSONAL'
  roomStatus: string
  role: string
  memberStatus: 'ACTIVE' | 'KICKED'
  joinedAt: string
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


