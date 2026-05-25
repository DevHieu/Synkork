import { defineStore } from "pinia";
import type {
  MessageEventSuggestion,
  SuggestedEventDraft,
  SuggestedNoteDraft,
  SuggestedTaskDraft,
} from "@/types/CalendarSuggestion";

interface PendingCalendarSuggestionDraft {
  spaceId: string;
  draft: SuggestedEventDraft;
}

interface PendingNoteSuggestionDraft {
  spaceId: string;
  draft: SuggestedNoteDraft;
}

interface PendingTaskSuggestionDraft {
  spaceId: string;
  draft: SuggestedTaskDraft;
}

// Cầu nối tạm từ chat sang calendar/note/task: chỉ giữ draft, không xử lý modal bên trong.
export const useCalendarSuggestionStore = defineStore("calendarSuggestion", {
  state: () => ({
    isChannelDialogOpen: false,
    selectedSuggestion: null as MessageEventSuggestion | null,
    pendingDraft: null as PendingCalendarSuggestionDraft | null,
    pendingNoteDraft: null as PendingNoteSuggestionDraft | null,
    pendingTaskDraft: null as PendingTaskSuggestionDraft | null,
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

    setPendingNoteDraft(spaceId: string, draft: SuggestedNoteDraft) {
      this.pendingNoteDraft = { spaceId, draft };
    },

    consumePendingNoteDraft(spaceId: string): SuggestedNoteDraft | null {
      if (!this.pendingNoteDraft || this.pendingNoteDraft.spaceId !== spaceId) {
        return null;
      }

      const draft = this.pendingNoteDraft.draft;
      this.pendingNoteDraft = null;
      return draft;
    },

    setPendingTaskDraft(spaceId: string, draft: SuggestedTaskDraft) {
      this.pendingTaskDraft = { spaceId, draft };
    },

    consumePendingTaskDraft(spaceId: string): SuggestedTaskDraft | null {
      if (!this.pendingTaskDraft || this.pendingTaskDraft.spaceId !== spaceId) {
        return null;
      }

      const draft = this.pendingTaskDraft.draft;
      this.pendingTaskDraft = null;
      return draft;
    },

    clearPendingCreationDrafts() {
      this.pendingNoteDraft = null;
      this.pendingTaskDraft = null;
    },
  },
});
