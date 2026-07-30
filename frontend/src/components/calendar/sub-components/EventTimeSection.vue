<script setup lang="ts">
import { ref, watch, computed } from "vue";
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

// Binding Date
const dateValue = computed({
  get: () => eventDate.value ? parseDate(eventDate.value) : undefined,
  set: (val) => {
    if (val) {
      const newDateStr = val.toString();
      eventDate.value = newDateStr;
      // Nếu endDate ở trước eventDate mới thì tự đẩy endDate = eventDate mới
      if (!endDate.value || dayjs(endDate.value).isBefore(dayjs(newDateStr))) {
        endDate.value = newDateStr;
      }
    }
  }
});

const endDateValue = computed({
  get: () => endDate.value ? parseDate(endDate.value) : undefined,
  set: (val) => {
    if (val) {
      const newEndDateStr = val.toString();
      // Không cho phép endDate nằm trước eventDate
      if (dayjs(newEndDateStr).isBefore(dayjs(eventDate.value))) {
        endDate.value = eventDate.value;
      } else {
        endDate.value = newEndDateStr;
      }
    }
  }
});

const isStartDateOpen = ref(false);
const isEndDateOpen = ref(false);
watch(dateValue, () => { isStartDateOpen.value = false; });
watch(endDateValue, () => { isEndDateOpen.value = false; });

// Time selector helpers
const {
  timeFormat, hours24, hours12, minutes,
  parseTime, formatTime, adjustEndTimeIfNeeded
} = useTimeSelector();

// UI States
const startHour = ref("09");
const startMinute = ref("00");
const startAmPm = ref("AM");

const endHour = ref("10");
const endMinute = ref("00");
const endAmPm = ref("AM");

// Đồng bộ trạng thái nội bộ với props
const syncInternalState = () => {
  eventDate.value = props.initialDate;
  endDate.value = props.initialEndDate || props.initialDate;

  const start = parseTime(props.initialStartTime, timeFormat.value);
  startHour.value = start.hour;
  startMinute.value = start.minute;
  startAmPm.value = start.ampm;

  const end = parseTime(props.initialEndTime, timeFormat.value);
  endHour.value = end.hour;
  endMinute.value = end.minute;
  endAmPm.value = end.ampm;
};

watch(() => props.show, (isOpen) => { if (isOpen) syncInternalState(); }, { immediate: true });

const notifyParent = () => {
  const startTime = formatTime(startHour.value, startMinute.value, startAmPm.value, timeFormat.value);
  const endTime = formatTime(endHour.value, endMinute.value, endAmPm.value, timeFormat.value);
  emit("change", {
    eventDate: eventDate.value,
    endDate: endDate.value || eventDate.value,
    startTime,
    endTime
  });
};

// Tự động đồng bộ AM/PM cho endAmPm khi đổi startAmPm ở định dạng 12h
watch(startAmPm, (newAmPm) => {
  if (timeFormat.value === "12h" && eventDate.value === endDate.value) {
    endAmPm.value = newAmPm;
  }
});

// Điều chỉnh endTime nếu <= startTime (chỉ áp dụng khi sự kiện ở CÙNG NGÀY)
watch([startHour, startMinute, startAmPm], () => {
  if (eventDate.value === endDate.value) {
    const start = formatTime(startHour.value, startMinute.value, startAmPm.value, timeFormat.value);
    const currentEnd = formatTime(endHour.value, endMinute.value, endAmPm.value, timeFormat.value);
    const adjustedEnd = adjustEndTimeIfNeeded(start, currentEnd);
    
    if (adjustedEnd !== currentEnd) {
      const parsedEnd = parseTime(adjustedEnd, timeFormat.value);
      endHour.value = parsedEnd.hour;
      endMinute.value = parsedEnd.minute;
      endAmPm.value = parsedEnd.ampm;
    }
  }
  notifyParent();
});

watch([endHour, endMinute, endAmPm, eventDate, endDate], notifyParent);

// Chuyển đổi định dạng 12h/24h giữ nguyên giờ
watch(timeFormat, (newFormat) => {
  const oldFormat = newFormat === "24h" ? "12h" : "24h";
  const start = formatTime(startHour.value, startMinute.value, startAmPm.value, oldFormat);
  const end = formatTime(endHour.value, endMinute.value, endAmPm.value, oldFormat);
  
  const parsedStart = parseTime(start, newFormat);
  startHour.value = parsedStart.hour;
  startMinute.value = parsedStart.minute;
  startAmPm.value = parsedStart.ampm;

  const parsedEnd = parseTime(end, newFormat);
  endHour.value = parsedEnd.hour;
  endMinute.value = parsedEnd.minute;
  endAmPm.value = parsedEnd.ampm;
});
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
