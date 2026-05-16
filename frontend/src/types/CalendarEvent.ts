export interface CalendarEvent {
  id: string;
  spaceId: string;
  title: string;
  description: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  recurrenceType?: string;
  recurrenceEndDate?: string;
  allowEditAll: boolean;
  createdById: string;
  createdByUsername: string;
  createdByDisplayName: string;
  createdAt: string;
  updatedAt: string;
}
