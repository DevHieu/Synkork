<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { LoaderCircle, CalendarClock, ArrowRight } from "lucide-vue-next";
import { toast } from "vue-sonner";
import { getAllSpacesFromRoomId } from "@/services/spaceService";
import { useRoomsStore } from "@/stores/roomStore";
import { storeToRefs } from "pinia";
import type { CalendarChannelOption } from "@/types/CalendarSuggestion";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";

const props = defineProps<{
  open: boolean;
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "select", option: CalendarChannelOption): void;
}>();

const roomStore = useRoomsStore();
const { rooms, currentRoom } = storeToRefs(roomStore);

const loading = ref(false);
const calendarChannels = ref<CalendarChannelOption[]>([]);

const currentRoomChannels = computed(() =>
  calendarChannels.value.filter(
    (channel) => channel.roomId === currentRoom.value?.id,
  ),
);

const otherRoomChannels = computed(() =>
  calendarChannels.value.filter(
    (channel) => channel.roomId !== currentRoom.value?.id,
  ),
);

const loadCalendarChannels = async () => {
  if (!rooms.value.length) {
    calendarChannels.value = [];
    return;
  }

  loading.value = true;

  try {
    const responses = await Promise.all(
      rooms.value.map(async (room) => {
        const response = await getAllSpacesFromRoomId(room.id);

        return response.data
          .filter((space: { type: string }) => space.type === "CALENDAR")
          .map(
            (space: { id: string; name: string }) =>
              ({
                roomId: room.id,
                roomName: room.name,
                spaceId: space.id,
                spaceName: space.name,
              }) satisfies CalendarChannelOption,
          );
      }),
    );

    calendarChannels.value = responses.flat();
  } catch (error) {
    toast.error("Không thể tải danh sách kênh lịch.");
    calendarChannels.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSelect = (option: CalendarChannelOption) => {
  emit("select", option);
};

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      loadCalendarChannels();
    }
  },
);
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-xl">
      <DialogHeader>
        <DialogTitle class="flex items-center gap-2">
          <CalendarClock class="h-5 w-5 text-primary" />
          Chọn kênh lịch để tạo sự kiện
        </DialogTitle>
        <DialogDescription>
          Sự kiện sẽ được tạo ngay trong kênh lịch mà bạn chọn bên dưới.
        </DialogDescription>
      </DialogHeader>

      <div v-if="loading" class="flex min-h-40 items-center justify-center">
        <LoaderCircle class="h-5 w-5 animate-spin text-primary" />
      </div>

      <div v-else class="flex flex-col gap-5">
        <section class="flex flex-col gap-3">
          <div class="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            Trong phòng hiện tại
          </div>

          <div
            v-if="currentRoomChannels.length"
            class="flex flex-col gap-2"
          >
            <button
              v-for="channel in currentRoomChannels"
              :key="channel.spaceId"
              type="button"
              class="flex items-center justify-between rounded-xl border border-border bg-background px-4 py-3 text-left transition-colors hover:border-primary hover:bg-primary/5"
              @click="handleSelect(channel)"
            >
              <div class="flex flex-col">
                <span class="text-sm font-medium text-foreground">
                  {{ channel.spaceName }}
                </span>
                <span class="text-xs text-muted-foreground">
                  {{ channel.roomName }}
                </span>
              </div>
              <ArrowRight class="h-4 w-4 text-primary" />
            </button>
          </div>

          <div
            v-else
            class="rounded-xl border border-dashed border-border px-4 py-6 text-sm text-muted-foreground"
          >
            Không có kênh lịch phù hợp trong phòng hiện tại.
          </div>
        </section>

        <Separator />

        <section class="flex flex-col gap-3">
          <div class="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            Kênh lịch khác
          </div>

          <div
            v-if="otherRoomChannels.length"
            class="flex max-h-64 flex-col gap-2 overflow-y-auto pr-1"
          >
            <button
              v-for="channel in otherRoomChannels"
              :key="channel.spaceId"
              type="button"
              class="flex items-center justify-between rounded-xl border border-border bg-background px-4 py-3 text-left transition-colors hover:border-primary hover:bg-primary/5"
              @click="handleSelect(channel)"
            >
              <div class="flex flex-col">
                <span class="text-sm font-medium text-foreground">
                  {{ channel.spaceName }}
                </span>
                <span class="text-xs text-muted-foreground">
                  {{ channel.roomName }}
                </span>
              </div>
              <ArrowRight class="h-4 w-4 text-primary" />
            </button>
          </div>

          <div
            v-else
            class="rounded-xl border border-dashed border-border px-4 py-6 text-sm text-muted-foreground"
          >
            Không có kênh lịch nào khác khả dụng.
          </div>
        </section>

        <div class="flex justify-end">
          <Button variant="outline" @click="emit('update:open', false)">
            Hủy
          </Button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>
