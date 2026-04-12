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

// Tên các thứ trong tuần
const dayNames = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

// Tính toán các ngày hiển thị trong tháng (bao gồm cả ngày đệm từ tháng trước/sau)
const monthDays = computed(() => {
  const startOfMonth = props.currentDate.startOf("month");
  const endOfMonth = props.currentDate.endOf("month");
  const startDay = startOfMonth.day();
  const days: dayjs.Dayjs[] = [];

  // Thêm các ngày của tháng trước
  for (let i = startDay - 1; i >= 0; i--) {
    days.push(startOfMonth.subtract(i + 1, "day"));
  }
  // Thêm các ngày của tháng hiện tại
  for (let d = startOfMonth; d.isBefore(endOfMonth) || d.isSame(endOfMonth, "day"); d = d.add(1, "day")) {
    days.push(d);
  }
  // Thêm các ngày của tháng sau để đủ 42 ô (6 tuần)
  while (days.length < 42) {
    days.push(endOfMonth.add(days.length - endOfMonth.date() - startDay + 1, "day"));
  }
  return days;
});

// Lấy danh sách sự kiện cho một ngày cụ thể
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

// Lấy danh sách sự kiện của ngày đang chọn
const selectedDateEvents = computed(() => {
  return getEventsForDate(props.selectedDate);
});

// Kiểm tra xem ngày có sự kiện không
const hasEvent = (date: dayjs.Dayjs) => {
  const targetDate = date.format("YYYY-MM-DD");
  for (let i = 0; i < props.events.length; i++) {
    const event = props.events[i];
    if (event && event.eventDate === targetDate) {
      return true;
    }
  }
  return false;
};

// Các hàm kiểm tra trạng thái ngày
const isToday = (date: dayjs.Dayjs) => date.isSame(dayjs(), "day");
const isSelected = (date: dayjs.Dayjs) => date.isSame(props.selectedDate, "day");
const isCurrentMonth = (date: dayjs.Dayjs) => date.month() === props.currentDate.month();

// Kiểm tra quyền chỉnh sửa
const canEdit = (event: CalendarEvent) => {
  return event.createdById === props.currentUserId || event.allowEditAll;
};

// Kiểm tra quyền xóa (chỉ người tạo)
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
          class="text-center text-xs font-semibold text-gray-400 py-1"
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
          <!-- Chấm chỉ báo sự kiện -->
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

    <!-- Danh sách sự kiện ngày đã chọn (Bên phải) -->
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
          class="group bg-zinc-800/50 rounded-xl p-4 hover:bg-zinc-800/80 transition-all duration-300 border border-white/5 hover:border-teal-500/30 shadow-lg"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0 space-y-3">
              <!-- Tiêu đề -->
              <div>
                <div class="flex items-center gap-2 mb-1">
                  <div class="w-1.5 h-1.5 rounded-full bg-teal-500"></div>
                  <span class="text-[10px] font-bold text-teal-500/80 uppercase tracking-widest">Tiêu đề</span>
                </div>
                <h4 class="font-semibold text-white text-sm leading-tight wrap-break-word">
                  {{ event.title }}
                </h4>
              </div>

              <!-- Thời gian -->
              <div class="flex items-center gap-4">
                <div class="flex flex-col">
                  <span class="text-[10px] font-medium text-gray-500 uppercase">Thời gian</span>
                  <div class="flex items-center gap-1.5 text-xs text-teal-400 font-medium mt-0.5">
                    <i class="pi pi-clock text-[10px]"></i>
                    {{ event.startTime.substring(0, 5) }} - {{ event.endTime.substring(0, 5) }}
                  </div>
                </div>
              </div>

              <!-- Mô tả -->
              <div v-if="event.description">
                <span class="text-[10px] font-medium text-gray-500 uppercase">Mô tả</span>
                <p class="text-xs text-gray-300 mt-1 leading-relaxed line-clamp-3 bg-white/5 p-2 rounded-lg border border-white/5 italic">
                  {{ event.description }}
                </p>
              </div>

              <!-- Người tạo -->
              <div class="pt-2 border-t border-white/5 flex items-center justify-between">
                <div class="flex flex-col">
                  <span class="text-[10px] font-medium text-gray-500 uppercase">Người tạo</span>
                  <div class="flex items-center gap-1.5 text-[11px] text-gray-400 mt-1">
                    <div class="w-5 h-5 rounded-full bg-teal-600/20 flex items-center justify-center border border-teal-500/20">
                      <i class="pi pi-user text-[10px] text-teal-500"></i>
                    </div>
                    <span class="truncate max-w-[120px]">{{ event.createdByDisplayName }}</span>
                  </div>
                </div>

                <!-- Hành động (Hiện khi hover trên Desktop) -->
                <div class="flex gap-1 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-all duration-300">
                  <button
                    v-if="canEdit(event)"
                    @click.stop="emit('editEvent', event)"
                    class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-teal-500/20 text-gray-400 hover:text-teal-400 transition-colors"
                    title="Chỉnh sửa"
                  >
                    <i class="pi pi-pencil text-xs"></i>
                  </button>
                  <button
                    v-if="canDelete(event)"
                    @click.stop="emit('deleteEvent', event)"
                    class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-red-500/20 text-gray-400 hover:text-red-400 transition-colors shadow-inner"
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
  scrollbar-color: rgba(255, 255, 255, 0.1) transparent;
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
