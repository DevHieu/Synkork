<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

import { toast } from "vue-sonner";

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
  (e: "deleteAllEvents", events: CalendarEvent[]): void;
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
    map[key]?.sort((a, b) => a.startTime.localeCompare(b.startTime));
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

/**
 * Nhóm các sự kiện liên tục theo scheduleId để tính toán ngày bắt đầu (min) và ngày kết thúc (max) của nhóm.
 */
const scheduleDateRanges = computed(() => {
  const map: Record<string, { startDate: string; endDate: string }> = {};
  for (const event of props.events) {
    if (event.schedule && event.scheduleId) {
      const sId = event.scheduleId;
      const curDate = event.eventDate;
      const curEnd = event.endDate || event.eventDate;
      if (!map[sId]) {
        map[sId] = { startDate: curDate, endDate: curEnd };
      } else {
        if (dayjs(curDate).isBefore(dayjs(map[sId].startDate))) {
          map[sId].startDate = curDate;
        }
        if (dayjs(curEnd).isAfter(dayjs(map[sId].endDate))) {
          map[sId].endDate = curEnd;
        }
        if (dayjs(curDate).isAfter(dayjs(map[sId].endDate))) {
          map[sId].endDate = curDate;
        }
      }
    }
  }
  return map;
});

const formatDateTimeLabel = (value: string | undefined, fallbackDate: string, fallbackTime: string) => {
  const dateTime = value ? dayjs(value) : dayjs(`${fallbackDate}T${fallbackTime}`);
  if (!dateTime.isValid()) return fallbackTime.substring(0, 5);
  return `${dateTime.format("HH:mm")} ${dateTime.format("DD/MM")}`;
};

const getScheduleRange = (event: CalendarEvent) => {
  if (event.schedule && event.scheduleId) {
    return scheduleDateRanges.value[event.scheduleId];
  }
};

const getOriginalStartLabel = (event: CalendarEvent) => {
  let startDate = event.eventDate;
  const range = getScheduleRange(event);
  if (range && dayjs(range.startDate).isBefore(dayjs(startDate))) {
    startDate = range.startDate;
  }
  return formatDateTimeLabel(event.originalStartDateTime, startDate, event.startTime);
};

const getOriginalEndLabel = (event: CalendarEvent) => {
  let endDate = event.endDate || event.eventDate;
  const range = getScheduleRange(event);
  if (range && dayjs(range.endDate).isAfter(dayjs(endDate))) {
    endDate = range.endDate;
  }
  return formatDateTimeLabel(event.originalEndDateTime, endDate, event.endTime);
};  

const getContinuationLabel = (event: CalendarEvent) => {
  if (event.continuesFromPreviousDay && event.continuesToNextDay) {
    return "BẮT ĐẦU TỪ NGÀY HÔM TRƯỚC VÀ TIẾP TỤC";
  }
  if (event.continuesFromPreviousDay) return "BẮT ĐẦU TỪ NGÀY HÔM TRƯỚC";
  if (event.continuesToNextDay) return "TIẾP TỤC Ở NGÀY HÔM SAU";
  return "";
};

const handleDeleteAllForDate = () => {
  const eventsToDelete = selectedDateEvents.value.filter(e => e.createdById === props.currentUserId);
  
  if (eventsToDelete.length === 0) {
    toast.error("Bạn không có quyền xóa các sự kiện trong ngày này!");
    return;
  }
  
  emit("deleteAllEvents", eventsToDelete);
};
</script>

