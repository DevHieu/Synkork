
export interface NotificationDTO {
  id: string
  type: 'TASK' | 'FRIEND' | 'CALENDAR' | 'NOTE' | 'CHAT'
  refType:   'CARD_ASSIGNED' 
           | 'CARD_OVER_DUE'
           | 'CARD_DUE_SOON' 
           | 'FRIEND_REQUEST' 
           | 'FRIEND_REJECT' 
           | 'FRIEND_ACCEPT'
           | 'EVENT_REMINDER' 
           | 'EVENT_ASSIGNED'
           | 'NOTE_REMINDER'
           
  refId: string
  actorName: string
  actorAvatar: string | null
  read: boolean
  createdAt: string | number
  spaceId: string | null
  roomId: string | null
}