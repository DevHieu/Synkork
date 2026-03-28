<script setup lang="ts">
import { ref, computed } from "vue";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { useSpaceStore } from "@/stores/spaceStore";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import { useCalendar } from "@/composables/useCalendar";
import type { CalendarEvent } from "@/types/CalendarEvent";

import CalendarMonthView from "@/components/calendar/CalendarMonthView.vue";
import CalendarWeekView from "@/components/calendar/CalendarWeekView.vue";
import CalendarYearView from "@/components/calendar/CalendarYearView.vue";
import CalendarEventDialog from "@/components/calendar/CalendarEventDialog.vue";

// ===== Store =====
const spaceStore = useSpaceStore();
const userStore = useUserStore();
const { currentSpace } = storeToRefs(spaceStore);
const { user } = storeToRefs(userStore);

const currentUserId = computed(() => (user.value as any)?.id || "");

// ===== Composable =====
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
} = useCalendar(spaceIdRef, currentUserId);

// ===== Dialog State =====
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

const handleSaveEvent = async (data: any) => {
  try {
    if (isEditing.value && editingEventId.value) {
      await updateEvent(editingEventId.value, data);
    } else {
      await createEvent(data);
    }
    showDialog.value = false;
  } catch (err) {
    console.error("Error saving event:", err);
    alert("Có lỗi xảy ra khi lưu sự kiện!");
  }
};

// ===== Delete State =====
const showDeleteEventDialog = ref(false);
const eventToDelete = ref<CalendarEvent | null>(null);
const isDeletingEvent = ref(false);

const handleDeleteEvent = (event: CalendarEvent) => {
  eventToDelete.value = event;
  showDeleteEventDialog.value = true;
};

const executeDelete = async () => {
  if (!eventToDelete.value) return;
  isDeletingEvent.value = true;
  try {
    await deleteEvent(eventToDelete.value.id);
    showDeleteEventDialog.value = false;
    eventToDelete.value = null;
  } catch (err) {
    console.error("Error deleting event:", err);
    alert("Có lỗi xảy ra khi xóa sự kiện!");
  } finally {
    isDeletingEvent.value = false;
  }
};
</script>

<template>
  <div class="flex flex-col h-screen bg-transparent overflow-hidden">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-white/10">
      <div class="flex items-center gap-3">
        <SidebarTrigger class="-ml-1" />
        <span class="font-semibold text-lg flex items-center gap-2">
          <i class="pi pi-calendar text-teal-400"></i>
          {{ currentSpace?.name }}
        </span>
      </div>

      <div class="flex items-center gap-2">
        <!-- View Mode Buttons -->
        <div class="flex rounded-lg overflow-hidden border border-white/20">
          <button @click="viewMode = 'week'" :class="[
            'px-3 py-1.5 text-sm font-medium transition-all duration-200',
            viewMode === 'week' ? 'bg-teal-600 text-white' : 'hover:bg-white/10 text-gray-300',
          ]">
            Tuần
          </button>
          <button @click="viewMode = 'month'" :class="[
            'px-3 py-1.5 text-sm font-medium transition-all duration-200',
            viewMode === 'month' ? 'bg-teal-600 text-white' : 'hover:bg-white/10 text-gray-300',
          ]">
            Tháng
          </button>
          <button @click="viewMode = 'year'" :class="[
            'px-3 py-1.5 text-sm font-medium transition-all duration-200',
            viewMode === 'year' ? 'bg-teal-600 text-white' : 'hover:bg-white/10 text-gray-300',
          ]">
            Năm
          </button>
        </div>
      </div>
    </div>

    <!-- Navigation Bar -->
    <div class="flex items-center justify-between px-4 py-2 border-b border-white/10">
      <div class="flex items-center gap-2">
        <button @click="goPrev"
          class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-white/10 transition-colors text-gray-300">
          <i class="pi pi-chevron-left"></i>
        </button>
        <button @click="goToday"
          class="px-3 py-1.5 text-sm rounded-lg bg-teal-600/20 text-teal-400 hover:bg-teal-600/30 transition-colors font-medium min-w-[90px]">
          {{ relativeTimeText }}
        </button>
        <button @click="goNext"
          class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-white/10 transition-colors text-gray-300">
          <i class="pi pi-chevron-right"></i>
        </button>
      </div>
      <span class="text-lg font-semibold text-white">{{ headerTitle }}</span>
      <button @click="openCreateDialog"
        class="px-4 py-1.5 bg-teal-600 text-white rounded-lg hover:bg-teal-700 transition-colors text-sm font-medium flex items-center gap-1.5">
        <i class="pi pi-plus"></i>
        Thêm sự kiện
      </button>
    </div>

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

    <!-- Modal Xóa Sự Kiện -->
    <Teleport to="body">
      <div v-if="showDeleteEventDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showDeleteEventDialog = false"></div>

        <!-- Content -->
        <div class="relative bg-zinc-900 rounded-xl border border-red-500/20 w-full max-w-sm mx-4 p-5 shadow-2xl">
          <h3 class="text-lg font-semibold text-white mb-2 flex items-center gap-2">
            <span class="text-red-500"><i class="pi pi-trash"></i></span>
            Xóa Sự Kiện
          </h3>

          <p class="text-sm text-gray-400 mb-6">
            Bạn có chắc chắn muốn xóa sự kiện "<span class="text-gray-200 font-medium">{{ eventToDelete?.title
              }}</span>" không?
            Hành động này không thể hoàn tác.
          </p>

          <div class="flex justify-end gap-2">
            <button type="button" @click="showDeleteEventDialog = false"
              class="px-4 py-2 rounded-lg text-sm text-gray-300 hover:bg-white/10 transition-colors"
              :disabled="isDeletingEvent">
              Hủy
            </button>
            <button @click="executeDelete"
              class="px-4 py-2 rounded-lg text-sm font-medium bg-red-600 text-white hover:bg-red-700 transition-colors disabled:opacity-50 flex items-center gap-2"
              :disabled="isDeletingEvent">
              <span v-if="isDeletingEvent"
                class="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin"></span>
              Xoá sự kiện
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
/* Reset some styles if needed */
</style>
