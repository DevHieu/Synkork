<script setup lang="ts">
import { ref, computed, watch } from "vue";
import Dialog from "@/components/ui/dialog/Dialog.vue";
import DialogContent from "@/components/ui/dialog/DialogContent.vue";
import DialogTitle from "@/components/ui/dialog/DialogTitle.vue";
import DialogDescription from "@/components/ui/dialog/DialogDescription.vue";
import DialogHeader from "@/components/ui/dialog/DialogHeader.vue";
import { Copy, Check, RefreshCw, Link, UserPlus, Users, Loader2 } from "lucide-vue-next";
import { useRoomsStore } from "@/features/rooms/stores/roomStore.ts";
import { storeToRefs } from "pinia";
import { useRoomService } from "@/features/rooms/services/roomService";
import { useRoomMemberStore } from '@/features/members/stores/roomMemberStore'
import { toast } from "vue-sonner";
import { useFriendStore } from "@/features/friends/stores/friendStore";
import type { Friend } from "@/features/friends/types/Friends";

const props = defineProps<{ open: boolean }>();
const emit = defineEmits<{ "update:open": [value: boolean] }>();

const handleClose = () => emit("update:open", false);

const roomService = useRoomService();

const roomStore = useRoomsStore();
const { currentRoom } = storeToRefs(roomStore);

const roomMemberStore = useRoomMemberStore();
const { canManage, members } = storeToRefs(roomMemberStore)

const friendStore = useFriendStore();
const { friends, loading: friendsLoading } = storeToRefs(friendStore);

const inviteCode = ref("");
const isLoading = ref(false);
const isCopied = ref(false);
const isResetting = ref(false);

const invitingFriendId = ref<string | null>(null);
const invitedFriendIds = ref<Set<string>>(new Set());

const isFriendInRoom = (friend: Friend) =>
  members.value.some((m) => m.username === friend.username);

const inviteLink = computed(() =>
  inviteCode.value
    ? `${window.location.origin}/invite/${inviteCode.value}`
    : "",
);

const fetchInviteCode = async () => {
  if (!currentRoom.value?.id) return;
  isLoading.value = true;
  try {
    inviteCode.value = await roomService.getInviteCode(currentRoom.value.id);
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
    const newCode = await roomService.resetInviteCode(currentRoom.value.id);
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

const handleInviteFriend = async (friend: Friend) => {
  if (!currentRoom.value?.id || invitingFriendId.value) return;

  invitingFriendId.value = friend.id;
  try {
    await roomService.inviteFriendToRoom(currentRoom.value.id, friend.id);
    invitedFriendIds.value.add(friend.id);
    toast.success(`Đã mời ${friend.username} vào phòng`);
  } catch (error: any) {
    const errorData = error?.response?.data;
    const message =
      typeof errorData === "string" ? errorData : errorData?.message;
    toast.error(message || "Không thể mời bạn bè vào phòng");
  } finally {
    invitingFriendId.value = null;
  }
};

watch(
  () => props.open,
  (val) => {
    if (val) {
      fetchInviteCode();
      friendStore.fetchFriends();
      invitedFriendIds.value = new Set();
    }
  },
);
</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent
      class="max-w-120! w-120! p-0! overflow-hidden rounded-xl border border-border bg-background shadow-2xl">
      <DialogHeader class="px-6 pt-6 pb-4 border-b border-border">
        <DialogTitle class="text-base font-semibold text-foreground">
          Mời bạn bè vào {{ currentRoom?.name }}
        </DialogTitle>
        <DialogDescription class="text-xs text-muted-foreground mt-1">
          Chia sẻ link bên dưới để mời người khác tham gia phòng
        </DialogDescription>
      </DialogHeader>

      <div class="px-6 pb-5 flex flex-col gap-5">
        <!-- Friends list -->
        <div class="flex flex-col gap-2">
          <label class="text-xs font-medium text-muted-foreground uppercase tracking-wide flex items-center gap-1.5">
            <Users class="h-3 w-3" />
            Mời bạn bè
          </label>

          <div v-if="friendsLoading && friends.length === 0" class="text-xs text-muted-foreground py-3 text-center">
            Đang tải danh sách bạn bè...
          </div>

          <div v-else-if="friends.length === 0" class="text-xs text-muted-foreground py-3 text-center">
            Bạn chưa có bạn bè nào để mời
          </div>

          <div v-else
            class="flex flex-col max-h-48 overflow-y-auto rounded-lg border border-border divide-y divide-border">
            <div v-for="friend in friends" :key="friend.id" class="flex items-center justify-between gap-2 px-3 py-2">
              <div class="flex items-center gap-2.5 min-w-0">
                <div class="relative shrink-0">
                  <div
                    class="h-8 w-8 rounded-full bg-primary/10 text-primary flex items-center justify-center text-xs font-semibold uppercase">
                    {{ friend.username?.charAt(0) }}
                  </div>
                  <span v-if="friend.isOnline"
                    class="absolute -bottom-0.5 -right-0.5 h-2.5 w-2.5 rounded-full bg-green-500 border-2 border-background" />
                </div>
                <div class="min-w-0">
                  <p class="text-sm font-medium text-foreground truncate">{{ friend.username }}</p>
                  <p class="text-xs text-muted-foreground">{{ friend.isOnline ? "Đang hoạt động" : "Ngoại tuyến" }}</p>
                </div>
              </div>

              <button v-if="isFriendInRoom(friend)" disabled
                class="px-2.5 py-1.5 rounded-lg text-xs font-medium text-muted-foreground bg-muted/50 shrink-0 cursor-default">
                Đã trong phòng
              </button>
              <button v-else @click="handleInviteFriend(friend)" :disabled="invitingFriendId === friend.id" :class="[
                'px-2.5 py-1.5 rounded-lg text-xs font-medium flex items-center gap-1 transition shrink-0',
                invitedFriendIds.has(friend.id)
                  ? 'bg-green-500/10 text-green-600 border border-green-500/30'
                  : 'bg-primary text-primary-foreground hover:bg-primary/90',
                invitingFriendId === friend.id ? 'opacity-60 cursor-not-allowed' : '',
              ]">
                <Check v-if="invitedFriendIds.has(friend.id)" class="h-3.5 w-3.5" />
                <Loader2 v-else-if="invitingFriendId === friend.id" class="h-3.5 w-3.5 animate-spin" />
                <UserPlus v-else class="h-3.5 w-3.5" />
                {{ invitedFriendIds.has(friend.id) ? "Đã mời" : invitingFriendId === friend.id ? "Đang mời..." : "Mời"
                }}
              </button>
            </div>
          </div>
        </div>

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
