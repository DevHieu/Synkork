<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";

const props = defineProps<{
  currentDate: dayjs.Dayjs;
  events: CalendarEvent[];
  isToday: (date: dayjs.Dayjs) => boolean;
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
      name: monthStart.format("MMMM").charAt(0).toUpperCase() + monthStart.format("MMMM").slice(1),
      days,
    });
  }
  return months;
});

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
  <div class="flex-1 overflow-y-auto p-4 calendar-scrollbar bg-background text-foreground">
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
      <div
        v-for="m in yearMonths"
        :key="m.month"
        @click="emit('clickYearMonth', m.month)"
        class="bg-background border-2 border-border p-0 cursor-pointer hover:border-primary hover:translate-x-1 hover:-translate-y-1 transition-all duration-200 group"
        style="box-shadow: 4px 4px 0px 0px var(--color-border);"
        onmouseover="this.style.boxShadow='4px 4px 0px 0px var(--color-primary)';"
        onmouseout="this.style.boxShadow='4px 4px 0px 0px var(--color-border)';"
      >
        <h4
          :class="[
            'text-xs font-mono font-bold uppercase tracking-widest text-center py-2 border-b-2 border-border group-hover:border-primary transition-colors',
            currentDate.month() === m.month
              ? 'bg-primary text-primary-foreground'
              : 'bg-muted text-muted-foreground group-hover:text-primary',
          ]"
        >
          {{ m.name }}
        </h4>
        <div class="grid grid-cols-7 gap-px p-2 bg-border group-hover:bg-primary/20 transition-colors">
          <div
            v-for="dn in ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7']"
            :key="dn"
            class="text-center text-[8px] font-mono font-bold text-muted-foreground bg-background py-1"
          >
            {{ dn }}
          </div>
          <div
            v-for="(day, di) in m.days.slice(0, 42)"
            :key="di"
            :class="[
              'text-center text-[10px] font-mono p-1 bg-background flex items-center justify-center',
              day.month() === m.month ? 'text-foreground' : 'text-muted-foreground opacity-30',
              isToday(day) ? 'bg-primary text-primary-foreground font-bold' : '',
              hasEvent(day, m.month) && !isToday(day) ? 'border border-primary text-primary font-bold bg-primary/5' : '',
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
</style>
