<script setup lang="ts">
import { watch, computed } from "vue";
import { CalendarPlus2, Pencil, X, Check } from "lucide-vue-next";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import CalendarNotificationDialog from "./CalendarNotificationDialog.vue";
import EventTimeSection from "../sub-components/EventTimeSection.vue";
import EventRecurrenceSection from "../sub-components/EventRecurrenceSection.vue";
import EventAttendeesSection from "../sub-components/EventAttendeesSection.vue";
import EventAttachmentsSection from "../sub-components/EventAttachmentsSection.vue";
import EventTaskSection from "../sub-components/EventTaskSection.vue";
import EventNoteSection from "../sub-components/EventNoteSection.vue";
import { useEventForm, type EventFormData } from "../../composable/useEventForm";
import type { Member } from "@/types/Member";
import { useSpaceStore } from "@/stores/spaceStore";
import { useRoomsStore } from "@/stores/roomStore";
import { useUserStore } from "@/stores/userStore";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { Checkbox } from "@/components/ui/checkbox/index.ts";

const spaceStore = useSpaceStore();
const roomsStore = useRoomsStore();
const userStore = useUserStore();
const roomMemberStore = useRoomMemberStore();

const voiceSpaces = computed(() => spaceStore.voiceSpaces || []);

// Khai báo Props và Emits
const props = defineProps<{
  show: boolean;
  isEditing: boolean;
  initialData: EventFormData;
  roomMembers: Member[];
  isSaving?: boolean;
  isSuccess?: boolean;
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

const onTaskChange = (data: { spaceId?: string; taskId?: string }) => {
  formData.value.taskSpaceId = data.spaceId;
  formData.value.taskId = data.taskId;
};

const onNoteChange = (data: { spaceId?: string; noteId?: string }) => {
  formData.value.noteSpaceId = data.spaceId;
  formData.value.noteId = data.noteId;
};

// Luôn reset form theo initialData mới nhất trước khi người dùng thao tác.
// Đồng bộ trạng thái khi Dialog đóng/mở và tải danh sách thành viên phòng
watch(
  () => props.show,
  async (isOpen) => {
    if (isOpen) {
      resetForm(props.initialData);

      const roomId = roomsStore.currentRoom?.id;
      const username = userStore.user?.username;
      if (roomId && username) {
        try {
          await roomMemberStore.fetchMembers(roomId, username);
        } catch (error) {
          console.error("Lỗi khi tải thành viên phòng:", error);
        }
      }
    }
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
  <Dialog :open="show" @update:open="emit('update:show', $event)">
    <DialogContent
      class="overflow-hidden rounded-md border border-border/60 bg-background p-0 text-foreground shadow-lg sm:max-w-2xl cursor-default flex flex-col max-h-[90vh]">
      <DialogHeader class="border-b border-border/60 bg-muted/40 px-5 py-3.5 cursor-default shrink-0">
        <div class="flex items-center gap-2">
          <div class="inline-flex items-center gap-2 rounded-sm border border-primary/10 bg-primary/5 px-2.5 py-1">
            <component :is="isEditing ? Pencil : CalendarPlus2" class="text-primary h-4 w-4" />
            <h2 class="text-xs font-sans font-bold text-primary uppercase tracking-wider">
              {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
            </h2>
          </div>
        </div>
      </DialogHeader>

      <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0 overflow-hidden">
        <!-- Scrollable body -->
        <ScrollArea class="flex-1 overflow-y-auto">
          <div class="space-y-4 px-6 py-5">
            <!-- Tiêu đề -->
            <div class="space-y-1.5">
              <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Tiêu đề
                *</Label>
              <Input v-model="formData.title" type="text" required placeholder="Nhập tiêu đề sự kiện..."
                class="w-full rounded-md border-border/60 h-10 font-sans" />
            </div>

            <!-- Mô tả -->
            <div class="space-y-1.5">
              <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Mô
                tả</Label>
              <Textarea v-model="formData.description" placeholder="Mô tả chi tiết sự kiện..."
                class="w-full min-h-20 rounded-md border-border/60 font-sans text-xs" />
            </div>

            <!-- Link sự kiện -->
            <div class="space-y-1.5">
              <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Link sự
                kiện</Label>
              <Input v-model="formData.eventLink" type="text" placeholder="https://..."
                class="w-full rounded-md border-border/60 h-10 font-sans" />
            </div>

            <!-- Ngày & Giờ Section -->
            <div class="space-y-1.5">
              <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Thời gian sự
                kiện</Label>
              <EventTimeSection :show="show" :initial-date="initialData.eventDate"
                :initial-end-date="initialData.endDate" :initial-start-time="initialData.startTime"
                :initial-end-time="initialData.endTime" @change="onTimeChange" />
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <!-- Chế độ lặp lại Section -->
              <div class="space-y-1.5">
                <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Chế độ lặp
                  lại</Label>
                <EventRecurrenceSection :initial-type="initialData.recurrenceType || 'NONE'"
                  :initial-end-date="initialData.recurrenceEndDate" :event-date="formData.eventDate"
                  @change="onRecurrenceChange" />
              </div>

              <!-- Liên kết phòng họp (Voice space) -->
              <div class="space-y-1.5">
                <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Phòng họp
                  trực tiếp</Label>
                <Select :model-value="formData.callRoomSpaceId || 'none'"
                  @update:model-value="val => formData.callRoomSpaceId = val === 'none' ? undefined : val">
                  <SelectTrigger class="w-full rounded-md border-border/60 h-10 font-sans">
                    <SelectValue placeholder="---" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectItem value="none">---</SelectItem>
                      <SelectItem v-for="space in voiceSpaces" :key="space.id" :value="space.id">
                        {{ space.name }}
                      </SelectItem>
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <!-- Liên kết Task -->
            <EventTaskSection
              :show="show"
              :initial-space-id="initialData.taskSpaceId"
              :initial-task-id="initialData.taskId"
              @change="onTaskChange"
            />

            <!-- Liên kết Note -->
            <EventNoteSection
              :show="show"
              :initial-space-id="initialData.noteSpaceId"
              :initial-note-id="initialData.noteId"
              @change="onNoteChange"
            />

            <!-- Người tham gia Section -->
            <div class="space-y-1.5">
              <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Người tham
                gia</Label>
              <EventAttendeesSection :show="show" :room-members="roomMembers"
                :initial-attendee-ids="formData.attendeeIds" @change="onAttendeesChange" />
            </div>

            <!-- Tệp đính kèm Section -->
            <div class="space-y-1.5">
              <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Tệp đính
                kèm</Label>
              <EventAttachmentsSection :show="show" :initial-attachments="formData.attachments"
                @change="onAttachmentsChange" />
            </div>

            <!-- Cho phép mọi người chỉnh sửa -->
            <div class="flex items-center space-x-2 rounded-md border border-border/60 bg-muted/15 p-3">
              <Checkbox id="allow-edit-all" v-model="formData.allowEditAll" type="checkbox"
                class="h-3.5 w-3.5 cursor-pointer rounded-sm border-border/60 text-primary focus:ring-primary" />
              <label for="allow-edit-all"
                class="text-[11px] font-sans font-semibold text-muted-foreground uppercase tracking-wider cursor-pointer select-none">
                Cho phép mọi người chỉnh sửa
              </label>
            </div>
          </div>
        </ScrollArea>

        <!-- Footer actions -->
        <div
          class="flex justify-end gap-2.5 border-t border-border/60 bg-background/95 p-4.5 pt-3.5 backdrop-blur shrink-0">
          <Button type="button" variant="outline" size="sm"
            class="rounded-sm border border-border/60 bg-background font-sans text-xs font-semibold px-4 py-2 hover:bg-accent"
            @click="emit('update:show', false)">
            <X class="mr-1.5 h-3.5 w-3.5" />
            Hủy
          </Button>
          <Button type="submit" size="sm" :disabled="isSaving || isSuccess"
            class="rounded-sm font-sans text-xs font-semibold px-4 py-2 shadow-sm transition-all duration-300 relative overflow-hidden"
            :class="[
              isSuccess ? 'bg-emerald-500 hover:bg-emerald-600 text-white' : 'bg-primary hover:bg-primary/90 text-primary-foreground'
            ]">
            <span class="flex items-center gap-1.5 transition-all duration-300" :class="{ 'opacity-0 scale-95': isSaving || isSuccess }">
              <component :is="isEditing ? Pencil : CalendarPlus2" class="h-3.5 w-3.5" />
              {{ isEditing ? "Cập nhật" : "Tạo sự kiện" }}
            </span>
            
            <span class="absolute inset-0 flex items-center justify-center gap-1.5 transition-all duration-300" :class="{ 'opacity-100 scale-100': isSaving, 'opacity-0 scale-105 pointer-events-none': !isSaving }">
              <div class="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              <span>Đang lưu...</span>
            </span>

            <span class="absolute inset-0 flex items-center justify-center gap-1.5 transition-all duration-300" :class="{ 'opacity-100 scale-100': isSuccess, 'opacity-0 scale-95 pointer-events-none': !isSuccess }">
              <Check class="h-3.5 w-3.5" />
              <span>Đã lưu!</span>
            </span>
          </Button>
        </div>
      </form>
    </DialogContent>
  </Dialog>

  <!-- Warning dialog -->
  <CalendarNotificationDialog v-model:show="showWarning" type="warning" title="Cảnh báo" :message="warningMessage"
    confirm-text="Đã hiểu" @confirm="showWarning = false" />
</template>
