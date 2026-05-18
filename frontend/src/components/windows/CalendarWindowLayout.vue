<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useSpaceStore } from "@/stores/spaceStore";
import { useUserStore } from "@/stores/userStore";
import { useCalendarSuggestionStore } from "@/stores/calendarSuggestionStore";
import { storeToRefs } from "pinia";
import { useCalendar } from "@/components/calendar/composables/useCalendar";
import type { CalendarEvent } from "@/types/CalendarEvent";
import type { SuggestedEventDraft } from "@/types/CalendarSuggestion";

import CalendarMonthView from "@/components/calendar/views/CalendarMonthView.vue";
import CalendarWeekView from "@/components/calendar/views/CalendarWeekView.vue";
import CalendarYearView from "@/components/calendar/views/CalendarYearView.vue";
import CalendarEventDialog from "@/components/calendar/dialogs/CalendarEventDialog.vue";
import CalendarToolbar from "@/components/calendar/sub-components/CalendarToolbar.vue";
import CalendarNotificationDialog from "@/components/calendar/dialogs/CalendarNotificationDialog.vue";
import type { NotificationType } from "@/components/calendar/dialogs/CalendarNotificationDialog.vue";

// Store state
const spaceStore = useSpaceStore();
const userStore = useUserStore();
const calendarSuggestionStore = useCalendarSuggestionStore();
const { currentSpace } = storeToRefs(spaceStore);
const { user } = storeToRefs(userStore);

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
  dayNames,
  dayNamesLong,
  isToday,
  isSelected,
} = useCalendar(spaceIdRef, currentUserId);

// Event modal state
const showDialog = ref(false);
const isEditing = ref(false);
const editingEventId = ref<string | undefined>(undefined);
const initialFormData = ref({
  title: "",
  description: "",
  eventDate: "",
  startTime: "09:00",
  endTime: "10:00",
  allowEditAll: false,
});

// Điều hướng bằng bàn phím
const handleKeyDown = (e: KeyboardEvent) => {
  // Không điều hướng nếu đang mở dialog hoặc đang nhập liệu
  if (showDialog.value || notificationState.value.show) return;
  
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

  initialFormData.value = {
    title: "",
    description: "",
    eventDate: selectedDate.value.format("YYYY-MM-DD"),
    startTime,
    endTime,
    allowEditAll: false,
  };
  showDialog.value = true;
};

const openSuggestedCreateDialog = (draft: SuggestedEventDraft) => {
  isEditing.value = false;
  editingEventId.value = undefined;

  initialFormData.value = {
    title: draft.title,
    description: draft.description,
    eventDate: draft.eventDate,
    startTime: draft.startTime,
    endTime: draft.endTime,
    allowEditAll: draft.allowEditAll,
  };

  showDialog.value = true;
};

// Mở chức năng sửa sự kiện
const openEditDialog = (event: CalendarEvent) => {
  isEditing.value = true;
  editingEventId.value = event.id;
  initialFormData.value = {
    title: event.title,
    description: event.description || "",
    eventDate: event.eventDate,
    startTime: event.startTime.substring(0, 5),
    endTime: event.endTime.substring(0, 5),
    allowEditAll: event.allowEditAll,
  };
  showDialog.value = true;
};

// State cho Notification (Errors, v.v.)
const notificationState = ref({
  show: false,
  type: "info" as NotificationType,
  title: "",
  message: "",
});

const showNotification = (type: NotificationType, title: string, message: string) => {
  notificationState.value = { show: true, type, title, message };
};

// Submit lưu sự kiện
const handleSaveEvent = async (data: any) => {
  try {
    if (isEditing.value && editingEventId.value) {
      await updateEvent(editingEventId.value, data);
    } else {
      await createEvent(data);
    }
    showDialog.value = false;
  } catch (err: any) {
    console.error("Lỗi khi lưu sự kiện:", err);
    const msg = err.response?.data || "CÓ LỖI XẢY RA KHI LƯU SỰ KIỆN!";
    showNotification("error", "LỖI LƯU SỰ KIỆN", msg);
  }
};

// Delete modal state (Dùng Notification Dialog)
const isDeletingEvent = ref(false);
const eventToDelete = ref<CalendarEvent | null>(null);

// Chuẩn bị xoá sự kiện
const handleDeleteEvent = (event: CalendarEvent) => {
  eventToDelete.value = event;
  showNotification(
    "delete", 
    "XÓA SỰ KIỆN", 
    `BẠN CÓ CHẮC CHẮN MUỐN XÓA SỰ KIỆN "<span class="text-foreground font-bold">${event.title}</span>" KHÔNG?<br/><br/>HÀNH ĐỘNG NÀY KHÔNG THỂ HOÀN TÁC.`
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
  } else {
    notificationState.value.show = false;
  }
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
        :is-selected="isSelected" @select-date="selectDate" @edit-event="openEditDialog"
        @delete-event="handleDeleteEvent" />

      <CalendarWeekView v-if="viewMode === 'week'" :current-date="currentDate" :selected-date="selectedDate"
        :events="events" :day-names="dayNamesLong" :is-today="isToday" :is-selected="isSelected" @select-date="selectDate"
        @edit-event="openEditDialog" />

      <CalendarYearView v-if="viewMode === 'year'" :current-date="currentDate" :events="events" :is-today="isToday"
        @click-year-month="setYearMonth" />
    </div>

    <CalendarEventDialog v-model:show="showDialog" :is-editing="isEditing" :initial-data="initialFormData"
      :check-conflicts="checkConflicts" :editing-event-id="editingEventId" @save="handleSaveEvent" />

    <!-- Unified Notification Dialog -->
    <CalendarNotificationDialog 
      v-model:show="notificationState.show" 
      :type="notificationState.type"
      :title="notificationState.title"
      :message="notificationState.message"
      :is-loading="isDeletingEvent"
      @confirm="handleNotificationConfirm" 
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
