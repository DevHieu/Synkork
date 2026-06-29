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
  validate, resetForm,
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
    emit("save", { ...formData.value });
  }
};
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
      <!-- Overlay -->
      <div class="absolute inset-0 bg-black/40 backdrop-blur-[2px]" @click="emit('update:show', false)" />

      <!-- Dialog -->
      <div
        class="relative mx-4 flex max-h-[90vh] w-full max-w-xl flex-col overflow-hidden rounded-lg border border-border/80 bg-background shadow-2xl cursor-default">

        <!-- Header -->
        <div
          class="sticky top-0 z-10 border-b border-border/60 bg-background/95 px-6 pb-3 pt-5 backdrop-blur cursor-default">
          <div class="inline-flex items-center gap-2 rounded-md border border-primary/10 bg-primary/5 px-3 py-1.5">
            <component :is="isEditing ? Pencil : CalendarPlus2" class="text-primary h-4 w-4" data-icon="inline-start" />
            <h2 class="text-sm font-sans font-bold text-primary">
              {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
            </h2>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0">
          <!-- Scrollable body -->
          <div class="calendar-scrollbar min-h-0 flex-1 overflow-y-auto">
            <div class="space-y-4.5 p-6 pb-8 pr-5">

              <!-- Tiêu đề -->
              <div
                class="rounded-md border border-border/60 bg-card/30 p-4 cursor-default">
                <label
                  class="block text-[10px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">Tiêu đề *</label>
                <input v-model="formData.title" type="text" required placeholder="Nhập tiêu đề sự kiện..."
                  class="w-full rounded-md border border-input bg-transparent px-3 py-2 font-sans text-sm text-foreground placeholder-muted-foreground transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" />
              </div>

              <!-- Mô tả -->
              <div
                class="rounded-md border border-border/60 bg-card/30 p-4 cursor-default">
                <label
                  class="block text-[10px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">Mô tả</label>
                <textarea v-model="formData.description" rows="3" placeholder="Mô tả chi tiết sự kiện..."
                  class="w-full resize-none rounded-md border border-input bg-transparent px-3 py-2 font-sans text-sm text-foreground placeholder-muted-foreground transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" />
              </div>

              <!-- Link sự kiện -->
              <div
                class="rounded-md border border-border/60 bg-card/30 p-4 cursor-default">
                <label
                  class="block text-[10px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">Link sự kiện</label>
                <input v-model="formData.eventLink" type="text" placeholder="https://..."
                  class="w-full rounded-md border border-input bg-transparent px-3 py-2 font-sans text-sm text-foreground placeholder-muted-foreground transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring" />
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
                class="rounded-md border border-border/60 bg-card/30 p-4 cursor-default">
                <label
                  class="block text-[10px] font-sans font-semibold text-muted-foreground uppercase tracking-wider mb-2 cursor-default">Phòng họp trực tiếp</label>
                <select v-model="formData.callRoomSpaceId"
                  class="w-full rounded-md border border-input bg-transparent px-3 py-2 font-sans text-sm text-foreground transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring">
                  <option :value="undefined">Không liên kết</option>
                  <option v-for="space in voiceSpaces" :key="space.id" :value="space.id">
                    {{ space.name }}
                  </option>
                </select>
              </div>

              <!-- Cho phép chỉnh sửa -->
              <div
                class="flex items-center gap-3 rounded-md border border-border/60 bg-card/30 p-4 cursor-default">
                <input v-model="formData.allowEditAll" type="checkbox"
                  class="h-4 w-4 cursor-pointer rounded-sm border border-input bg-background text-primary focus:ring-0 focus:ring-offset-0" />
                <span
                  class="text-[10px] font-sans font-semibold text-foreground uppercase tracking-wider cursor-pointer select-none"
                  @click="formData.allowEditAll = !formData.allowEditAll">Cho phép mọi người chỉnh sửa</span>
              </div>
            </div>
          </div>

          <!-- Footer actions -->
          <div
            class="sticky bottom-0 z-10 flex justify-end gap-3 border-t border-border/60 bg-background/95 p-4.5 pt-3.5 backdrop-blur">
            <Button type="button" variant="outline"
              class="rounded-md border border-border bg-background font-sans text-xs font-semibold px-4 py-2 hover:bg-accent"
              @click="emit('update:show', false)">
              <X class="h-3.5 w-3.5" data-icon="inline-start" />
              Hủy
            </Button>
            <Button type="submit"
              class="rounded-md bg-primary font-sans text-xs font-semibold text-primary-foreground px-4 py-2 shadow-sm hover:bg-primary/90">
              <component :is="isEditing ? Pencil : CalendarPlus2" class="h-3.5 w-3.5" data-icon="inline-start" />
              {{ isEditing ? "Cập nhật" : "Tạo sự kiện" }}
            </Button>
          </div>
        </form>
      </div>
    </div>

    <!-- Warning dialog -->
    <CalendarNotificationDialog v-model:show="showWarning" type="warning" title="Cảnh báo" :message="warningMessage"
      confirm-text="Đã hiểu" @confirm="showWarning = false" />
  </Teleport>
</template>

<style scoped>
.create-btn {
  background: var(--primary);
  color: var(--primary-foreground);
}
</style>
