<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";

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
  (e: "editEvent", event: CalendarEvent): void;
  (e: "deleteEvent", event: CalendarEvent): void;
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
 * Lọc và sắp xếp sự kiện cho một ngày cụ thể
 */
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

// Các sự kiện cho ngày hiện đang được chọn (hiển thị ở thanh bên)
const selectedDateEvents = computed(() => {
  return getEventsForDate(props.selectedDate);
});

const hasEvent = (date: dayjs.Dayjs) => {
  const targetDate = date.format("YYYY-MM-DD");
  for (let i = 0; i < props.events.length; i++) {
    const event = props.events[i];
    if (event && event.eventDate === targetDate) return true;
  }
  return false;
};

const isCurrentMonth = (date: dayjs.Dayjs) => date.month() === props.currentDate.month();

// Kiểm tra quyền hạn
const canEdit = (event: CalendarEvent) => {
  return event.createdById === props.currentUserId || event.allowEditAll;
};

const canDelete = (event: CalendarEvent) => {
  return event.createdById === props.currentUserId;
};
</script>

<template>
  <div class="flex-1 flex flex-col md:flex-row overflow-hidden bg-background text-foreground">
    <!-- Lưới lịch -->
    <div class="flex-1 flex flex-col overflow-hidden p-4 md:pr-0">
      <div class="border-2 border-border flex-1 flex flex-col bg-background">
        <!-- Tiêu đề các thứ -->
        <div class="grid grid-cols-7 border-b-2 border-border bg-muted text-muted-foreground">
          <div
            v-for="day in dayNames"
            :key="day"
            class="text-center text-[10px] sm:text-xs font-mono font-bold uppercase tracking-widest py-2 border-r-2 last:border-r-0 border-border"
          >
            {{ day }}
          </div>
        </div>

        <!-- Lưới các ngày -->
        <div class="grid grid-cols-7 flex-1">
          <div
            v-for="(date, idx) in monthDays"
            :key="idx"
            @click="emit('selectDate', date)"
            :class="[
              'relative p-2 cursor-pointer transition-all flex flex-col items-start border-b-2 border-r-2 border-border hover:bg-muted/50',
              (idx + 1) % 7 === 0 ? 'border-r-0' : '',
              idx >= monthDays.length - 7 ? 'border-b-0' : '',
              isSelected(date) ? 'bg-primary/5 ring-inset ring-2 ring-primary' : '',
              !isCurrentMonth(date) ? 'opacity-40 bg-muted/20' : '',
            ]"
          >
            <div class="flex justify-between w-full items-start">
              <span
                :class="[
                  'text-xs font-mono font-bold flex items-center justify-center p-1 min-w-[24px]',
                  isToday(date)
                    ? 'bg-primary text-primary-foreground'
                    : 'text-foreground',
                ]"
              >
                {{ date.date() }}
              </span>
            </div>
            <!-- Chấm chỉ báo sự kiện -->
            <div v-if="hasEvent(date)" class="flex gap-1 mt-auto pt-2 flex-wrap w-full">
              <span
                v-for="n in Math.min(getEventsForDate(date).length, 3)"
                :key="n"
                class="w-full h-1.5 bg-primary border border-primary/20"
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
    <div class="w-full md:w-96 border-l-2 border-border flex flex-col overflow-hidden bg-background md:ml-4">
      <div class="px-5 py-4 border-b-2 border-border bg-muted flex justify-between items-end">
        <div>
          <h3 class="font-mono font-bold text-xl tracking-widest uppercase leading-none text-primary">
            {{ selectedDate.format("DD/MM/YYYY") }}
          </h3>
          <p class="text-[10px] font-mono font-bold opacity-80 mt-2 uppercase tracking-widest text-muted-foreground">
            [{{ selectedDateEvents.length }} SỰ KIỆN]
          </p>
        </div>
      </div>
      <div class="flex-1 overflow-y-auto p-4 space-y-4 calendar-scrollbar">
        <div
          v-if="selectedDateEvents.length === 0"
          class="text-center font-mono text-sm uppercase tracking-widest text-muted-foreground mt-8 border-2 border-dashed border-muted-foreground p-8"
        >
          KHÔNG CÓ SỰ KIỆN
        </div>
        <div
          v-for="event in selectedDateEvents"
          :key="event.id"
          class="group bg-background border-2 border-border p-0 hover:border-primary hover:translate-x-1 hover:-translate-y-1 transition-all duration-200 text-foreground"
          style="box-shadow: 4px 4px 0px 0px var(--color-primary);"
        >
          <!-- Header Event -->
          <div class="px-4 py-2 border-b-2 border-border bg-muted/50 flex justify-between items-center group-hover:bg-primary/5 transition-colors">
            <div class="flex items-center gap-2">
              <div class="w-2 h-2 bg-primary"></div>
              <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest group-hover:text-primary transition-colors">ID: {{ event.id?.substring(0, 6) || 'SYS' }}</span>
            </div>
            <!-- Actions -->
            <div class="flex gap-2">
              <button
                v-if="canEdit(event)"
                @click.stop="emit('editEvent', event)"
                class="text-foreground hover:text-muted-foreground transition-colors"
                title="Chỉnh sửa"
              >
                <i class="pi pi-pencil text-xs"></i>
              </button>
              <button
                v-if="canDelete(event)"
                @click.stop="emit('deleteEvent', event)"
                class="text-foreground hover:text-destructive transition-colors"
                title="Xóa"
              >
                <i class="pi pi-trash text-xs"></i>
              </button>
            </div>
          </div>
          
          <div class="p-4 space-y-4">
            <!-- Tiêu đề -->
            <div>
              <h4 class="font-mono font-bold text-foreground text-base leading-tight uppercase">
                {{ event.title }}
              </h4>
            </div>

            <!-- Thời gian -->
            <div class="flex flex-col border-l-4 border-primary pl-3">
              <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-wider">THỜI GIAN</span>
              <div class="font-mono text-sm font-bold mt-1 text-primary">
                {{ event.startTime.substring(0, 5) }} &rarr; {{ event.endTime.substring(0, 5) }}
              </div>
            </div>

            <!-- Mô tả -->
            <div v-if="event.description" class="border-t-2 border-dashed border-border pt-3">
              <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-wider">CHI TIẾT</span>
              <p class="text-xs font-mono text-muted-foreground mt-1 leading-relaxed bg-muted/30 p-3 border-l-2 border-border">
                {{ event.description }}
              </p>
            </div>

            <!-- Người tạo -->
            <div class="pt-3 border-t-2 border-border flex items-center justify-between">
              <div class="flex flex-col">
                <span class="text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-wider">NGƯỜI TẠO</span>
                <div class="flex items-center gap-2 mt-1">
                  <div class="w-5 h-5 bg-muted flex items-center justify-center border border-border">
                    <i class="pi pi-user text-[10px] text-muted-foreground"></i>
                  </div>
                  <span class="font-mono text-xs font-bold uppercase truncate max-w-[150px]">{{ event.createdByDisplayName }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Scoped styles can remain empty, relying on Tailwind */
</style>
