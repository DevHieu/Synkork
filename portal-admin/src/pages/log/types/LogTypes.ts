export interface AuditLog {
  id: string
  actorId?: string
  actorEmail?: string
  action: string
  entityType?: string
  entityId?: string
  entityName?: string
  workspaceId?: string
  description?: string
  metadata?: string
  createdAt: string
}
export interface LogParams {
  page?: number
  size?: number
  search?: string
  action?: string
  entityType?: string
  workspaceId?: string
  actorEmail?: string
  fromDate?: string
  toDate?: string
}
