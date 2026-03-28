<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";

const props = defineProps<{
  currentDate: dayjs.Dayjs;
  events: CalendarEvent[];
}>();

const emit = defineEmits<{
  (e: "clickYearMonth", monthIndex: number): void;
}>();

// Tính toán dữ liệu 12 tháng trong năm
const yearMonths = computed(() => {
  const months: { month: number; name: string; days: dayjs.Dayjs[] }[] = [];
  for (let m = 0; m < 12; m++) {
    const monthStart = props.currentDate.month(m).startOf("month");
    const monthEnd = monthStart.endOf("month");
    const startDay = monthStart.day();
    const days: dayjs.Dayjs[] = [];

    // Ngày đệm từ tháng trước
    for (let i = startDay - 1; i >= 0; i--) {
      days.push(monthStart.subtract(i + 1, "day"));
    }
    // Các ngày trong tháng
    for (let d = monthStart; d.isBefore(monthEnd) || d.isSame(monthEnd, "day"); d = d.add(1, "day")) {
      days.push(d);
    }
    // Ngày đệm tháng sau
    while (days.length < 42) {
      days.push(monthEnd.add(days.length - monthEnd.date() - startDay + 1, "day"));
    }

    months.push({
      month: m,
      name: monthStart.format("MMMM"),
      days,
    });
  }
  return months;
});

// Kiểm tra ngày hiện tại
const isToday = (date: dayjs.Dayjs) => date.isSame(dayjs(), "day");

// Kiểm tra xem ngày có sự kiện không (phải khớp đúng tháng đang hiển thị)
const hasEvent = (date: dayjs.Dayjs, monthIndex: number) => {
  return date.month() === monthIndex && props.events.some((e) => e.eventDate === date.format("YYYY-MM-DD"));
};
</script>

<template>
  <div class="flex-1 overflow-y-auto p-4">
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
      <div
        v-for="m in yearMonths"
        :key="m.month"
        @click="emit('clickYearMonth', m.month)"
        class="bg-white/5 rounded-xl p-3 cursor-pointer hover:bg-white/10 transition-all duration-200 hover:ring-1 hover:ring-teal-500/30"
      >
        <h4
          :class="[
            'text-sm font-semibold mb-2 text-center',
            currentDate.month() === m.month
              ? 'text-teal-400'
              : 'text-gray-300',
          ]"
        >
          {{ m.name }}
        </h4>
        <div class="grid grid-cols-7 gap-px">
          <div
            v-for="dn in ['C', 'H', 'B', 'T', 'N', 'S', 'B']"
            :key="dn"
            class="text-center text-[8px] text-gray-500 font-medium"
          >
            {{ dn }}
          </div>
          <div
            v-for="(day, di) in m.days.slice(0, 42)"
            :key="di"
            :class="[
              'text-center text-[10px] rounded p-px',
              day.month() === m.month ? 'text-gray-300' : 'text-gray-600',
              isToday(day) ? 'bg-teal-500 text-white font-bold' : '',
              hasEvent(day, m.month) ? 'text-teal-400 font-semibold' : '',
            ]"
          >
            {{ day.date() }}
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
