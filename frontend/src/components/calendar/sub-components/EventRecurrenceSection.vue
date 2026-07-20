<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { Ban, RefreshCw, CalendarPlus, Calendar, Star, Info, CalendarIcon } from "lucide-vue-next";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar as CalendarComponent, type LayoutTypes } from "@/components/ui/calendar";
import { parseDate } from "@internationalized/date";
import { cn } from "@/lib/utils";

dayjs.locale("vi");

const recurrenceOptions = [
  { val: "NONE",    label: "Không", icon: Ban },
  { val: "DAILY",   label: "Ngày",  icon: RefreshCw },
  { val: "WEEKLY",  label: "Tuần",  icon: CalendarPlus },
  { val: "MONTHLY", label: "Tháng", icon: Calendar },
  { val: "YEARLY",  label: "Năm",   icon: Star },
];

const props = defineProps<{
  initialType: string;
  initialEndDate?: string;
  eventDate: string;
}>();

const emit = defineEmits<{
  (e: "change", data: { recurrenceType: string; recurrenceEndDate?: string }): void;
}>();

// Trạng thái nội bộ được khởi tạo từ props
const recurrenceType = ref(props.initialType || "NONE");
const recurrenceEndDate = ref(props.initialEndDate);

const recurrenceEndDateValue = computed({
  get: () => recurrenceEndDate.value ? parseDate(recurrenceEndDate.value) : undefined,
  set: (val) => {
    if (val) recurrenceEndDate.value = val.toString();
  }
});

const minRecurrenceDate = computed(() => props.eventDate ? parseDate(props.eventDate) : undefined);

const calendarLayout = computed((): LayoutTypes => {
  if (recurrenceType.value === "MONTHLY") return "month-and-year";
  if (recurrenceType.value === "YEARLY") return "year-only";
  return undefined;
});

const isPopoverOpen = ref(false);

watch(recurrenceEndDateValue, () => {
  isPopoverOpen.value = false;
});

/**
 * Tạo mô tả dễ đọc về quy luật lặp lại của sự kiện
 */
const recurrenceSummary = computed((): string => {
  if (!recurrenceType.value || recurrenceType.value === "NONE") return "";

  const date = dayjs(props.eventDate);
  const baseText: Record<string, string> = {
    DAILY: "Sự kiện sẽ lặp lại vào mỗi ngày.",
    WEEKLY: `Sự kiện sẽ lặp lại vào mỗi thứ ${date.format("dddd")} hàng tuần.`,
    MONTHLY: `Sự kiện sẽ lặp lại vào ngày ${date.date()} hàng tháng.`,
    YEARLY: `Sự kiện sẽ lặp lại vào ngày ${date.format("DD [tháng] MM")} hàng năm.`,
  };

  const suffix = recurrenceEndDate.value
    ? ` Kết thúc vào ngày ${dayjs(recurrenceEndDate.value).format("DD/MM/YYYY")}.`
    : " Tiếp diễn trong vòng 1 năm tiếp theo.";

  return (baseText[recurrenceType.value] ?? "") + suffix;
});

// Watcher thông báo thay đổi cho parent dialog
watch([recurrenceType, recurrenceEndDate], () => {
  emit("change", {
    recurrenceType: recurrenceType.value,
    recurrenceEndDate: recurrenceEndDate.value,
  });
});

// Đồng bộ lại state khi dialog mở bằng dữ liệu khác.
// Phản hồi: Đồng bộ khi dữ liệu bên ngoài thay đổi (ví dụ: khi mở lại dialog)
watch(() => props.initialType, (val) => recurrenceType.value = val || "NONE");
watch(() => props.initialEndDate, (val) => recurrenceEndDate.value = val);
</script>

<template>
  <div class="space-y-4 rounded-md border border-border/60 bg-background p-4 shadow-sm cursor-default">
    <div>
      <label class="block text-[9px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-3 cursor-default">CHẾ ĐỘ LẶP LẠI</label>
      <div class="grid grid-cols-5 gap-1.5 rounded-md border border-border/60 bg-muted/15 p-1.5">
        <button v-for="opt in recurrenceOptions" :key="opt.val" type="button" @click="recurrenceType = opt.val" :class="[
            'flex flex-col items-center gap-1 rounded-sm px-1 py-2.5 transition-all duration-200',
            recurrenceType === opt.val
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'bg-background text-muted-foreground hover:bg-muted/50 hover:text-foreground'
          ]">
          <component :is="opt.icon" :size="15" />
          <span class="text-[9px] font-sans font-bold uppercase tracking-wider">{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <Transition enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0" enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in" leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0">
      <div v-if="recurrenceType !== 'NONE'" class="space-y-4 pt-2">
        <div class="flex items-start gap-2 rounded-md border border-primary/10 bg-primary/5 px-3.5 py-2.5 cursor-default">
          <Info :size="13" class="text-primary mt-0.5 flex-shrink-0" />
          <p class="text-xs font-sans text-foreground leading-relaxed">{{ recurrenceSummary }}</p>
        </div>
        <div>
          <label class="block text-[9px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">NGÀY KẾT THÚC</label>
          <div class="relative group">
            <Popover :modal="true" v-model:open="isPopoverOpen">
              <PopoverTrigger as-child>
                <Button
                  variant="outline"
                  type="button"
                  :class="cn(
                    'w-full h-10 justify-start text-left font-sans font-normal rounded-md border border-border/60 bg-background px-3.5 text-sm text-foreground hover:bg-muted/10',
                    !recurrenceEndDate && 'text-muted-foreground'
                  )"
                >
                  <CalendarIcon class="mr-2 h-4 w-4 shrink-0 opacity-50" />
                  <span>{{ recurrenceEndDate ? dayjs(recurrenceEndDate).format("DD/MM/YYYY") : "Chọn ngày" }}</span>
                </Button>
              </PopoverTrigger>
              <PopoverContent class="w-auto p-0" align="start">
                <CalendarComponent v-model="recurrenceEndDateValue" :min-value="minRecurrenceDate" :layout="calendarLayout" initial-focus />
              </PopoverContent>
            </Popover>
            <div v-if="!recurrenceEndDate"
              class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[9px] font-sans text-muted-foreground uppercase tracking-wider">
              MẶC ĐỊNH: 1 NĂM
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
