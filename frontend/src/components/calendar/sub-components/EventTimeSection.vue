<script setup lang="ts">
import { ref, watch, onMounted } from "vue";
import { useTimeSelector } from "../composables/useTimeSelector";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { CalendarIcon } from "lucide-vue-next";
import { computed } from "vue";
import dayjs from "dayjs";
import { parseDate } from "@internationalized/date";
import { cn } from "@/lib/utils";

const props = defineProps<{
  initialDate: string;
  initialEndDate?: string;
  initialStartTime: string;
  initialEndTime: string;
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", data: { eventDate: string; endDate: string; startTime: string; endTime: string }): void;
}>();

const eventDate = ref(props.initialDate);
const endDate = ref(props.initialEndDate || props.initialDate);

const dateValue = computed({
  get: () => eventDate.value ? parseDate(eventDate.value) : undefined,
  set: (val) => {
    if (val) {
      const newDateStr = val.toString();
      const oldDateStr = eventDate.value;
      eventDate.value = newDateStr;

      // Khi người dùng tương tác chọn ngày bắt đầu mới, duy trì khoảng cách số ngày của sự kiện sang endDate
      if (oldDateStr && endDate.value) {
        const diffDays = dayjs(endDate.value).diff(dayjs(oldDateStr), "day");
        if (diffDays >= 0) {
          endDate.value = dayjs(newDateStr).add(diffDays, "day").format("YYYY-MM-DD");
        } else {
          endDate.value = newDateStr;
        }
      } else {
        endDate.value = newDateStr;
      }
    }
  }
});

const endDateValue = computed({
  get: () => endDate.value ? parseDate(endDate.value) : undefined,
  set: (val) => {
    if (val) endDate.value = val.toString();
  }
});

const isStartDateOpen = ref(false);
const isEndDateOpen = ref(false);

watch(dateValue, () => {
  isStartDateOpen.value = false;
});

watch(endDateValue, () => {
  isEndDateOpen.value = false;
});

const {
  timeFormat, hours24, hours12, minutes,
  startHour, startMinute, startAmPm,
  endHour, endMinute, endAmPm,
  parseTimeString, buildTimeString, adjustEndTimeIfNeeded, syncDropdownsOnFormatChange,
} = useTimeSelector();

