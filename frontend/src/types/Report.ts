export type ReportReason = 'SPAM' | 'HARASSMENT' | 'INAPPROPRIATE' | 'HATE_SPEECH' | 'OTHER'

export interface ReportRequest {
  targetId: string;
  reason: ReportReason;
  description: string;
}