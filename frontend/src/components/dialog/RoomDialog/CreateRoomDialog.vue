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
import { ArrowLeft, Camera } from "lucide-vue-next";
import { toast } from "vue-sonner";
import { useRoomsStore } from "@/stores/roomStore";

defineProps<{ open: boolean }>();
const emit = defineEmits<{
  "update:open": [val: boolean];
  back: [];
  done: [];
}>();

const roomStore = useRoomsStore();

const roomName = ref("");
const loading = ref(false);
const avatarFile = ref<File | null>(null);
const avatarPreview = ref<string | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);

const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  avatarFile.value = file;
  avatarPreview.value = URL.createObjectURL(file);
};

const handleCreate = async () => {
  if (!roomName.value.trim()) return;
  loading.value = true;
  try {
    await roomStore.createRoom({
      name: roomName.value.trim(),
      ownerId: "",
      imageFile: avatarFile.value ?? undefined,
    });
    emit("done");
  } catch (error: any) {
    const msg =
      error?.response?.data?.message ||
      error?.response?.data ||
      error?.message ||
      "Có lỗi xảy ra";
    toast.error(msg);
  } finally {
    loading.value = false;
    avatarFile.value = null;
    avatarPreview.value = null;
    roomName.value = "";
  }
};
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <div class="flex items-center gap-2">
          <button
            class="p-1 rounded hover:bg-muted transition-colors"
            @click="emit('back')"
          >
            <ArrowLeft class="size-4" />
          </button>
          <DialogTitle>Tạo phòng mới</DialogTitle>
        </div>
      </DialogHeader>

      <div class="flex flex-col gap-6 py-4">
        <!-- Chọn ảnh -->
        <div class="flex flex-col items-center gap-2">
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleFileChange"
          />
          <button
            type="button"
            class="relative w-24 h-24 rounded-full overflow-hidden bg-muted border-2 border-dashed border-muted-foreground/40 hover:border-primary hover:bg-primary/10 transition-all duration-200 group"
            @click="fileInputRef?.click()"
          >
            <img
              v-if="avatarPreview"
              :src="avatarPreview"
              class="w-full h-full object-cover"
            />
            <div
              v-else
              class="w-full h-full flex flex-col items-center justify-center gap-1"
            >
              <Camera
                class="size-6 text-muted-foreground group-hover:text-primary transition-colors"
              />
              <span
                class="text-xs text-muted-foreground group-hover:text-primary transition-colors"
              >
                Thêm ảnh
              </span>
            </div>
            <div
              v-if="avatarPreview"
              class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center"
            >
              <Camera class="size-6 text-white" />
            </div>
          </button>
          <span class="text-xs text-muted-foreground">
            {{ avatarPreview ? "Nhấn để đổi ảnh" : "Ảnh đại diện phòng (tùy chọn)" }}
          </span>
        </div>

        <!-- Tên phòng -->
        <div class="flex flex-col gap-2">
          <Label for="room-name">Tên phòng</Label>
          <Input
            id="room-name"
            v-model="roomName"
            placeholder="Nhập tên phòng..."
            @keyup.enter="handleCreate"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="emit('back')">Quay lại</Button>
        <Button :disabled="!roomName.trim() || loading" @click="handleCreate">
          {{ loading ? "Đang tạo..." : "Tạo phòng" }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>