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

// Loại gợi ý LLM trả về để frontend chọn đúng modal.
export type MessageSuggestionType = "EVENT" | "NOTE" | "TASK" | "NONE";

export interface MessageEventSuggestion {
  messageId: string;
  suggestionType: MessageSuggestionType;
  hasEvent: boolean;
  hasNote: boolean;
  hasTask: boolean;
  title: string | null;
  description: string | null;
  eventDate: string | null;
  startTime: string | null;
  endTime: string | null;
  noteTitle: string | null;
  noteContent: string | null;
  noteColor: string | null;
  notePinned: boolean | null;
  noteAllowEditAll: boolean | null;
  taskTitle: string | null;
  taskDescription: string | null;
  taskColumnName: string | null;
  taskDueDate: string | null;
}

export interface SuggestedEventDraft {
  title: string;
  description: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  allowEditAll: boolean;
}

export interface SuggestedNoteDraft {
  title: string;
  note: string;
  color: string;
  pinned: boolean;
}

export interface SuggestedTaskDraft {
  title: string;
  description: string;
  columnName: string;
  dueDate?: string | null;
}

export interface CalendarChannelOption {
  roomId: string;
  roomName: string;
  spaceId: string;
  spaceName: string;
}
