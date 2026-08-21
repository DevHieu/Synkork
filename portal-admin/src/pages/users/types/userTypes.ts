export type UserStatus = 'ACTIVE' | 'BANNED' | 'NOT_VERIFIED'
export type UserPlan = 'FREE' | 'TEAM' | 'BUSINESS'
export type UserRole = 'USER' | 'MANAGER' | 'ADMIN'

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
  updatedAt?: string | null
  warning?: number
}

export interface UserJoinedRoom {
  id: string
  name: string
  avatarUrl?: string | null
  description?: string | null
  type: 'GROUP' | 'DM' | 'PERSONAL'
  status: 'OPEN' | 'LOCKED' | 'PENDING_REMOVAL'
  memberCount: number
  inviteCode?: string | null
  ownerId?: string | null
  ownerUsername?: string | null
  warning: number
  memberRole: string
  memberStatus: 'ACTIVE' | 'INACTIVE' | 'KICKED'
  joinedAt?: string | null
  createdAt?: string | null
}

export interface UserParams {
  search?: string
  status?: UserStatus
  plan?: UserPlan
  page?: number
  size?: number
  dateFrom?: string
  dateTo?: string
  minWarning?: number
  maxWarning?: number
}
