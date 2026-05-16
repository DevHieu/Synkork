<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";

const props = defineProps<{
  currentDate: dayjs.Dayjs;
  selectedDate: dayjs.Dayjs;
  events: CalendarEvent[];
  dayNames: string[];
  isToday: (date: dayjs.Dayjs) => boolean;
  isSelected: (date: dayjs.Dayjs) => boolean;
}>();

const emit = defineEmits<{
  (e: "selectDate", date: dayjs.Dayjs): void;
  (e: "editEvent", event: CalendarEvent): void;
}>();

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
</script>
ư
<template>
  <div class="flex-1 overflow-y-auto p-4 calendar-scrollbar bg-background text-foreground">
    <div class="grid grid-cols-7 border-2 border-border bg-background">
      <div
        v-for="(date, idx) in weekDays"
        :key="idx"
        :class="['flex flex-col h-full min-h-[400px] border-r-2 border-border last:border-r-0']"
      >
        <!-- Day Header -->
        <div
          @click="emit('selectDate', date)"
          :class="[
            'text-center p-3 border-b-2 border-border cursor-pointer transition-colors',
            isToday(date) ? 'bg-primary text-primary-foreground' : 'bg-muted/30 hover:bg-muted/80',
            isSelected(date) ? 'ring-inset ring-2 ring-primary bg-primary/5' : '',
          ]"
        >
          <div :class="['text-[10px] font-mono font-bold uppercase tracking-widest', isToday(date) ? 'text-primary-foreground/80' : 'text-muted-foreground']">
            {{ dayNames[date.day()] }}
          </div>
          <div
            :class="[
              'text-xl font-mono font-bold mt-1',
              isToday(date) ? 'text-primary-foreground' : 'text-foreground',
            ]"
          >
            {{ date.date() }}
          </div>
        </div>

        <!-- Events List -->
        <div class="flex-1 bg-background p-2 space-y-2">
          <div
            v-for="event in getEventsForDate(date)"
            :key="event.id"
            @click="emit('editEvent', event)"
            class="bg-muted/50 border-2 border-border p-2 cursor-pointer hover:border-primary hover:translate-x-0.5 hover:-translate-y-0.5 transition-all text-foreground"
            style="box-shadow: 2px 2px 0px 0px var(--color-primary);"
          >
            <p class="text-xs font-mono font-bold truncate uppercase text-primary">
              {{ event.title }}
            </p>
            <p class="text-[10px] font-mono font-bold mt-1">
              {{ event.startTime.substring(0, 5) }}
            </p>
          </div>
          <div
            v-if="getEventsForDate(date).length === 0"
            class="text-center font-mono text-muted-foreground text-[10px] uppercase mt-4"
          >
            KHÔNG SỰ KIỆN
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
