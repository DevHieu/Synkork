<script setup lang="ts">
import { getUserInfoByUsername } from "@/services/userService";
import { useFriendStore } from "@/stores/friendStore";
import type { User } from "@/types/User";
import { computed, onMounted, ref } from "vue";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import Avatar from "../ui/avatar/Avatar.vue";
import AvatarImage from "../ui/avatar/AvatarImage.vue";
import AvatarFallback from "../ui/avatar/AvatarFallback.vue";
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
  TooltipProvider,
} from "@/components/ui/tooltip";
import { UserPlus, UserMinus, MessageCircle, Flag, Clock3, TicketCheck, TicketX } from "lucide-vue-next";
import { useUserStore } from "@/stores/userStore";

const props = defineProps<{ username: string }>();

const emit = defineEmits<{
  sendMessage: [username: string];
  report: [username: string];
}>();

const friendStore = useFriendStore();

const isLoading = ref(true);
const userInfo = ref<User | null>(null);
const isOpen = ref(false);
const isMyself = ref(false);
const friendship = computed(() =>
  friendStore.getFriendshipStatus(props.username),
);
const isFriendLoading = ref(false);

onMounted(async () => {
  isLoading.value = true;
  userInfo.value = await getUserInfoByUsername(props.username);
  isMyself.value = (useUserStore().user as any)?.username === props.username;
  isLoading.value = false;
});

async function toggleFriend() {
  isFriendLoading.value = true;

  try {
    if (friendship.value.isFriend) {
      await friendStore.removeFriend(
        friendship.value.friend!.id,
      );
      return;
    }

    if (friendship.value.isPending) {
      await friendStore.cancelRequest(
        friendship.value.sentRequest!.id,
      );
      return;
    }

    await friendStore.sendRequest(props.username);
    await friendStore.fetchSentRequests();

  } catch (e: any) {
    if (
      typeof e === "string" &&
      e.includes("đã gửi lời mời")
    ) {
      await friendStore.fetchSentRequests();
      return;
    }

    console.error(e);
  } finally {
    isFriendLoading.value = false;
  }
}


</script>

<template>
  <Popover v-model:open="isOpen">
    <PopoverTrigger as-child>
      <slot />
    </PopoverTrigger>

    <PopoverContent class="w-64 p-0 bg-black/40 backdrop-blur-md border-white/10 overflow-hidden" side="right"
      align="start" @open-auto-focus.prevent>
      <div class="relative h-16 bg-primary/30">
        <div v-if="!isLoading && userInfo && !isMyself" class="absolute top-2 right-2 flex items-center gap-1.5">
          <!-- Kết bạn / Xóa bạn -->
          <TooltipProvider>

            <template v-if="!friendship.isReceived">
              <Tooltip>
                <TooltipTrigger as-child>
                  <button
                    class="h-8 w-8 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center transition-colors disabled:opacity-50"
                    :disabled="isFriendLoading" @click="toggleFriend">
                    <UserMinus v-if="friendship.isFriend" class="h-4 w-4 text-destructive" />
                    <Clock3 v-else-if="friendship.isPending" class="h-4 w-4 text-yellow-500" />
                    <UserPlus v-else class="h-4 w-4 text-white" />
                  </button>
                </TooltipTrigger>
                <TooltipContent side="bottom">
                  {{ friendship.isFriend ? "Xóa bạn" : friendship.isPending ? "Đã gửi lời mời" : "Kết bạn" }}
                </TooltipContent>
              </Tooltip>
            </template>

            <!-- Accept / Reject buttons — only shown when they sent you a request -->
            <template v-else>
              <Tooltip>
                <TooltipTrigger as-child>
                  <button
                    class="h-8 w-8 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center transition-colors disabled:opacity-50"
                    :disabled="isFriendLoading" @click="() => {
                      friendStore.acceptRequest(friendship.requestId!);
                    }">
                    <TicketCheck class="h-4 w-4 text-green-500" />
                  </button>
                </TooltipTrigger>
                <TooltipContent side="bottom">Chấp nhận</TooltipContent>
              </Tooltip>

              <Tooltip>
                <TooltipTrigger as-child>
                  <button
                    class="h-8 w-8 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center transition-colors disabled:opacity-50"
                    :disabled="isFriendLoading" @click="() => {
                      friendStore.rejectRequest(friendship.requestId!);
                    }">
                    <TicketX class="h-4 w-4 text-destructive" />
                  </button>
                </TooltipTrigger>
                <TooltipContent side="bottom">Từ chối</TooltipContent>
              </Tooltip>
            </template>
            <!-- Nhắn tin -->
            <Tooltip>
              <TooltipTrigger as-child>
                <button v-if="friendship.isFriend"
                  class="h-8 w-8 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center transition-colors"
                  @click="emit('sendMessage', username)">
                  <MessageCircle class="h-4 w-4 text-white" />
                </button>
              </TooltipTrigger>
              <TooltipContent side="bottom">Nhắn tin</TooltipContent>
            </Tooltip>

            <!-- Tố cáo -->
            <Tooltip>
              <TooltipTrigger as-child>
                <button
                  class="h-8 w-8 rounded-full bg-black/50 hover:bg-black/70 flex items-center justify-center transition-colors"
                  @click="() => {
                    console.log(friendship);
                  }">
                  <Flag class="h-4 w-4 text-white hover:text-destructive" />
                </button>
              </TooltipTrigger>
              <TooltipContent side="bottom">Tố cáo</TooltipContent>
            </Tooltip>
          </TooltipProvider>
        </div>

        <!-- Avatar — đè lên border banner/content -->
        <div class="absolute -bottom-8 left-4">
          <Avatar class="h-16 w-16 text-xs font-bold uppercase ring-4 ring-black/40">
            <AvatarImage v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" />
            <AvatarFallback class="bg-primary" />
          </Avatar>
        </div>
      </div>

      <!-- Content -->
      <div class="pt-10 px-4 pb-4">
        <!-- Loaded -->
        <div v-if="!isLoading && userInfo" class="space-y-3">
          <div class="flex flex-col gap-0.5">
            <span class="text-muted-foreground text-xs font-medium uppercase tracking-wide">
              Tên hiển thị
            </span>
            <span class="text-sm font-medium">{{ userInfo.displayName }}</span>
          </div>
          <div class="flex flex-col gap-0.5">
            <span class="text-muted-foreground text-xs font-medium uppercase tracking-wide">
              Tên đăng nhập
            </span>
            <span class="text-sm font-medium">{{ userInfo.username }}</span>
          </div>
          <div class="flex flex-col gap-0.5">
            <span class="text-muted-foreground text-xs font-medium uppercase tracking-wide">
              Email
            </span>
            <span class="text-sm font-medium">{{ userInfo.email }}</span>
          </div>
        </div>

        <!-- Skeleton -->
        <div v-else class="space-y-3">
          <div v-for="i in 3" :key="i" class="flex flex-col gap-1.5">
            <div class="h-3 w-20 rounded bg-muted animate-pulse" />
            <div class="h-4 w-36 rounded bg-muted animate-pulse" />
          </div>
        </div>
      </div>
    </PopoverContent>
  </Popover>
</template>