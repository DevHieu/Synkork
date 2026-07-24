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
import dayjs from "dayjs";

import CalendarMonthView from "@/components/calendar/views/CalendarMonthView.vue";
import CalendarWeekView from "@/components/calendar/views/CalendarWeekView.vue";
import CalendarYearView from "@/components/calendar/views/CalendarYearView.vue";
import CalendarEventDialog from "@/components/calendar/dialogs/CalendarEventDialog.vue";
import CalendarEventViewDialog from "@/components/calendar/dialogs/CalendarEventViewDialog.vue";
import CalendarToolbar from "@/components/calendar/sub-components/CalendarToolbar.vue";
import CalendarNotificationDialog from "@/components/calendar/dialogs/CalendarNotificationDialog.vue";
import type { NotificationType } from "@/components/calendar/dialogs/CalendarNotificationDialog.vue";
import type { EventFormData } from "@/components/calendar/composables/useEventForm";
import { PlanLimitUtils } from "@/utils/PlanLimitUtils";
import PremiumFeatureDialog from "@/components/dialog/PremiumFeatureDialog.vue";
import { extractNewFiles, formatPayload } from "@/components/calendar/composables/calendarUtils";
import { createEvent as apiCreateEvent, deleteEvent as apiDeleteEvent, checkConflicts as apiCheckConflicts, summarizeAttachment as apiSummarizeAttachment } from "@/services/calendarService";

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
    fetchEvents,
    dayNamesLong,
    isToday,
    isSelected,
  } = useCalendar(spaceIdRef, currentUserId);

// Event modal state
const showDialog = ref(false);
const showViewDialog = ref(false);
const showPremiumDialog = ref(false);
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

/**
 * Chuẩn hóa sự kiện liên tục: nếu sự kiện thuộc nhóm scheduleId, tìm ngày bắt đầu thực tế (min) và ngày kết thúc thực tế (max) của nhóm.
 */
const resolveScheduleEvent = (event: CalendarEvent): CalendarEvent => {
  if (event.schedule && event.scheduleId) {
    const group = events.value.filter((e) => e.scheduleId === event.scheduleId);
    if (group.length > 0) {
      let minDate = event.eventDate;
      let maxDate = event.endDate || event.eventDate;
      for (const e of group) {
        if (e.eventDate && dayjs(e.eventDate).isValid()) {
          if (!minDate || dayjs(e.eventDate).isBefore(dayjs(minDate))) {
            minDate = e.eventDate;
          }
        }
        const eEnd = e.endDate || e.eventDate;
        if (eEnd && dayjs(eEnd).isValid()) {
          if (!maxDate || dayjs(eEnd).isAfter(dayjs(maxDate))) {
            maxDate = eEnd;
          }
        }
        if (e.eventDate && dayjs(e.eventDate).isValid()) {
          if (!maxDate || dayjs(e.eventDate).isAfter(dayjs(maxDate))) {
            maxDate = e.eventDate;
          }
        }
      }
      return {
        ...event,
        eventDate: minDate || event.eventDate,
        endDate: maxDate || event.endDate || event.eventDate,
      };
    }
  }
  return event;
};

const openViewDialog = (event: CalendarEvent) => {
  selectedEvent.value = resolveScheduleEvent(event);
  showViewDialog.value = true;
};

// Mở chức năng sửa sự kiện
const openEditDialog = (event: CalendarEvent) => {
  const resolved = resolveScheduleEvent(event);
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
    isSaveSuccess.value = true;
    
    setTimeout(() => {
      showDialog.value = false;
      showViewDialog.value = false;
      notificationState.value.show = false;
      pendingSavePayload.value = null;
      isSaveSuccess.value = false;
    }, 1200);
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
    
    // Đảm bảo parse chuỗi thành object nếu cần
    let data = res.data;
    if (typeof data === "string") {
      try {
        data = JSON.parse(data);
      } catch(e) {
        // Bỏ qua nếu không phải JSON
      }
    }

    // Nếu data là object (JSON từ AI)
    let summaryHtml = "";
    if (data && typeof data === "object") {
      const iconTarget = `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-calendar inline mr-1 -mt-0.5"><path d="M8 2v4"/><path d="M16 2v4"/><rect width="18" height="18" x="3" y="4" rx="2"/><path d="M3 10h18"/></svg>`;
      const iconClock = `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-clock inline mr-1 -mt-0.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`;
      const iconDoc = `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-file-text inline mr-1 -mt-0.5"><path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="M10 9H8"/><path d="M16 13H8"/><path d="M16 17H8"/></svg>`;
      const iconCheck = `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-check-square inline mr-1 -mt-0.5"><rect width="18" height="18" x="3" y="3" rx="2"/><path d="m9 12 2 2 4-4"/></svg>`;

      if (data.event_name) summaryHtml += `<div class="mb-2 flex items-start"><span class="shrink-0 text-primary mt-0.5 mr-1">${iconTarget}</span><div><strong>Tên sự kiện:</strong> ${data.event_name}</div></div>`;
      if (data.time_location) summaryHtml += `<div class="mb-2 flex items-start"><span class="shrink-0 text-primary mt-0.5 mr-1">${iconClock}</span><div><strong>Thời gian & Địa điểm:</strong> ${data.time_location}</div></div>`;
      if (data.summary) summaryHtml += `<div class="mb-3 flex items-start"><span class="shrink-0 text-primary mt-0.5 mr-1">${iconDoc}</span><div><strong>Tóm tắt:</strong> ${data.summary}</div></div>`;
      
      if (data.action_items && Array.isArray(data.action_items) && data.action_items.length > 0) {
        summaryHtml += `<div class="mb-1 flex items-center"><span class="shrink-0 text-primary mr-1">${iconCheck}</span><strong>Công việc cần làm:</strong></div><ul class="list-disc pl-5 mb-2 ml-5">`;
        data.action_items.forEach((item: any) => {
          if (typeof item === "string") {
            summaryHtml += `<li>${item}</li>`;
          } else if (typeof item === "object" && item !== null) {
            // Đề phòng AI thỉnh thoảng trả về object thay vì string (vd: { task: "...", assignee: "..." })
            const text = Object.values(item).filter(v => typeof v === "string").join(" - ");
            summaryHtml += `<li>${text || JSON.stringify(item)}</li>`;
          }
        });
        summaryHtml += `</ul>`;
      }
      
      if (!summaryHtml) {
        summaryHtml = `<pre class="text-xs whitespace-pre-wrap">${JSON.stringify(data, null, 2)}</pre>`;
      }
    } else {
      summaryHtml = data;
    }

    summaryHtml += `
      <div class="mt-4 pt-3 border-t border-border/60 text-[18px] font-semibold text-destructive flex items-start italic leading-tight">
        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-alert-triangle inline shrink-0 mr-1.5"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>
        <span>Nội dung được tóm tắt bởi AI có thể không chính xác 100%, bạn nên kiểm tra lại tài liệu gốc nếu có thể.</span>
      </div>
    `;

    // Gói toàn bộ nội dung trong thẻ div scroll, giới hạn chiều cao (max-h-[60vh]), kèm font-sans của dự án
    const finalHtml = `<div class="font-sans text-sm text-foreground max-h-[60vh] overflow-y-auto calendar-scrollbar pr-2">${summaryHtml}</div>`;

    showNotification("success", "TÓM TẮT BỞI AI", finalHtml);
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
