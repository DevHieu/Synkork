import type { User } from './schema'

// Synkork trả về displayName thay vì firstName/lastName
export function mapSynkorkUser(raw: any): User {
  const displayName: string = raw.displayName ?? raw.username ?? ''
  const parts = displayName.trim().split(' ')
  const firstName = parts.length > 1 ? parts.slice(0, -1).join(' ') : displayName
  const lastName = parts.length > 1 ? parts[parts.length - 1] : ''

  return {
    id: raw.id ?? '',
    firstName: raw.firstName ?? firstName,
    lastName: raw.lastName ?? lastName,
    username: raw.username ?? '',
    email: raw.email ?? '',
    status: raw.status?.toLowerCase() ?? 'active',
    role: raw.role?.toLowerCase() ?? 'user',
    displayName: raw.displayName ?? null,
    avatarUrl: raw.avatarUrl ?? null,
    provider: raw.provider ?? null,
    createdAt: raw.createdAt ? new Date(raw.createdAt) : new Date(),
    updatedAt: raw.updatedAt ? new Date(raw.updatedAt) : new Date(),
  }
}
