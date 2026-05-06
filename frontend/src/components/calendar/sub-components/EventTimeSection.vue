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
  <div class="space-y-5 ">
    <!-- Định dạng giờ -->
    <div class="bg-muted/50 p-1 rounded-xl">
      <label class="block text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-3">Định dạng giờ</label>
      <div class="inline-flex bg-card p-1 rounded-xl border border-border gap-1.5">
        <button type="button" @click="timeFormat = '24h'" :class="[
          'px-4 py-1.5 rounded-lg text-xs font-bold transition-all duration-300',
          timeFormat === '24h' ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/20' : 'text-muted-foreground hover:text-foreground hover:bg-muted'
        ]">24h</button>
        <button type="button" @click="timeFormat = '12h'" :class="[
          'px-4 py-1.5 rounded-lg text-xs font-bold transition-all duration-300',
          timeFormat === '12h' ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/20' : 'text-muted-foreground hover:text-foreground hover:bg-muted'
        ]">12h (AM/PM)</button>
      </div>
    </div>

    <!-- Ngày & Giờ -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="md:col-span-2">
        <label class="block text-sm text-muted-foreground mb-1.5 font-medium">Ngày diễn ra *</label>
        <input v-model="eventDate" type="date" required
          class="w-full bg-background border border-border rounded-lg px-3 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary text-sm transition-all" />
      </div>

      <!-- Giờ bắt đầu -->
      <div class="bg-muted/50 p-1 rounded-xl">
        <label class="block text-sm text-muted-foreground mb-1.5 font-medium">Giờ bắt đầu *</label>
        <div class="flex gap-2 ">
          <select v-model="startHour"
            class="bg-background border border-border rounded-lg px-2 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 text-sm w-full custom-scrollbar">
            <option class="text-background" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">{{ h
              }}</option>
          </select>
          <span class="text-foreground font-bold self-center">:</span>
          <select v-model="startMinute"
            class="bg-background border border-border  rounded-lg px-2 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 text-sm w-full custom-scrollbar">
            <option class="text-background" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
          </select>
          <select v-if="timeFormat === '12h'" v-model="startAmPm"
            class="bg-background border border-border rounded-lg px-2 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 text-sm w-full">
            <option class="text-background" value="AM">AM</option>
            <option class="text-background" value="PM">PM</option>
          </select>
        </div>
      </div>

      <!-- Giờ kết thúc -->
      <div class="bg-muted/50 p-1 rounded-xl">
        <label class="block text-sm text-muted-foreground mb-1.5 font-medium">Giờ kết thúc *</label>
        <div class="flex gap-2">
          <select v-model="endHour"
            class="bg-background border border-border rounded-lg px-2 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 text-sm w-full custom-scrollbar">
            <option class="text-background" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">{{ h
              }}</option>
          </select>
          <span class="text-foreground font-bold self-center">:</span>
          <select v-model="endMinute"
            class="bg-background border border-border rounded-lg px-2 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 text-sm w-full custom-scrollbar">
            <option class="text-background" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
          </select>
          <select v-if="timeFormat === '12h'" v-model="endAmPm"
            class="bg-background border border-border rounded-lg px-2 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 text-sm w-full">
            <option class="text-background" value="AM">AM</option>
            <option class="text-background" value="PM">PM</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
