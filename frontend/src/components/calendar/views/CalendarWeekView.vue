<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";

const props = defineProps<{
  currentDate: dayjs.Dayjs;
  selectedDate: dayjs.Dayjs;
  events: CalendarEvent[];
}>();

const emit = defineEmits<{
  (e: "selectDate", date: dayjs.Dayjs): void;
  (e: "editEvent", event: CalendarEvent): void;
}>();

// Tên các thứ trong tuần
const dayNames = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

// Tính toán các ngày trong tuần hiện tại
const weekDays = computed(() => {
  const start = props.currentDate.startOf("week");
  const days: dayjs.Dayjs[] = [];
  for (let i = 0; i < 7; i++) {
    days.push(start.add(i, "day"));
  }
  return days;
});

// Lấy danh sách sự kiện của một ngày
const getEventsForDate = (date: dayjs.Dayjs) => {
  const targetDate = date.format("YYYY-MM-DD");
  const result: CalendarEvent[] = [];
  
  for (let i = 0; i < props.events.length; i++) {
    const event = props.events[i];
    if (event && event.eventDate === targetDate) {
      result.push(event);
    }
  }
  
  result.sort((a, b) => a.startTime.localeCompare(b.startTime));
  return result;
};

// Các hàm kiểm tra trạng thái ngày
const isToday = (date: dayjs.Dayjs) => date.isSame(dayjs(), "day");
const isSelected = (date: dayjs.Dayjs) => date.isSame(props.selectedDate, "day");
</script>

<template>
  <div class="flex-1 overflow-y-auto p-3">
    <div class="grid grid-cols-7 gap-2">
      <div
        v-for="(date, idx) in weekDays"
        :key="idx"
        class="flex flex-col h-full min-h-[400px]"
      >
        <!-- Day Header -->
        <div
          @click="emit('selectDate', date)"
          :class="[
            'text-center p-2 rounded-t-lg cursor-pointer transition-colors',
            isToday(date) ? 'bg-teal-600/30' : 'bg-white/5',
            isSelected(date) ? 'ring-1 ring-teal-500' : '',
          ]"
        >
          <div class="text-xs text-gray-400">{{ dayNames[date.day()] }}</div>
          <div
            :class="[
              'text-lg font-bold',
              isToday(date) ? 'text-teal-400' : 'text-white',
            ]"
          >
            {{ date.date() }}
          </div>
        </div>

        <!-- Events List -->
        <div class="flex-1 bg-white/5 rounded-b-lg p-1.5 space-y-1">
          <div
            v-for="event in getEventsForDate(date)"
            :key="event.id"
            @click="emit('editEvent', event)"
            class="bg-teal-600/20 rounded p-1.5 cursor-pointer hover:bg-teal-600/30 transition-colors border-l-2 border-teal-500"
          >
            <p class="text-xs font-medium text-white truncate">
              {{ event.title }}
            </p>
            <p class="text-xs text-teal-300">
              {{ event.startTime.substring(0, 5) }}
            </p>
          </div>
          <div
            v-if="getEventsForDate(date).length === 0"
            class="text-center text-gray-600 text-xs mt-4"
          >
            —
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
</style>
