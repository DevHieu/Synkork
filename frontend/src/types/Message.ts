import type { Sender } from "./Sender";

export interface ReplyPreview {
  id: string;
  content: string;
  deleted: boolean;
  senderDisplayName: string;
}

export interface Message {
  id: string;
  content: string;
  spaceId: string;
  deleted: boolean;
  pinned: boolean;
  edited: boolean;
  type: "TEXT" | "IMAGE" | "FILE";
  attachmentUrl: string | null;
  sender: Sender;
  replyTo: ReplyPreview | null;
  createdAt: string;
  updatedAt: string;
}