<template>
  <div class="flex flex-1 flex-col overflow-hidden bg-transparent text-foreground cursor-default md:flex-row">
    <!-- Lưới lịch -->
    <div class="flex flex-1 flex-col overflow-hidden p-4 md:pr-2">
      <div class="flex flex-1 flex-col overflow-hidden rounded-lg border border-border/60 bg-background shadow-sm">
        <!-- Tiêu đề các thứ -->
        <div class="grid grid-cols-7 border-b border-border/60 bg-muted/40 text-muted-foreground cursor-default">
          <div
            v-for="day in dayNames"
            :key="day"
            class="text-center text-xs font-sans font-semibold uppercase tracking-wider py-2.5 border-r border-border/60 last:border-r-0 cursor-default"
          >
            {{ day }}
          </div>
        </div>

        <!-- Lưới các ngày -->
        <div class="grid flex-1 grid-cols-7 bg-border/20">
          <div
            v-for="(date, idx) in monthDays"
            :key="idx"
            @click="emit('selectDate', date)"
            :class="[
              'relative flex flex-col items-start border-b border-r border-border/60 bg-background p-2 transition-all hover:bg-muted/40 cursor-pointer',
              (idx + 1) % 7 === 0 ? 'border-r-0' : '',
              idx >= monthDays.length - 7 ? 'border-b-0' : '',
              isSelected(date) ? 'bg-primary/5 ring-1 ring-inset ring-primary/45' : '',
              isToday(date) ? 'bg-muted/15' : '',
              !isCurrentMonth(date) ? 'opacity-35 bg-muted/10' : '',
            ]"
          >
            <div class="flex justify-between w-full items-start mb-1.5">
              <span
                :class="[
                  'flex h-6 w-6 items-center justify-center rounded-full text-xs font-sans font-semibold transition-colors',
                  isToday(date)
                    ? 'bg-primary text-primary-foreground shadow-sm'
                    : 'text-foreground',
                ]"
              >
                {{ date.date() }}
              </span>
            </div>

            <!-- Thanh hiển thị sự kiện dạng banner -->
            <div v-if="getEventsForDate(date).length" class="flex flex-col gap-1 w-full mt-auto">
              <div
                v-for="event in getEventsForDate(date).slice(0, 2)"
                :key="event.id"
                @click.stop="emit('viewEvent', event)"
                :class="[
                  'w-full truncate rounded-sm border-l-2 px-1.5 py-0.5 text-[10px] font-sans font-medium transition-all',
                  event.schedule
                    ? 'border-amber-500 bg-amber-500/10 text-amber-700 dark:text-amber-400 hover:bg-amber-500/15'
                    : 'border-primary bg-primary/10 text-primary hover:bg-primary/15',
                ]"
                :title="event.title"
              >
                {{ event.title }}
              </div>
              <div v-if="getEventsForDate(date).length > 2" class="text-[9px] font-sans text-muted-foreground/80 pl-1.5">
                +{{ getEventsForDate(date).length - 2 }} sự kiện
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Danh sách sự kiện ngày đã chọn (Bên phải) -->
    <div class="flex w-full flex-col overflow-hidden p-4 pt-0 md:pt-4 md:pl-2 md:w-96 shrink-0">
      <div class="flex h-full flex-col overflow-hidden rounded-lg border border-border/60 bg-background shadow-sm">
        <div class="flex items-center justify-between border-b border-border/60 bg-muted/40 px-4 py-3 cursor-default">
          <div>
            <h3 class="font-sans font-bold text-base tracking-wide text-foreground">
              {{ selectedDate.format("DD/MM/YYYY") }}
            </h3>
            <p class="text-[10px] font-sans font-medium opacity-80 mt-1 uppercase tracking-wider text-muted-foreground">
              {{ selectedDateEvents.length }} SỰ KIỆN
            </p>
          </div>
          <button
            v-if="selectedDateEvents.length > 0"
            @click="handleDeleteAllForDate"
            class="text-[11px] font-semibold px-2.5 py-1.5 bg-destructive text-white rounded-sm hover:bg-destructive/90 transition-colors shadow-sm"
          >
            Xóa TOÀN BỘ SỰ KIỆN Ở ngày này
          </button>
        </div>
        <ScrollArea class="calendar-scroll-area min-h-0 flex-1">
          <div class="flex flex-col gap-3.5 p-4 pr-5">
            <div
              v-if="selectedDateEvents.length === 0"
              class="mt-8 rounded-lg border border-dashed border-border bg-muted/10 p-6 text-center font-sans text-xs uppercase tracking-wider text-muted-foreground cursor-default"
            >
              KHÔNG CÓ SỰ KIỆN
            </div>
            <div
              v-for="event in selectedDateEvents"
              :key="event.id"
              class="group cursor-pointer rounded-lg border border-border/60 bg-card p-0 text-foreground shadow-sm transition-all duration-200 hover:border-primary/80"
              @click="emit('viewEvent', event)"
            >
              <!-- Header Event -->
              <div class="flex items-center justify-between rounded-t-lg border-b border-border/60 bg-muted/30 px-3.5 py-2.5 transition-colors group-hover:bg-primary/5">
                <div class="flex items-center gap-2">
                  <div :class="['h-2 w-2 rounded-full', event.schedule ? 'bg-amber-500' : 'bg-primary']"></div>
                  <span class="text-[10px] font-sans font-semibold text-muted-foreground uppercase tracking-wider group-hover:text-primary transition-colors">ID: {{ event.id?.substring(0, 6) || 'SYS' }}</span>
                  <!-- schedule label -->
                  <!-- <span v-if="event.schedule" class="text-[9px] font-sans font-bold uppercase tracking-wider text-amber-600 dark:text-amber-400">LIÊN TỤC</span> -->
                </div>
                <span class="text-[10px] font-sans font-semibold text-primary uppercase tracking-wider">XEM CHI TIẾT</span>
              </div>

              <div class="space-y-3 p-3.5">
                <!-- Tiêu đề -->
                <div>
                  <h4 class="font-sans font-semibold text-foreground text-sm leading-snug truncate" :title="event.title">
                    {{ event.title }}
                  </h4>
                </div>

                <!-- Thời gian -->
                <div class="rounded-md border border-primary/10 bg-primary/5 px-3 py-2">
                  <span class="text-[9px] font-sans font-semibold text-muted-foreground/80 uppercase tracking-wider">THỜI GIAN</span>
                  <div class="font-sans text-xs font-bold mt-0.5 text-primary">
                    <span class="group-hover:hidden">{{ getDisplayStartTime(event) }} &rarr; {{ getDisplayEndTime(event) }}</span>
                    <span class="hidden group-hover:inline">{{ getOriginalStartLabel(event) }} &rarr; {{ getOriginalEndLabel(event) }}</span>
                  </div>
                  <div v-if="getContinuationLabel(event)" class="mt-1 text-[9px] font-sans font-semibold uppercase tracking-wider text-muted-foreground">
                    {{ getContinuationLabel(event) }}
                  </div>
                </div>

                <!-- Mô tả -->
                <div v-if="event.description" class="border-t border-dashed border-border/60 pt-2.5">
                  <span class="text-[9px] font-sans font-semibold text-muted-foreground/80 uppercase tracking-wider">CHI TIẾT</span>
                  <p class="mt-1.5 rounded-md border border-border/60 bg-muted/20 p-2.5 text-xs font-sans leading-normal text-muted-foreground truncate" :title="event.description">
                    {{ event.description }}
                  </p>
                </div>

                <!-- Người tạo -->
                <div class="pt-2.5 border-t border-border/60 flex items-center justify-between">
                  <div class="flex flex-col">
                    <span class="text-[9px] font-sans font-semibold text-muted-foreground/80 uppercase tracking-wider">NGƯỜI TẠO</span>
                    <div class="flex items-center gap-2 mt-1">
                      <Avatar class="size-6 border border-border">
                        <AvatarImage
                          v-if="event.createdByAvatarUrl"
                          :src="event.createdByAvatarUrl"
                          :alt="getCreatorLabel(event)"
                        />
                        <AvatarFallback />
                      </Avatar>
                      <span class="font-sans text-xs font-medium uppercase truncate max-w-37.5">{{ getCreatorLabel(event) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </ScrollArea>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Scoped styles can remain empty, relying on Tailwind */
</style>
