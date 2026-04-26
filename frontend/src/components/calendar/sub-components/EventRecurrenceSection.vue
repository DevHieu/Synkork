<script setup lang="ts">
import { ref, computed, watch } from "vue";
import dayjs from "dayjs";
import "dayjs/locale/vi";

dayjs.locale("vi");

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
  <div class="space-y-4 p-4 bg-zinc-800/40 rounded-2xl border border-white/5 shadow-inner">
    <div>
      <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-3">Chế độ lặp lại</label>
      <div class="grid grid-cols-5 gap-1.5 bg-black/20 p-1 rounded-xl border border-white/5">
        <button v-for="opt in [
          { val: 'NONE', label: 'Không', icon: 'pi pi-ban' },
          { val: 'DAILY', label: 'Ngày', icon: 'pi pi-sync' },
          { val: 'WEEKLY', label: 'Tuần', icon: 'pi pi-calendar-plus' },
          { val: 'MONTHLY', label: 'Tháng', icon: 'pi pi-calendar' },
          { val: 'YEARLY', label: 'Năm', icon: 'pi pi-star' },
        ]" :key="opt.val" type="button" @click="recurrenceType = opt.val" :class="[
            'flex flex-col items-center gap-1.5 py-2 px-1 rounded-lg transition-all duration-300',
            recurrenceType === opt.val
              ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20'
              : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
          ]">
          <i :class="[opt.icon, 'text-sm']" />
          <span class="text-[9px] font-bold uppercase">{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <Transition enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0" enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in" leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0">
      <div v-if="recurrenceType !== 'NONE'" class="space-y-4 pt-1">
        <div class="flex items-start gap-2.5 px-3 py-2 bg-teal-500/10 rounded-lg border border-teal-500/20">
          <i class="pi pi-info-circle text-teal-400 mt-0.5 text-xs" />
          <p class="text-[11px] text-teal-300/90 leading-relaxed font-medium">{{ recurrenceSummary }}</p>
        </div>
        <div>
          <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-2">Ngày kết thúc</label>
          <div class="relative group">
            <input v-model="recurrenceEndDate" type="date"
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm" />
            <div v-if="!recurrenceEndDate"
              class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[10px] text-gray-500 italic">
              Mặc định: 1 năm
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>
