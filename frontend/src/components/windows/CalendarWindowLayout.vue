<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useSpaceStore } from "@/stores/spaceStore";
import { useUserStore } from "@/stores/userStore";
import { useSuggestionStore } from "@/stores/calendarStore";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { storeToRefs } from "pinia";
import { useCalendar } from "@/components/calendar/composables/useCalendar";
import type { CalendarEvent } from "@/types/CalendarEvent";
import type { SuggestedEventDraft } from "@/types/CalendarSuggestion";

import CalendarMonthView from "@/components/calendar/views/CalendarMonthView.vue";
import CalendarWeekView from "@/components/calendar/views/CalendarWeekView.vue";
import CalendarYearView from "@/components/calendar/views/CalendarYearView.vue";
import CalendarEventDialog from "@/components/calendar/dialogs/CalendarEventDialog.vue";
import CalendarEventViewDialog from "@/components/calendar/dialogs/CalendarEventViewDialog.vue";
import CalendarToolbar from "@/components/calendar/sub-components/CalendarToolbar.vue";
import CalendarNotificationDialog from "@/components/calendar/dialogs/CalendarNotificationDialog.vue";
import type { NotificationType } from "@/components/calendar/dialogs/CalendarNotificationDialog.vue";
import type { EventFormData } from "@/components/calendar/composables/useEventForm";

// Store state
const spaceStore = useSpaceStore();
const userStore = useUserStore();
const calendarSuggestionStore = useSuggestionStore();
const roomMemberStore = useRoomMemberStore();
const { currentSpace, isPersonalSpace } = storeToRefs(spaceStore);
const { user } = storeToRefs(userStore);
const { members } = storeToRefs(roomMemberStore);

// Current user ID
const currentUserId = computed(() => (user.value as any)?.id || "");

// Calendar logic
const spaceIdRef = computed(() => currentSpace.value?.id);
const {
  viewMode,
  currentDate,
  selectedDate,
  events,
  loading,
  headerTitle,
  relativeTimeText,
  goNext,
  goPrev,
  goToday,
  selectDate,
  setYearMonth,
  createEvent,
  updateEvent,
  deleteEvent,
  checkConflicts,
  dayNamesLong,
  isToday,
  isSelected,
} = useCalendar(spaceIdRef, currentUserId);

// Event modal state
const showDialog = ref(false);
const showViewDialog = ref(false);
const isEditing = ref(false);
const editingEventId = ref<string | undefined>(undefined);
const selectedEvent = ref<CalendarEvent | null>(null);
const initialFormData = ref<EventFormData>({
  title: "",
  description: "",
  eventLink: "",
  eventDate: "",
  endDate: "",
  startTime: "09:00",
  endTime: "10:00",
  recurrenceType: "NONE",
  recurrenceEndDate: undefined,
  allowEditAll: false,
  attendees: [],
  attachments: [],
  callRoomSpaceId: undefined,
});

const createInitialFormData = (overrides: Partial<EventFormData> = {}): EventFormData => ({
  title: "",
  description: "",
  eventLink: "",
  eventDate: "",
  endDate: "",
  startTime: "09:00",
  endTime: "10:00",
  recurrenceType: "NONE",
  recurrenceEndDate: undefined,
  allowEditAll: false,
  attendees: [],
  attachments: [],
  callRoomSpaceId: undefined,
  taskSpaceId: undefined,
  taskId: undefined,
  noteSpaceId: undefined,
  noteId: undefined,
  ...overrides,
});

const createFormDataFromEvent = (event: CalendarEvent): EventFormData => createInitialFormData({
  title: event.title,
  description: event.description || "",
  eventLink: event.eventLink || "",
  eventDate: event.eventDate,
  endDate: event.endDate || event.eventDate,
  startTime: event.startTime.substring(0, 5),
  endTime: event.endTime.substring(0, 5),
  recurrenceType: event.recurrenceType || "NONE",
  recurrenceEndDate: event.recurrenceEndDate,
  allowEditAll: event.allowEditAll,
  attendeeIds: event.attendeeIds || event.attendees?.map((attendee) => attendee.memberId) || [],
  attendees: event.attendees || [],
  attachments: event.attachments || [],
  callRoomSpaceId: event.callRoomSpaceId,
  taskSpaceId: event.taskSpaceId,
  taskId: event.taskId,
  noteSpaceId: event.noteSpaceId,
  noteId: event.noteId,
});

// Điều hướng bằng bàn phím
const handleKeyDown = (e: KeyboardEvent) => {
  // Không điều hướng nếu đang mở dialog hoặc đang nhập liệu
  if (showDialog.value || showViewDialog.value || notificationState.value.show) return;

  const target = e.target as HTMLElement;
  if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable) {
    return;
  }

  if (e.key === 'ArrowRight') {
    e.preventDefault();
    e.stopPropagation();
    goNext();
  } else if (e.key === 'ArrowLeft') {
    e.preventDefault();
    e.stopPropagation();
    goPrev();
  }
};

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});

