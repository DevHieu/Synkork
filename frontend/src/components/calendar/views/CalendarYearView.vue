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
  if (date.month() !== monthIndex) return false;
  const targetDate = date.format("YYYY-MM-DD");
  
  for (let i = 0; i < props.events.length; i++) {
    const event = props.events[i];
    if (event && event.eventDate === targetDate) {
      return true;
    }
  }
  return false;
};
</script>

<template>
  <div class="flex-1 overflow-y-auto p-4">
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4">
      <div
        v-for="m in yearMonths"
        :key="m.month"
        @click="emit('clickYearMonth', m.month)"
        class="bg-card rounded-xl p-3 cursor-pointer hover:bg-muted transition-all duration-200 hover:ring-1 hover:ring-primary/30"
      >
        <h4
          :class="[
            'text-sm font-semibold mb-2 text-center',
            currentDate.month() === m.month
              ? 'text-primary'
              : 'text-foreground',
          ]"
        >
          {{ m.name }}
        </h4>
        <div class="grid grid-cols-7 gap-px">
          <div
            v-for="dn in ['C', 'H', 'B', 'T', 'N', 'S', 'B']"
            :key="dn"
            class="text-center text-[8px] text-muted-foreground font-medium"
          >
            {{ dn }}
          </div>
          <div
            v-for="(day, di) in m.days.slice(0, 42)"
            :key="di"
            :class="[
              'text-center text-[10px] rounded p-px',
              day.month() === m.month ? 'text-foreground' : 'text-muted-foreground',
              isToday(day) ? 'bg-primary text-primary-foreground font-bold' : '',
              hasEvent(day, m.month) ? 'text-primary font-semibold' : '',
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
  scrollbar-color: var(--border) transparent;
}
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
</style>
