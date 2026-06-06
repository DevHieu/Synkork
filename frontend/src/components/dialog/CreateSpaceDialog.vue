<script setup lang="ts">
import { ref } from "vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Hash,
  Volume2,
  Calendar,
  CheckSquare,
  FileText,
} from "lucide-vue-next";
import { watch } from "vue";
import { useRoomsStore } from "@/stores/roomStore";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import { PlanLimitUtils, type LimitType } from "@/utils/PlanLimitUtils";
import type { SpaceType } from "@/types/Space";
import PlanLimitDialog from "./PlanLimitDialog.vue";

const { roomPlan } = storeToRefs(useRoomsStore());
const { getSpaceTypeSize } = storeToRefs(useSpaceStore());

const props = defineProps<{ open: boolean; type: SpaceType }>();
const emit = defineEmits<{
  "update:open": [value: boolean];
  created: [data: { name: string; type: SpaceType }];
}>();

watch(
  () => props.type,
  (newType) => {
    if (newType) selectedType.value = newType;
  }
);

const spaceName = ref("");
const loading = ref(false);
const selectedType = ref<SpaceType>("CHAT");
const planLimitDialog = ref(false);

const spaceTypes = [
  { value: "CHAT" as const, label: "Chat", description: "Kênh nhắn tin nhóm", icon: Hash },
  { value: "VOICE" as const, label: "Đàm thoại", description: "Kênh thoại & video", icon: Volume2 },
  { value: "TASK" as const, label: "Task", description: "Quản lý công việc", icon: CheckSquare },
  { value: "NOTE" as const, label: "Ghi chú", description: "Ghi chú nhóm", icon: FileText },
  { value: "CALENDAR" as const, label: "Lịch trình", description: "Quản lý lịch", icon: Calendar },
] satisfies { value: SpaceType; label: string; description: string; icon: unknown }[];

const handleCreate = async () => {
  if (!spaceName.value.trim()) return;
  loading.value = true;
  try {

    if (getSpaceTypeSize.value(selectedType.value) >= PlanLimitUtils.maxSpaces(roomPlan.value, selectedType.value)) {
      planLimitDialog.value = true;
      return;
    }

    emit("created", { name: spaceName.value.trim(), type: selectedType.value });
    handleClose();
  } finally {
    loading.value = false;
  }
};

const handleClose = () => {
  spaceName.value = "";
  selectedType.value = "CHAT";
  emit("update:open", false);
};
</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>Tạo kênh mới</DialogTitle>
      </DialogHeader>

      <div class="flex flex-col gap-5 py-2">
        <!-- Loại kênh -->
        <div class="flex flex-col gap-2">
          <Label>Loại kênh</Label>
          <div class="grid grid-cols-1 gap-2">
            <button v-for="type in spaceTypes" :key="type.value" @click="selectedType = type.value"
              class="flex items-center gap-3 px-3 py-2.5 rounded-lg border transition-all duration-150 text-left"
              :class="selectedType === type.value
                ? 'border-primary bg-primary/10 text-foreground'
                : 'border-border hover:border-muted-foreground/50 hover:bg-muted text-muted-foreground'
                ">
              <component :is="type.icon" class="h-4 w-4 shrink-0" />
              <div class="flex flex-col">
                <span class="text-sm font-medium leading-none">{{
                  type.label
                }}</span>
                <span class="text-xs text-muted-foreground mt-0.5">{{
                  type.description
                }}</span>
              </div>
            </button>
          </div>
        </div>

        <!-- Tên kênh -->
        <div class="flex flex-col gap-2">
          <Label for="space-name">Tên kênh</Label>
          <Input id="space-name" v-model="spaceName" placeholder="Nhập tên kênh..." @keyup.enter="handleCreate" />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="handleClose">Hủy</Button>
        <Button :disabled="!spaceName.trim() || loading" @click="handleCreate">
          {{ loading ? "Đang tạo..." : "Tạo kênh" }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>

  <PlanLimitDialog v-model:open="planLimitDialog" :limit-type="selectedType.toLowerCase() as LimitType"
    :current-plan="roomPlan" />
</template>
