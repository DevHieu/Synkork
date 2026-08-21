import { defineStore } from "pinia";
import type { SuggestedEventDraft } from "@/types/SuggestionTypes";

interface PendingCalendarSuggestionDraft {
  spaceId: string;
  draft: SuggestedEventDraft;
}

// Cầu nối tạm từ chat sang calendar/note/task: chỉ giữ draft, không xử lý modal bên trong.
export const useSuggestionStore = defineStore("suggestion", {
  state: () => ({
    pendingDraft: null as PendingCalendarSuggestionDraft | null,
  }),

  actions: {
    setPendingDraft(spaceId: string, draft: SuggestedEventDraft) {
      console.log(draft);

      this.pendingDraft = { spaceId, draft };
    },

    consumePendingDraft(spaceId: string): SuggestedEventDraft | null {
      if (!this.pendingDraft || this.pendingDraft.spaceId !== spaceId) {
        return null;
      }

      const draft = this.pendingDraft.draft;
      this.pendingDraft = null;
      return draft;
    },

    clearPendingDraft() {
      this.pendingDraft = null;
    },
  },
});
