export interface Friend {
  id: string
  name: string
  avatarUrl: string | null
  status: string
}

export interface FriendRequest {
  id: string
  senderName: string
  receiverName: string
  status: string
}