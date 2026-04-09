<script setup lang="ts">
import { ref, watch, computed } from "vue";
import type { CalendarEvent } from "@/types/CalendarEvent";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import CalendarWarningDialog from "./CalendarWarningDialog.vue";

dayjs.locale("vi");

// Khởi tạo phông chữ tiếng Việt cho dayjs
dayjs.locale("vi");

const props = defineProps<{
  show: boolean;
  isEditing: boolean;
  initialData: {
    title: string;
    description: string;
    eventDate: string;
    startTime: string;
    endTime: string;
    recurrenceType?: string;
    recurrenceEndDate?: string;
    allowEditAll: boolean;
  };
  checkConflicts: (date: string, start: string, end: string, excludeId?: string) => Promise<CalendarEvent[]>;
  editingEventId?: string;
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "save", data: typeof props.initialData): void;
}>();

const formData = ref({ ...props.initialData });
const conflictEvents = ref<CalendarEvent[]>([]);
const isCheckingConflict = ref(false);

let conflictDebounce: ReturnType<typeof setTimeout> | null = null;
const showWarning = ref(false);
const warningMessage = ref("");

// Reset dữ liệu khi mở dialog
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      formData.value = { ...props.initialData };
      conflictEvents.value = [];
    }
  }
);

// Tự động kiểm tra trùng lịch khi thay đổi thời gian
watch(
  () => [formData.value.eventDate, formData.value.startTime, formData.value.endTime],
  ([date, start, end]) => {
    if (!props.show || !date || !start || !end) {
      conflictEvents.value = [];
      return;
    }
    if (conflictDebounce) clearTimeout(conflictDebounce);
    conflictDebounce = setTimeout(async () => {
      isCheckingConflict.value = true;
      try {
        conflictEvents.value = await props.checkConflicts(
          date as string,
          start as string,
          end as string,
          props.isEditing ? props.editingEventId : undefined
        );
      } catch {
        conflictEvents.value = [];
      } finally {
        isCheckingConflict.value = false;
      }
    }, 400);
  }
);

const handleSubmit = () => {
  if (!formData.value.title.trim()) return;

  if (!props.isEditing) {
    const eventDateTime = dayjs(`${formData.value.eventDate}T${formData.value.startTime}`);
    if (eventDateTime.isBefore(dayjs())) {
      warningMessage.value = "Bạn không thể tạo sự kiện với thời gian nằm ở trong quá khứ! Vui lòng chọn lại ngày và giờ phù hợp.";
      showWarning.value = true;
      return;
    }
  }

  emit("save", formData.value);
};

// Tạo văn bản mô tả chế độ lặp lại
const recurrenceSummary = computed(() => {
  const type = formData.value.recurrenceType;
  if (!type || type === "NONE") return "";

  const date = dayjs(formData.value.eventDate);

  let text = "";
  switch (type) {
    case "DAILY":
      text = "Sự kiện sẽ lặp lại vào mỗi ngày.";
      break;
    case "WEEKLY":
      text = `Sự kiện sẽ lặp lại vào mỗi thứ ${date.format("dddd")} hàng tuần.`;
      break;
    case "MONTHLY":
      text = `Sự kiện sẽ lặp lại vào ngày ${date.date()} hàng tháng.`;
      break;
    case "YEARLY":
      text = `Sự kiện sẽ lặp lại vào ngày ${date.format("DD [tháng] MM")} hàng năm.`;
      break;
  }

  if (formData.value.recurrenceEndDate) {
    text += ` Kết thúc vào ngày ${dayjs(formData.value.recurrenceEndDate).format("DD/MM/YYYY")}.`;
  } else {
    text += " Tiếp diễn trong vòng 1 năm tiếp theo.";
  }

  return text;
});
</script>

