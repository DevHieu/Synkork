export interface MessageEventSuggestion {
  // Payload suggestion nhận trực tiếp từ websocket theo từng message.
  messageId: string;
  hasEvent: boolean;
  title: string | null;
  description: string | null;
  eventDate: string | null;
  startTime: string | null;
  endTime: string | null;
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

export interface CalendarChannelOption {
  // Dùng cho dialog chọn kênh lịch trước khi điều hướng.
  roomId: string;
  roomName: string;
  spaceId: string;
  spaceName: string;
}
