<script setup lang="ts">
import { computed, ref, watch } from "vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
import { Lock } from "lucide-vue-next";

const props = defineProps<{
  open: boolean;
  spaceId: string;
  spaceName: string;
  restricted: boolean;
}>();

const emit = defineEmits<{
  "update:open": [value: boolean];
  save: [data: { name: string; restricted: boolean }]; // Gom lại thành một event save
  delete: [];
}>();

const nameInput = ref(props.spaceName);
const restrictedInput = ref(props.restricted);
const isDeleteConfirm = ref(false);

watch(
  () => props.spaceName,
  (v) => {
    nameInput.value = v;
  },
);
watch(
  () => props.restricted,
  (v) => {
    restrictedInput.value = v;
  },
);
watch(
  () => props.open,
  () => {
    isDeleteConfirm.value = false;
  },
);

const hasChanges = computed(
  () =>
    nameInput.value.trim() !== props.spaceName ||
    restrictedInput.value !== props.restricted,
);

const handleSave = () => {
  const trimmed = nameInput.value.trim();
  if (!trimmed) return;

  // Chỉ emit MỘT lần duy nhất khi nhấn Save
  emit("save", {
    name: trimmed,
    restricted: restrictedInput.value,
  });

  emit("update:open", false);
};

const handleDelete = () => {
  if (!isDeleteConfirm.value) {
    isDeleteConfirm.value = true;
    return;
  }
  emit("delete");
  emit("update:open", false);
};
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="max-w-sm cursor-default">
      <DialogHeader>
        <DialogTitle>Cài đặt kênh</DialogTitle>
      </DialogHeader>

      <div class="space-y-4 py-2">
        <!-- Tên kênh -->
        <div class="space-y-2">
          <label class="text-sm font-medium cursor-default">Tên kênh</label>
          <Input
            v-model="nameInput"
            placeholder="Tên kênh..."
            @keydown.enter="handleSave"
          />
        </div>

        <!-- Restricted toggle -->
        <div class="flex items-start justify-between gap-4">
          <div class="space-y-1">
            <div class="flex items-center gap-2 cursor-default">
              <Lock class="h-4 w-4 text-muted-foreground" />
              <p class="text-sm font-medium cursor-default">Kênh riêng tư</p>
            </div>
            <p class="text-xs text-muted-foreground cursor-default">
              Chỉ admin và mod mới có thể xem và sử dụng kênh này.
            </p>
          </div>
          <Switch
            :checked="restrictedInput"
            @click="restrictedInput = !restrictedInput"
          />
        </div>

        <!-- Nút lưu -->
        <Button
          class="w-full"
          @click="handleSave"
          :disabled="!nameInput.trim() || !hasChanges"
        >
          Lưu thay đổi
        </Button>

        <div class="border-t" />

        <!-- Xóa -->
        <div class="space-y-2">
          <p class="text-sm font-medium text-destructive cursor-default">Vùng nguy hiểm</p>
          <p class="text-xs text-muted-foreground cursor-default">
            Xóa kênh sẽ xóa toàn bộ dữ liệu và không thể khôi phục.
          </p>
          <Button variant="destructive" class="w-full" @click="handleDelete">
            {{ isDeleteConfirm ? "Xác nhận xóa kênh này?" : "Xóa kênh" }}
          </Button>
          <button
            v-if="isDeleteConfirm"
            @click="isDeleteConfirm = false"
            class="w-full text-xs text-muted-foreground hover:underline"
          >
            Hủy
          </button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>