<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <!-- Overlay -->
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="emit('update:show', false)"
      ></div>

      <!-- Dialog Content -->
      <div
        class="relative bg-zinc-900 rounded-2xl shadow-2xl border border-white/10 w-full max-w-md mx-4 flex flex-col max-h-[90vh] overflow-hidden"
      >
        <!-- Header -->
        <div class="p-6 pb-4 border-b border-white/5 bg-zinc-900/50 backdrop-blur-md sticky top-0 z-10">
          <h2 class="text-lg font-semibold text-white">
            {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
          </h2>
        </div>

        <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0">
          <!-- Scrollable Body -->
          <div class="flex-1 overflow-y-auto p-6 space-y-5 custom-scrollbar">
            <!-- Title -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Tiêu đề *</label>
              <input
                v-model="formData.title"
                type="text"
                required
                placeholder="Nhập tiêu đề sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm"
              />
            </div>

            <!-- Description -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Mô tả</label>
              <textarea
                v-model="formData.description"
                rows="3"
                placeholder="Mô tả chi tiết sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 resize-none text-sm transition-all"
              ></textarea>
            </div>

            <!-- Date & Time Grid -->
            <div class="grid grid-cols-2 gap-4">
              <div class="col-span-2">
                <label class="block text-sm text-gray-400 mb-1.5 font-medium">Ngày diễn ra *</label>
                <input
                  v-model="formData.eventDate"
                  type="date"
                  required
                  class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 text-sm transition-all"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-400 mb-1.5 font-medium">Giờ bắt đầu *</label>
                <input
                  v-model="formData.startTime"
                  type="time"
                  required
                  class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 text-sm transition-all"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-400 mb-1.5 font-medium">Giờ kết thúc *</label>
                <input
                  v-model="formData.endTime"
                  type="time"
                  required
                  class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 text-sm transition-all"
                />
              </div>
            </div>

            <!-- Recurrence Settings -->
            <div class="space-y-4 p-4 bg-zinc-800/40 rounded-2xl border border-white/5 shadow-inner">
              <div>
                <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-3">Chế độ lặp lại</label>
                <div class="grid grid-cols-5 gap-1.5 bg-black/20 p-1 rounded-xl border border-white/5">
                  <button
                    v-for="opt in [
                      { val: 'NONE', label: 'Không', icon: 'pi pi-ban' },
                      { val: 'DAILY', label: 'Ngày', icon: 'pi pi-sync' },
                      { val: 'WEEKLY', label: 'Tuần', icon: 'pi pi-calendar-plus' },
                      { val: 'MONTHLY', label: 'Tháng', icon: 'pi pi-calendar' },
                      { val: 'YEARLY', label: 'Năm', icon: 'pi pi-star' },
                    ]"
                    :key="opt.val"
                    type="button"
                    @click="formData.recurrenceType = opt.val"
                    :class="[
                      'flex flex-col items-center gap-1.5 py-2 px-1 rounded-lg transition-all duration-300',
                      formData.recurrenceType === opt.val
                        ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20'
                        : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
                    ]"
                  >
                    <i :class="[opt.icon, 'text-sm']"></i>
                    <span class="text-[9px] font-bold uppercase">{{ opt.label }}</span>
                  </button>
                </div>
              </div>

              <Transition
                enter-active-class="transition duration-300 ease-out"
                enter-from-class="transform -translate-y-2 opacity-0"
                enter-to-class="transform translate-y-0 opacity-100"
                leave-active-class="transition duration-200 ease-in"
                leave-from-class="transform translate-y-0 opacity-100"
                leave-to-class="transform -translate-y-2 opacity-0"
              >
                <div v-if="formData.recurrenceType !== 'NONE'" class="space-y-4 pt-1">
                  <!-- Summary -->
                  <div class="flex items-start gap-2.5 px-3 py-2 bg-teal-500/10 rounded-lg border border-teal-500/20">
                    <i class="pi pi-info-circle text-teal-400 mt-0.5 text-xs"></i>
                    <p class="text-[11px] text-teal-300/90 leading-relaxed font-medium">
                      {{ recurrenceSummary }}
                    </p>
                  </div>

                  <!-- End Date -->
                  <div>
                    <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-2">Ngày kết thúc</label>
                    <div class="relative group">
                      <input
                        v-model="formData.recurrenceEndDate"
                        type="date"
                        class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm"
                      />
                      <div v-if="!formData.recurrenceEndDate" class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[10px] text-gray-500 italic">
                        Mặc định: 1 năm
                      </div>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>

            <!-- Conflict Warning -->
            <div
              v-if="conflictEvents.length > 0"
              class="bg-amber-500/10 border border-amber-500/30 rounded-xl p-4 shadow-lg shadow-amber-500/5"
            >
              <div class="flex items-center gap-2.5 text-amber-400 text-sm font-semibold mb-2">
                <i class="pi pi-exclamation-triangle"></i>
                Trùng giờ với {{ conflictEvents.length }} sự kiện:
              </div>
              <ul class="text-xs text-amber-300/70 space-y-1.5 ml-6">
                <li v-for="c in conflictEvents" :key="c.id" class="list-disc leading-relaxed">
                  <span class="font-bold text-amber-400/90">{{ c.title }}</span>
                  <br/>
                  <span class="text-[10px] italic">({{ c.startTime.substring(0, 5) }} - {{ c.endTime.substring(0, 5) }})</span>
                </li>
              </ul>
            </div>

            <!-- Allow Edit All -->
            <div class="flex items-center gap-3 py-1">
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="formData.allowEditAll"
                  type="checkbox"
                  class="sr-only peer"
                />
                <div
                  class="w-9 h-5 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-teal-600"
                ></div>
              </label>
              <span class="text-sm text-gray-300 font-medium">Cho phép mọi người chỉnh sửa</span>
            </div>
          </div>

          <!-- Actions Footer -->
          <div class="p-6 pt-4 border-t border-white/5 flex gap-2 justify-end bg-zinc-900/50 backdrop-blur-md sticky bottom-0 z-10 shadow-[0_-10px_20px_-10px_rgba(0,0,0,0.3)]">
            <button
              type="button"
              @click="emit('update:show', false)"
              class="px-5 py-2.5 rounded-xl text-gray-400 hover:text-white hover:bg-white/5 transition-all text-sm font-medium"
            >
              Hủy
            </button>
            <button
              type="submit"
              class="px-6 py-2.5 bg-teal-600 text-white rounded-xl hover:bg-teal-700 hover:shadow-lg hover:shadow-teal-500/20 active:scale-95 transition-all text-sm font-bold"
            >
              {{ isEditing ? "Cập nhật" : "Tạo sự kiện" }}
            </button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- Modal Cảnh báo -->
    <CalendarWarningDialog 
      v-model:show="showWarning" 
      :message="warningMessage" 
    />
  </Teleport>
</template>

<style scoped>
input[type="date"]::-webkit-calendar-picker-indicator,
input[type="time"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}

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
