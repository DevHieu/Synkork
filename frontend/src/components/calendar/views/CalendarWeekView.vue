<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";
import { ScrollArea } from "@/components/ui/scroll-area";

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

// View tuần render trực tiếp từ mảng event đã được store đồng bộ sẵn.
</script>
<template>
  <ScrollArea class="calendar-scroll-area min-h-0 flex-1">
    <div class="bg-transparent p-4 text-foreground">
      <div class="grid grid-cols-7 overflow-hidden rounded-[1.5rem] border-2 border-border bg-background shadow-[0_30px_80px_-48px_var(--color-foreground)]">
        <div
          v-for="(date, idx) in weekDays"
          :key="idx"
          :class="['flex flex-col h-full min-h-[400px] border-r-2 border-border last:border-r-0']"
        >
          <!-- Day Header -->
          <div
            @click="emit('selectDate', date)"
            :class="[
              'cursor-pointer border-b-2 border-border p-3 text-center transition-colors',
              isToday(date) ? 'bg-primary text-primary-foreground' : 'bg-muted/30 hover:bg-muted/70',
              isSelected(date) ? 'bg-primary/5 ring-2 ring-inset ring-primary' : '',
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
          <div class="flex flex-1 flex-col gap-2 bg-background p-3">
            <div
              v-for="event in getEventsForDate(date)"
              :key="event.id"
              @click="emit('editEvent', event)"
              class="cursor-pointer rounded-xl border-2 border-border bg-muted/35 p-3 text-foreground shadow-[0_16px_28px_-28px_var(--color-primary)] transition-all hover:-translate-y-0.5 hover:border-primary hover:bg-background"
            >
              <p class="text-xs font-mono font-bold truncate uppercase text-primary">
                {{ event.title }}
              </p>
              <p class="mt-2 rounded-full bg-primary/10 px-2 py-1 text-[10px] font-mono font-bold text-primary w-fit">
                {{ event.startTime.substring(0, 5) }}
              </p>
            </div>
            <div
              v-if="getEventsForDate(date).length === 0"
              class="mt-4 rounded-lg border border-dashed border-border bg-muted/20 px-3 py-4 text-center font-mono text-[10px] uppercase text-muted-foreground"
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
