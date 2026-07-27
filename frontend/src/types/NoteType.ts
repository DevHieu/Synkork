export interface Note {
  id: string       
  spaceId: string 
  title: string
  note: string | null    
  color: string | null
  pinned: boolean
  allowEditAll: boolean
  createdAt: string
  updatedAt: string
  posX: number
  posY: number
  width: number
  height: number
  reminderAt: string | null     
  reminderSent: boolean | null  
}

export interface NoteRequest {
  title: string
  note?: string       
  color?: string
  pinned?: boolean
  allowEditAll?: boolean
  posX?: number
  posY?: number
  width?: number
  height?: number
  reminderAt?: string | null  
}
