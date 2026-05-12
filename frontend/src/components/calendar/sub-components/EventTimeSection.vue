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
  <div class="space-y-5">
    <!-- Định dạng giờ -->
    <div>
      <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-3">Định dạng giờ</label>
      <div class="inline-flex bg-black/20 p-1 rounded-xl border border-white/5 gap-1.5">
        <button type="button" @click="timeFormat = '24h'" :class="[
          'px-4 py-1.5 rounded-lg text-xs font-bold transition-all duration-300',
          timeFormat === '24h' ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20' : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
        ]">24h</button>
        <button type="button" @click="timeFormat = '12h'" :class="[
          'px-4 py-1.5 rounded-lg text-xs font-bold transition-all duration-300',
          timeFormat === '12h' ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20' : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
        ]">12h (AM/PM)</button>
      </div>
    </div>

    <!-- Ngày & Giờ -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="md:col-span-2">
        <label class="block text-sm text-gray-400 mb-1.5 font-medium">Ngày diễn ra *</label>
        <input v-model="eventDate" type="date" required
          class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 text-sm transition-all" />
      </div>

      <!-- Giờ bắt đầu -->
      <div>
        <label class="block text-sm text-gray-400 mb-1.5 font-medium">Giờ bắt đầu *</label>
        <div class="flex gap-2">
          <select v-model="startHour"
            class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
            <option class="text-black" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h"
              :value="h">{{ h }}</option>
          </select>
          <span class="text-white font-bold self-center">:</span>
          <select v-model="startMinute"
            class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
            <option class="text-black" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
          </select>
          <select v-if="timeFormat === '12h'" v-model="startAmPm"
            class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full">
            <option class="text-black" value="AM">AM</option>
            <option class="text-black" value="PM">PM</option>
          </select>
        </div>
      </div>

      <!-- Giờ kết thúc -->
      <div>
        <label class="block text-sm text-gray-400 mb-1.5 font-medium">Giờ kết thúc *</label>
        <div class="flex gap-2">
          <select v-model="endHour"
            class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
            <option class="text-black" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h"
              :value="h">{{ h }}</option>
          </select>
          <span class="text-white font-bold self-center">:</span>
          <select v-model="endMinute"
            class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
            <option class="text-black" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
          </select>
          <select v-if="timeFormat === '12h'" v-model="endAmPm"
            class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full">
            <option class="text-black" value="AM">AM</option>
            <option class="text-black" value="PM">PM</option>
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
