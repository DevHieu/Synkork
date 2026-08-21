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

export interface ChannelOption {
  roomId: string;
  roomName: string;
  spaceId: string;
  spaceName: string;
}
