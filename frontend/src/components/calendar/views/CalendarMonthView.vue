<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

const props = defineProps<{
  currentDate: dayjs.Dayjs;
  selectedDate: dayjs.Dayjs;
  events: CalendarEvent[];
  currentUserId: string;
  dayNames: string[];
  isToday: (date: dayjs.Dayjs) => boolean;
  isSelected: (date: dayjs.Dayjs) => boolean;
}>();

const emit = defineEmits<{
  (e: "selectDate", date: dayjs.Dayjs): void;
  (e: "viewEvent", event: CalendarEvent): void;
}>();

/**
 * Tính toán các ngày hiển thị trong tháng bao gồm cả ngày đệm từ tháng trước và sau (tổng 6 tuần)
 */
const monthDays = computed(() => {
  const startOfMonth = props.currentDate.startOf("month");
  const endOfMonth = props.currentDate.endOf("month");
  const startDay = startOfMonth.day();
  const days: dayjs.Dayjs[] = [];

  // Đệm từ tháng trước
  for (let i = startDay - 1; i >= 0; i--) {
    days.push(startOfMonth.subtract(i + 1, "day"));
  }
  // Các ngày trong tháng hiện tại
  for (let d = startOfMonth; d.isBefore(endOfMonth) || d.isSame(endOfMonth, "day"); d = d.add(1, "day")) {
    days.push(d);
  }
  // Đệm từ tháng sau để đủ 42 ô
  while (days.length < 42) {
    days.push(endOfMonth.add(days.length - endOfMonth.date() - startDay + 1, "day"));
  }
  return days;
});

/**
 * Memoize: nhóm sự kiện theo ngày, sắp xếp mỗi nhóm theo startTime.
 * Template chỉ cần tra cứu O(1) thay vì lặp toàn bộ mảng events.
 */
const eventsByDate = computed(() => {
  const map: Record<string, CalendarEvent[]> = {};
  for (const event of props.events) {
    const d = event.displayDate || event.eventDate;
    if (!map[d]) map[d] = [];
    map[d].push(event);
  }
  for (const key in map) {
    map[key].sort((a, b) => a.startTime.localeCompare(b.startTime));
  }
  return map;
});

const getEventsForDate = (date: dayjs.Dayjs): CalendarEvent[] =>
  eventsByDate.value[date.format("YYYY-MM-DD")] || [];

const selectedDateEvents = computed(() => getEventsForDate(props.selectedDate));

const isCurrentMonth = (date: dayjs.Dayjs) => date.month() === props.currentDate.month();

const getCreatorLabel = (event: CalendarEvent) => {
  if (event.createdById === props.currentUserId) {
    return "Do bạn tạo";
  }
  return event.createdByDisplayName || event.createdByUsername;
};

const getDisplayStartTime = (event: CalendarEvent) =>
  (event.displayStartTime || event.startTime).substring(0, 5);

const getDisplayEndTime = (event: CalendarEvent) =>
  (event.displayEndTime || event.endTime).substring(0, 5);

const formatDateTimeLabel = (value: string | undefined, fallbackDate: string, fallbackTime: string) => {
  const dateTime = value ? dayjs(value) : dayjs(`${fallbackDate}T${fallbackTime}`);
  if (!dateTime.isValid()) return fallbackTime.substring(0, 5);
  return `${dateTime.format("HH:mm")} ${dateTime.format("DD/MM")}`;
};

const getOriginalStartLabel = (event: CalendarEvent) =>
  formatDateTimeLabel(event.originalStartDateTime, event.eventDate, event.startTime);

const getOriginalEndLabel = (event: CalendarEvent) =>
  formatDateTimeLabel(event.originalEndDateTime, event.endDate || event.eventDate, event.endTime);

const getContinuationLabel = (event: CalendarEvent) => {
  if (event.continuesFromPreviousDay && event.continuesToNextDay) {
    return "BẮT ĐẦU TỪ NGÀY HÔM TRƯỚC VÀ TIẾP TỤC";
  }
  if (event.continuesFromPreviousDay) return "BẮT ĐẦU TỪ NGÀY HÔM TRƯỚC";
  if (event.continuesToNextDay) return "TIẾP TỤC Ở NGÀY HÔM SAU";
  return "";
};
</script>