// Mở chức năng thêm sự kiện
const openCreateDialog = () => {
  isEditing.value = false;
  editingEventId.value = undefined;

  const now = new Date();

  now.setHours(now.getHours() + 1);
  const startH = now.getHours().toString().padStart(2, '0');
  const startM = now.getMinutes().toString().padStart(2, '0');
  const startTime = `${startH}:${startM}`;

  now.setHours(now.getHours() + 1);
  const endH = now.getHours().toString().padStart(2, '0');
  const endM = now.getMinutes().toString().padStart(2, '0');
  const endTime = `${endH}:${endM}`;

  initialFormData.value = createInitialFormData({
    eventDate: selectedDate.value.format("YYYY-MM-DD"),
    endDate: selectedDate.value.format("YYYY-MM-DD"),
    startTime,
    endTime,
  });
  showDialog.value = true;
};

const openSuggestedCreateDialog = (draft: SuggestedEventDraft) => {
  isEditing.value = false;
  editingEventId.value = undefined;

  // Draft này đã được chuẩn hóa từ suggestion trước khi chuyển sang kênh lịch.
  initialFormData.value = createInitialFormData({
    title: draft.title,
    description: draft.description,
    eventDate: draft.eventDate,
    endDate: draft.eventDate,
    startTime: draft.startTime,
    endTime: draft.endTime,
    allowEditAll: draft.allowEditAll,
  });

  showDialog.value = true;
};

const openViewDialog = (event: CalendarEvent) => {
  selectedEvent.value = event;
  showViewDialog.value = true;
};

// Mở chức năng sửa sự kiện
const openEditDialog = (event: CalendarEvent) => {
  isEditing.value = true;
  selectedEvent.value = event;
  editingEventId.value = event.id;
  initialFormData.value = createFormDataFromEvent(event);
  showViewDialog.value = false;
  showDialog.value = true;
};

// State cho Notification (Errors, v.v.)
const notificationState = ref({
  show: false,
  type: "info" as NotificationType,
  title: "",
  message: "",
  confirmText: "ĐỒNG Ý",
  cancelText: "HỦY",
});

const showNotification = (
  type: NotificationType,
  title: string,
  message: string,
  options?: { confirmText?: string; cancelText?: string },
) => {
  notificationState.value = {
    show: true,
    type,
    title,
    message,
    confirmText: options?.confirmText || "ĐỒNG Ý",
    cancelText: options?.cancelText || "HỦY",
  };
};

const isSavingEvent = ref(false);
const pendingSavePayload = ref<{
  isEditing: boolean;
  eventId?: string;
  data: EventFormData;
} | null>(null);

const escapeHtml = (value: string) =>
  value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");

const buildConflictMessage = (conflicts: CalendarEvent[]) => {
  const items = conflicts
    .slice(0, 4)
    .map(
      (event) =>
        `<li class="break-all"><span class="text-foreground font-bold break-all">${escapeHtml(event.title)}</span> (${event.startTime.substring(0, 5)} - ${event.endTime.substring(0, 5)})</li>`,
    )
    .join("");
  const moreCount = conflicts.length - 4;
  const moreMessage = moreCount > 0 ? `<p class="mt-2">VÀ ${moreCount} SỰ KIỆN KHÁC.</p>` : "";

  return `
    <p>CÓ ${conflicts.length} LỊCH ĐANG BỊ TRÙNG THỜI GIAN.</p>
    <ul class="mt-3 ml-5 list-disc space-y-1">${items}</ul>
    ${moreMessage}
    <p class="mt-3">BẠN VẪN MUỐN LƯU SỰ KIỆN NÀY KHÔNG?</p>
  `;
};

const persistEvent = async (payload: { isEditing: boolean; eventId?: string; data: EventFormData }) => {
  isSavingEvent.value = true;
  try {
    if (payload.isEditing && payload.eventId) {
      await updateEvent(payload.eventId, payload.data);
    } else {
      await createEvent(payload.data);
    }
    showDialog.value = false;
    showViewDialog.value = false;
    notificationState.value.show = false;
    pendingSavePayload.value = null;
  } catch (err: any) {
    console.error("Lỗi khi lưu sự kiện:", err);
    pendingSavePayload.value = null;
    const msg = err.response?.data || "CÓ LỖI XẢY RA KHI LƯU SỰ KIỆN!";
    showNotification("error", "LỖI LƯU SỰ KIỆN", msg);
  } finally {
    isSavingEvent.value = false;
  }
};

// Submit lưu sự kiện
const handleSaveEvent = async (data: EventFormData) => {
  const payload = {
    isEditing: isEditing.value,
    eventId: editingEventId.value,
    data,
  };

  const conflicts = await checkConflicts(
    data.eventDate,
    data.endDate || data.eventDate,
    data.startTime,
    data.endTime,
    payload.isEditing ? payload.eventId : undefined,
  );

  if (conflicts.length > 0) {
    pendingSavePayload.value = payload;
    showNotification(
      "confirm",
      "LỊCH BỊ TRÙNG",
      buildConflictMessage(conflicts),
      { confirmText: "VẪN LƯU", cancelText: "HỦY" },
    );
    return;
  }

  await persistEvent(payload);
};

