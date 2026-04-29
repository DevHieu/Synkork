<script setup lang="ts">
import { watch } from "vue";
import { AlertTriangle } from "lucide-vue-next";
import type { CalendarEvent } from "@/types/CalendarEvent";
import CalendarWarningDialog from "./CalendarWarningDialog.vue";
import EventTimeSection from "../sub-components/EventTimeSection.vue";
import EventRecurrenceSection from "../sub-components/EventRecurrenceSection.vue";
import EventAttendeesSection from "../sub-components/EventAttendeesSection.vue";
import EventAttachmentsSection from "../sub-components/EventAttachmentsSection.vue";
import { useEventForm, type EventFormData } from "../composables/useEventForm";

// Khai báo Props và Emits
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

// Logic điều khiển Form
const {
  formData, conflictEvents,
  warningMessage, showWarning,
  validate, resetForm,
} = useEventForm(props.initialData, props.checkConflicts, props.isEditing, props.editingEventId);

// Các hàm xử lý cập nhật dữ liệu từ component con
const onTimeChange = (data: { eventDate: string; startTime: string; endTime: string }) => {
  formData.value.eventDate = data.eventDate;
  formData.value.startTime = data.startTime;
  formData.value.endTime = data.endTime;
};

const onRecurrenceChange = (data: { recurrenceType: string; recurrenceEndDate?: string }) => {
  formData.value.recurrenceType = data.recurrenceType;
  formData.value.recurrenceEndDate = data.recurrenceEndDate;
};

const onAttendeesChange = (list: string[]) => {
  formData.value.attendees = list;
};

const onAttachmentsChange = (list: any[]) => {
  formData.value.attachments = list;
};

// Đồng bộ trạng thái khi Dialog đóng/mở
watch(
  () => props.show,
  (isOpen) => {
    if (isOpen) resetForm(props.initialData);
  }
);

// Xử lý gửi biểu mẫu
const handleSubmit = (): void => {
  if (validate()) {
    emit("save", { ...formData.value });
  }
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

            <!-- Ngày & Giờ Section -->
            <EventTimeSection
              :show="show"
              :initial-date="initialData.eventDate"
              :initial-start-time="initialData.startTime"
              :initial-end-time="initialData.endTime"
              @change="onTimeChange"
            />

            <!-- Chế độ lặp lại Section -->
            <EventRecurrenceSection
              :initial-type="initialData.recurrenceType || 'NONE'"
              :initial-end-date="initialData.recurrenceEndDate"
              :event-date="formData.eventDate"
              @change="onRecurrenceChange"
            />

            <!-- Cảnh báo trùng giờ -->
            <div v-if="conflictEvents.length > 0"
              class="bg-amber-500/10 border border-amber-500/30 rounded-xl p-4 shadow-lg shadow-amber-500/5">
              <div class="flex items-center gap-2.5 text-amber-400 text-sm font-semibold mb-2">
                <AlertTriangle :size="14" />
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

            <!-- Người tham gia Section -->
            <EventAttendeesSection
              :show="show"
              :initial-attendees="initialData.attendees"
              @change="onAttendeesChange"
            />

            <!-- Tệp đính kèm Section -->
            <EventAttachmentsSection
              :show="show"
              :initial-attachments="initialData.attachments"
              @change="onAttachmentsChange"
            />

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
