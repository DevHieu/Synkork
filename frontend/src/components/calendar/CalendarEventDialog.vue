<script setup lang="ts">
import { watch } from "vue";
import type { CalendarEvent } from "@/types/CalendarEvent";
import CalendarWarningDialog from "./CalendarWarningDialog.vue";
import { useEventForm, type EventFormData } from "@/composables/calendarTS/useEventForm";
import { useTimeSelector } from "@/composables/calendarTS/useTimeSelector";
import { useAttendees } from "@/composables/calendarTS/useAttendees";
import { useAttachments } from "@/composables/calendarTS/useAttachments";

// ─── Props & Emits ────────────────────────────────────────────────────────────

const props = defineProps<{
  show: boolean;
  isEditing: boolean;
  initialData: EventFormData;
  checkConflicts: (date: string, start: string, end: string, excludeId?: string) => Promise<CalendarEvent[]>;
  editingEventId?: string;
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "save", data: EventFormData): void;
}>();

// ─── Composables ──────────────────────────────────────────────────────────────

const {
  formData, conflictEvents, isCheckingConflict,
  warningMessage, showWarning, recurrenceSummary,
  validate, resetForm,
} = useEventForm(props.initialData, props.checkConflicts, props.isEditing, props.editingEventId);

const {
  timeFormat, hours24, hours12, minutes,
  startHour, startMinute, startAmPm,
  endHour, endMinute, endAmPm,
  parseTimeString, buildTimeString, adjustEndTimeIfNeeded, syncDropdownsOnFormatChange,
} = useTimeSelector();

const { attendees, attendeeInput, addAttendee, removeAttendee, resetAttendees } =
  useAttendees(props.initialData.attendees);

const { attachments, addFromFileInput, removeAttachment, resetAttachments } =
  useAttachments(props.initialData.attachments);

// ─── Sync: dropdown → formData ────────────────────────────────────────────────

watch([startHour, startMinute, startAmPm], () => {
  formData.value.startTime = buildTimeString(startHour.value, startMinute.value, startAmPm.value);
});

watch([endHour, endMinute, endAmPm], () => {
  formData.value.endTime = buildTimeString(endHour.value, endMinute.value, endAmPm.value);
});

// ─── Sync: auto-adjust endTime khi startTime thay đổi ────────────────────────

watch(
  () => formData.value.startTime,
  (newStart) => {
    if (!newStart || !formData.value.endTime) return;
    const adjusted = adjustEndTimeIfNeeded(newStart, formData.value.endTime);
    if (adjusted !== formData.value.endTime) {
      formData.value.endTime = adjusted;
      parseTimeString(adjusted, false);
    }
  }
);

// ─── Sync: format toggle → re-parse dropdowns ────────────────────────────────

watch(timeFormat, () => {
  syncDropdownsOnFormatChange(formData.value.startTime, formData.value.endTime);
});

// ─── Reset khi dialog mở ─────────────────────────────────────────────────────

watch(
  () => props.show,
  (isOpen) => {
    if (!isOpen) return;
    resetForm(props.initialData);
    resetAttendees(props.initialData.attendees);
    resetAttachments(props.initialData.attachments);
    parseTimeString(props.initialData.startTime, true);
    parseTimeString(props.initialData.endTime, false);
  }
);

// ─── Submit ───────────────────────────────────────────────────────────────────

