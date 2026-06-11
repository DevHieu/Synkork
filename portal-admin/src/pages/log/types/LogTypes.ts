export interface AuditLog {
  id: string
  actorEmail?: string
  action: string
  entityType?: string
  entityName?: string
  createdAt: string
}

export interface AuditLogDetail {
  id: string
  actorId?: number
  actorEmail?: string
  action: string
  entityType?: string
  entityId?: string
  entityName?: string
  workspaceId?: number
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
  workspaceId?: number
  actorEmail?: string
  dateFrom?: string
  dateTo?: string
}
