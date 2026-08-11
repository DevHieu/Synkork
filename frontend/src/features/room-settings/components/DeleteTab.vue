<script setup lang="ts">
import { computed, ref } from "vue";
import { Trash2 } from "lucide-vue-next";
import { useRoomService } from "@/features/rooms/services/roomService";

const props = defineProps<{ roomName: string; roomId: string }>();

const roomService = useRoomService();

const confirmDeleteText = ref("");
const roomNameToDelete = computed(() => props.roomName);
const canDelete = computed(() => confirmDeleteText.value == props.roomName);

const isDeleting = ref(false);

const handleDeleteRoom = async () => {
  if (!canDelete.value) return;
  isDeleting.value = true;
  await roomService.deleteRoom(props.roomId);
  isDeleting.value = false;
};
</script>
<template>
  <div>
    <div class="rounded-xl border border-destructive/30 bg-destructive/5 p-4 flex flex-col gap-3">
      <div class="flex items-start gap-3">
        <div class="p-2 rounded-lg bg-destructive/10 text-destructive shrink-0 mt-0.5">
          <Trash2 class="h-4 w-4" />
        </div>
        <div>
          <p class="text-sm font-semibold text-foreground">Xóa phòng</p>
          <p class="text-xs text-muted-foreground mt-1 leading-relaxed">
            Hành động này
            <span class="font-semibold text-destructive">không thể hoàn tác</span>. Toàn bộ kênh, tin nhắn, file và dữ
            liệu trong phòng sẽ bị xóa
            vĩnh viễn.
          </p>
        </div>
      </div>

      <div class="flex flex-col gap-1.5">
        <label class="text-xs text-muted-foreground">
          Nhập tên phòng
          <span class="font-semibold text-foreground">{{
            roomNameToDelete
          }}</span>
          để xác nhận:
        </label>
        <input v-model="confirmDeleteText" type="text"
          class="w-full rounded-lg border border-destructive/40 bg-background px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-destructive/50 transition" />
      </div>

      <button @click="handleDeleteRoom" :disabled="!canDelete || isDeleting"
        class="w-full py-2 rounded-lg bg-destructive text-destructive-foreground text-sm font-semibold hover:bg-destructive/90 transition disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2">
        <span v-if="isDeleting"
          class="h-3.5 w-3.5 border-2 border-destructive-foreground/50 border-t-destructive-foreground rounded-full animate-spin" />
        {{ isDeleting ? "Đang xóa..." : "Xóa phòng vĩnh viễn" }}
      </button>
    </div>
  </div>
</template>

<style scoped></style>
