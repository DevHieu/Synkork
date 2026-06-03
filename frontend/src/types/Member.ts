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
}
