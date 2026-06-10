export interface User {
  id: string;
  username: string;
  displayName: string;
  email: string;
  avatarUrl?: string;
  provider: "LOCAL" | "GOOGLE" | "FACEBOOK" | "GITHUB";
  hasPassword: boolean;
}
