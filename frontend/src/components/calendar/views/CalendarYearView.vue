<script setup lang="ts">
import { computed } from "vue";
import dayjs from "dayjs";
import type { CalendarEvent } from "@/types/CalendarEvent";
import { ScrollArea } from "@/components/ui/scroll-area";

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

// View năm chỉ cần biết ngày nào có event để làm chỉ báo nhanh.
</script>

<template>
  <ScrollArea class="calendar-scroll-area min-h-0 flex-1">
    <div class="bg-transparent p-4 text-foreground">
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4">
        <div
          v-for="m in yearMonths"
          :key="m.month"
          @click="emit('clickYearMonth', m.month)"
          class="group cursor-pointer overflow-hidden rounded-[1.5rem] border-2 border-border bg-background p-0 shadow-[0_26px_60px_-44px_var(--color-foreground)] transition-all duration-200 hover:-translate-y-1 hover:border-primary"
        >
          <h4
            :class="[
              'border-b-2 border-border py-3 text-center text-xs font-mono font-bold uppercase tracking-widest transition-colors group-hover:border-primary',
              currentDate.month() === m.month
                ? 'bg-primary text-primary-foreground'
                : 'bg-muted/45 text-muted-foreground group-hover:text-primary',
            ]"
          >
            {{ m.name }}
          </h4>
          <div class="grid grid-cols-7 gap-px bg-border/60 p-3 transition-colors group-hover:bg-primary/20">
            <div
              v-for="dn in ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7']"
              :key="dn"
              class="bg-background py-1 text-center text-[8px] font-mono font-bold text-muted-foreground"
            >
              {{ dn }}
            </div>
            <div
              v-for="(day, di) in m.days.slice(0, 42)"
              :key="di"
              :class="[
                'flex items-center justify-center bg-background p-1 text-center text-[10px] font-mono',
                day.month() === m.month ? 'text-foreground' : 'text-muted-foreground opacity-30',
                isToday(day) ? 'bg-primary text-primary-foreground font-bold shadow-[0_8px_20px_-14px_var(--color-primary)]' : '',
                hasEvent(day, m.month) && !isToday(day) ? 'border border-primary bg-primary/5 font-bold text-primary' : '',
              ]"
            >
              {{ day.date() }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </ScrollArea>
</template>

<style scoped>
</style>
