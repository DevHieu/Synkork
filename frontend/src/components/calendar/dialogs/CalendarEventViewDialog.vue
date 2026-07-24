<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
import { useRouter, useRoute } from "vue-router";
import {
  CalendarDays,
  Clock3,
  FileText,
  Link as LinkIcon,
  Paperclip,
  Pencil,
  ShieldCheck,
  Trash2,
  UserRound,
  Users,
  PhoneCall,
  CheckSquare,
  CalendarPlus,
  Sparkles,
} from "lucide-vue-next";
import type { CalendarEvent } from "@/types/CalendarEvent";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Separator } from "@/components/ui/separator";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { useSpaceStore } from "@/stores/spaceStore";

const props = defineProps<{
  show: boolean;
  event: CalendarEvent | null;
  currentUserId: string;
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "edit", event: CalendarEvent): void;
  (e: "delete", event: CalendarEvent): void;
  (e: "addToPersonalCalendar", event: CalendarEvent): void;
  (e: "summarizeAttachment", attachment: any, event: CalendarEvent): void;
}>();

const isCreator = computed(() => props.event?.createdById === props.currentUserId);
const canEdit = computed(() => {
  if (!props.event) return false;
  return props.event.createdById === props.currentUserId || props.event.allowEditAll;
});
const canDelete = computed(() => props.event?.createdById === props.currentUserId);

const creatorLabel = computed(() => {
  if (!props.event) return "";
  return isCreator.value ? "Do bạn tạo" : props.event.createdByDisplayName;
});

const recurrenceLabel = computed(() => {
  const recurrenceType = props.event?.recurrenceType;
  const recurrenceEndDate = props.event?.recurrenceEndDate;

  if (!recurrenceType || recurrenceType === "NONE") {
    return "Không lặp lại";
  }

  const baseLabel: Record<string, string> = {
    DAILY: "Lặp hằng ngày",
    WEEKLY: "Lặp hằng tuần",
    MONTHLY: "Lặp hằng tháng",
    YEARLY: "Lặp hằng năm",
  };

  const suffix = recurrenceEndDate
    ? `, đến ${dayjs(recurrenceEndDate).format("DD/MM/YYYY")}`
    : "";

  return `${baseLabel[recurrenceType] ?? recurrenceType}${suffix}`;
});

const formattedEventDate = computed(() =>
  props.event ? dayjs(props.event.displayDate || props.event.eventDate).format("dddd, DD/MM/YYYY") : "",
);

const formattedCreatedAt = computed(() =>
  props.event?.createdAt ? dayjs(props.event.createdAt).format("DD/MM/YYYY HH:mm") : "",
);

const formattedUpdatedAt = computed(() =>
  props.event?.updatedAt ? dayjs(props.event.updatedAt).format("DD/MM/YYYY HH:mm") : "",
);

const displayEventDate = computed(() =>
  props.event ? dayjs(props.event.displayDate || props.event.eventDate).format("DD/MM/YYYY") : "",
);

const displayEndDate = computed(() =>
  props.event ? dayjs(props.event.endDate || props.event.eventDate).format("DD/MM/YYYY") : "",
);

const attachments = computed(() => props.event?.attachments ?? []);
const attendees = computed(() => props.event?.attendees ?? []);
const eventLink = computed(() => props.event?.eventLink?.trim() ?? "");
const displayStartTime = computed(() =>
  props.event ? (props.event.displayStartTime || props.event.startTime).substring(0, 5) : "",
);
const displayEndTime = computed(() =>
  props.event ? (props.event.displayEndTime || props.event.endTime).substring(0, 5) : "",
);
const formatDateTimeLabel = (value: string | undefined, fallbackDate: string, fallbackTime: string) => {
  const dateTime = value ? dayjs(value) : dayjs(`${fallbackDate}T${fallbackTime}`);
  if (!dateTime.isValid()) return fallbackTime.substring(0, 5);
  return `${dateTime.format("HH:mm")} ${dateTime.format("DD/MM")}`;
};
const originalStartLabel = computed(() =>
  props.event
    ? formatDateTimeLabel(props.event.originalStartDateTime, props.event.eventDate, props.event.startTime)
    : "",
);
const originalEndLabel = computed(() =>
  props.event
    ? formatDateTimeLabel(props.event.originalEndDateTime, props.event.endDate || props.event.eventDate, props.event.endTime)
    : "",
);
const continuationLabel = computed(() => {
  if (!props.event) return "";
  if (props.event.continuesFromPreviousDay && props.event.continuesToNextDay) {
    return "Event này bắt đầu từ ngày hôm trước và tiếp tục ở ngày hôm sau";
  }
  if (props.event.continuesFromPreviousDay) return "Event này bắt đầu từ ngày hôm trước";
  if (props.event.continuesToNextDay) return "Event này tiếp tục ở ngày hôm sau";
  return "";
});

