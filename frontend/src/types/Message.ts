import type { Member } from "./Member";

export interface ReplyPreview {
  id: string;
  content: string;
  deleted: boolean;
  senderDisplayName: string;
}

export interface Message {
  id: string;
  content: string | null;
  spaceId: string;
  deleted: boolean;
  pinned: boolean;
  edited: boolean;
  type: "TEXT" | "IMAGE" | "FILE";
  sender: Member;
  replyTo: ReplyPreview | null;
  attachmentUrl: string | null;
  attachmentName: string | null;
  sending?: boolean;
  failed?: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface MessageRequest {
  content: string;
  version?: number;
  replyToId: string | null;
}
