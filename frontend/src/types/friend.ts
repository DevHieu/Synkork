export interface Friend {
  id: string
  name: string
  avatarUrl: string
  status: string
}

export interface FriendRequest {
  id: string;           // ← phải là string (UUID)
  senderName: string;
  receiverName: string;
  status: string;
}