const openEdit = () => {
  if (props.event) {
    emit("edit", props.event);
  }
};

const openDelete = () => {
  if (props.event) {
    emit("delete", props.event);
  }
};

const voiceSpaceStore = useVoiceSpaceStore();
const spaceStore = useSpaceStore();

const joinVoiceRoom = () => {
  if (props.event?.callRoomSpaceId) {
    spaceStore.changeSpaceById(props.event.callRoomSpaceId, "VOICE");
    voiceSpaceStore.joinRoom(props.event.callRoomSpaceId, false);
    emit("update:show", false);
  }
};

const router = useRouter();
const route = useRoute();

const goToTaskSpace = async () => {
  if (props.event?.taskSpaceId) {
    await spaceStore.changeSpaceById(props.event.taskSpaceId, "TASK");
    const roomId = route.params.roomId;
    router.replace({
      path: `/rooms/task/${roomId}/${props.event.taskSpaceId}`,
      query: { cardId: props.event.taskId }
    });
    emit("update:show", false);
  }
};

const goToNoteSpace = async () => {
  if (props.event?.noteSpaceId) {
    await spaceStore.changeSpaceById(props.event.noteSpaceId, "NOTE");
    const roomId = route.params.roomId;
    router.replace({
      path: `/rooms/note/${roomId}/${props.event.noteSpaceId}`,
      query: { noteId: props.event.noteId }
    });
    emit("update:show", false);
  }
};
</script>