<template>
  <div class="flex flex-1 flex-col overflow-hidden bg-transparent text-foreground cursor-default md:flex-row">
    <!-- Lưới lịch -->
    <div class="flex flex-1 flex-col overflow-hidden p-4 md:pr-0">
      <div class="flex flex-1 flex-col overflow-hidden rounded-[1.5rem] border-2 border-border bg-background shadow-[0_30px_80px_-48px_var(--color-foreground)]">
        <!-- Tiêu đề các thứ -->
        <div class="grid grid-cols-7 border-b-2 border-border bg-muted/55 text-muted-foreground cursor-default">
          <div
            v-for="day in dayNames"
            :key="day"
            class="text-center text-[10px] sm:text-xs font-mono font-bold uppercase tracking-widest py-2 border-r-2 last:border-r-0 border-border cursor-default"
          >
            {{ day }}
          </div>
        </div>

        <!-- Lưới các ngày -->
        <div class="grid flex-1 grid-cols-7 bg-border/30">
          <div
            v-for="(date, idx) in monthDays"
            :key="idx"
            @click="emit('selectDate', date)"
            :class="[
              'relative flex flex-col items-start border-b-2 border-r-2 border-border bg-background p-2 transition-all hover:bg-muted/35 cursor-pointer',
              (idx + 1) % 7 === 0 ? 'border-r-0' : '',
              idx >= monthDays.length - 7 ? 'border-b-0' : '',
              isSelected(date) ? 'bg-primary/8 ring-2 ring-inset ring-primary' : '',
              !isCurrentMonth(date) ? 'opacity-40 bg-muted/20' : '',
            ]"
          >
            <div class="flex justify-between w-full items-start">
              <span
                :class="[
                  'flex min-w-[2rem] items-center justify-center rounded-full px-2 py-1 text-xs font-mono font-bold',
                  isToday(date)
                    ? 'bg-primary text-primary-foreground shadow-[0_8px_20px_-14px_var(--color-primary)]'
                    : 'text-foreground',
                ]"
              >
                {{ date.date() }}
              </span>
            </div>
            <!-- Chấm chỉ báo sự kiện -->
            <div v-if="getEventsForDate(date).length" class="flex gap-1 mt-auto pt-2 flex-wrap w-full">
              <span
                v-for="n in Math.min(getEventsForDate(date).length, 3)"
                :key="n"
                class="h-1.5 w-full rounded-full border border-primary/20 bg-primary"
              ></span>
              <span v-if="getEventsForDate(date).length > 3" class="text-[9px] font-mono font-bold text-primary mt-0.5 ml-0.5">
                +{{ getEventsForDate(date).length - 3 }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Danh sách sự kiện ngày đã chọn (Bên phải) -->
    <div class="mt-4 flex w-full flex-col overflow-hidden rounded-[1.5rem] border-2 border-border bg-background shadow-[0_30px_80px_-48px_var(--color-foreground)] md:ml-4 md:mt-0 md:w-96">
      <div class="flex items-end justify-between border-b-2 border-border bg-muted/55 px-5 py-4 cursor-default">
        <div>
          <h3 class="font-mono font-bold text-xl tracking-widest uppercase leading-none text-primary">
            {{ selectedDate.format("DD/MM/YYYY") }}
          </h3>
          <p class="text-[10px] font-mono font-bold opacity-80 mt-2 uppercase tracking-widest text-muted-foreground">
            [{{ selectedDateEvents.length }} SỰ KIỆN]
          </p>
        </div>
      </div>
      <ScrollArea class="calendar-scroll-area min-h-0 flex-1">
        <div class="flex flex-col gap-4 p-4 pr-5">
          <div
            v-if="selectedDateEvents.length === 0"
            class="mt-8 rounded-xl border-2 border-dashed border-muted-foreground/50 bg-muted/20 p-8 text-center font-mono text-sm uppercase tracking-widest text-muted-foreground cursor-default"
          >
            KHÔNG CÓ SỰ KIỆN
          </div>
          <div
            v-for="event in selectedDateEvents"
            :key="event.id"
            class="group cursor-pointer rounded-xl border-2 border-border bg-background p-0 text-foreground shadow-[0_20px_42px_-32px_var(--color-primary)] transition-all duration-200 hover:-translate-y-1 hover:border-primary"
            @click="emit('viewEvent', event)"
          >
            <!-- Header Event -->
            <div class="flex items-center justify-between rounded-t-xl border-b-2 border-border bg-muted/40 px-4 py-3 transition-colors group-hover:bg-primary/5">
              <div class="flex items-center gap-2">
                <div class="h-2.5 w-2.5 rounded-full bg-primary"></div>
                <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest group-hover:text-primary transition-colors">ID: {{ event.id?.substring(0, 6) || 'SYS' }}</span>
              </div>
              <span class="text-[10px] font-mono font-bold text-primary uppercase tracking-widest">XEM CHI TIẾT</span>
            </div>
            
            <div class="space-y-4 p-4">
              <!-- Tiêu đề -->
              <div>
                <h4 class="font-mono font-bold text-foreground text-base leading-tight uppercase">
                  {{ event.title }}
                </h4>
              </div>

              <!-- Thời gian -->
              <div class="rounded-xl border border-primary/20 bg-primary/5 px-4 py-3">
                <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-wider">THỜI GIAN</span>
                <div class="font-mono text-sm font-bold mt-1 text-primary">
                  <span class="group-hover:hidden">{{ getDisplayStartTime(event) }} &rarr; {{ getDisplayEndTime(event) }}</span>
                  <span class="hidden group-hover:inline">{{ getOriginalStartLabel(event) }} &rarr; {{ getOriginalEndLabel(event) }}</span>
                </div>
                <div v-if="getContinuationLabel(event)" class="mt-2 text-[9px] font-mono font-bold uppercase tracking-widest text-muted-foreground">
                  {{ getContinuationLabel(event) }}
                </div>
              </div>

              <!-- Mô tả -->
              <div v-if="event.description" class="border-t-2 border-dashed border-border pt-3">
                <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-wider">CHI TIẾT</span>
                <p class="mt-2 rounded-lg border border-border bg-muted/30 p-3 text-xs font-mono leading-relaxed text-muted-foreground">
                  {{ event.description }}
                </p>
              </div>

              <!-- Người tạo -->
              <div class="pt-3 border-t-2 border-border flex items-center justify-between">
                <div class="flex flex-col">
                  <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-wider">NGƯỜI TẠO</span>
                  <div class="flex items-center gap-2 mt-1">
                    <Avatar class="size-6 border border-border">
                      <AvatarImage
                        v-if="event.createdByAvatarUrl"
                        :src="event.createdByAvatarUrl"
                        :alt="getCreatorLabel(event)"
                      />
                      <AvatarFallback />
                    </Avatar>
                    <span class="font-mono text-xs font-bold uppercase truncate max-w-[150px]">{{ getCreatorLabel(event) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </ScrollArea>
    </div>
  </div>
</template>

<style scoped>
/* Scoped styles can remain empty, relying on Tailwind */
</style>
