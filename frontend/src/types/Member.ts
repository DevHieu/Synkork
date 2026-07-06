export interface Member {
  memberId: string;
  userId?: string;
  email?: string;
  username: string;
  displayName: string;
  avatarUrl?: string;
  role: "OWNER" | "ADMIN" | "MEMBER";
  muted: boolean;
  deafen: boolean;
  chatDisableUntil: string | null;
}

export type ChatDisableTime = "NOT_DISABLE"
  | "MINUTE"
  | "FIVE_MINUTES"
  | "FIFTEEN_MINUTES"
  | "HOUR"
  | "DAY"
  | "WEEK";