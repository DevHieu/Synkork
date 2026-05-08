<script setup lang="ts">
import { ref, watch } from "vue";
import { Camera } from "lucide-vue-next";
import { updateRoomInfo } from "@/services/roomService";

const props = defineProps<{ room: any }>();

const roomName = ref(props.room?.name ?? "");
const roomDescription = ref(props.room?.description ?? "");
const selectedFile = ref<File | null>(null);
const avatarPreview = ref(props.room?.avatarUrl ?? "");

// Sync lại khi room thay đổi
watch(
  () => props.room,
  (val) => {
    roomName.value = val?.name ?? "";
    roomDescription.value = val?.description ?? "";
    selectedFile.value = null;
    avatarPreview.value = val?.avatarUrl ?? "";
  },
);

// Xử lý chọn ảnh
const fileInput = ref<HTMLInputElement | null>(null);

const openFilePicker = () => fileInput.value?.click();

const handleFileChange = (e: Event) => {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  selectedFile.value = file;
  avatarPreview.value = URL.createObjectURL(file);
};

const isSaving = ref(false);

const handleSaveInfo = async () => {
  isSaving.value = true;
  const data = {
    name: roomName.value,
    description: roomDescription.value,
    imageFile: selectedFile.value ?? undefined,
  };

  await updateRoomInfo(props.room.id, data);

  isSaving.value = false;
};
</script>

<template>
  <div class="flex flex-col gap-5">
    <!-- Avatar -->
    <div class="flex items-center gap-4">
      <div class="relative group cursor-pointer" @click="openFilePicker">
        <!-- Có ảnh -->
        <img
          v-if="avatarPreview"
          :src="avatarPreview"
          class="w-16 h-16 rounded-xl object-cover"
          alt="Room avatar"
        />
        <!-- Chưa có ảnh -->
        <div
          v-else
          class="w-16 h-16 rounded-xl bg-primary/20 flex items-center justify-center text-primary font-bold text-xl select-none"
        >
          {{ roomName.charAt(0) }}
        </div>

        <!-- Overlay hover -->
        <div
          class="absolute inset-0 rounded-xl bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <Camera class="h-5 w-5 text-white" />
        </div>
      </div>

      <div>
        <p class="text-sm font-medium text-foreground">Ảnh phòng</p>
        <p class="text-xs text-muted-foreground mt-0.5">
          Nhấn để thay đổi ảnh đại diện
        </p>
        <button
          v-if="avatarPreview"
          @click="
            avatarPreview = '';
            avatarUrl = '';
          "
          class="text-xs text-destructive mt-1 hover:underline"
        >
          Xóa ảnh
        </button>
      </div>

      <!-- Hidden file input -->
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleFileChange"
      />
    </div>

    <!-- Name -->
    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-foreground">Tên phòng</label>
      <input
        v-model="roomName"
        type="text"
        placeholder="Nhập tên phòng..."
        class="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring transition"
      />
    </div>

    <!-- Description -->
    <div class="flex flex-col gap-1.5">
      <label class="text-sm font-medium text-foreground">Mô tả</label>
      <textarea
        v-model="roomDescription"
        rows="3"
        placeholder="Mô tả ngắn về phòng..."
        class="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring transition resize-none"
      />
    </div>

    <!-- Save -->
    <div class="flex justify-end pt-1">
      <button
        @click="handleSaveInfo"
        :disabled="isSaving"
        class="px-4 py-2 rounded-lg bg-primary text-primary-foreground text-sm font-medium hover:bg-primary/90 transition disabled:opacity-60 disabled:cursor-not-allowed flex items-center gap-2"
      >
        <span
          v-if="isSaving"
          class="h-3.5 w-3.5 border-2 border-primary-foreground/50 border-t-primary-foreground rounded-full animate-spin"
        />
        {{ isSaving ? "Đang lưu..." : "Lưu thay đổi" }}
      </button>
    </div>
  </div>
</template>
