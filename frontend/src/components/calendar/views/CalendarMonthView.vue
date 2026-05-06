<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";

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

// Cấu hình
const dayNames = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

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

// Các hàm kiểm tra trạng thái ngày
const isToday = (date: dayjs.Dayjs) => date.isSame(dayjs(), "day");
const isSelected = (date: dayjs.Dayjs) => date.isSame(props.selectedDate, "day");
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
  <div class="flex-1 flex flex-col md:flex-row overflow-hidden">
    <!-- Lưới lịch -->
    <div class="flex-1 flex flex-col overflow-hidden p-3">
      <!-- Tiêu đề các thứ -->
      <div class="grid grid-cols-7 gap-1 mb-1">
        <div
          v-for="day in dayNames"
          :key="day"
          class="text-center text-xs font-semibold text-muted-foreground py-1"
        >
          {{ day }}
        </div>
      </div>

      <!-- Lưới các ngày -->
      <div class="grid grid-cols-7 gap-1 flex-1">
        <div
          v-for="(date, idx) in monthDays"
          :key="idx"
          @click="emit('selectDate', date)"
          :class="[
            'relative rounded-lg p-1 cursor-pointer transition-all duration-200 flex flex-col items-center',
            'hover:bg-muted',
            isSelected(date) ? 'bg-primary/20 ring-1 ring-primary' : '',
            isToday(date) ? 'ring-1 ring-primary/50' : '',
            !isCurrentMonth(date) ? 'opacity-30' : '',
          ]"
        >
          <span
            :class="[
              'text-sm font-medium w-7 h-7 flex items-center justify-center rounded-full',
              isToday(date)
                ? 'bg-primary text-primary-foreground'
                : 'text-foreground',
            ]"
          >
            {{ date.date() }}
          </span>
          <!-- Chấm chỉ báo sự kiện -->
          <div v-if="hasEvent(date)" class="flex gap-0.5 mt-0.5">
            <span
              v-for="n in Math.min(getEventsForDate(date).length, 3)"
              :key="n"
              class="w-1.5 h-1.5 rounded-full bg-primary"
            ></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Danh sách sự kiện ngày đã chọn (Bên phải) -->
    <div class="w-full md:w-80 border-t md:border-t-0 md:border-l border-border flex flex-col overflow-hidden">
      <div class="px-4 py-3 border-b border-border">
        <h3 class="font-semibold text-foreground">
          {{ selectedDate.format("DD/MM/YYYY") }}
        </h3>
        <p class="text-xs text-muted-foreground">
          {{ selectedDateEvents.length }} sự kiện
        </p>
      </div>
      <div class="flex-1 overflow-y-auto px-3 py-2 space-y-2">
        <div
          v-if="selectedDateEvents.length === 0"
          class="text-center text-muted-foreground text-sm mt-8"
        >
          Không có sự kiện nào
        </div>
        <div
          v-for="event in selectedDateEvents"
          :key="event.id"
          class="group bg-card rounded-xl p-4 hover:bg-muted transition-all duration-300 border border-border hover:border-primary/30 shadow-lg"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0 space-y-3">
              <!-- Tiêu đề -->
              <div>
                <div class="flex items-center gap-2 mb-1">
                  <div class="w-1.5 h-1.5 rounded-full bg-primary"></div>
                  <span class="text-[10px] font-bold text-primary/80 uppercase tracking-widest">Tiêu đề</span>
                </div>
                <h4 class="font-semibold text-foreground text-sm leading-tight wrap-break-word">
                  {{ event.title }}
                </h4>
              </div>

              <!-- Thời gian -->
              <div class="flex items-center gap-4">
                <div class="flex flex-col">
                  <span class="text-[10px] font-medium text-muted-foreground uppercase">Thời gian</span>
                  <div class="flex items-center gap-1.5 text-xs text-primary font-medium mt-0.5">
                    <i class="pi pi-clock text-[10px]"></i>
                    {{ event.startTime.substring(0, 5) }} - {{ event.endTime.substring(0, 5) }}
                  </div>
                </div>
              </div>

              <!-- Mô tả -->
              <div v-if="event.description">
                <span class="text-[10px] font-medium text-muted-foreground uppercase">Mô tả</span>
                <p class="text-xs text-foreground mt-1 leading-relaxed line-clamp-3 bg-muted p-2 rounded-lg border border-border italic">
                  {{ event.description }}
                </p>
              </div>

              <!-- Người tạo -->
              <div class="pt-2 border-t border-border flex items-center justify-between">
                <div class="flex flex-col">
                  <span class="text-[10px] font-medium text-muted-foreground uppercase">Người tạo</span>
                  <div class="flex items-center gap-1.5 text-[11px] text-muted-foreground mt-1">
                    <div class="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center border border-primary/20">
                      <i class="pi pi-user text-[10px] text-primary"></i>
                    </div>
                    <span class="truncate max-w-[120px]">{{ event.createdByDisplayName }}</span>
                  </div>
                </div>

                <!-- Hành động (Hiện khi hover trên Desktop) -->
                <div class="flex gap-1 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-all duration-300">
                  <button
                    v-if="canEdit(event)"
                    @click.stop="emit('editEvent', event)"
                    class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-primary/20 text-muted-foreground hover:text-primary transition-colors"
                    title="Chỉnh sửa"
                  >
                    <i class="pi pi-pencil text-xs"></i>
                  </button>
                  <button
                    v-if="canDelete(event)"
                    @click.stop="emit('deleteEvent', event)"
                    class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-destructive/20 text-muted-foreground hover:text-destructive transition-colors shadow-inner"
                    title="Xóa"
                  >
                    <i class="pi pi-trash text-xs"></i>
                  </button>
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
.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: var(--border) transparent;
}
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
