export interface User {
  id: string;
  username: string;
  displayName: string;
  email: string;
  avatarUrl?: string;
  currentPlan: "FREE" | "TEAM" | "BUSINESS";
  planExpiresAt: Date | null;
  personalNoteId: string;
  personalCalendarId: string;
}
