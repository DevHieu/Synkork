<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useSpaceStore } from "@/stores/spaceStore";
import { useUserStore } from "@/stores/userStore";
import { useSuggestionStore } from "@/features/calendar/stores/calendarStore";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { storeToRefs } from "pinia";
import { useCalendarDate } from "@/features/calendar/composable/useCalendarDate";
import { useCalendarEvents } from "@/features/calendar/composable/useCalendarEvents";
import { useCalendarRealtime } from "@/features/calendar/composable/useCalendarRealtime";
import type { CalendarEvent } from "@/features/calendar/types/calendar.types";
import type { SuggestedEventDraft } from "@/features/calendar/types/calendar.types";
import dayjs from "dayjs";

import CalendarMonthView from "@/features/calendar/components/views/CalendarMonthView.vue";
import CalendarWeekView from "@/features/calendar/components/views/CalendarWeekView.vue";
import CalendarYearView from "@/features/calendar/components/views/CalendarYearView.vue";
import CalendarEventDialog from "@/features/calendar/components/dialogs/CalendarEventDialog.vue";
import CalendarEventViewDialog from "@/features/calendar/components/dialogs/CalendarEventViewDialog.vue";
import CalendarToolbar from "@/features/calendar/components/sub-components/CalendarToolbar.vue";
import CalendarNotificationDialog from "@/features/calendar/components/dialogs/CalendarNotificationDialog.vue";
import type { NotificationType } from "@/features/calendar/components/dialogs/CalendarNotificationDialog.vue";
import type { EventFormData } from "@/features/calendar/composable/useEventForm";
import { PlanLimitUtils } from "@/utils/PlanLimitUtils";
import PremiumFeatureDialog from "@/components/dialog/PremiumFeatureDialog.vue";
import { extractNewFiles, formatPayload } from "@/features/calendar/utils/calendar.utils";
import { CalendarVersionConflictError } from "@/features/calendar/services/calendarService";
import { buildAttachmentSummaryHtml } from "@/features/calendar/utils/calendar-summary.utils";
import {
  buildConflictMessage,
  createFormDataFromEvent,
  createInitialFormData,
  escapeHtml,
  resolveScheduleEvent,
} from "@/features/calendar/utils/calendar-view.utils";
import { createEvent as apiCreateEvent, deleteEvent as apiDeleteEvent, checkConflicts as apiCheckConflicts, summarizeAttachment as apiSummarizeAttachment } from "@/features/calendar/services/calendarService";

// Store state
const spaceStore = useSpaceStore();
const userStore = useUserStore();
const calendarSuggestionStore = useSuggestionStore();
const roomMemberStore = useRoomMemberStore();
const { currentSpace } = storeToRefs(spaceStore);
const { user, userPlan } = storeToRefs(userStore);
const { members } = storeToRefs(roomMemberStore);

// Current user ID
const currentUserId = computed(() => (user.value as any)?.id || "");

// Calendar logic
const spaceIdRef = computed(() => currentSpace.value?.id);
const calendarDate = useCalendarDate();
const calendarEvents = useCalendarEvents(
  spaceIdRef,
  currentUserId,
  calendarDate.currentDate,
  calendarDate.viewMode
);
useCalendarRealtime(spaceIdRef, calendarEvents.events, calendarEvents.fetchEvents);

const {
  viewMode,
  currentDate,
  selectedDate,
  headerTitle,
  relativeTimeText,
  goNext,
  goPrev,
  goToday,
  selectDate,
  setYearMonth,
  dayNamesLong,
  isToday,
  isSelected,
} = calendarDate;

const {
  events,
  loading,
  createEvent,
  updateEvent,
  deleteEvent,
  checkConflicts,
  fetchEvents,
} = calendarEvents;

// Event modal state
const showDialog = ref(false);
const showViewDialog = ref(false);
const showPremiumDialog = ref(false);
const isEditing = ref(false);
const editingEventId = ref<string | undefined>(undefined);
const selectedEvent = ref<CalendarEvent | null>(null);
const isConflictDialogOpen = ref(false);
const conflictPayload = ref<{ isEditing: boolean; eventId?: string; data: EventFormData } | null>(null);
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
  attendeeIds: [],
  attendees: [],
  attachments: [],
  callRoomSpaceId: undefined,
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
  selectedEvent.value = resolveScheduleEvent(event, events.value);
  showViewDialog.value = true;
};