const handleSubmit = (): void => {
  if (!validate()) return;
  emit("save", {
    ...formData.value,
    attendees: attendees.value,
    attachments: attachments.value,
  });
};
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="emit('update:show', false)" />

      <!-- Dialog -->
      <div
        class="relative bg-zinc-900 rounded-2xl shadow-2xl border border-white/10 w-full max-w-md mx-4 flex flex-col max-h-[90vh] overflow-hidden">

        <!-- Header -->
        <div class="p-6 pb-4 border-b border-white/5 bg-zinc-900/50 backdrop-blur-md sticky top-0 z-10">
          <h2 class="text-lg font-semibold text-white">
            {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
          </h2>
        </div>

        <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0">
          <!-- Scrollable body -->
          <div class="flex-1 overflow-y-auto p-6 space-y-5 custom-scrollbar">

            <!-- Tiêu đề -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Tiêu đề *</label>
              <input v-model="formData.title" type="text" required placeholder="Nhập tiêu đề sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm" />
            </div>

            <!-- Mô tả -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Mô tả</label>
              <textarea v-model="formData.description" rows="3" placeholder="Mô tả chi tiết sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 resize-none text-sm transition-all" />
            </div>

            <!-- Định dạng giờ -->
            <div>
              <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-3">Định dạng
                giờ</label>
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
                <input v-model="formData.eventDate" type="date" required
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

            <!-- Chế độ lặp lại -->
            <div class="space-y-4 p-4 bg-zinc-800/40 rounded-2xl border border-white/5 shadow-inner">
              <div>
                <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-3">Chế độ lặp
                  lại</label>
                <div class="grid grid-cols-5 gap-1.5 bg-black/20 p-1 rounded-xl border border-white/5">
                  <button v-for="opt in [
                    { val: 'NONE', label: 'Không', icon: 'pi pi-ban' },
                    { val: 'DAILY', label: 'Ngày', icon: 'pi pi-sync' },
                    { val: 'WEEKLY', label: 'Tuần', icon: 'pi pi-calendar-plus' },
                    { val: 'MONTHLY', label: 'Tháng', icon: 'pi pi-calendar' },
                    { val: 'YEARLY', label: 'Năm', icon: 'pi pi-star' },
                  ]" :key="opt.val" type="button" @click="formData.recurrenceType = opt.val" :class="[
                      'flex flex-col items-center gap-1.5 py-2 px-1 rounded-lg transition-all duration-300',
                      formData.recurrenceType === opt.val
                        ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20'
                        : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
                    ]">
                    <i :class="[opt.icon, 'text-sm']" />
                    <span class="text-[9px] font-bold uppercase">{{ opt.label }}</span>
                  </button>
                </div>
              </div>

              <Transition enter-active-class="transition duration-300 ease-out"
                enter-from-class="transform -translate-y-2 opacity-0"
                enter-to-class="transform translate-y-0 opacity-100"
                leave-active-class="transition duration-200 ease-in"
                leave-from-class="transform translate-y-0 opacity-100"
                leave-to-class="transform -translate-y-2 opacity-0">
                <div v-if="formData.recurrenceType !== 'NONE'" class="space-y-4 pt-1">
                  <div class="flex items-start gap-2.5 px-3 py-2 bg-teal-500/10 rounded-lg border border-teal-500/20">
                    <i class="pi pi-info-circle text-teal-400 mt-0.5 text-xs" />
                    <p class="text-[11px] text-teal-300/90 leading-relaxed font-medium">{{ recurrenceSummary }}</p>
                  </div>
                  <div>
                    <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-2">Ngày kết
                      thúc</label>
                    <div class="relative group">
                      <input v-model="formData.recurrenceEndDate" type="date"
                        class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm" />
                      <div v-if="!formData.recurrenceEndDate"
                        class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[10px] text-gray-500 italic">
                        Mặc định: 1 năm
                      </div>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>

            <!-- Cảnh báo trùng giờ -->
            <div v-if="conflictEvents.length > 0"
              class="bg-amber-500/10 border border-amber-500/30 rounded-xl p-4 shadow-lg shadow-amber-500/5">
              <div class="flex items-center gap-2.5 text-amber-400 text-sm font-semibold mb-2">
                <i class="pi pi-exclamation-triangle" />
                Trùng giờ với {{ conflictEvents.length }} sự kiện:
              </div>
              <ul class="text-xs text-amber-300/70 space-y-1.5 ml-6">
                <li v-for="c in conflictEvents" :key="c.id" class="list-disc leading-relaxed">
                  <span class="font-bold text-amber-400/90">{{ c.title }}</span><br />
                  <span class="text-[10px] italic">({{ c.startTime.substring(0, 5) }} - {{ c.endTime.substring(0, 5)
                    }})</span>
                </li>
              </ul>
            </div>

            <!-- Người tham gia -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Người tham gia</label>
              <div class="flex flex-col gap-2">
                <div class="flex gap-2">
                  <input v-model="attendeeInput" @keyup.enter="addAttendee" @keydown.enter.prevent type="text"
                    placeholder="Nhập email và ấn Enter hoặc nút thêm..."
                    class="flex-1 bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm" />
                  <button type="button" @click="addAttendee"
                    class="bg-white/10 text-white px-3 py-2 rounded-lg hover:bg-white/20 transition-all">
                    <i class="pi pi-plus" />
                  </button>
                </div>
                <div v-if="attendees.length > 0" class="flex flex-wrap gap-2 mt-1">
                  <div v-for="(email, idx) in attendees" :key="idx"
                    class="flex items-center gap-1.5 bg-teal-500/20 text-teal-300 px-2 py-1 rounded-md text-xs border border-teal-500/20">
                    <span>{{ email }}</span>
                    <button type="button" @click="removeAttendee(idx)" class="hover:text-white transition-colors">
                      <i class="pi pi-times text-[10px]" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Tệp đính kèm -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Tệp đính kèm</label>
              <div class="flex flex-col gap-2">
                <label
                  class="flex justify-center items-center w-full h-20 px-4 transition bg-white/5 border-2 border-white/10 border-dashed rounded-lg appearance-none cursor-pointer hover:border-teal-500/50 hover:bg-white/10 focus:outline-none">
                  <span class="flex items-center space-x-2">
                    <i class="pi pi-upload text-gray-400" />
                    <span class="font-medium text-gray-400 text-sm">Nhấn để chọn tệp...</span>
                  </span>
                  <input type="file" multiple class="hidden" @change="addFromFileInput" />
                </label>
                <div v-if="attachments.length > 0" class="flex flex-col gap-1.5 mt-1">
                  <div v-for="(file, idx) in attachments" :key="idx"
                    class="flex items-center justify-between bg-black/20 p-2 rounded-lg border border-white/5 text-xs">
                    <div class="flex items-center gap-2 truncate">
                      <i class="pi pi-file text-gray-400" />
                      <span class="text-gray-300 truncate">{{ file.name }}</span>
                    </div>
                    <button type="button" @click="removeAttachment(idx)"
                      class="text-red-400/80 hover:text-red-400 px-2 shrink-0">
                      <i class="pi pi-trash" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Cho phép chỉnh sửa -->
            <div class="flex items-center gap-3 py-1">
              <label class="relative inline-flex items-center cursor-pointer">
                <input v-model="formData.allowEditAll" type="checkbox" class="sr-only peer" />
                <div
                  class="w-9 h-5 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-teal-600" />
              </label>
              <span class="text-sm text-gray-300 font-medium">Cho phép mọi người chỉnh sửa</span>
            </div>

          </div>

          <!-- Footer actions -->
          <div
            class="p-6 pt-4 border-t border-white/5 flex gap-2 justify-end bg-zinc-900/50 backdrop-blur-md sticky bottom-0 z-10 shadow-[0_-10px_20px_-10px_rgba(0,0,0,0.3)]">
            <button type="button" @click="emit('update:show', false)"
              class="px-5 py-2.5 rounded-xl text-gray-400 hover:text-white hover:bg-white/5 transition-all text-sm font-medium">Hủy</button>
            <button type="submit"
              class="px-6 py-2.5 bg-teal-600 text-white rounded-xl hover:bg-teal-700 hover:shadow-lg hover:shadow-teal-500/20 active:scale-95 transition-all text-sm font-bold">{{
                isEditing ? "Cập nhật" : "Tạo sự kiện" }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Warning dialog -->
    <CalendarWarningDialog v-model:show="showWarning" :message="warningMessage" />
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
