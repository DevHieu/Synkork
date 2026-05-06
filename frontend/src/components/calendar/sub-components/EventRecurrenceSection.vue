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
  <div class="space-y-4 p-4 bg-card/40 rounded-2xl border border-border shadow-inner">
    <div>
      <label class="block text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-3">Chế độ lặp lại</label>
      <div class="grid grid-cols-5 gap-1.5 bg-muted/50 p-1 rounded-xl border border-border">
        <button v-for="opt in recurrenceOptions" :key="opt.val" type="button" @click="recurrenceType = opt.val" :class="[
            'flex flex-col items-center gap-1.5 py-2 px-1 rounded-lg transition-all duration-300',
            recurrenceType === opt.val
              ? 'bg-primary text-primary-foreground shadow-lg shadow-primary/20'
              : 'text-muted-foreground hover:text-foreground hover:bg-muted'
          ]">
          <component :is="opt.icon" :size="14" />
          <span class="text-[9px] font-bold uppercase">{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <Transition enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0" enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in" leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0">
      <div v-if="recurrenceType !== 'NONE'" class="space-y-4 pt-1">
        <div class="flex items-start gap-2.5 px-3 py-2 bg-primary/10 rounded-lg border border-primary/20">
          <Info :size="12" class="text-primary mt-0.5 flex-shrink-0" />
          <p class="text-[11px] text-primary/90 leading-relaxed font-medium">{{ recurrenceSummary }}</p>
        </div>
        <div>
          <label class="block text-[10px] font-bold text-muted-foreground uppercase tracking-widest mb-2">Ngày kết thúc</label>
          <div class="relative group">
            <input v-model="recurrenceEndDate" type="date"
              class="w-full bg-muted border border-border rounded-lg px-3 pr-36 py-2.5 text-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-sm" />
            <div v-if="!recurrenceEndDate"
              class="absolute right-14 top-1/2 -translate-y-1/2 pointer-events-none text-[10px] text-muted-foreground italic">
              Mặc định: 1 năm
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
