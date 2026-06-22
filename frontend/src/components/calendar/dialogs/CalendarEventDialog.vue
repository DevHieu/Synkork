<script setup lang="ts">
import { watch, computed } from "vue";
import { CalendarPlus2, Pencil, X } from "lucide-vue-next";
import { Button } from "@/components/ui/button";
import CalendarNotificationDialog from "./CalendarNotificationDialog.vue";
import EventTimeSection from "../sub-components/EventTimeSection.vue";
import EventRecurrenceSection from "../sub-components/EventRecurrenceSection.vue";
import EventAttendeesSection from "../sub-components/EventAttendeesSection.vue";
import EventAttachmentsSection from "../sub-components/EventAttachmentsSection.vue";
import { useEventForm, type EventFormData } from "../composables/useEventForm";
import type { Member } from "@/types/Member";
import { useSpaceStore } from "@/stores/spaceStore";

const spaceStore = useSpaceStore();

const voiceSpaces = computed(() => spaceStore.voiceSpaces || []);

// Khai báo Props và Emits
const props = defineProps<{
  show: boolean;
  isEditing: boolean;
  initialData: EventFormData;
  roomMembers: Member[];
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "save", data: EventFormData): void;
}>();

// Logic điều khiển Form
const {
  formData,
  warningMessage, showWarning,
  validate, warnInvalidEventLink, resetForm,
} = useEventForm(props.initialData, props.isEditing);

// Các hàm xử lý cập nhật dữ liệu từ component con
const onTimeChange = (data: { eventDate: string; endDate: string; startTime: string; endTime: string }) => {
  formData.value.eventDate = data.eventDate;
  formData.value.endDate = data.endDate;
  formData.value.startTime = data.startTime;
  formData.value.endTime = data.endTime;
};

const onRecurrenceChange = (data: { recurrenceType: string; recurrenceEndDate?: string }) => {
  formData.value.recurrenceType = data.recurrenceType;
  formData.value.recurrenceEndDate = data.recurrenceEndDate;
};

const onAttendeesChange = (list: string[]) => {
  formData.value.attendeeIds = list;
};

const onAttachmentsChange = (list: any[]) => {
  formData.value.attachments = list;
};

