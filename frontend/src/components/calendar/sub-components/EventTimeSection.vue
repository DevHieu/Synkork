<script setup lang="ts">
import { ref, watch, onMounted } from "vue";
import { useTimeSelector } from "../composables/useTimeSelector";

const props = defineProps<{
  initialDate: string;
  initialStartTime: string;
  initialEndTime: string;
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", data: { eventDate: string; startTime: string; endTime: string }): void;
}>();

const eventDate = ref(props.initialDate);

const {
  timeFormat, hours24, hours12, minutes,
  startHour, startMinute, startAmPm,
  endHour, endMinute, endAmPm,
  parseTimeString, buildTimeString, adjustEndTimeIfNeeded, syncDropdownsOnFormatChange,
} = useTimeSelector();

// Đồng bộ trạng thái nội bộ với props khi dialog mở hoặc dữ liệu thay đổi
const syncInternalState = () => {
  eventDate.value = props.initialDate;
  parseTimeString(props.initialStartTime, true);
  parseTimeString(props.initialEndTime, false);
};

watch(() => props.show, (isOpen) => {
  if (isOpen) syncInternalState();
}, { immediate: true });

// Thông báo các thay đổi cho component cha
const notifyParent = () => {
  const startTime = buildTimeString(startHour.value, startMinute.value, startAmPm.value);
  const endTime = buildTimeString(endHour.value, endMinute.value, endAmPm.value);

  emit("change", {
    eventDate: eventDate.value,
    startTime,
    endTime
  });
};

// Theo dõi để tự động gửi dữ liệu và điều chỉnh thời gian kết thúc
watch([startHour, startMinute, startAmPm], () => {
  const newStart = buildTimeString(startHour.value, startMinute.value, startAmPm.value);
  const currentEnd = buildTimeString(endHour.value, endMinute.value, endAmPm.value);

  const adjustedEnd = adjustEndTimeIfNeeded(newStart, currentEnd);
  if (adjustedEnd !== currentEnd) {
    parseTimeString(adjustedEnd, false);
  }
  notifyParent();
});

watch([endHour, endMinute, endAmPm, eventDate], notifyParent);

watch(timeFormat, () => {
  const start = buildTimeString(startHour.value, startMinute.value, startAmPm.value);
  const end = buildTimeString(endHour.value, endMinute.value, endAmPm.value);
  syncDropdownsOnFormatChange(start, end);
});

onMounted(syncInternalState);
</script>

<template>
  <div class="space-y-6">
    <!-- Định dạng giờ -->
    <div class="bg-background border-2 border-border p-3">
      <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-3">ĐỊNH DẠNG GIỜ</label>
      <div class="flex gap-0 border-2 border-border bg-background w-fit">
        <button type="button" @click="timeFormat = '24h'" :class="[
          'px-6 py-2 text-[10px] font-mono font-bold uppercase tracking-widest transition-colors border-r-2 border-border',
          timeFormat === '24h' ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:text-foreground hover:bg-muted'
        ]">24H</button>
        <button type="button" @click="timeFormat = '12h'" :class="[
          'px-6 py-2 text-[10px] font-mono font-bold uppercase tracking-widest transition-colors',
          timeFormat === '12h' ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:text-foreground hover:bg-muted'
        ]">12H (AM/PM)</button>
      </div>
    </div>

    <!-- Ngày & Giờ -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="md:col-span-2">
        <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">NGÀY DIỄN RA *</label>
        <input v-model="eventDate" type="date" required
          class="w-full bg-background border-2 border-border rounded-none px-3 py-2.5 font-mono text-sm text-foreground focus:outline-none focus:border-primary uppercase transition-colors" />
      </div>

      <!-- Giờ bắt đầu -->
      <div class="bg-background border-2 border-border p-3">
        <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">GIỜ BẮT ĐẦU *</label>
        <div class="flex gap-2 items-center">
          <select v-model="startHour"
            class="bg-background border-2 border-border rounded-none px-2 py-2 font-mono text-center text-foreground focus:outline-none focus:border-primary text-sm w-full appearance-none !bg-none cursor-pointer calendar-scrollbar">
            <option class="text-foreground bg-background font-mono" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">{{ h }}</option>
          </select>
          <span class="text-foreground font-mono font-bold">:</span>
          <select v-model="startMinute"
            class="bg-background border-2 border-border rounded-none px-2 py-2 font-mono text-center text-foreground focus:outline-none focus:border-primary text-sm w-full appearance-none !bg-none cursor-pointer calendar-scrollbar">
            <option class="text-foreground bg-background font-mono" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
          </select>
          <select v-if="timeFormat === '12h'" v-model="startAmPm"
            class="bg-background border-2 border-border rounded-none px-2 py-2 font-mono text-center text-foreground focus:outline-none focus:border-primary text-sm w-full appearance-none !bg-none cursor-pointer">
            <option class="text-foreground bg-background font-mono" value="AM">AM</option>
            <option class="text-foreground bg-background font-mono" value="PM">PM</option>
          </select>
        </div>
      </div>

      <!-- Giờ kết thúc -->
      <div class="bg-background border-2 border-border p-3">
        <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">GIỜ KẾT THÚC *</label>
        <div class="flex gap-2 items-center">
          <select v-model="endHour"
            class="bg-background border-2 border-border rounded-none px-2 py-2 font-mono text-center text-foreground focus:outline-none focus:border-primary text-sm w-full appearance-none !bg-none cursor-pointer calendar-scrollbar">
            <option class="text-foreground bg-background font-mono" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">{{ h }}</option>
          </select>
          <span class="text-foreground font-mono font-bold">:</span>
          <select v-model="endMinute"
            class="bg-background border-2 border-border rounded-none px-2 py-2 font-mono text-center text-foreground focus:outline-none focus:border-primary text-sm w-full appearance-none !bg-none cursor-pointer calendar-scrollbar">
            <option class="text-foreground bg-background font-mono" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
          </select>
          <select v-if="timeFormat === '12h'" v-model="endAmPm"
            class="bg-background border-2 border-border rounded-none px-2 py-2 font-mono text-center text-foreground focus:outline-none focus:border-primary text-sm w-full appearance-none !bg-none cursor-pointer">
            <option class="text-foreground bg-background font-mono" value="AM">AM</option>
            <option class="text-foreground bg-background font-mono" value="PM">PM</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