// Gom logic đồng bộ giờ vào một hàm để dialog mở lại không bị lệch dropdown.
// Đồng bộ trạng thái nội bộ với props khi dialog mở hoặc dữ liệu thay đổi
const syncInternalState = () => {
  eventDate.value = props.initialDate;
  endDate.value = props.initialEndDate || props.initialDate;
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
    endDate: endDate.value || eventDate.value,
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

watch([endHour, endMinute, endAmPm, eventDate, endDate], notifyParent);

watch(timeFormat, () => {
  const start = buildTimeString(startHour.value, startMinute.value, startAmPm.value);
  const end = buildTimeString(endHour.value, endMinute.value, endAmPm.value);
  syncDropdownsOnFormatChange(start, end);
});

onMounted(syncInternalState);
</script>

<template>
  <div class="space-y-6 rounded-md border border-border/60 bg-background p-4 shadow-sm cursor-default">
    <!-- Định dạng giờ -->
    <div class="rounded-md border border-border/60 bg-muted/15 p-4 cursor-default">
      <label class="block text-[9px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-3 cursor-default">ĐỊNH DẠNG GIỜ</label>
      <div class="flex w-fit rounded-md border border-border/60 bg-background p-0.5">
        <button type="button" @click="timeFormat = '24h'" :class="[
          'rounded-sm px-4 py-1.5 text-[10px] font-sans font-bold uppercase tracking-wider transition-all duration-200',
          timeFormat === '24h' ? 'bg-primary text-primary-foreground shadow-sm' : 'text-muted-foreground hover:bg-muted hover:text-foreground'
        ]">24H</button>
        <button type="button" @click="timeFormat = '12h'" :class="[
          'rounded-sm px-4 py-1.5 text-[10px] font-sans font-bold uppercase tracking-wider transition-all duration-200',
          timeFormat === '12h' ? 'bg-primary text-primary-foreground shadow-sm' : 'text-muted-foreground hover:bg-muted hover:text-foreground'
        ]">12H (AM/PM)</button>
      </div>
    </div>

    <!-- Ngày & Giờ -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <label class="block text-[9px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">NGÀY BẮT ĐẦU *</label>
        <Popover :modal="true" v-model:open="isStartDateOpen">
          <PopoverTrigger as-child>
            <Button
              variant="outline"
              type="button"
              :class="cn(
                'w-full h-10 justify-start text-left font-sans font-normal rounded-md border border-border/60 bg-background px-3.5 text-sm text-foreground hover:bg-muted/10',
                !eventDate && 'text-muted-foreground'
              )"
            >
              <CalendarIcon class="mr-2 h-4 w-4 shrink-0 opacity-50" />
              <span>{{ eventDate ? dayjs(eventDate).format("DD/MM/YYYY") : "Chọn ngày" }}</span>
            </Button>
          </PopoverTrigger>
          <PopoverContent class="w-auto p-0" align="start">
            <Calendar v-model="dateValue" initial-focus />
          </PopoverContent>
        </Popover>
      </div>
      <div>
        <label class="block text-[9px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">NGÀY KẾT THÚC *</label>
        <Popover :modal="true" v-model:open="isEndDateOpen">
          <PopoverTrigger as-child>
            <Button
              variant="outline"
              type="button"
              :class="cn(
                'w-full h-10 justify-start text-left font-sans font-normal rounded-md border border-border/60 bg-background px-3.5 text-sm text-foreground hover:bg-muted/10',
                !endDate && 'text-muted-foreground'
              )"
            >
              <CalendarIcon class="mr-2 h-4 w-4 shrink-0 opacity-50" />
              <span>{{ endDate ? dayjs(endDate).format("DD/MM/YYYY") : "Chọn ngày" }}</span>
            </Button>
          </PopoverTrigger>
          <PopoverContent class="w-auto p-0" align="start">
            <Calendar v-model="endDateValue" :min-value="dateValue" initial-focus />
          </PopoverContent>
        </Popover>
      </div>

      <!-- Giờ bắt đầu -->
      <div class="rounded-md border border-border/60 bg-muted/15 p-4 cursor-default">
        <label class="block text-[9px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">GIỜ BẮT ĐẦU *</label>
        <div class="flex gap-2 items-center">
          <Select v-model="startHour">
            <SelectTrigger class="w-full font-sans rounded-md border-border/60 h-10">
              <SelectValue />
            </SelectTrigger>
            <SelectContent class="max-h-60">
              <SelectItem class="font-sans" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">
                {{ h }}
              </SelectItem>
            </SelectContent>
          </Select>
          <span class="text-foreground font-sans font-bold">:</span>
          <Select v-model="startMinute">
            <SelectTrigger class="w-full font-sans rounded-md border-border/60 h-10">
              <SelectValue />
            </SelectTrigger>
            <SelectContent class="max-h-60">
              <SelectItem class="font-sans" v-for="m in minutes" :key="m" :value="m">
                {{ m }}
              </SelectItem>
            </SelectContent>
          </Select>
          <Select v-if="timeFormat === '12h'" v-model="startAmPm">
            <SelectTrigger class="w-full font-sans rounded-md border-border/60 h-10">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem class="font-sans" value="AM">AM</SelectItem>
              <SelectItem class="font-sans" value="PM">PM</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <!-- Giờ kết thúc -->
      <div class="rounded-md border border-border/60 bg-muted/15 p-4 cursor-default">
        <label class="block text-[9px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">GIỜ KẾT THÚC *</label>
        <div class="flex gap-2 items-center">
          <Select v-model="endHour">
            <SelectTrigger class="w-full font-sans rounded-md border-border/60 h-10">
              <SelectValue />
            </SelectTrigger>
            <SelectContent class="max-h-60">
              <SelectItem class="font-sans" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">
                {{ h }}
              </SelectItem>
            </SelectContent>
          </Select>
          <span class="text-foreground font-sans font-bold">:</span>
          <Select v-model="endMinute">
            <SelectTrigger class="w-full font-sans rounded-md border-border/60 h-10">
              <SelectValue />
            </SelectTrigger>
            <SelectContent class="max-h-60">
              <SelectItem class="font-sans" v-for="m in minutes" :key="m" :value="m">
                {{ m }}
              </SelectItem>
            </SelectContent>
          </Select>
          <Select v-if="timeFormat === '12h'" v-model="endAmPm">
            <SelectTrigger class="w-full font-sans rounded-md border-border/60 h-10">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem class="font-sans" value="AM">AM</SelectItem>
              <SelectItem class="font-sans" value="PM">PM</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
