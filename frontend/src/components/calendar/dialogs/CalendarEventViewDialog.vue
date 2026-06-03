<script setup lang="ts">
import { computed } from "vue";
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
</script>

<template>
  <Dialog :open="show" @update:open="emit('update:show', $event)">
    <DialogContent
      class="overflow-hidden rounded-[1.5rem] border-2 border-border bg-background p-0 text-foreground shadow-[0_36px_110px_-52px_rgba(0,0,0,0.8)] sm:max-w-2xl cursor-default"
    >
      <DialogHeader class="border-b-2 border-border bg-muted/35 px-5 py-4 cursor-default">
        <div class="flex flex-col gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <Badge variant="default" class="font-mono uppercase tracking-widest">
              Sự kiện
            </Badge>
            <Badge
              v-if="event?.allowEditAll"
              variant="outline"
              class="font-mono uppercase tracking-widest"
            >
              Cho phép chỉnh sửa
            </Badge>
          </div>

          <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
            <div class="min-w-0">
              <DialogTitle class="font-mono text-xl font-bold uppercase leading-tight tracking-[0.14em] text-foreground">
                {{ event?.title }}
              </DialogTitle>
              <p class="mt-2 font-mono text-xs uppercase tracking-widest text-muted-foreground">
                {{ formattedEventDate }}
              </p>
            </div>

            <div class="rounded-2xl border-2 border-primary/30 bg-primary/10 px-3 py-2.5 cursor-default">
              <p class="font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                Khung giờ
              </p>
              <p class="group mt-1 font-mono text-sm font-bold uppercase tracking-widest text-primary">
                <span class="group-hover:hidden">{{ displayStartTime }} → {{ displayEndTime }}</span>
                <span class="hidden group-hover:inline">{{ originalStartLabel }} → {{ originalEndLabel }}</span>
              </p>
              <p v-if="continuationLabel" class="mt-2 font-mono text-[9px] font-bold uppercase tracking-widest text-muted-foreground">
                {{ continuationLabel }}
              </p>
            </div>
          </div>
        </div>
      </DialogHeader>

      <ScrollArea class="max-h-[65vh]">
        <div class="flex flex-col gap-4 px-5 py-5">
          <div class="grid gap-4 md:grid-cols-[1.2fr_0.8fr]">
            <section class="rounded-2xl border-2 border-border bg-background p-4 shadow-[0_16px_34px_-30px_var(--color-foreground)]">
              <div class="flex items-center gap-2">
                <FileText class="text-primary" data-icon="inline-start" />
                <h3 class="font-mono text-xs font-bold uppercase tracking-widest text-muted-foreground">
                  Mô tả
                </h3>
              </div>
              <p class="mt-3 whitespace-pre-wrap rounded-xl border border-border bg-muted/20 p-4 font-mono text-sm leading-relaxed text-foreground cursor-default">
                {{ event?.description || "Không có mô tả." }}
              </p>
            </section>

            <section class="rounded-2xl border-2 border-border bg-background p-4 shadow-[0_16px_34px_-30px_var(--color-foreground)] cursor-default">
              <div class="flex items-center gap-2">
                <UserRound class="text-primary" data-icon="inline-start" />
                <h3 class="font-mono text-xs font-bold uppercase tracking-widest text-muted-foreground">
                  Người tạo
                </h3>
              </div>

              <div class="mt-4 flex items-center gap-3">
                <Avatar class="size-12 border-2 border-border">
                  <AvatarImage
                    v-if="event?.createdByAvatarUrl"
                    :src="event.createdByAvatarUrl"
                    :alt="creatorLabel"
                  />
                  <AvatarFallback />
                </Avatar>

                <div class="min-w-0">
                  <p class="truncate font-mono text-sm font-bold uppercase tracking-wider text-foreground">
                    {{ creatorLabel }}
                  </p>
                  <p class="mt-1 truncate font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                    {{ event?.createdByUsername }}
                  </p>
                </div>
              </div>

              <Separator class="my-4" />

              <div class="grid gap-3">
                <div>
                  <p class="font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                    Tạo lúc
                  </p>
                  <p class="mt-1 font-mono text-xs font-bold uppercase tracking-wider text-foreground">
                    {{ formattedCreatedAt }}
                  </p>
                </div>
                <div>
                  <p class="font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                    Cập nhật lúc
                  </p>
                  <p class="mt-1 font-mono text-xs font-bold uppercase tracking-wider text-foreground">
                    {{ formattedUpdatedAt }}
                  </p>
                </div>
              </div>
            </section>
          </div>

          <section class="grid gap-4 md:grid-cols-3">
            <div class="rounded-2xl border-2 border-border bg-background p-3.5 cursor-default">
              <div class="flex items-center gap-2">
                <CalendarDays class="text-primary" data-icon="inline-start" />
                <p class="font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                  Ngày
                </p>
              </div>
              <p class="mt-3 font-mono text-sm font-bold uppercase tracking-wider text-foreground">
                {{ displayEventDate }}
              </p>
            </div>

            <div class="rounded-2xl border-2 border-border bg-background p-3.5 cursor-default">
              <div class="flex items-center gap-2">
                <Clock3 class="text-primary" data-icon="inline-start" />
                <p class="font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                  Lặp lại
                </p>
              </div>
              <p class="mt-3 font-mono text-sm font-bold uppercase tracking-wider text-foreground">
                {{ recurrenceLabel }}
              </p>
            </div>

            <div class="rounded-2xl border-2 border-border bg-background p-3.5 cursor-default">
              <div class="flex items-center gap-2">
                <ShieldCheck class="text-primary" data-icon="inline-start" />
                <p class="font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                  Quyền sửa
                </p>
              </div>
              <p class="mt-3 font-mono text-sm font-bold uppercase tracking-wider text-foreground">
                {{ event?.allowEditAll ? "Mọi người có thể sửa" : "Chỉ người tạo được sửa" }}
              </p>
            </div>
          </section>

          <div class="grid gap-4 md:grid-cols-2">
            <section class="rounded-2xl border-2 border-border bg-background p-4 cursor-default">
              <div class="flex items-center gap-2">
                <Users class="text-primary" data-icon="inline-start" />
                <h3 class="font-mono text-xs font-bold uppercase tracking-widest text-muted-foreground">
                  Người tham gia
                </h3>
              </div>

              <div v-if="attendees.length > 0" class="mt-4 flex flex-wrap gap-2">
                <Badge
                  v-for="attendee in attendees"
                  :key="attendee.userId"
                  variant="outline"
                  class="font-mono uppercase tracking-wide"
                >
                  {{ attendee.displayName || attendee.username }}
                </Badge>
              </div>
              <p v-else class="mt-4 font-mono text-xs uppercase tracking-widest text-muted-foreground">
                Không có người tham gia.
              </p>
            </section>

            <section class="rounded-2xl border-2 border-border bg-background p-4 cursor-default">
              <div class="flex items-center gap-2">
                <Paperclip class="text-primary" data-icon="inline-start" />
                <h3 class="font-mono text-xs font-bold uppercase tracking-widest text-muted-foreground">
                  Tệp đính kèm
                </h3>
              </div>

              <div v-if="attachments.length > 0" class="mt-4 flex flex-col gap-3">
                <div
                  v-for="attachment in attachments"
                  :key="`${attachment.name}-${attachment.fileUrl}`"
                  class="rounded-xl border border-border bg-muted/15 px-4 py-3"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0">
                      <p class="truncate font-mono text-xs font-bold uppercase tracking-wider text-foreground">
                        {{ attachment.name }}
                      </p>
                      <p class="mt-1 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                        {{ attachment.size }} KB
                      </p>
                    </div>

                    <a
                      v-if="attachment.fileUrl"
                      :href="attachment.fileUrl"
                      target="_blank"
                      rel="noreferrer"
                      class="inline-flex shrink-0 items-center gap-1 rounded-full border border-border px-2 py-1 font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground transition-colors hover:border-primary hover:text-primary"
                    >
                      <LinkIcon data-icon="inline-end" />
                      Mở
                    </a>
                  </div>
                </div>
              </div>
              <p v-else class="mt-4 font-mono text-xs uppercase tracking-widest text-muted-foreground">
                Không có tệp đính kèm.
              </p>
            </section>
          </div>

          <section
            v-if="eventLink"
            class="rounded-2xl border-2 border-border bg-background p-4 cursor-default"
          >
            <div class="flex items-center gap-2">
              <LinkIcon class="text-primary" data-icon="inline-start" />
              <h3 class="font-mono text-xs font-bold uppercase tracking-widest text-muted-foreground">
                Link sự kiện
              </h3>
            </div>

            <div class="mt-4 flex flex-col gap-3 rounded-xl border border-border bg-muted/15 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
              <p class="min-w-0 break-all font-mono text-xs font-bold tracking-wider text-foreground">
                {{ eventLink }}
              </p>

              <a
                :href="eventLink"
                target="_blank"
                rel="noopener noreferrer"
                class="inline-flex shrink-0 items-center justify-center gap-1 rounded-full border border-border px-3 py-1.5 font-mono text-[10px] font-bold uppercase tracking-widest text-muted-foreground transition-colors hover:border-primary hover:text-primary"
              >
                <LinkIcon data-icon="inline-start" />
                Mở link
              </a>
            </div>
          </section>
        </div>
      </ScrollArea>

      <div class="flex flex-wrap items-center justify-end gap-2.5 border-t-2 border-border bg-background px-5 py-3.5">
        <Button
          type="button"
          variant="outline"
          size="sm"
          class="rounded-full border-2 font-mono text-xs font-bold uppercase tracking-widest"
          @click="emit('update:show', false)"
        >
          Đóng
        </Button>

        <Button
          v-if="canDelete"
          type="button"
          variant="outline"
          size="sm"
          class="rounded-full border-2 border-destructive font-mono text-xs font-bold uppercase tracking-widest text-destructive hover:bg-destructive hover:text-destructive-foreground"
          @click="openDelete"
        >
          <Trash2 data-icon="inline-start" />
          Xóa
        </Button>

        <Button
          v-if="canEdit"
          type="button"
          size="sm"
          class="rounded-full border-2 border-primary bg-primary font-mono text-xs font-bold uppercase tracking-widest text-primary-foreground shadow-[0_16px_34px_-22px_var(--color-primary)] hover:bg-background hover:text-primary"
          @click="openEdit"
        >
          <Pencil data-icon="inline-start" />
          Chỉnh sửa
        </Button>
      </div>
    </DialogContent>
  </Dialog>
</template>
