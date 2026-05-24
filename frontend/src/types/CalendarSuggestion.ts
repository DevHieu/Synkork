// Loại gợi ý LLM trả về để frontend chọn đúng modal.
export type MessageSuggestionType = "EVENT" | "NOTE" | "TASK" | "NONE";

export interface MessageEventSuggestion {
  // Payload suggestion nhận trực tiếp từ websocket theo từng message.
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
  // Draft đã được áp fallback đầy đủ để mở form calendar ngay.
  title: string;
  description: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  allowEditAll: boolean;
}

// Draft note dùng để đổ thẳng vào NoteDialog hiện có.
export interface SuggestedNoteDraft {
  title: string;
  note: string;
  color: string;
  pinned: boolean;
}

// Draft task dùng để đổ thẳng vào CardFormDialog hiện có.
export interface SuggestedTaskDraft {
  title: string;
  description: string;
  columnName: string;
  dueDate?: string | null;
}

export interface CalendarChannelOption {
  // Dùng cho dialog chọn kênh lịch trước khi điều hướng.
  roomId: string;
  roomName: string;
  spaceId: string;
  spaceName: string;
}
