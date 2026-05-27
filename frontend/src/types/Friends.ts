export interface Friend {
  id: string;
  name: string;
  username: string;
  avatarUrl: string | null;
  isOnline: boolean;
  conversationId: string;
}

export interface FriendRequest {
  id: string;
  senderName: string;
  receiverName: string;
  senderUsername: string;
  receiverUsername: string;
  status: string;
}