// Mở chức năng sửa sự kiện
const openEditDialog = (event: CalendarEvent) => {
  const resolved = resolveScheduleEvent(event, events.value);
  isEditing.value = true;
  selectedEvent.value = resolved;
  editingEventId.value = resolved.id;
  initialFormData.value = createFormDataFromEvent(resolved);
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
  requireInput: undefined as string | undefined,
});

const showNotification = (
  type: NotificationType,
  title: string,
  message: string,
  options?: { confirmText?: string; cancelText?: string, requireInput?: string },
) => {
  notificationState.value = {
    show: true,
    type,
    title,
    message,
    confirmText: options?.confirmText || "ĐỒNG Ý",
    cancelText: options?.cancelText || "HỦY",
    requireInput: options?.requireInput,
  };
};

const isSavingEvent = ref(false);
const isSaveSuccess = ref(false);
const pendingSavePayload = ref<{
  isEditing: boolean;
  eventId?: string;
  data: EventFormData;
} | null>(null);

const persistEvent = async (payload: { isEditing: boolean; eventId?: string; data: EventFormData }) => {
  isSavingEvent.value = true;
  try {
    if (payload.isEditing && payload.eventId) {
      await updateEvent(payload.eventId, payload.data);
    } else {
      await createEvent(payload.data);
    }
    isSaveSuccess.value = true;
    
    setTimeout(() => {
      showDialog.value = false;
      showViewDialog.value = false;
      notificationState.value.show = false;
      pendingSavePayload.value = null;
      isSaveSuccess.value = false;
    }, 1200);
  } catch (err: any) {
    if (err instanceof CalendarVersionConflictError) {
      conflictPayload.value = payload;
      showDialog.value = false;
      pendingSavePayload.value = null;
      isConflictDialogOpen.value = true;
    } else {
      console.error("Lỗi khi lưu sự kiện:", err);
      pendingSavePayload.value = null;
      const msg = err.response?.data || "CÓ LỖI XẢY RA KHI LƯU SỰ KIỆN!";
      showNotification("error", "LỖI LƯU SỰ KIỆN", msg);
    }
  } finally {
    isSavingEvent.value = false;
  }
};

