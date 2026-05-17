<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { Ban, RefreshCw, CalendarPlus, Calendar, Star, Info } from "lucide-vue-next";
import dayjs from "dayjs";
import "dayjs/locale/vi";

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

// Phản hồi: Đồng bộ khi dữ liệu bên ngoài thay đổi (ví dụ: khi mở lại dialog)
watch(() => props.initialType, (val) => recurrenceType.value = val || "NONE");
watch(() => props.initialEndDate, (val) => recurrenceEndDate.value = val);
</script>

<template>
  <div class="space-y-4 p-4 bg-background border-2 border-border border-dashed">
    <div>
      <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-3">CHẾ ĐỘ LẶP LẠI</label>
      <div class="grid grid-cols-5 gap-0 border-2 border-border bg-background">
        <button v-for="opt in recurrenceOptions" :key="opt.val" type="button" @click="recurrenceType = opt.val" :class="[
            'flex flex-col items-center gap-1.5 py-3 px-1 border-r-2 border-border last:border-r-0 transition-colors',
            recurrenceType === opt.val
              ? 'bg-primary text-primary-foreground'
              : 'text-muted-foreground hover:bg-muted hover:text-foreground'
          ]">
          <component :is="opt.icon" :size="16" />
          <span class="text-[10px] font-mono font-bold uppercase tracking-wider">{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <Transition enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0" enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in" leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0">
      <div v-if="recurrenceType !== 'NONE'" class="space-y-4 pt-2">
        <div class="flex items-start gap-2.5 px-3 py-3 bg-muted/20 border-l-4 border-primary">
          <Info :size="14" class="text-primary mt-0.5 flex-shrink-0" />
          <p class="text-xs font-mono text-foreground leading-relaxed">{{ recurrenceSummary }}</p>
        </div>
        <div>
          <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">NGÀY KẾT THÚC</label>
          <div class="relative group">
            <input v-model="recurrenceEndDate" type="date"
              class="w-full bg-background border-2 border-border px-3 pr-36 py-2.5 font-mono text-sm text-foreground focus:outline-none focus:border-primary transition-colors uppercase" />
            <div v-if="!recurrenceEndDate"
              class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[10px] font-mono text-muted-foreground uppercase tracking-widest">
              MẶC ĐỊNH: 1 NĂM
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
