export interface MessageEventSuggestion {
  messageId: string;
  hasEvent: boolean;
  title: string | null;
  description: string | null;
  eventDate: string | null;
  startTime: string | null;
  endTime: string | null;
}

export interface SuggestedEventDraft {
  title: string;
  description: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  allowEditAll: boolean;
}

export interface CalendarChannelOption {
  roomId: string;
  roomName: string;
  spaceId: string;
  spaceName: string;
}
