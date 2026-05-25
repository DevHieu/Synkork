<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { LoaderCircle, CalendarClock, NotebookPen, ListTodo, ArrowRight } from "lucide-vue-next";
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

const props = defineProps<{
  open: boolean;
  targetType: "CALENDAR" | "NOTE" | "TASK";
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "select", option: CalendarChannelOption, type: "CALENDAR" | "NOTE" | "TASK"): void;
}>();

const selectedType = ref<"CALENDAR" | "NOTE" | "TASK">("CALENDAR");

const typeOptions = [
  { value: "CALENDAR", label: "Sự kiện", icon: CalendarClock },
  { value: "NOTE", label: "Ghi chú", icon: NotebookPen },
  { value: "TASK", label: "Công việc", icon: ListTodo },
] as const;

const roomStore = useRoomsStore();
const { currentRoom } = storeToRefs(roomStore);

const loading = ref(false);
const currentRoomChannels = ref<CalendarChannelOption[]>([]);

const title = computed(() => {
  switch (selectedType.value) {
    case "NOTE": return "Chọn kênh ghi chú";
    case "TASK": return "Chọn kênh task";
    case "CALENDAR":
    default:
      return "Chọn kênh lịch";
  }
});

const description = computed(() => {
  switch (selectedType.value) {
    case "NOTE": return "Ghi chú sẽ được tạo ngay trong kênh ghi chú mà bạn chọn bên dưới.";
    case "TASK": return "Thẻ task sẽ được tạo ngay trong kênh task mà bạn chọn bên dưới.";
    case "CALENDAR":
    default:
      return "Sự kiện sẽ được tạo ngay trong kênh lịch mà bạn chọn bên dưới.";
  }
});

const emptyCurrentText = computed(() => {
  switch (selectedType.value) {
    case "NOTE": return "Không có kênh ghi chú phù hợp trong phòng hiện tại.";
    case "TASK": return "Không có kênh task phù hợp trong phòng hiện tại.";
    case "CALENDAR":
    default:
      return "Không có kênh lịch phù hợp trong phòng hiện tại.";
  }
});

const iconComponent = computed(() => {
  switch (selectedType.value) {
    case "NOTE": return NotebookPen;
    case "TASK": return ListTodo;
    case "CALENDAR":
    default:
      return CalendarClock;
  }
});

const loadChannels = async () => {
  if (!currentRoom.value?.id) {
    currentRoomChannels.value = [];
    return;
  }

  loading.value = true;

  try {
    const response = await getAllSpacesFromRoomId(currentRoom.value.id);

    currentRoomChannels.value = response.data
      .filter((space: { type: string }) => space.type === selectedType.value)
      .map(
        (space: { id: string; name: string }) =>
          ({
            roomId: currentRoom.value!.id,
            roomName: currentRoom.value!.name,
            spaceId: space.id,
            spaceName: space.name,
          }) satisfies CalendarChannelOption,
      );
  } catch (error) {
    toast.error("Không thể tải danh sách kênh.");
    currentRoomChannels.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSelect = (option: CalendarChannelOption) => {
  emit("select", option, selectedType.value);
};

watch(selectedType, () => {
  if (props.open) {
    loadChannels();
  }
});

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      selectedType.value = props.targetType;
      loadChannels();
    }
  },
);
</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-xl">
      <DialogHeader>
        <DialogTitle class="flex items-center gap-2">
          <component :is="iconComponent" class="h-5 w-5 text-primary" />
          {{ title }}
        </DialogTitle>
        <DialogDescription>
          {{ description }}
        </DialogDescription>
      </DialogHeader>

      <!-- Giao diện chọn loại nội dung để Tạo nhanh -->
      <div class="grid grid-cols-3 gap-2 p-1 bg-muted/60 rounded-xl">
        <button v-for="typeOpt in typeOptions" :key="typeOpt.value" type="button"
          class="flex flex-col items-center justify-center gap-1.5 py-3 px-2 rounded-lg border transition-all duration-200"
          :class="selectedType === typeOpt.value
            ? 'bg-background border-border shadow-sm text-primary font-medium'
            : 'border-transparent text-muted-foreground hover:bg-background/50 hover:text-foreground'"
          @click="selectedType = typeOpt.value">
          <component :is="typeOpt.icon" class="h-5 w-5" />
          <span class="text-xs">{{ typeOpt.label }}</span>
          <span v-if="props.targetType === typeOpt.value"
            class="text-[9px] px-1.5 py-0.5 rounded-full bg-primary/10 text-primary font-bold uppercase tracking-wider scale-95">
            Gợi ý
          </span>
        </button>
      </div>

      <div v-if="loading" class="flex min-h-40 items-center justify-center">
        <LoaderCircle class="h-5 w-5 animate-spin text-primary" />
      </div>

      <div v-else class="flex flex-col gap-5">
        <section class="flex flex-col gap-3">
          <div class="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            Danh sách space
          </div>

          <div v-if="currentRoomChannels.length" class="flex flex-col gap-2">
            <button v-for="channel in currentRoomChannels" :key="channel.spaceId" type="button"
              class="flex items-center justify-between rounded-xl border border-border bg-background px-4 py-3 text-left transition-colors hover:border-primary hover:bg-primary/5"
              @click="handleSelect(channel)">
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

          <div v-else class="rounded-xl border border-dashed border-border px-4 py-6 text-sm text-muted-foreground">
            {{ emptyCurrentText }}
          </div>
        </section>

        <!-- <Separator />

        <section class="flex flex-col gap-3">
          <div class="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            Kênh khác
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
            {{ emptyOtherText }}
          </div>
        </section> -->

        <div class="flex justify-end">
          <Button variant="outline" @click="emit('update:open', false)">
            Hủy
          </Button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>
