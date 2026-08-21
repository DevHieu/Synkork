<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/features/calendar/types/calendar.types";
import { ScrollArea } from "@/components/ui/scroll-area";
import { continuationLabel, displayTime, formatDateTimeLabel } from "@/features/calendar/utils/calendar-display.utils";

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
  (e: "viewEvent", event: CalendarEvent): void;
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
    if (!event) continue;
    if (event.schedule) {
      const singleDate = (event.displayDate || event.eventDate || "").toString().substring(0, 10);
      if (singleDate === targetDate) {
        result.push(event);
      }
      continue;
    }
    const startDate = event.displayDate || event.eventDate;
    const endDate = event.endDate || startDate;
    if (targetDate >= startDate && targetDate <= endDate) {
      result.push(event);
    }
  }

  result.sort((a, b) => a.startTime.localeCompare(b.startTime));
  return result;
};

// View tuần render trực tiếp từ mảng event đã được store đồng bộ sẵn.
const getDisplayStartTime = (event: CalendarEvent) => displayTime(event.displayStartTime || event.startTime);

const getOriginalStartLabel = (event: CalendarEvent) =>
  formatDateTimeLabel(event.originalStartDateTime, event.eventDate, event.startTime);

const getContinuationLabel = (event: CalendarEvent) => continuationLabel(event);
</script>
<template>
  <ScrollArea class="calendar-scroll-area min-h-0 flex-1">
    <div class="bg-transparent p-4 text-foreground">
      <div class="grid grid-cols-7 overflow-hidden rounded-lg border border-border/60 bg-background shadow-sm">
        <div
          v-for="(date, idx) in weekDays"
          :key="idx"
          :class="['flex flex-col h-full min-h-[400px] border-r border-border/60 last:border-r-0']"
        >
          <!-- Day Header -->
          <div
            @click="emit('selectDate', date)"
            :class="[
              'cursor-pointer border-b border-border/60 p-3 text-center transition-colors',
              isToday(date) ? 'bg-primary text-primary-foreground' : 'bg-muted/30 hover:bg-muted/70',
              isSelected(date) ? 'bg-primary/5 ring-1 ring-inset ring-primary/45' : '',
            ]"
          >
            <div :class="['text-[10px] font-sans font-semibold uppercase tracking-wider', isToday(date) ? 'text-primary-foreground/80' : 'text-muted-foreground']">
              {{ dayNames[date.day()] }}
            </div>
            <div
              :class="[
                'text-lg font-sans font-bold mt-1',
                isToday(date) ? 'text-primary-foreground' : 'text-foreground',
              ]"
            >
              {{ date.date() }}
            </div>
          </div>

          <!-- Events List -->
          <div class="flex flex-1 flex-col gap-2 bg-background p-3">
            <div
              v-for="event in getEventsForDate(date)"
              :key="event.id"
              @click="emit('viewEvent', event)"
              class="group cursor-pointer rounded-md border border-border/60 bg-muted/25 p-3 text-foreground shadow-sm transition-all hover:border-primary/80 hover:bg-background"
            >
              <p class="text-xs font-sans font-semibold truncate text-primary">
                {{ event.title }}
              </p>
              <p class="mt-2 rounded-md bg-primary/10 px-2 py-0.5 text-[10px] font-sans font-semibold text-primary w-fit">
                <span class="group-hover:hidden">{{ getDisplayStartTime(event) }}</span>
                <span class="hidden group-hover:inline">{{ getOriginalStartLabel(event) }}</span>
              </p>
              <p v-if="getContinuationLabel(event)" class="mt-2 text-[9px] font-sans font-semibold uppercase tracking-wider text-muted-foreground">
                {{ getContinuationLabel(event) }}
              </p>
            </div>
            <div
              v-if="getEventsForDate(date).length === 0"
              class="mt-4 rounded-md border border-dashed border-border bg-muted/10 px-3 py-4 text-center font-sans text-[10px] uppercase text-muted-foreground"
            >
              KHÔNG SỰ KIỆN
            </div>
          </div>
        </div>
      </div>
    </div>
  </ScrollArea>
</template>

<style scoped>
</style>