// Luôn reset form theo initialData mới nhất trước khi người dùng thao tác.
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
    warnInvalidEventLink();
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
        class="relative mx-4 flex max-h-[90vh] w-full max-w-xl flex-col overflow-hidden rounded-3xl border-2 border-border bg-background shadow-[0_32px_100px_-48px_rgba(0,0,0,0.75)] cursor-default">

        <!-- Header -->
        <div
          class="sticky top-0 z-10 border-b-2 border-border bg-background/95 px-6 pb-4 pt-6 backdrop-blur cursor-default">
          <div class="inline-flex items-center gap-2 rounded-full border border-primary/20 bg-primary/10 px-4 py-2">
            <component :is="isEditing ? Pencil : CalendarPlus2" class="text-primary" data-icon="inline-start" />
            <h2 class="text-lg font-mono font-bold uppercase tracking-widest text-primary">
              {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
            </h2>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0">
          <!-- Scrollable body -->
          <div class="calendar-scrollbar min-h-0 flex-1 overflow-y-auto">
            <div class="space-y-5 p-6 pb-8 pr-5">

              <!-- Tiêu đề -->
              <div
                class="rounded-xl border-2 border-border bg-background p-4 shadow-[0_16px_34px_-30px_var(--color-foreground)] cursor-default">
                <label
                  class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2 cursor-default">TIÊU
                  ĐỀ *</label>
                <input v-model="formData.title" type="text" required placeholder="NHẬP TIÊU ĐỀ SỰ KIỆN..."
                  class="w-full rounded-lg border-2 border-border bg-background px-4 py-3 font-mono text-sm uppercase text-foreground placeholder-muted-foreground transition-colors focus:outline-none focus:border-primary" />
              </div>

              <!-- Mô tả -->
              <div
                class="rounded-xl border-2 border-border bg-background p-4 shadow-[0_16px_34px_-30px_var(--color-foreground)] cursor-default">
                <label
                  class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2 cursor-default">MÔ
                  TẢ</label>
                <textarea v-model="formData.description" rows="3" placeholder="MÔ TẢ CHI TIẾT SỰ KIỆN..."
                  class="w-full resize-none rounded-lg border-2 border-border bg-background px-4 py-3 font-mono text-sm uppercase text-foreground placeholder-muted-foreground transition-colors focus:outline-none focus:border-primary" />
              </div>

              <!-- Link sự kiện -->
              <div
                class="rounded-xl border-2 border-border bg-background p-4 shadow-[0_16px_34px_-30px_var(--color-foreground)] cursor-default">
                <label
                  class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2 cursor-default">LINK
                  SỰ KIỆN</label>
                <input v-model="formData.eventLink" type="text" placeholder="HTTPS://..."
                  class="w-full rounded-lg border-2 border-border bg-background px-4 py-3 font-mono text-sm text-foreground placeholder-muted-foreground transition-colors focus:outline-none focus:border-primary" />
              </div>

              <!-- Ngày & Giờ Section -->
              <EventTimeSection :show="show" :initial-date="initialData.eventDate" :initial-end-date="initialData.endDate"
                :initial-start-time="initialData.startTime" :initial-end-time="initialData.endTime"
                @change="onTimeChange" />

              <!-- Chế độ lặp lại Section -->
              <EventRecurrenceSection :initial-type="initialData.recurrenceType || 'NONE'"
                :initial-end-date="initialData.recurrenceEndDate" :event-date="formData.eventDate"
                @change="onRecurrenceChange" />
              <!-- Người tham gia Section -->
              <EventAttendeesSection :show="show" :room-members="roomMembers" :initial-attendee-ids="initialData.attendeeIds"
                @change="onAttendeesChange" />

              <!-- Tệp đính kèm Section -->
              <EventAttachmentsSection :show="show" :initial-attachments="initialData.attachments"
                @change="onAttachmentsChange" />

              <!-- Liên kết phòng họp (Voice space) -->
              <div
                class="rounded-xl border-2 border-border bg-background p-4 shadow-[0_16px_34px_-30px_var(--color-foreground)] cursor-default">
                <label
                  class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2 cursor-default">PHÒNG HỌP TRỰC TIẾP</label>
                <select v-model="formData.callRoomSpaceId"
                  class="w-full rounded-lg border-2 border-border bg-background px-4 py-3 font-mono text-sm uppercase text-foreground transition-colors focus:outline-none focus:border-primary">
                  <option :value="undefined">KHÔNG LIÊN KẾT</option>
                  <option v-for="space in voiceSpaces" :key="space.id" :value="space.id">
                    {{ space.name }}
                  </option>
                </select>
              </div>

              <!-- Cho phép chỉnh sửa -->
              <div
                class="flex items-center gap-3 rounded-xl border-2 border-border bg-background p-4 shadow-[0_16px_34px_-30px_var(--color-foreground)] cursor-default">
                <input v-model="formData.allowEditAll" type="checkbox"
                  class="h-4 w-4 cursor-pointer rounded-sm border-2 border-border bg-background text-primary focus:ring-0 focus:ring-offset-0" />
                <span
                  class="text-[10px] font-mono font-bold text-foreground uppercase tracking-widest cursor-pointer select-none"
                  @click="formData.allowEditAll = !formData.allowEditAll">CHO PHÉP MỌI NGƯỜI CHỈNH SỬA</span>
              </div>
            </div>
          </div>

          <!-- Footer actions -->
          <div
            class="sticky bottom-0 z-10 flex justify-end gap-3 border-t-2 border-border bg-background/95 p-6 pt-4 backdrop-blur">
            <Button type="button" variant="outline"
              class="rounded-full border-2 font-mono text-xs font-bold uppercase tracking-widest"
              @click="emit('update:show', false)">
              <X data-icon="inline-start" />
              Hủy
            </Button>
            <Button type="submit"
              class="rounded-full border-2 border-primary bg-primary font-mono text-xs font-bold uppercase tracking-widest text-primary-foreground shadow-[0_16px_34px_-22px_var(--color-primary)] hover:bg-background hover:text-primary">
              <component :is="isEditing ? Pencil : CalendarPlus2" data-icon="inline-start" />
              {{ isEditing ? "CẬP NHẬT" : "TẠO SỰ KIỆN" }}
            </Button>
          </div>
        </form>
      </div>
    </div>

    <!-- Warning dialog -->
    <CalendarNotificationDialog v-model:show="showWarning" type="warning" title="CẢNH BÁO" :message="warningMessage"
      confirm-text="ĐÃ HIỂU" @confirm="showWarning = false" />
  </Teleport>
</template>

<style scoped>
.create-btn {
  background: var(--primary);
  color: var(--primary-foreground);
}
</style>
