import { defineStore } from "pinia";
import type {
  MessageEventSuggestion,
  SuggestedEventDraft,
} from "@/types/CalendarSuggestion";

interface PendingCalendarSuggestionDraft {
  spaceId: string;
  draft: SuggestedEventDraft;
}

// Store này chỉ làm cầu nối tạm giữa chat và calendar, không lưu lâu dài.
export const useCalendarSuggestionStore = defineStore("calendarSuggestion", {
  state: () => ({
    isChannelDialogOpen: false,
    selectedSuggestion: null as MessageEventSuggestion | null,
    pendingDraft: null as PendingCalendarSuggestionDraft | null,
  }),

  actions: {
    openChannelDialog(suggestion: MessageEventSuggestion) {
      this.selectedSuggestion = suggestion;
      this.isChannelDialogOpen = true;
    },

    closeChannelDialog() {
      this.isChannelDialogOpen = false;
      this.selectedSuggestion = null;
    },

    setPendingDraft(spaceId: string, draft: SuggestedEventDraft) {
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
