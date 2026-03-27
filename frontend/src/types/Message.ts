import type { Sender } from "./Sender";

export interface Message {
  id: string;
  content: string;
  spaceId: string;
  deleted: boolean;
  pinned: boolean;
  type: "TEXT" | "IMAGE" | "FILE";
  attachmentUrl: string | null;
  sender: Sender;
  createdAt: string;
  updatedAt: string;
}