// Delete modal state (Dùng Notification Dialog)
const isDeletingEvent = ref(false);
const eventToDelete = ref<CalendarEvent | null>(null);

// Chuẩn bị xoá sự kiện
const handleDeleteEvent = (event: CalendarEvent) => {
  showViewDialog.value = false;
  eventToDelete.value = event;
  showNotification(
    "delete",
    "XÓA SỰ KIỆN",
    `BẠN CÓ CHẮC CHẮN MUỐN XÓA SỰ KIỆN "<span class="text-foreground font-bold">${escapeHtml(event.title)}</span>" KHÔNG?<br/><br/>HÀNH ĐỘNG NÀY KHÔNG THỂ HOÀN TÁC.`
  );
};

// Submit xoá sự kiện
const executeDelete = async () => {
  if (!eventToDelete.value) {
    notificationState.value.show = false;
    return;
  }
  isDeletingEvent.value = true;
  try {
    await deleteEvent(eventToDelete.value.id);
    notificationState.value.show = false;
    eventToDelete.value = null;
  } catch (err) {
    console.error("Lỗi khi xóa sự kiện:", err);
    showNotification("error", "LỖI", "CÓ LỖI XẢY RA KHI XÓA SỰ KIỆN!");
  } finally {
    isDeletingEvent.value = false;
  }
};

const handleNotificationConfirm = () => {
  if (notificationState.value.type === 'delete') {
    executeDelete();
    return;
  }

  if (notificationState.value.type === 'confirm' && pendingSavePayload.value) {
    persistEvent(pendingSavePayload.value);
    return;
  }

  notificationState.value.show = false;
};

const handleNotificationCancel = () => {
  if (notificationState.value.type === "confirm") {
    pendingSavePayload.value = null;
  }

  if (notificationState.value.type === "delete") {
    eventToDelete.value = null;
  }
  
  notificationState.value.show = false;
};

watch(
  () => currentSpace.value?.id,
  (spaceId) => {
    if (!spaceId) return;

    // Nếu có draft được chuyển sang từ chat thì mở dialog tạo event ngay khi vào đúng kênh lịch.
    const pendingDraft = calendarSuggestionStore.consumePendingDraft(spaceId);
    if (!pendingDraft) return;

    openSuggestedCreateDialog(pendingDraft);
  },
  { immediate: true },
);
</script>

<template>
  <div class="flex flex-col h-screen bg-transparent overflow-hidden">
    <!-- Component quản lý thanh công cụ -->
    <CalendarToolbar v-model:view-mode="viewMode" :current-space-name="currentSpace?.name" :header-title="headerTitle"
      :relative-time-text="relativeTimeText" @go-prev="goPrev" @go-next="goNext" @go-today="goToday"
      @open-create-dialog="openCreateDialog" />

    <!-- Main Content -->
    <div class="flex-1 overflow-hidden flex relative">
      <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-black/20 z-10 backdrop-blur-sm">
        <div class="w-8 h-8 rounded-full border-4 border-teal-500/30 border-t-teal-500 animate-spin"></div>
      </div>

      <CalendarMonthView v-if="viewMode === 'month'" :current-date="currentDate" :selected-date="selectedDate"
        :events="events" :current-user-id="currentUserId" :day-names="dayNamesLong" :is-today="isToday"
        :is-selected="isSelected" @select-date="selectDate" @view-event="openViewDialog" />

      <CalendarWeekView v-if="viewMode === 'week'" :current-date="currentDate" :selected-date="selectedDate"
        :events="events" :day-names="dayNamesLong" :is-today="isToday" :is-selected="isSelected"
        @select-date="selectDate" @view-event="openViewDialog" />

      <CalendarYearView v-if="viewMode === 'year'" :current-date="currentDate" :events="events" :is-today="isToday"
        @click-year-month="setYearMonth" />
    </div>

    <CalendarEventViewDialog v-model:show="showViewDialog" :event="selectedEvent" :current-user-id="currentUserId"
      @edit="openEditDialog" @delete="handleDeleteEvent" />

    <CalendarEventDialog v-model:show="showDialog" :is-editing="isEditing" :initial-data="initialFormData" :room-members="members"
      @save="handleSaveEvent" />

    <!-- Unified Notification Dialog -->
    <CalendarNotificationDialog v-model:show="notificationState.show" :type="notificationState.type"
      :title="notificationState.title" :message="notificationState.message"
      :confirm-text="notificationState.confirmText" :cancel-text="notificationState.cancelText"
      :is-loading="isDeletingEvent || isSavingEvent" @confirm="handleNotificationConfirm"
      @cancel="handleNotificationCancel" />
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.no-scrollbar {
  -ms-overflow-style: none;
  /* IE and Edge */
  scrollbar-width: none;
  /* Firefox */
}
</style>
