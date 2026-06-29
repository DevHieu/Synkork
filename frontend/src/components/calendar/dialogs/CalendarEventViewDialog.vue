<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
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
</script>

<template>
  <Dialog :open="show" @update:open="emit('update:show', $event)">
    <DialogContent
      class="overflow-hidden rounded-lg border border-border/80 bg-background p-0 text-foreground shadow-2xl sm:max-w-3xl cursor-default flex flex-col max-h-[90vh]"
    >
      <!-- Dialog Header -->
      <DialogHeader class="border-b border-border/60 bg-muted/30 px-6 py-4 cursor-default shrink-0">
        <div class="flex flex-col gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <Badge variant="default" class="font-sans text-[10px] font-semibold uppercase tracking-wider bg-primary text-primary-foreground">
              Sự kiện
            </Badge>
            <Badge
              v-if="event?.allowEditAll"
              variant="outline"
              class="font-sans text-[10px] font-semibold uppercase tracking-wider text-muted-foreground"
            >
              Mọi người cùng sửa
            </Badge>
          </div>

          <div class="flex flex-col gap-2 min-w-0">
            <DialogTitle class="font-sans text-xl font-bold text-foreground leading-tight break-words">
              {{ event?.title }}
            </DialogTitle>
            <p class="font-sans text-xs text-muted-foreground flex items-center gap-1.5">
              <CalendarDays class="h-3.5 w-3.5 text-muted-foreground/70" />
              {{ formattedEventDate }}
            </p>
          </div>
        </div>
      </DialogHeader>

      <!-- Scrollable content -->
      <ScrollArea class="flex-1 overflow-y-auto min-h-0">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 p-6">
          
          <!-- Column Trái: Nội dung chính (Mô tả, Link, Room) -->
          <div class="md:col-span-2 space-y-5">
            <!-- Mô tả -->
            <div class="space-y-2">
              <div class="flex items-center gap-2 border-b border-border/60 pb-1.5">
                <FileText class="text-primary h-4 w-4" />
                <h3 class="font-sans text-xs font-semibold text-foreground uppercase tracking-wider">
                  Mô tả chi tiết
                </h3>
              </div>
              <p class="whitespace-pre-wrap break-words rounded-md border border-border/60 bg-muted/15 p-4 font-sans text-xs leading-relaxed text-foreground cursor-default min-h-[100px]">
                {{ event?.description || "Không có mô tả cho sự kiện này." }}
              </p>
            </div>

            <!-- Phòng họp trực tiếp (Voice Room) -->
            <div v-if="event?.callRoomSpaceId" class="space-y-2">
              <div class="flex items-center gap-2 border-b border-border/60 pb-1.5">
                <PhoneCall class="text-primary h-4 w-4" />
                <h3 class="font-sans text-xs font-semibold text-foreground uppercase tracking-wider">
                  Phòng họp trực tiếp
                </h3>
              </div>
              <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 rounded-md border border-primary/20 bg-primary/5 p-4">
                <div class="min-w-0">
                  <p class="font-sans text-xs font-bold text-foreground truncate">
                    {{ event?.callRoomSpaceName || 'Phòng voice' }}
                  </p>
                  <p class="font-sans text-[10px] text-muted-foreground/80 mt-0.5">
                    Click để tham gia cuộc họp bằng âm thanh và hình ảnh
                  </p>
                </div>
                <Button
                  @click="joinVoiceRoom"
                  size="sm"
                  class="rounded-md bg-primary font-sans text-xs font-semibold text-primary-foreground px-4 py-1.5 shadow-sm hover:bg-primary/95 shrink-0"
                >
                  Vào phòng call
                </Button>
              </div>
            </div>

            <!-- Link sự kiện -->
            <div v-if="eventLink" class="space-y-2">
              <div class="flex items-center gap-2 border-b border-border/60 pb-1.5">
                <LinkIcon class="text-primary h-4 w-4" />
                <h3 class="font-sans text-xs font-semibold text-foreground uppercase tracking-wider">
                  Link sự kiện
                </h3>
              </div>
              <div class="flex items-center justify-between gap-3 rounded-md border border-border/60 bg-muted/10 p-3">
                <p class="min-w-0 break-all font-sans text-xs text-foreground/90 font-medium">
                  {{ eventLink }}
                </p>
                <a
                  :href="eventLink"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="inline-flex shrink-0 items-center justify-center gap-1.5 rounded-md border border-border bg-background px-3 py-1.5 font-sans text-xs font-semibold text-muted-foreground transition-colors hover:bg-accent"
                >
                  <LinkIcon class="h-3 w-3" />
                  Mở link
                </a>
              </div>
            </div>
          </div>

          <!-- Column Phải: Metadata Sidebar (Thời gian, Người tạo, Người tham gia, Tệp đính kèm) -->
          <div class="space-y-5">
            <!-- Khung giờ -->
            <div class="rounded-lg border border-primary/20 bg-primary/5 p-4 space-y-3">
              <div>
                <p class="font-sans text-[10px] font-bold uppercase tracking-wider text-primary/80">
                  Thời gian hoạt động
                </p>
                <p class="font-sans text-sm font-bold text-foreground mt-1 flex items-center gap-1.5">
                  <Clock3 class="h-4 w-4 text-primary" />
                  <span>{{ displayStartTime }} → {{ displayEndTime }}</span>
                </p>
                <p class="font-sans text-[10px] text-muted-foreground mt-1">
                  Múi giờ địa phương ({{ originalStartLabel }} - {{ originalEndLabel }})
                </p>
                <p v-if="continuationLabel" class="mt-1 font-sans text-[10px] font-medium text-warning-foreground bg-warning/10 px-2 py-0.5 rounded-sm inline-block">
                  {{ continuationLabel }}
                </p>
              </div>

              <div class="pt-2.5 border-t border-primary/10 grid grid-cols-2 gap-2">
                <div>
                  <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                    Ngày bắt đầu
                  </p>
                  <p class="font-sans text-xs font-medium text-foreground mt-0.5">
                    {{ displayEventDate }}
                  </p>
                </div>
                <div>
                  <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                    Lặp lại
                  </p>
                  <p class="font-sans text-xs font-medium text-foreground mt-0.5 truncate" :title="recurrenceLabel">
                    {{ recurrenceLabel }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Người tạo -->
            <div class="rounded-lg border border-border/60 bg-card/40 p-4 space-y-3">
              <div class="flex items-center gap-2">
                <UserRound class="text-primary h-3.5 w-3.5" />
                <h4 class="font-sans text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                  Người tạo sự kiện
                </h4>
              </div>
              <div class="flex items-center gap-3">
                <Avatar class="size-9 border border-border shrink-0">
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
                  <p class="truncate font-sans text-[10px] text-muted-foreground">
                    @{{ event?.createdByUsername }}
                  </p>
                </div>
              </div>
              
              <div class="pt-2 border-t border-border/40 grid grid-cols-2 gap-2 text-[10px] text-muted-foreground">
                <div>
                  <span class="block text-[8px] font-semibold uppercase tracking-wider">Tạo ngày</span>
                  <span class="font-medium mt-0.5 block">{{ formattedCreatedAt }}</span>
                </div>
                <div>
                  <span class="block text-[8px] font-semibold uppercase tracking-wider">Cập nhật</span>
                  <span class="font-medium mt-0.5 block">{{ formattedUpdatedAt }}</span>
                </div>
              </div>
            </div>

            <!-- Người tham gia -->
            <div class="rounded-lg border border-border/60 bg-card/40 p-4 space-y-2.5">
              <div class="flex items-center gap-2">
                <Users class="text-primary h-3.5 w-3.5" />
                <h4 class="font-sans text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                  Người tham gia ({{ attendees.length }})
                </h4>
              </div>
              <div v-if="attendees.length > 0" class="flex flex-wrap gap-1.5">
                <Badge
                  v-for="attendee in attendees"
                  :key="attendee.userId"
                  variant="secondary"
                  class="font-sans text-[10px] font-medium"
                >
                  {{ attendee.displayName || attendee.username }}
                </Badge>
              </div>
              <p v-else class="font-sans text-[11px] text-muted-foreground italic">
                Chưa có ai tham gia.
              </p>
            </div>

            <!-- Tệp đính kèm -->
            <div class="rounded-lg border border-border/60 bg-card/40 p-4 space-y-2.5">
              <div class="flex items-center gap-2">
                <Paperclip class="text-primary h-3.5 w-3.5" />
                <h4 class="font-sans text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                  Tệp đính kèm ({{ attachments.length }})
                </h4>
              </div>
              <div v-if="attachments.length > 0" class="flex flex-col gap-2 max-h-[180px] overflow-y-auto calendar-scrollbar">
                <div
                  v-for="attachment in attachments"
                  :key="`${attachment.name}-${attachment.fileUrl}`"
                  class="flex items-center justify-between gap-3 p-2 rounded-md border border-border/60 bg-muted/20"
                >
                  <div class="min-w-0">
                    <p class="truncate font-sans text-xs font-semibold text-foreground" :title="attachment.name">
                      {{ attachment.name }}
                    </p>
                    <p class="font-sans text-[9px] text-muted-foreground/80 mt-0.5">
                      {{ attachment.size }} KB
                    </p>
                  </div>
                  <a
                    v-if="attachment.fileUrl"
                    :href="attachment.fileUrl"
                    target="_blank"
                    rel="noreferrer"
                    class="inline-flex items-center gap-1 rounded-md border border-border bg-background px-2 py-1 font-sans text-[10px] font-semibold text-muted-foreground hover:bg-accent shrink-0 transition-colors"
                  >
                    <LinkIcon class="h-2.5 w-2.5" />
                    Mở
                  </a>
                </div>
              </div>
              <p v-else class="font-sans text-[11px] text-muted-foreground italic">
                Không có tệp đính kèm.
              </p>
            </div>
          </div>
          
        </div>
      </ScrollArea>

      <!-- Footer Buttons -->
      <div class="flex flex-wrap items-center justify-end gap-2.5 border-t border-border/60 bg-background px-6 py-3.5 shrink-0">
        <Button
          type="button"
          variant="outline"
          size="sm"
          class="rounded-md border border-border bg-background font-sans text-xs font-semibold px-4 py-2 hover:bg-accent"
          @click="emit('update:show', false)"
        >
          Đóng
        </Button>

        <Button
          v-if="canDelete"
          type="button"
          variant="destructive"
          size="sm"
          class="rounded-md font-sans text-xs font-semibold px-4 py-2"
          @click="openDelete"
        >
          <Trash2 class="mr-1.5 h-3.5 w-3.5" />
          Xóa sự kiện
        </Button>

        <Button
          v-if="canEdit"
          type="button"
          size="sm"
          class="rounded-md bg-primary font-sans text-xs font-semibold text-primary-foreground px-4 py-2 shadow-sm hover:bg-primary/95"
          @click="openEdit"
        >
          <Pencil class="mr-1.5 h-3.5 w-3.5" />
          Chỉnh sửa
        </Button>
      </div>
    </DialogContent>
  </Dialog>
</template>
