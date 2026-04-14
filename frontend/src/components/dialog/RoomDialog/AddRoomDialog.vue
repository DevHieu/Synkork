<script setup lang="ts">
import { ref } from "vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Users, Plus } from "lucide-vue-next";
import CreateRoomDialog from "./CreateRoomDialog.vue";
import JoinRoomDialog from "./JoinRoomDialog.vue";

const props = defineProps<{ open: boolean }>();
const emit = defineEmits<{ "update:open": [val: boolean] }>();

const mode = ref<"select" | "create" | "join">("select");

const handleClose = () => {
  mode.value = "select";
  emit("update:open", false);
};
</script>

<template>
  <!-- Dialog chọn -->
  <Dialog :open="open && mode === 'select'" @update:open="handleClose">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>Bạn muốn làm gì?</DialogTitle>
      </DialogHeader>
      <div class="flex gap-4 py-4">
        <button
          class="flex-1 flex flex-col items-center gap-3 p-6 rounded-xl border bg-muted hover:bg-primary/10 hover:border-primary transition-all duration-200"
          @click="mode = 'create'"
        >
          <div
            class="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center"
          >
            <Plus class="size-6 text-primary" />
          </div>
          <div class="text-center">
            <p class="font-semibold">Tạo phòng</p>
            <p class="text-xs text-muted-foreground mt-1">
              Tạo phòng mới của riêng bạn
            </p>
          </div>
        </button>

        <button
          class="flex-1 flex flex-col items-center gap-3 p-6 rounded-xl border bg-muted hover:bg-primary/10 hover:border-primary transition-all duration-200"
          @click="mode = 'join'"
        >
          <div
            class="w-12 h-12 rounded-full bg-primary/20 flex items-center justify-center"
          >
            <Users class="size-6 text-primary" />
          </div>
          <div class="text-center">
            <p class="font-semibold">Tham gia phòng</p>
            <p class="text-xs text-muted-foreground mt-1">
              Nhập mã để vào phòng có sẵn
            </p>
          </div>
        </button>
      </div>
    </DialogContent>
  </Dialog>

  <!-- Dialog tạo phòng -->
  <CreateRoomDialog
    :open="mode === 'create'"
    @update:open="
      (val) => {
        if (!val) mode = 'select';
      }
    "
    @back="mode = 'select'"
    @done="handleClose"
  />

  <!-- Dialog tham gia phòng -->
  <JoinRoomDialog
    :open="mode === 'join'"
    @update:open="
      (val) => {
        if (!val) mode = 'select';
      }
    "
    @back="mode = 'select'"
    @done="handleClose"
  />
</template>
