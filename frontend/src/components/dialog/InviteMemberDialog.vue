<script setup lang="ts">
import { ref, computed, watch } from "vue";
import Dialog from "@/components/ui/dialog/Dialog.vue";
import DialogContent from "@/components/ui/dialog/DialogContent.vue";
import DialogTitle from "@/components/ui/dialog/DialogTitle.vue";
import DialogHeader from "@/components/ui/dialog/DialogHeader.vue";
import { Copy, Check, RefreshCw, Link } from "lucide-vue-next";
import { useRoomsStore } from "@/stores/roomStore";
import { storeToRefs } from "pinia";
import { getInviteCode, resetInviteCode } from "@/services/roomService";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { toast } from "vue-sonner";

const props = defineProps<{ open: boolean }>();
const emit = defineEmits<{ "update:open": [value: boolean] }>();

const handleClose = () => emit("update:open", false);

const roomStore = useRoomsStore();
const { currentRoom } = storeToRefs(roomStore);

const roomMemberStore = useRoomMemberStore();
const { canManage } = storeToRefs(roomMemberStore)

const inviteCode = ref("");
const isLoading = ref(false);
const isCopied = ref(false);
const isResetting = ref(false);

const inviteLink = computed(() =>
  inviteCode.value
    ? `${window.location.origin}/invite/${inviteCode.value}`
    : "",
);

const fetchInviteCode = async () => {
  if (!currentRoom.value?.id) return;
  isLoading.value = true;
  try {
    inviteCode.value = await getInviteCode(currentRoom.value.id);
  } finally {
    isLoading.value = false;
  }
};

const resetCode = async () => {
  if (!currentRoom.value?.id) return;
  if (!canManage.value) {
    toast.error("Bạn không có quyền làm mới link mời");
    return;
  }

  isResetting.value = true;
  try {
    const newCode = await resetInviteCode(currentRoom.value.id);
    inviteCode.value = String(newCode).trim();
    isCopied.value = false;
    toast.success("Đã làm mới link mời");
  } catch (error: any) {
    const errorData = error?.response?.data;
    const message =
      typeof errorData === "string" ? errorData : errorData?.message;
    toast.error(message || "Không thể làm mới link mời");
  } finally {
    isResetting.value = false;
  }
};

const copyLink = async () => {
  if (!inviteLink.value) return;
  await navigator.clipboard.writeText(inviteLink.value);
  isCopied.value = true;
  setTimeout(() => (isCopied.value = false), 2000);
};

watch(
  () => props.open,
  (val) => {
    if (val) fetchInviteCode();
  },
);
</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent
      class="!max-w-[480px] !w-[480px] !p-0 overflow-hidden rounded-xl border border-border bg-background shadow-2xl">
      <DialogHeader class="px-6 pt-6 pb-4 border-b border-border">
        <DialogTitle class="text-base font-semibold text-foreground">
          Mời bạn bè vào {{ currentRoom?.name }}
        </DialogTitle>
        <p class="text-xs text-muted-foreground mt-1">
          Chia sẻ link bên dưới để mời người khác tham gia phòng
        </p>
      </DialogHeader>

      <div class="px-6 py-5 flex flex-col gap-5">
        <!-- Link box -->
        <div class="flex flex-col gap-2">
          <label class="text-xs font-medium text-muted-foreground uppercase tracking-wide flex items-center gap-1.5">
            <Link class="h-3 w-3" />
            Link mời
          </label>
          <div class="flex gap-2">
            <div
              class="flex-1 rounded-lg border border-input bg-muted/40 px-3 py-2 text-sm text-foreground truncate font-mono select-all">
              <span v-if="isLoading" class="text-muted-foreground">Đang tải link...</span>
              <span v-else>{{ inviteLink || "Chưa có link" }}</span>
            </div>
            <button @click="copyLink" :disabled="!inviteLink || isLoading" :class="[
              'px-3 py-2 rounded-lg text-sm font-medium flex items-center gap-1.5 transition shrink-0',
              isCopied
                ? 'bg-green-500/10 text-green-600 border border-green-500/30'
                : 'bg-primary text-primary-foreground hover:bg-primary/90',
              !inviteLink || isLoading ? 'opacity-50 cursor-not-allowed' : '',
            ]">
              <Check v-if="isCopied" class="h-4 w-4" />
              <Copy v-else class="h-4 w-4" />
              {{ isCopied ? "Đã sao chép" : "Sao chép" }}
            </button>
          </div>
        </div>

        <!-- Reset link -->
        <div class="flex items-center justify-between pt-1 border-t border-border">
          <div>
            <p class="text-xs font-medium text-foreground">Làm mới link</p>
            <p class="text-xs text-muted-foreground mt-0.5">
              Vô hiệu hóa link cũ và tạo link mới
            </p>
          </div>
          <button @click="resetCode" :disabled="isResetting"
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-input text-sm font-medium text-foreground hover:bg-muted transition disabled:opacity-50 disabled:cursor-not-allowed">
            <RefreshCw :class="['h-3.5 w-3.5', isResetting ? 'animate-spin' : '']" />
            {{ isResetting ? "Đang làm mới..." : "Làm mới" }}
          </button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>
