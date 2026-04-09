<script setup lang="ts">
import { ref, computed } from "vue";
import { useSpaceStore } from "@/stores/spaceStore";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import { useCalendar } from "@/composables/calendar/useCalendar";
import type { CalendarEvent } from "@/types/CalendarEvent";

import CalendarMonthView from "@/components/calendar/CalendarMonthView.vue";
import CalendarWeekView from "@/components/calendar/CalendarWeekView.vue";
import CalendarYearView from "@/components/calendar/CalendarYearView.vue";
import CalendarEventDialog from "@/components/calendar/CalendarEventDialog.vue";
import CalendarToolbar from "@/components/calendar/CalendarToolbar.vue";
import CalendarDeleteDialog from "@/components/calendar/CalendarDeleteDialog.vue";

// ===== Kho lưu trữ (Store) =====
const spaceStore = useSpaceStore();
const userStore = useUserStore();
const { currentSpace } = storeToRefs(spaceStore);
const { user } = storeToRefs(userStore);

// Lấy ID người dùng hiện tại
const currentUserId = computed(() => (user.value as any)?.id || "");

// ===== Logic lịch (Composable) =====
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
  jumpDate,
  selectDate,
  setYearMonth,
  createEvent,
  updateEvent,
  deleteEvent,
  checkConflicts,
} = useCalendar(spaceIdRef, currentUserId);

// ===== Trạng thái Dialog thêm/sửa =====
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

// Mở dialog tạo mới
const openCreateDialog = () => {
  isEditing.value = false;
  editingEventId.value = undefined;
  initialFormData.value = {
    title: "",
    description: "",
    eventDate: selectedDate.value.format("YYYY-MM-DD"),
    startTime: "09:00",
    endTime: "10:00",
    allowEditAll: false,
  };
  showDialog.value = true;
};

// Mở dialog chỉnh sửa
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

// Lưu sự kiện (Tạo mới hoặc Cập nhật)
const handleSaveEvent = async (data: any) => {
  try {
    if (isEditing.value && editingEventId.value) {
      await updateEvent(editingEventId.value, data);
    } else {
      await createEvent(data);
    }
    showDialog.value = false;
  } catch (err) {
    console.error("Lỗi khi lưu sự kiện:", err);
    alert("Có lỗi xảy ra khi lưu sự kiện!");
  }
};

// ===== Trạng thái Xóa =====
const showDeleteEventDialog = ref(false);
const eventToDelete = ref<CalendarEvent | null>(null);
const isDeletingEvent = ref(false);

// Xác nhận xóa
const handleDeleteEvent = (event: CalendarEvent) => {
  eventToDelete.value = event;
  showDeleteEventDialog.value = true;
};

// Thực hiện xóa
const executeDelete = async () => {
  if (!eventToDelete.value) return;
  isDeletingEvent.value = true;
  try {
    await deleteEvent(eventToDelete.value.id);
    showDeleteEventDialog.value = false;
    eventToDelete.value = null;
  } catch (err) {
    console.error("Lỗi khi xóa sự kiện:", err);
    alert("Có lỗi xảy ra khi xóa sự kiện!");
  } finally {
    isDeletingEvent.value = false;
  }
};
</script>

<template>
  <div class="flex flex-col h-screen bg-transparent overflow-hidden">
    <!-- Component quản lý thanh công cụ -->
    <CalendarToolbar
      v-model:view-mode="viewMode"
      :current-space-name="currentSpace?.name"
      :header-title="headerTitle"
      :relative-time-text="relativeTimeText"
      @go-prev="goPrev"
      @go-next="goNext"
      @go-today="goToday"
      @jump-date="jumpDate"
      @open-create-dialog="openCreateDialog"
    />

    <!-- Main Content -->
    <div class="flex-1 overflow-hidden flex relative">
      <div v-if="loading" class="absolute inset-0 flex items-center justify-center bg-black/20 z-10 backdrop-blur-sm">
        <div class="w-8 h-8 rounded-full border-4 border-teal-500/30 border-t-teal-500 animate-spin"></div>
      </div>

      <CalendarMonthView v-if="viewMode === 'month'" :current-date="currentDate" :selected-date="selectedDate"
        :events="events" :current-user-id="currentUserId" @select-date="selectDate" @edit-event="openEditDialog"
        @delete-event="handleDeleteEvent" />

      <CalendarWeekView v-if="viewMode === 'week'" :current-date="currentDate" :selected-date="selectedDate"
        :events="events" @select-date="selectDate" @edit-event="openEditDialog" />

      <CalendarYearView v-if="viewMode === 'year'" :current-date="currentDate" :events="events"
        @click-year-month="setYearMonth" />
    </div>

    <!-- Event Dialog -->
    <CalendarEventDialog v-model:show="showDialog" :is-editing="isEditing" :initial-data="initialFormData"
      :check-conflicts="checkConflicts" :editing-event-id="editingEventId" @save="handleSaveEvent" />

    <!-- Modal Xóa Sự Kiện đã được trích xuất -->
    <CalendarDeleteDialog
      v-model:show="showDeleteEventDialog"
      :event-to-delete="eventToDelete"
      :is-deleting-event="isDeletingEvent"
      @execute-delete="executeDelete"
    />
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.no-scrollbar {
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}
</style>
