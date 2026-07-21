export interface Room {
  id: string
  name: string
  description?: string
  avatarUrl?: string
  type: 'GROUP' | 'DM' | 'PERSONAL'
  status: 'OPEN' | 'LOCKED' | 'PENDING_REMOVAL'
  inviteCode?: string
  memberCount: number
  warning: number
  ownerId?: string
  ownerUsername?: string
}

export interface Member {
  id: string
  username: string
  email: string
  avatarUrl?: string
  role: string
}

export interface Space {
  id: string
  name: string
  type: string
}

export interface RoomDetail extends Room {
  createdAt: string
  updatedAt: string

  owner?: {
    id: string
    username: string
    email: string
    avatarUrl?: string
  }
  spaceCount: number
}

export interface RoomParams {
  page?: number
  size?: number
  search?: string
  status?: string
  type?: string
  minMembers?: number
  maxMembers?: number
  minWarning?: number
  maxWarning?: number
}
export interface RoomFormPayload {
  name: string
  description?: string
  avatarUrl?: string
  status: 'OPEN' | 'LOCKED'
  ownerId?: string
}

export interface UserOption {
  id: string
  username: string
  email: string
  avatarUrl?: string
}
