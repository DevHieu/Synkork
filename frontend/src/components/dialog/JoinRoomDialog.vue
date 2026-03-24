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
import { ArrowLeft } from "lucide-vue-next";

defineProps<{ open: boolean }>();
const emit = defineEmits<{
  "update:open": [val: boolean];
  back: [];
  done: [];
}>();

const inviteCode = ref("");
const loading = ref(false);

const handleJoin = async () => {
  if (!inviteCode.value.trim()) return;
  loading.value = true;
  try {
    // TODO: gọi API tham gia phòng
    console.log("Tham gia phòng:", inviteCode.value);
    emit("done");
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
          <button
            class="p-1 rounded hover:bg-muted transition-colors"
            @click="emit('back')"
          >
            <ArrowLeft class="size-4" />
          </button>
          <DialogTitle>Tham gia phòng</DialogTitle>
        </div>
      </DialogHeader>

      <div class="flex flex-col gap-4 py-4">
        <div class="flex flex-col gap-2">
          <Label for="invite-code">Mã mời</Label>
          <Input
            id="invite-code"
            v-model="inviteCode"
            placeholder="Nhập mã mời..."
            @keyup.enter="handleJoin"
          />
          <p class="text-xs text-muted-foreground">
            Mã mời thường có dạng: ABC123
          </p>
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="emit('back')">Quay lại</Button>
        <Button :disabled="!inviteCode.trim() || loading" @click="handleJoin">
          {{ loading ? "Đang tham gia..." : "Tham gia" }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
