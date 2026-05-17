
export interface NotificationDTO {
  id: string
  type: 'CARD' | 'FRIEND_REQUEST' | 'CALENDAR_EVENT' | 'NOTE'
  refType: 'CARD_ASSIGNED' | 'CARD_DUE_SOON' | 'FRIEND_REQUEST' | 'EVENT_REMINDER' | 'CARD_OVER_DUE'
  refId: string
  actorName: string
  actorAvatar: string | null
  read: boolean
  createdAt: string
  spaceId: string | null
  roomId: string | null
}