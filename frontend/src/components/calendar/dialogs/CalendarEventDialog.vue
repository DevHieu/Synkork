<script setup lang="ts">
import { watch } from "vue";
import { AlertTriangle } from "lucide-vue-next";
import type { CalendarEvent } from "@/types/CalendarEvent";
import CalendarNotificationDialog from "./CalendarNotificationDialog.vue";
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
        class="relative bg-background rounded-none shadow-2xl border-2 border-border w-full max-w-md mx-4 flex flex-col max-h-[90vh] overflow-hidden">

        <!-- Header -->
        <div class="p-6 pb-4 border-b-2 border-border bg-background sticky top-0 z-10">
          <h2 class="text-lg font-mono font-bold uppercase tracking-widest text-primary">
            {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
          </h2>
        </div>

        <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0">
          <!-- Scrollable body -->
          <div class="flex-1 overflow-y-auto p-6 space-y-5 calendar-scrollbar">

            <!-- Tiêu đề -->
            <div class="bg-background border-2 border-border p-3">
              <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">TIÊU ĐỀ *</label>
              <input v-model="formData.title" type="text" required placeholder="NHẬP TIÊU ĐỀ SỰ KIỆN..."
                class="w-full bg-background border-2 border-border px-3 py-2.5 font-mono text-sm text-foreground placeholder-muted-foreground focus:outline-none focus:border-primary transition-colors uppercase" />
            </div>

            <!-- Mô tả -->
            <div class="bg-background border-2 border-border p-3">
              <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">MÔ TẢ</label>
              <textarea v-model="formData.description" rows="3" placeholder="MÔ TẢ CHI TIẾT SỰ KIỆN..."
                class="w-full bg-background border-2 border-border px-3 py-2 font-mono text-sm text-foreground placeholder-muted-foreground focus:outline-none focus:border-primary resize-none transition-colors uppercase" />
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
              class="bg-accent/10 border-2 border-accent p-4 shadow-[4px_4px_0px_0px_var(--color-accent)]">
              <div class="flex items-center gap-2.5 text-accent text-[10px] font-mono font-bold uppercase tracking-widest mb-2">
                <AlertTriangle :size="14" />
                TRÙNG GIỜ VỚI {{ conflictEvents.length }} SỰ KIỆN:
              </div>
              <ul class="text-xs font-mono text-accent/80 space-y-1.5 ml-6">
                <li v-for="c in conflictEvents" :key="c.id" class="list-disc leading-relaxed uppercase">
                  <span class="font-bold text-accent">{{ c.title }}</span><br />
                  <span class="text-[10px] italic">({{ c.startTime.substring(0, 5) }} - {{ c.endTime.substring(0, 5) }})</span>
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
            <div class="flex items-center gap-3 py-2 border-2 border-border bg-background p-3">
              <input v-model="formData.allowEditAll" type="checkbox" class="w-4 h-4 bg-background border-2 border-border text-primary focus:ring-0 focus:ring-offset-0 rounded-none cursor-pointer" />
              <span class="text-[10px] font-mono font-bold text-foreground uppercase tracking-widest cursor-pointer select-none" @click="formData.allowEditAll = !formData.allowEditAll">CHO PHÉP MỌI NGƯỜI CHỈNH SỬA</span>
            </div>

          </div>

          <!-- Footer actions -->
          <div
            class="p-6 pt-4 border-t-2 border-border flex gap-2 justify-end bg-background sticky bottom-0 z-10">
            <button type="button" @click="emit('update:show', false)"
              class="px-5 py-2.5 bg-background border-2 border-border text-muted-foreground hover:text-foreground hover:border-foreground transition-all font-mono text-xs font-bold uppercase tracking-widest">HỦY</button>
            <button type="submit"
              class="px-6 py-2.5 bg-primary text-primary-foreground border-2 border-primary hover:bg-background hover:text-primary transition-all font-mono text-xs font-bold uppercase tracking-widest" style="box-shadow: 4px 4px 0px 0px var(--color-primary);">
              {{ isEditing ? "CẬP NHẬT" : "TẠO SỰ KIỆN" }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Warning dialog -->
    <CalendarNotificationDialog 
      v-model:show="showWarning" 
      type="warning"
      title="CẢNH BÁO"
      :message="warningMessage" 
      confirm-text="ĐÃ HIỂU"
      @confirm="showWarning = false"
    />
  </Teleport>
</template>

<style scoped>
.create-btn {
  background: var(--primary);
  color: var(--primary-foreground);
}
</style>
