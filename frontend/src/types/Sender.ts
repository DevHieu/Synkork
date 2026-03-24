export interface Sender {
  displayName: string;
  username: string;
  avatarUrl: string;
  role: "OWNER" | "MEMBER" | "ADMIN";
}
