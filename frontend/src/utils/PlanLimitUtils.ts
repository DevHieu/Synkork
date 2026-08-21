import type { SpaceType } from "@/features/spaces/types/Space";

export type PlanType = "FREE" | "TEAM" | "BUSINESS";
export type LimitType =
  | "file"
  | "rooms"
  | "chat"
  | "voice"
  | "note"
  | "calendar"
  | "task";

export const PlanLimitUtils = {
  maxRooms(plan: PlanType): number {
    switch (plan) {
      case "FREE":
        return 5;
      case "TEAM":
        return 10;
      case "BUSINESS":
        return 30;
    }
  },

  maxSpaces(plan: PlanType, spaceType: SpaceType): number {
    switch (spaceType) {
      case "CHAT":
        return this.maxChatSpaces(plan);
      case "VOICE":
        return this.maxVoiceSpaces(plan);
      case "NOTE":
        return this.maxCollaborationSpaces(plan);
      case "CALENDAR":
        return this.maxCollaborationSpaces(plan);
      case "TASK":
        return this.maxCollaborationSpaces(plan);
      default:
        return 0;
    }
  },

  maxChatSpaces(plan: PlanType): number {
    switch (plan) {
      case "FREE":
        return 3;
      case "TEAM":
        return 10;
      case "BUSINESS":
        return 20;
    }
  },

  maxVoiceSpaces(plan: PlanType): number {
    switch (plan) {
      case "FREE":
        return 2;
      case "TEAM":
        return 5;
      case "BUSINESS":
        return 10;
    }
  },

  maxCollaborationSpaces(plan: PlanType): number {
    switch (plan) {
      case "FREE":
        return 1;
      case "TEAM":
        return 3;
      case "BUSINESS":
        return 10;
    }
  },

  maxFileSizeBytes(plan: PlanType): number {
    switch (plan) {
      case "FREE":
        return 1 * 1024 * 1024;
      case "TEAM":
        return 10 * 1024 * 1024;
      case "BUSINESS":
        return 50 * 1024 * 1024;
    }
  },

  formatFileSize(bytes: number): string {
    if (bytes >= 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(0)} MB`;
    return `${(bytes / 1024).toFixed(0)} KB`;
  },

  fileSizeLabel(plan: PlanType): string {
    return this.formatFileSize(this.maxFileSizeBytes(plan));
  },
};