<template>
  <Dialog :open="show" @update:open="emit('update:show', $event)">
    <DialogContent
      class="overflow-hidden rounded-md border border-border/60 bg-background p-0 text-foreground shadow-lg sm:max-w-2xl cursor-default flex flex-col max-h-[90vh]"
    >
      <!-- Dialog Header -->
      <DialogHeader class="border-b border-border/60 bg-muted/30 px-6 py-4 cursor-default shrink-0">
        <div class="flex flex-col gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <Badge variant="default" class="font-sans text-[9px] font-bold uppercase tracking-wider bg-primary text-primary-foreground rounded-sm px-1.5 py-0.5">
              Sự kiện
            </Badge>
            <!-- schedule badge -->
            <Badge
              v-if="event?.schedule"
              variant="outline"
              class="font-sans text-[9px] font-bold uppercase tracking-wider text-amber-600 dark:text-amber-400 rounded-sm px-1.5 py-0.5 border-amber-400/60"
            >
              Sự kiện liên tục
            </Badge>
            <Badge
              v-if="event?.allowEditAll"
              variant="outline"
              class="font-sans text-[9px] font-bold uppercase tracking-wider text-muted-foreground rounded-sm px-1.5 py-0.5 border-border/60"
            >
              Mọi người cùng sửa
            </Badge>
          </div>

          <div class="flex flex-col gap-1.5 min-w-0">
            <DialogTitle class="font-sans text-lg font-bold text-foreground leading-tight break-words">
              {{ event?.title }}
            </DialogTitle>
            <p class="font-sans text-[11px] text-muted-foreground flex items-center gap-1.5 uppercase tracking-wider">
              <CalendarDays class="h-3.5 w-3.5 text-muted-foreground/75" />
              {{ formattedEventDate }}
            </p>
          </div>
        </div>
      </DialogHeader>

      <!-- Scrollable content -->
      <ScrollArea class="flex-1 overflow-y-auto min-h-0">
        <div class="grid grid-cols-1 md:grid-cols-5 gap-5 p-5">
          
          <!-- Column Trái: Nội dung chính (Mô tả, Link, Room) - Chiếm 3/5 cột -->
          <div class="md:col-span-3 space-y-4">
            <!-- Mô tả -->
            <div class="rounded-md border border-border/60 bg-card overflow-hidden">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <FileText class="text-primary h-3.5 w-3.5" />
                <h3 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Mô tả chi tiết
                </h3>
              </div>
              <div class="p-3.5 font-sans text-xs leading-relaxed text-foreground whitespace-pre-wrap break-words min-h-[100px] cursor-default">
                {{ event?.description || "Không có mô tả cho sự kiện này." }}
              </div>
            </div>

            <!-- Phòng họp trực tiếp (Voice Room) -->
            <div v-if="event?.callRoomSpaceId" class="rounded-md border border-border/60 bg-card overflow-hidden">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <PhoneCall class="text-primary h-3.5 w-3.5" />
                <h3 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Phòng họp trực tiếp
                </h3>
              </div>
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 p-3.5">
                <div class="min-w-0">
                  <p class="font-sans text-xs font-bold text-foreground truncate">
                    {{ event?.callRoomSpaceName || 'Phòng voice' }}
                  </p>
                  <p class="font-sans text-[9px] text-muted-foreground/80 mt-0.5 uppercase tracking-wider">
                    Click tham gia cuộc họp bằng âm thanh và hình ảnh
                  </p>
                </div>
                <Button
                  @click="joinVoiceRoom"
                  size="sm"
                  class="rounded-sm bg-primary font-sans text-[10px] font-bold text-primary-foreground px-3.5 py-1.5 shadow-sm hover:bg-primary/95 shrink-0"
                >
                  Vào phòng call
                </Button>
              </div>
            </div>

            <!-- Task liên kết -->
            <div v-if="event?.taskId" 
              @click="goToTaskSpace"
              class="rounded-md border border-border/60 bg-card overflow-hidden cursor-pointer hover:bg-muted/10 transition-colors">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <CheckSquare class="text-primary h-3.5 w-3.5" />
                <h3 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Task liên kết
                </h3>
              </div>
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 p-3.5">
                <div class="min-w-0">
                  <p class="font-sans text-xs font-bold text-foreground truncate">
                    {{ event?.taskName || 'Xem chi tiết task' }}
                  </p>
                  <p class="font-sans text-[9px] text-muted-foreground/80 mt-0.5 uppercase tracking-wider">
                    Click để chuyển đến kênh task chứa công việc này
                  </p>
                </div>
                <Button
                  size="sm"
                  class="rounded-sm bg-primary font-sans text-[10px] font-bold text-primary-foreground px-3.5 py-1.5 shadow-sm hover:bg-primary/95 shrink-0 animate-none pointer-events-none"
                >
                  Mở Task Space
                </Button>
              </div>
            </div>

            <!-- Note liên kết -->
            <div v-if="event?.noteId" 
              @click="goToNoteSpace"
              class="rounded-md border border-border/60 bg-card overflow-hidden cursor-pointer hover:bg-muted/10 transition-colors">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <FileText class="text-primary h-3.5 w-3.5" />
                <h3 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Note liên kết
                </h3>
              </div>
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 p-3.5">
                <div class="min-w-0">
                  <p class="font-sans text-xs font-bold text-foreground truncate">
                    {{ event?.noteTitle || 'Xem chi tiết note' }}
                  </p>
                  <p class="font-sans text-[9px] text-muted-foreground/80 mt-0.5 uppercase tracking-wider">
                    Click để chuyển đến kênh note chứa ghi chú này
                  </p>
                </div>
                <Button
                  size="sm"
                  class="rounded-sm bg-primary font-sans text-[10px] font-bold text-primary-foreground px-3.5 py-1.5 shadow-sm hover:bg-primary/95 shrink-0 animate-none pointer-events-none"
                >
                  Mở Note Space
                </Button>
              </div>
            </div>

            <!-- Link sự kiện -->
            <div v-if="eventLink" class="rounded-md border border-border/60 bg-card overflow-hidden">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <LinkIcon class="text-primary h-3.5 w-3.5" />
                <h3 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Link sự kiện
                </h3>
              </div>
              <div class="flex items-center justify-between gap-3 p-3.5 bg-muted/5">
                <p class="min-w-0 break-all font-sans text-xs text-foreground/90 font-medium">
                  {{ eventLink }}
                </p>
                <a
                  :href="eventLink"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="inline-flex shrink-0 items-center justify-center gap-1.5 rounded-sm border border-border bg-background px-3 py-1.5 font-sans text-[10px] font-semibold text-muted-foreground transition-colors hover:bg-accent"
                >
                  <LinkIcon class="h-3 w-3" />
                  Mở link
                </a>
              </div>
            </div>
          </div>

          <!-- Column Phải: Metadata Sidebar (Thời gian, Người tạo, Người tham gia, Tệp đính kèm) - Chiếm 2/5 cột -->
          <div class="md:col-span-2 space-y-4">
            <!-- Khung giờ -->
            <div class="rounded-md border border-primary/20 bg-primary/5 p-4 space-y-3">
              <div>
                <p class="font-sans text-[9px] font-bold uppercase tracking-wider text-primary/80">
                  Thời gian hoạt động
                </p>
                <p class="font-sans text-xs font-bold text-primary mt-1.5 flex items-center gap-1.5">
                  <Clock3 class="h-3.5 w-3.5 text-primary" />
                  <span>{{ displayStartTime }} &rarr; {{ displayEndTime }}</span>
                </p>
                <p class="font-sans text-[9px] text-muted-foreground/80 mt-1">
                  Múi giờ: {{ originalStartLabel }} - {{ originalEndLabel }}
                </p>
                <p v-if="continuationLabel" class="mt-1 font-sans text-[9px] font-medium text-warning-foreground bg-warning/10 px-2 py-0.5 rounded-sm inline-block uppercase tracking-wider">
                  {{ continuationLabel }}
                </p>
              </div>

              <div class="pt-3 border-t border-primary/10 grid grid-cols-3 gap-2">
                <div>
                  <p class="font-sans text-[8px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                    Ngày bắt đầu
                  </p>
                  <p class="font-sans text-[11px] font-medium text-foreground mt-0.5">
                    {{ displayEventDate }}
                  </p>
                </div>
                <div>
                  <p class="font-sans text-[8px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                    Ngày kết thúc
                  </p>
                  <p class="font-sans text-[11px] font-medium text-foreground mt-0.5">
                    {{ displayEndDate }}
                  </p>
                </div>
                <div>
                  <p class="font-sans text-[8px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                    Lặp lại
                  </p>
                  <p class="font-sans text-[11px] font-medium text-foreground mt-0.5 truncate" :title="recurrenceLabel">
                    {{ recurrenceLabel }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Người tạo -->
            <div class="rounded-md border border-border/60 bg-card overflow-hidden">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <UserRound class="text-primary h-3.5 w-3.5" />
                <h4 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Người tạo
                </h4>
              </div>
              <div class="p-3.5 space-y-3">
                <div class="flex items-center gap-3">
                  <Avatar class="size-8 border border-border/60 shrink-0 rounded-sm">
                    <AvatarImage
                      v-if="event?.createdByAvatarUrl"
                      :src="event.createdByAvatarUrl"
                      :alt="creatorLabel"
                    />
                    <AvatarFallback />
                  </Avatar>
                  <div class="min-w-0">
                    <p class="truncate font-sans text-xs font-bold text-foreground">
                      {{ creatorLabel }}
                    </p>
                    <p class="truncate font-sans text-[9px] text-muted-foreground">
                      @{{ event?.createdByUsername }}
                    </p>
                  </div>
                </div>
                
                <div class="pt-2.5 border-t border-border/40 grid grid-cols-2 gap-2 text-[9px] text-muted-foreground">
                  <div>
                    <span class="block text-[8px] font-semibold uppercase tracking-wider text-muted-foreground/80">Tạo ngày</span>
                    <span class="font-medium mt-0.5 block">{{ formattedCreatedAt }}</span>
                  </div>
                  <div>
                    <span class="block text-[8px] font-semibold uppercase tracking-wider text-muted-foreground/80">Cập nhật</span>
                    <span class="font-medium mt-0.5 block">{{ formattedUpdatedAt }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Người tham gia -->
            <div class="rounded-md border border-border/60 bg-card overflow-hidden">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <Users class="text-primary h-3.5 w-3.5" />
                <h4 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Tham gia ({{ attendees.length }})
                </h4>
              </div>
              <div class="p-3.5">
                <div v-if="attendees.length > 0" class="flex flex-wrap gap-1.5">
                  <Badge
                    v-for="attendee in attendees"
                    :key="attendee.memberId"
                    variant="secondary"
                    class="font-sans text-[9px] font-medium rounded-sm px-1.5 py-0.5"
                  >
                    {{ attendee.displayName || attendee.username }}
                  </Badge>
                </div>
                <p v-else class="font-sans text-[10px] text-muted-foreground italic uppercase tracking-wider">
                  Chưa có ai tham gia.
                </p>
              </div>
            </div>

            <!-- Tệp đính kèm -->
            <div class="rounded-md border border-border/60 bg-card overflow-hidden">
              <div class="flex items-center gap-2 border-b border-border/60 bg-muted/20 px-3.5 py-2">
                <Paperclip class="text-primary h-3.5 w-3.5" />
                <h4 class="font-sans text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
                  Đính kèm ({{ attachments.length }})
                </h4>
              </div>
              <div class="p-3.5">
                <div v-if="attachments.length > 0" class="flex flex-col gap-2 max-h-[150px] overflow-y-auto calendar-scrollbar">
                  <div
                    v-for="attachment in attachments"
                    :key="`${attachment.name}-${attachment.fileUrl}`"
                    class="flex items-center justify-between gap-2 p-2 rounded-sm border border-border/60 bg-muted/15"
                  >
                    <div class="min-w-0">
                      <p class="truncate font-sans text-xs font-semibold text-foreground" :title="attachment.name">
                        {{ attachment.name }}
                      </p>
                      <p class="font-sans text-[9px] text-muted-foreground/80 mt-0.5">
                        {{ attachment.size }} KB
                      </p>
                    </div>
                    <div class="flex items-center gap-1 shrink-0">
                      <Button
                        v-if="attachment.fileUrl"
                        type="button"
                        variant="outline"
                        size="sm"
                        class="h-auto py-1 px-2 text-[9px] font-sans font-semibold rounded-sm bg-background hover:bg-accent hover:text-primary transition-colors border-border shadow-none"
                        @click="emit('summarizeAttachment', attachment, event!)"
                        title="Tóm tắt nội dung file bằng AI"
                      >
                        <Sparkles class="h-2.5 w-2.5 mr-1" />
                        AI Tóm tắt
                      </Button>
                      <a
                        v-if="attachment.fileUrl"
                        :href="attachment.fileUrl"
                        target="_blank"
                        rel="noreferrer"
                        class="inline-flex items-center gap-1 rounded-sm border border-border bg-background px-2 py-1 font-sans text-[9px] font-semibold text-muted-foreground hover:bg-accent shrink-0 transition-colors"
                      >
                        <LinkIcon class="h-2.5 w-2.5" />
                        Mở
                      </a>
                    </div>
                  </div>
                </div>
                <p v-else class="font-sans text-[10px] text-muted-foreground italic uppercase tracking-wider">
                  Không có đính kèm.
                </p>
              </div>
            </div>
          </div>
          
        </div>
      </ScrollArea>

      <!-- Footer Buttons -->
      <div class="flex flex-wrap items-center justify-end gap-2 border-t border-border/60 bg-background px-6 py-3.5 shrink-0">
        <Button
          type="button"
          variant="outline"
          size="sm"
          class="rounded-sm border border-border/60 bg-background font-sans text-xs font-semibold px-4 py-2 hover:bg-accent"
          @click="emit('update:show', false)"
        >
          Đóng
        </Button>

        <Button
          v-if="event"
          type="button"
          variant="secondary"
          size="sm"
          class="rounded-sm font-sans text-xs font-semibold px-4 py-2 border border-border/60 shadow-sm"
          @click="emit('addToPersonalCalendar', event)"
        >
          <CalendarPlus class="mr-1.5 h-3.5 w-3.5" />
          Lưu lịch cá nhân
        </Button>

        <Button
          v-if="canDelete"
          type="button"
          variant="destructive"
          size="sm"
          class="rounded-sm font-sans text-xs font-semibold px-4 py-2"
          @click="openDelete"
        >
          <Trash2 class="mr-1.5 h-3.5 w-3.5" />
          Xóa sự kiện
        </Button>

        <Button
          v-if="canEdit"
          type="button"
          size="sm"
          class="rounded-sm bg-primary font-sans text-xs font-semibold text-primary-foreground px-4 py-2 shadow-sm hover:bg-primary/95"
          @click="openEdit"
        >
          <Pencil class="mr-1.5 h-3.5 w-3.5" />
          Chỉnh sửa
        </Button>
      </div>
    </DialogContent>
  </Dialog>
</template>
