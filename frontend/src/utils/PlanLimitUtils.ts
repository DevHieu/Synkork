export type PlanType = "FREE" | "TEAM" | "BUSINESS";
export type LimitType = "file" | "rooms" | "chatSpaces" | "voiceSpaces" | "noteSpaces";


export const PlanLimitUtils = {
  maxRooms(plan: PlanType): number {
    switch (plan) {
      case "FREE": return 5;
      case "TEAM": return 10;
      case "BUSINESS": return 30;
    }
  },

  maxChatSpaces(plan: PlanType): number {
    switch (plan) {
      case "FREE": return 3;
      case "TEAM": return 10;
      case "BUSINESS": return 20;
    }
  },

  maxVoiceSpaces(plan: PlanType): number {
    switch (plan) {
      case "FREE": return 2;
      case "TEAM": return 5;
      case "BUSINESS": return 10;
    }
  },

  maxNoteSpaces(plan: PlanType): number {
    switch (plan) {
      case "FREE": return 1;
      case "TEAM": return 3;
      case "BUSINESS": return 10;
    }
  },

  maxFileSizeBytes(plan: PlanType): number {
    switch (plan) {
      case "FREE": return 1 * 1024 * 1024;
      case "TEAM": return 10 * 1024 * 1024;
      case "BUSINESS": return 50 * 1024 * 1024;
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