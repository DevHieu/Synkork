<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/composables/useCalendar";

const props = defineProps<{
  currentDate: dayjs.Dayjs;
  selectedDate: dayjs.Dayjs;
  events: CalendarEvent[];
  currentUserId: string;
}>();

const emit = defineEmits<{
  (e: "selectDate", date: dayjs.Dayjs): void;
  (e: "editEvent", event: CalendarEvent): void;
  (e: "deleteEvent", event: CalendarEvent): void;
}>();

const dayNames = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

const monthDays = computed(() => {
  const startOfMonth = props.currentDate.startOf("month");
  const endOfMonth = props.currentDate.endOf("month");
  const startDay = startOfMonth.day();
  const days: dayjs.Dayjs[] = [];

  for (let i = startDay - 1; i >= 0; i--) {
    days.push(startOfMonth.subtract(i + 1, "day"));
  }
  for (let d = startOfMonth; d.isBefore(endOfMonth) || d.isSame(endOfMonth, "day"); d = d.add(1, "day")) {
    days.push(d);
  }
  while (days.length < 42) {
    days.push(endOfMonth.add(days.length - endOfMonth.date() - startDay + 1, "day"));
  }
  return days;
});

const selectedDateEvents = computed(() => {
  return props.events
    .filter((e) => e.eventDate === props.selectedDate.format("YYYY-MM-DD"))
    .sort((a, b) => a.startTime.localeCompare(b.startTime));
});

const hasEvent = (date: dayjs.Dayjs) => {
  return props.events.some((e) => e.eventDate === date.format("YYYY-MM-DD"));
};

const getEventsForDate = (date: dayjs.Dayjs) => {
  return props.events
    .filter((e) => e.eventDate === date.format("YYYY-MM-DD"))
    .sort((a, b) => a.startTime.localeCompare(b.startTime));
};

const isToday = (date: dayjs.Dayjs) => date.isSame(dayjs(), "day");
const isSelected = (date: dayjs.Dayjs) => date.isSame(props.selectedDate, "day");
const isCurrentMonth = (date: dayjs.Dayjs) => date.month() === props.currentDate.month();

const canEdit = (event: CalendarEvent) => {
  return event.createdById === props.currentUserId || event.allowEditAll;
};

const canDelete = (event: CalendarEvent) => {
  return event.createdById === props.currentUserId;
};
</script>

<template>
  <div class="flex-1 flex flex-col md:flex-row overflow-hidden">
    <!-- Calendar Grid -->
    <div class="flex-1 flex flex-col overflow-hidden p-3">
      <!-- Day Headers -->
      <div class="grid grid-cols-7 gap-1 mb-1">
        <div
          v-for="day in dayNames"
          :key="day"
          class="text-center text-xs font-semibold text-gray-400 py-1"
        >
          {{ day }}
        </div>
      </div>

      <!-- Days Grid -->
      <div class="grid grid-cols-7 gap-1 flex-1">
        <div
          v-for="(date, idx) in monthDays"
          :key="idx"
          @click="emit('selectDate', date)"
          :class="[
            'relative rounded-lg p-1 cursor-pointer transition-all duration-200 flex flex-col items-center',
            'hover:bg-white/10',
            isSelected(date) ? 'bg-teal-600/30 ring-1 ring-teal-500' : '',
            isToday(date) ? 'ring-1 ring-teal-400/50' : '',
            !isCurrentMonth(date) ? 'opacity-30' : '',
          ]"
        >
          <span
            :class="[
              'text-sm font-medium w-7 h-7 flex items-center justify-center rounded-full',
              isToday(date)
                ? 'bg-teal-500 text-white'
                : 'text-gray-200',
            ]"
          >
            {{ date.date() }}
          </span>
          <!-- Event Indicator Dots -->
          <div v-if="hasEvent(date)" class="flex gap-0.5 mt-0.5">
            <span
              v-for="n in Math.min(getEventsForDate(date).length, 3)"
              :key="n"
              class="w-1.5 h-1.5 rounded-full bg-teal-400"
            ></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Selected Day Event List (Right Panel) -->
    <div class="w-full md:w-80 border-t md:border-t-0 md:border-l border-white/10 flex flex-col overflow-hidden">
      <div class="px-4 py-3 border-b border-white/10">
        <h3 class="font-semibold text-white">
          {{ selectedDate.format("DD/MM/YYYY") }}
        </h3>
        <p class="text-xs text-gray-400">
          {{ selectedDateEvents.length }} sự kiện
        </p>
      </div>
      <div class="flex-1 overflow-y-auto px-3 py-2 space-y-2">
        <div
          v-if="selectedDateEvents.length === 0"
          class="text-center text-gray-500 text-sm mt-8"
        >
          Không có sự kiện nào
        </div>
        <div
          v-for="event in selectedDateEvents"
          :key="event.id"
          class="group bg-white/5 rounded-lg p-3 hover:bg-white/10 transition-colors border border-white/5"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <h4 class="font-medium text-white text-sm truncate">
                {{ event.title }}
              </h4>
              <p class="text-xs text-teal-400 mt-1 flex items-center gap-1.5">
                <font-awesome-icon icon="clock" />
                {{ event.startTime.substring(0, 5) }} -
                {{ event.endTime.substring(0, 5) }}
              </p>
              <p
                v-if="event.description"
                class="text-xs text-gray-400 mt-1 line-clamp-2"
              >
                {{ event.description }}
              </p>
              <p class="text-xs text-gray-500 mt-1 flex items-center gap-1.5">
                <font-awesome-icon icon="user" />
                {{ event.createdByDisplayName }}
              </p>
            </div>
            <div class="flex gap-1 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-opacity ml-2">
              <button
                v-if="canEdit(event)"
                @click.stop="emit('editEvent', event)"
                class="p-1 w-6 h-6 flex items-center justify-center rounded hover:bg-white/20 text-gray-300 text-xs"
                title="Chỉnh sửa"
              >
                <font-awesome-icon icon="edit" />
              </button>
              <button
                v-if="canDelete(event)"
                @click.stop="emit('deleteEvent', event)"
                class="p-1 w-6 h-6 flex items-center justify-center rounded hover:bg-red-500/20 text-gray-300 text-xs hover:text-red-400 transition-colors"
                title="Xóa"
              >
                <font-awesome-icon icon="trash" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.1) transparent;
}
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
