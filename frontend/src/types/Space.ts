export interface Space {
  id: string;
  name: string;
  type: "CHAT" | "VOICE" | "NOTE" | "CALENDAR" | "TASK";
  restricted: boolean;
}
