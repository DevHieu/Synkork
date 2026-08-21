export interface CalendarEventAttachment {
  id?: string;
  name: string;
  size: number;
  fileUrl?: string;
  publicId?: string;
  resourceType?: string;
  type?: string;
}

export interface CalendarEventAttendee {
  memberId: string;
  username: string;
  displayName: string;
  avatarUrl?: string;
}

export interface CalendarEvent {
  id: string;
  spaceId: string;
  version?: number;
  title: string;
  description: string;
  eventLink?: string;
  callRoomSpaceId?: string;
  callRoomSpaceName?: string;
  taskSpaceId?: string;
  taskId?: string;
  taskName?: string;
  noteSpaceId?: string;
  noteId?: string;
  noteTitle?: string;
  eventDate: string;
  endDate: string;
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
  attendeeIds?: string[];
  attendees?: CalendarEventAttendee[];
  attachments?: CalendarEventAttachment[];
  schedule?: boolean;
  scheduleId?: string;
  displayDate?: string;
  displayStartTime?: string;
  displayEndTime?: string;
  continuesFromPreviousDay?: boolean;
  continuesToNextDay?: boolean;
  originalStartDateTime?: string;
  originalEndDateTime?: string;
  createdAt: string;
  updatedAt: string;
}
