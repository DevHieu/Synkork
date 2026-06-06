export enum AuditStatus {
  SUCCESS = 'SUCCESS',
  FAILURE = 'FAILURE',
}

export interface AuditLog {
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
  status: AuditStatus
  createdAt: string
}
export interface LogParams {
  page?: number
  size?: number
  search?: string
  action?: string
  entityType?: string
  status?: 'SUCCESS' | 'FAILURE'
  workspaceId?: number
  actorEmail?: string
  fromDate?: string
  toDate?: string
}