// Submit lưu sự kiện
const handleSaveEvent = async (data: EventFormData) => {

  const newFiles = extractNewFiles(data);
  const limit = PlanLimitUtils.maxFileSizeBytes(userPlan.value);
  if (newFiles.some(f => f.size > limit)) {
    showPremiumDialog.value = true;
    return; // block creation, keep modal open
  }

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

const eventsToDeleteAll = ref<CalendarEvent[]>([]);
const deleteAllStep = ref(0);

const showDeleteAllConfirmationStep = () => {
  const events = eventsToDeleteAll.value;
  if (!events || events.length === 0 || !events[0]) return;
  
  const rawDate = events[0].eventDate;
  const parts = rawDate.split("-");
  const displayDate = parts.length === 3 ? `${parts[2]}/${parts[1]}/${parts[0]}` : rawDate;

  if (deleteAllStep.value === 1) {
    showNotification(
      "delete",
      "CẢNH BÁO LẦN 1: XÁC NHẬN XÓA TOÀN BỘ",
      `Bạn đang yêu cầu <b>xóa toàn bộ ${events.length} sự kiện</b> do bạn tạo trong ngày <b>${displayDate}</b>.<br/><br/>Hành động này có thể ảnh hưởng đến người khác nếu đó là sự kiện nhóm.`,
      { 
        confirmText: "Tiếp tục", 
        cancelText: "Hủy" 
      }
    );
  } else if (deleteAllStep.value === 2) {
    showNotification(
      "delete",
      "CẢNH BÁO LẦN 2: CHẮC CHẮN MUỐN XÓA TOÀN BỘ",
      `Bạn <b>chắc chắn muốn xóa toàn bộ</b> sự kiện trong ngày <b>${displayDate}</b> chứ?<br/><br/>Hành động này <b>tuyệt đối KHÔNG THỂ hoàn tác</b>.`,
      { 
        confirmText: "Chắc chắn", 
        cancelText: "Quay Lại" 
      }
    );
  } else if (deleteAllStep.value === 3) {
    showNotification(
      "delete",
      `XÓA TOÀN BỘ SỰ KIỆN TRONG ${displayDate}`,
      `Bước cuối cùng. Hãy nhập từ khóa để hoàn tất việc xóa <b>${events.length}</b> sự kiện.`,
      { 
        confirmText: "XÁC NHẬN XÓA", 
        cancelText: "Hủy",
        requireInput: "DELETE"
      }
    );
  }
};

const requestDeleteAllEvents = (events: CalendarEvent[]) => {
  if (events.length === 0) return;
  eventsToDeleteAll.value = events;
  deleteAllStep.value = 1;
  showDeleteAllConfirmationStep();
};

const executeDeleteAllEvents = async () => {
  isDeletingEvent.value = true;
  try {
    await Promise.all(
      eventsToDeleteAll.value.map(e => apiDeleteEvent(e.id).catch(err => {
        console.warn("Bỏ qua lỗi xóa sự kiện (có thể đã bị xóa trước đó):", e.id, err);
      }))
    );
    notificationState.value.show = false;
    await fetchEvents();
  } catch (err) {
    console.error(err);
    notificationState.value.show = false;
  } finally {
    isDeletingEvent.value = false;
    eventsToDeleteAll.value = [];
    deleteAllStep.value = 0;
  }
};

const eventToCopyToPersonal = ref<CalendarEvent | null>(null);

// Mở confirm dialog
const handleAddToPersonalCalendar = async (eventToCopy: CalendarEvent) => {
  if (!user.value?.personalCalendarId) {
    showNotification("error", "LỖI", "Bạn chưa có không gian lịch cá nhân.");
    return;
  }
  showViewDialog.value = false;
  eventToCopyToPersonal.value = eventToCopy;


  let conflicts: CalendarEvent[] = [];
  try {
    const res = await apiCheckConflicts(
      user.value.personalCalendarId,
      eventToCopy.eventDate,
      eventToCopy.endDate || eventToCopy.eventDate,
      eventToCopy.startTime,
      eventToCopy.endTime
    );
    conflicts = res.data || [];
  } catch (e) {
    console.error(e);
  }

  showNotification(
    "confirm",
    conflicts.length > 0 ? "CẢNH BÁO TRÙNG LỊCH" : "LƯU VÀO LỊCH CÁ NHÂN",
    conflicts.length > 0 ? buildConflictMessage(conflicts) : "Bạn có chắc chắn muốn lưu bản sao của sự kiện này vào lịch cá nhân không?",
    { confirmText: "LƯU NGAY", cancelText: "HỦY" }
  );
};

// Thực thi call API
const executeAddToPersonalCalendar = async () => {
  if (!eventToCopyToPersonal.value || !user.value?.personalCalendarId) {
    notificationState.value.show = false;
    return;
  }
  isSavingEvent.value = true;
  try {
    const eventToCopy = eventToCopyToPersonal.value;
    const payload = formatPayload({
      title: eventToCopy.title,
      description: eventToCopy.description,
      eventDate: eventToCopy.eventDate,
      endDate: eventToCopy.endDate || eventToCopy.eventDate,
      startTime: eventToCopy.startTime,
      endTime: eventToCopy.endTime,
      recurrenceType: eventToCopy.recurrenceType || "NONE",
      recurrenceEndDate: eventToCopy.recurrenceEndDate,
      eventLink: eventToCopy.eventLink,
      attachments: eventToCopy.attachments,
    }, ref(user.value.personalCalendarId), currentUserId);

    await apiCreateEvent(payload);
    notificationState.value.show = false;
    eventToCopyToPersonal.value = null;
    showNotification("success", "THÀNH CÔNG", "Đã lưu sự kiện vào lịch cá nhân.");
  } catch (err: any) {
    showNotification("error", "LỖI", err.response?.data || "CÓ LỖI XẢY RA KHI LƯU VÀO LỊCH CÁ NHÂN!");
  } finally {
    isSavingEvent.value = false;
  }
};

// Xử lý tóm tắt tài liệu
const handleSummarizeAttachment = async (attachment: any, event: CalendarEvent) => {
  if (!event.id) return;
  try {
    showNotification("info", "ĐANG XỬ LÝ", "AI đang phân tích và tóm tắt tài liệu của bạn...");
    const res = await apiSummarizeAttachment(event.id, attachment.id || "0");
    showNotification("success", "TÓM TẮT BỞI AI", buildAttachmentSummaryHtml(res.data));
  } catch (error: any) {
    console.error(error);
    showNotification("error", "LỖI", error.response?.data || "CÓ LỖI KHI TÓM TẮT TÀI LIỆU!");
  }
};

const handleNotificationConfirm = () => {
  if (eventsToDeleteAll.value.length > 0) {
    if (deleteAllStep.value === 1) {
      deleteAllStep.value = 2;
      showDeleteAllConfirmationStep();
    } else if (deleteAllStep.value === 2) {
      deleteAllStep.value = 3;
      showDeleteAllConfirmationStep();
    } else if (deleteAllStep.value === 3) {
      executeDeleteAllEvents();
    }
    return;
  }

  if (notificationState.value.type === 'delete') {
    executeDelete();
    return;
  }

  if (notificationState.value.type === 'confirm') {
    if (pendingSavePayload.value) {
      persistEvent(pendingSavePayload.value);
    } else if (eventToCopyToPersonal.value) {
      executeAddToPersonalCalendar();
    }
    return;
  }

  notificationState.value.show = false;
};

const handleNotificationCancel = () => {
  if (notificationState.value.type === "confirm") {
    pendingSavePayload.value = null;
    eventToCopyToPersonal.value = null;
  }
  if (notificationState.value.type === "delete") {
    eventToDelete.value = null;
  }
  
  if (eventsToDeleteAll.value.length > 0) {
    eventsToDeleteAll.value = [];
    deleteAllStep.value = 0;
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

// Conflict dialog handlers
const handleConflictDiscard = () => {
  isConflictDialogOpen.value = false;
  conflictPayload.value = null;
  fetchEvents();
};

const handleConflictCreateCopy = async () => {
  if (!conflictPayload.value) return;
  isConflictDialogOpen.value = false;
  const copyData = { ...conflictPayload.value.data };
  delete (copyData as any).version;
  try {
    await createEvent(copyData);
    showNotification("success", "THÀNH CÔNG", "Đã tạo sự kiện mới từ nội dung của bạn.");
  } catch (err: any) {
    showNotification("error", "LỖI", err.response?.data || "Có lỗi khi tạo sự kiện mới!");
  }
  conflictPayload.value = null;
};
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
        :is-selected="isSelected" @select-date="selectDate" @view-event="openViewDialog" @delete-all-events="requestDeleteAllEvents" />

      <CalendarWeekView v-if="viewMode === 'week'" :current-date="currentDate" :selected-date="selectedDate"
        :events="events" :day-names="dayNamesLong" :is-today="isToday" :is-selected="isSelected"
        @select-date="selectDate" @view-event="openViewDialog" />

      <CalendarYearView v-if="viewMode === 'year'" :current-date="currentDate" :events="events" :is-today="isToday"
        @click-year-month="setYearMonth" />
    </div>

    <CalendarEventViewDialog v-model:show="showViewDialog" :event="selectedEvent" :current-user-id="currentUserId"
      @edit="openEditDialog" @delete="handleDeleteEvent" @add-to-personal-calendar="handleAddToPersonalCalendar"
      @summarize-attachment="handleSummarizeAttachment" />

    <CalendarEventDialog v-model:show="showDialog" :is-editing="isEditing" :initial-data="initialFormData" :room-members="members"
      :is-saving="isSavingEvent" :is-success="isSaveSuccess" @save="handleSaveEvent" />

    <CalendarNotificationDialog v-model:show="notificationState.show" :type="notificationState.type"
      :title="notificationState.title" :message="notificationState.message"
      :confirm-text="notificationState.confirmText" :cancel-text="notificationState.cancelText"
      :require-input="notificationState.requireInput"
      :is-loading="isDeletingEvent || isSavingEvent" @confirm="handleNotificationConfirm"
      @cancel="handleNotificationCancel" />

    <!-- Conflict Dialog -->
    <CalendarNotificationDialog
      v-model:show="isConflictDialogOpen"
      type="confirm"
      title="SỰ KIỆN ĐÃ BỊ THAY ĐỔI BỞI NGƯỜI KHÁC"
      message="Trong lúc bạn chỉnh sửa, một người khác đã lưu thay đổi cho sự kiện này. Nếu lưu đè, nội dung của họ sẽ bị mất.<br/><br/>Bạn có muốn tạo một sự kiện mới chứa nội dung bạn vừa nhập không?"
      confirm-text="Tạo sự kiện mới"
      cancel-text="Bỏ qua, xem bản mới nhất"
      @confirm="handleConflictCreateCopy"
      @cancel="handleConflictDiscard"
    />

    <PremiumFeatureDialog
      v-model:open="showPremiumDialog"
      feature-name="Tải lên file lớn"
    />
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
