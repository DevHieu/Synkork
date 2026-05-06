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
            isToday(date) ? 'bg-primary/20' : 'bg-muted',
            isSelected(date) ? 'ring-1 ring-primary' : '',
          ]"
        >
          <div class="text-xs text-muted-foreground">{{ dayNames[date.day()] }}</div>
          <div
            :class="[
              'text-lg font-bold',
              isToday(date) ? 'text-primary' : 'text-foreground',
            ]"
          >
            {{ date.date() }}
          </div>
        </div>

        <!-- Events List -->
        <div class="flex-1 bg-muted rounded-b-lg p-1.5 space-y-1">
          <div
            v-for="event in getEventsForDate(date)"
            :key="event.id"
            @click="emit('editEvent', event)"
            class="bg-primary/20 rounded p-1.5 cursor-pointer hover:bg-primary/30 transition-colors border-l-2 border-primary"
          >
            <p class="text-xs font-medium text-foreground truncate">
              {{ event.title }}
            </p>
            <p class="text-xs text-primary">
              {{ event.startTime.substring(0, 5) }}
            </p>
          </div>
          <div
            v-if="getEventsForDate(date).length === 0"
            class="text-center text-muted-foreground text-xs mt-4"
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
  scrollbar-color: var(--border) transparent;
}
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
</style>
