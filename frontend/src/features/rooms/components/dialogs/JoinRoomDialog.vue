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
import { ArrowLeft, AlertCircle } from "lucide-vue-next";
import { useRoomComposable } from "../../composables/roomComposable";

defineProps<{ open: boolean }>();
const emit = defineEmits<{
  "update:open": [val: boolean];
  back: [];
  done: [];
}>();

const roomComposable = useRoomComposable();

const inviteUrl = ref("");
const loading = ref(false);
const errorMsg = ref("");

const handleJoin = async () => {
  if (!inviteUrl.value.trim()) return;

  loading.value = true;
  errorMsg.value = "";
  try {
    let inviteCode = inviteUrl.value.includes("/invite/")
      ? inviteUrl.value.split("/invite/")[1]
      : inviteUrl.value.trim();

    if (!inviteCode) {
      errorMsg.value = "Liên kết mời không hợp lệ.";
      return;
    }

    await roomComposable.joinRoom(inviteCode);
    emit("done");
  } catch (err: any) {
    errorMsg.value = err.response?.data || "Đã có lỗi xảy ra, thử lại sau.";
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <div class="flex items-center gap-2">
          <button class="p-1 rounded hover:bg-muted transition-colors" @click="emit('back')">
            <ArrowLeft class="size-4" />
          </button>
          <DialogTitle>Tham gia phòng</DialogTitle>
        </div>
      </DialogHeader>

      <div class="flex flex-col gap-4 py-4">
        <div class="flex flex-col gap-2">
          <Label for="invite-code">Liên kết mời</Label>
          <Input id="invite-code" v-model="inviteUrl" :class="errorMsg
            ? 'border-destructive focus-visible:ring-destructive'
            : ''
            " placeholder="Liên kết mời..." @keyup.enter="handleJoin" @input="errorMsg = ''" />
          <!-- Error message -->
          <div v-if="errorMsg" class="flex items-center gap-1.5 text-destructive text-xs">
            <AlertCircle class="h-3.5 w-3.5 shrink-0" />
            <span>{{ errorMsg }}</span>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="emit('back')">Quay lại</Button>
        <Button :disabled="!inviteUrl.trim() || loading" @click="handleJoin">
          {{ loading ? "Đang tham gia..." : "Tham gia" }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
