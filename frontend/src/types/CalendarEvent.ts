export interface CalendarEventAttachment {
  name: string;
  size: number;
  fileUrl?: string;
  type?: string;
}

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
  remindBeforeMinutes?: number;
  createdById: string;
  createdByUsername: string;
  createdByDisplayName: string;
  createdByAvatarUrl?: string;
  attendees?: string[];
  attachments?: CalendarEventAttachment[];
  createdAt: string;
  updatedAt: string;
}
