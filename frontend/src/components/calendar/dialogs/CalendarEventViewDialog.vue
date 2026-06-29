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
      class="overflow-hidden rounded-lg border border-border/80 bg-background p-0 text-foreground shadow-2xl sm:max-w-2xl cursor-default"
    >
      <DialogHeader class="border-b border-border/60 bg-muted/40 px-5 py-3.5 cursor-default">
        <div class="flex flex-col gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <Badge variant="default" class="font-sans text-[10px] font-semibold uppercase tracking-wider">
              Sự kiện
            </Badge>
            <Badge
              v-if="event?.allowEditAll"
              variant="outline"
              class="font-sans text-[10px] font-semibold uppercase tracking-wider"
            >
              Cho phép chỉnh sửa
            </Badge>
          </div>

          <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
            <div class="min-w-0">
              <DialogTitle class="font-sans text-lg font-bold text-foreground leading-tight">
                {{ event?.title }}
              </DialogTitle>
              <p class="mt-1 font-sans text-xs text-muted-foreground">
                {{ formattedEventDate }}
              </p>
            </div>

            <div class="rounded-md border border-primary/25 bg-primary/5 px-3 py-2 cursor-default shrink-0">
              <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/90">
                Khung giờ
              </p>
              <p class="group mt-0.5 font-sans text-xs font-bold text-primary">
                <span class="group-hover:hidden">{{ displayStartTime }} → {{ displayEndTime }}</span>
                <span class="hidden group-hover:inline">{{ originalStartLabel }} → {{ originalEndLabel }}</span>
              </p>
              <p v-if="continuationLabel" class="mt-1 font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                {{ continuationLabel }}
              </p>
            </div>
          </div>
        </div>
      </DialogHeader>

      <ScrollArea class="max-h-[65vh]">
        <div class="flex flex-col gap-4 px-5 py-5">
          <div class="grid gap-4 md:grid-cols-[1.2fr_0.8fr]">
            <section class="rounded-md border border-border/60 bg-card/30 p-3.5">
              <div class="flex items-center gap-2">
                <FileText class="text-primary h-4 w-4" data-icon="inline-start" />
                <h3 class="font-sans text-xs font-semibold text-muted-foreground">
                  Mô tả
                </h3>
              </div>
              <p class="mt-2.5 whitespace-pre-wrap rounded-md border border-border/60 bg-muted/10 p-3 font-sans text-xs leading-normal text-foreground cursor-default">
                {{ event?.description || "Không có mô tả." }}
              </p>
            </section>

            <section class="rounded-md border border-border/60 bg-card/30 p-3.5 cursor-default">
              <div class="flex items-center gap-2">
                <UserRound class="text-primary h-4 w-4" data-icon="inline-start" />
                <h3 class="font-sans text-xs font-semibold text-muted-foreground">
                  Người tạo
                </h3>
              </div>

              <div class="mt-3 flex items-center gap-3">
                <Avatar class="size-10 border border-border">
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
                  <p class="mt-0.5 truncate font-sans text-[10px] text-muted-foreground">
                    {{ event?.createdByUsername }}
                  </p>
                </div>
              </div>

              <Separator class="my-3.5" />

              <div class="grid gap-2.5">
                <div>
                  <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                    Tạo lúc
                  </p>
                  <p class="mt-0.5 font-sans text-xs font-medium text-foreground">
                    {{ formattedCreatedAt }}
                  </p>
                </div>
                <div>
                  <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                    Cập nhật lúc
                  </p>
                  <p class="mt-0.5 font-sans text-xs font-medium text-foreground">
                    {{ formattedUpdatedAt }}
                  </p>
                </div>
              </div>
            </section>
          </div>

          <section class="grid gap-4 md:grid-cols-3">
            <div class="rounded-md border border-border/60 bg-card/30 p-3 cursor-default">
              <div class="flex items-center gap-2">
                <CalendarDays class="text-primary h-4 w-4" data-icon="inline-start" />
                <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                  Ngày
                </p>
              </div>
              <p class="mt-2 font-sans text-xs font-semibold text-foreground">
                {{ displayEventDate }}
              </p>
            </div>

            <div class="rounded-md border border-border/60 bg-card/30 p-3 cursor-default">
              <div class="flex items-center gap-2">
                <Clock3 class="text-primary h-4 w-4" data-icon="inline-start" />
                <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                  Lặp lại
                </p>
              </div>
              <p class="mt-2 font-sans text-xs font-semibold text-foreground">
                {{ recurrenceLabel }}
              </p>
            </div>

            <div class="rounded-md border border-border/60 bg-card/30 p-3 cursor-default">
              <div class="flex items-center gap-2">
                <ShieldCheck class="text-primary h-4 w-4" data-icon="inline-start" />
                <p class="font-sans text-[9px] font-semibold uppercase tracking-wider text-muted-foreground/80">
                  Quyền sửa
                </p>
              </div>
              <p class="mt-2 font-sans text-xs font-semibold text-foreground">
                {{ event?.allowEditAll ? "Mọi người có thể sửa" : "Chỉ người tạo được sửa" }}
              </p>
            </div>
          </section>

          <div class="grid gap-4 md:grid-cols-2">
            <section class="rounded-md border border-border/60 bg-card/30 p-3.5 cursor-default">
              <div class="flex items-center gap-2">
                <Users class="text-primary h-4 w-4" data-icon="inline-start" />
                <h3 class="font-sans text-xs font-semibold text-muted-foreground">
                  Người tham gia
                </h3>
              </div>

              <div v-if="attendees.length > 0" class="mt-3 flex flex-wrap gap-1.5">
                <Badge
                  v-for="attendee in attendees"
                  :key="attendee.userId"
                  variant="outline"
                  class="font-sans text-[10px] font-medium"
                >
                  {{ attendee.displayName || attendee.username }}
                </Badge>
              </div>
              <p v-else class="mt-3 font-sans text-xs text-muted-foreground">
                Không có người tham gia.
              </p>
            </section>

            <section class="rounded-md border border-border/60 bg-card/30 p-3.5 cursor-default">
              <div class="flex items-center gap-2">
                <Paperclip class="text-primary h-4 w-4" data-icon="inline-start" />
                <h3 class="font-sans text-xs font-semibold text-muted-foreground">
                  Tệp đính kèm
                </h3>
              </div>

              <div v-if="attachments.length > 0" class="mt-3 flex flex-col gap-2">
                <div
                  v-for="attachment in attachments"
                  :key="`${attachment.name}-${attachment.fileUrl}`"
                  class="rounded-md border border-border/60 bg-muted/10 px-3 py-2"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0">
                      <p class="truncate font-sans text-xs font-semibold text-foreground">
                        {{ attachment.name }}
                      </p>
                      <p class="mt-0.5 font-sans text-[10px] text-muted-foreground/80">
                        {{ attachment.size }} KB
                      </p>
                    </div>

                    <a
                      v-if="attachment.fileUrl"
                      :href="attachment.fileUrl"
                      target="_blank"
                      rel="noreferrer"
                      class="inline-flex shrink-0 items-center gap-1 rounded-md border border-border bg-background px-2.5 py-1 font-sans text-[10px] font-semibold text-muted-foreground transition-colors hover:bg-accent"
                    >
                      <LinkIcon class="h-3 w-3" data-icon="inline-end" />
                      Mở
                    </a>
                  </div>
                </div>
              </div>
              <p v-else class="mt-3 font-sans text-xs text-muted-foreground">
                Không có tệp đính kèm.
              </p>
            </section>
          </div>

          <!-- Phòng họp trực tiếp -->
          <section
            v-if="event?.callRoomSpaceId"
            class="rounded-md border border-border/60 bg-card/30 p-3.5 cursor-default"
          >
            <div class="flex items-center gap-2">
              <PhoneCall class="text-primary h-4 w-4" data-icon="inline-start" />
              <h3 class="font-sans text-xs font-semibold text-muted-foreground">
                Phòng họp trực tiếp
              </h3>
            </div>

            <div class="mt-3 flex flex-col gap-3 rounded-md border border-border/60 bg-muted/10 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
              <p class="min-w-0 break-all font-sans text-xs font-semibold text-foreground">
                {{ event?.callRoomSpaceName || 'Phòng voice' }}
              </p>

              <Button
                @click="joinVoiceRoom"
                class="rounded-md bg-primary font-sans text-xs font-semibold text-primary-foreground px-3 py-1.5 shadow-sm hover:bg-primary/95"
              >
                Vào phòng call
              </Button>
            </div>
          </section>

          <section
            v-if="eventLink"
            class="rounded-md border border-border/60 bg-card/30 p-3.5 cursor-default"
          >
            <div class="flex items-center gap-2">
              <LinkIcon class="text-primary h-4 w-4" data-icon="inline-start" />
              <h3 class="font-sans text-xs font-semibold text-muted-foreground">
                Link sự kiện
              </h3>
            </div>

            <div class="mt-3 flex flex-col gap-3 rounded-md border border-border/60 bg-muted/10 px-3 py-2 sm:flex-row sm:items-center sm:justify-between">
              <p class="min-w-0 break-all font-sans text-xs font-semibold text-foreground">
                {{ eventLink }}
              </p>

              <a
                :href="eventLink"
                target="_blank"
                rel="noopener noreferrer"
                class="inline-flex shrink-0 items-center justify-center gap-1 rounded-md border border-border bg-background px-3 py-1.5 font-sans text-[10px] font-semibold text-muted-foreground transition-colors hover:bg-accent"
              >
                <LinkIcon class="h-3 w-3" data-icon="inline-start" />
                Mở link
              </a>
            </div>
          </section>
        </div>
      </ScrollArea>

      <div class="flex flex-wrap items-center justify-end gap-2.5 border-t border-border/60 bg-background px-5 py-3">
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
          <Trash2 class="h-3.5 w-3.5" data-icon="inline-start" />
          Xóa
        </Button>

        <Button
          v-if="canEdit"
          type="button"
          size="sm"
          class="rounded-md bg-primary font-sans text-xs font-semibold text-primary-foreground px-4 py-2 shadow-sm hover:bg-primary/95"
          @click="openEdit"
        >
          <Pencil class="h-3.5 w-3.5" data-icon="inline-start" />
          Chỉnh sửa
        </Button>
      </div>
    </DialogContent>
  </Dialog>
</template>
