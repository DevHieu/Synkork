export interface Account {
  id: string
  username: string
  displayName: string
  email: string
  avatarUrl?: string
  role: 'USER' | 'MANAGER' | 'ADMIN'
}
