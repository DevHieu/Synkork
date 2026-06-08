export interface Room {
  id: string
  name: string
  description?: string
  type: 'GROUP' | 'DM'
  status: 'OPEN' | 'CLOSED'
  inviteCode?: string
  memberCount: number
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

  members: {
    id: string
    username: string
    email: string
    avatarUrl?: string
    role: string
  }[]

  spaces: {
    id: string
    name: string
    type: string
  }[]
}

export interface RoomParams {
  page?: number
  size?: number
  search?: string
  status?: string
  type?: string
}
