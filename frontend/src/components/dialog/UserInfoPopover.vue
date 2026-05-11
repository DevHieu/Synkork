<script setup lang="ts">
import { getUserInfoByUsername } from "@/services/userService";
import type { User } from "@/types/User";
import { onMounted, ref } from "vue";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import Avatar from "../ui/avatar/Avatar.vue";
import AvatarImage from "../ui/avatar/AvatarImage.vue";
import AvatarFallback from "../ui/avatar/AvatarFallback.vue";

const props = defineProps<{ username: string }>();

const isLoading = ref(true);
const userInfo = ref<User | null>(null);
const isOpen = ref(false);

onMounted(async () => {
  isLoading.value = true;
  userInfo.value = await getUserInfoByUsername(props.username);
  isLoading.value = false;
});
</script>

<template>
  <Popover v-model:open="isOpen">
    <PopoverTrigger as-child>
      <slot />
    </PopoverTrigger>

    <PopoverContent
      class="w-64 p-4 bg-black/40 backdrop-blur-md border-white/10"
      side="right"
      align="start"
    >
      <!-- Loaded -->
      <div
        v-if="!isLoading && userInfo"
        class="flex flex-col items-center gap-4"
      >
        <Avatar class="h-16 w-16 text-xs font-bold uppercase">
          <AvatarImage v-if="userInfo.avatarUrl" :src="userInfo.avatarUrl" />
          <AvatarFallback class="bg-primary"> </AvatarFallback>
        </Avatar>

        <div class="w-full space-y-3">
          <div class="flex flex-col gap-0.5">
            <span
              class="text-muted-foreground text-xs font-medium uppercase tracking-wide"
            >
              Tên hiển thị
            </span>
            <span class="text-sm font-medium">{{ userInfo.displayName }}</span>
          </div>
          <div class="flex flex-col gap-0.5">
            <span
              class="text-muted-foreground text-xs font-medium uppercase tracking-wide"
            >
              Tên đăng nhập
            </span>
            <span class="text-sm font-medium">{{ userInfo.username }}</span>
          </div>
          <div class="flex flex-col gap-0.5">
            <span
              class="text-muted-foreground text-xs font-medium uppercase tracking-wide"
            >
              Email
            </span>
            <span class="text-sm font-medium">{{ userInfo.email }}</span>
          </div>
        </div>
      </div>

      <!-- Skeleton -->
      <div v-else class="flex flex-col items-center gap-4">
        <div class="h-16 w-16 rounded-full bg-muted animate-pulse" />
        <div class="w-full space-y-3">
          <div v-for="i in 3" :key="i" class="flex flex-col gap-1.5">
            <div class="h-3 w-20 rounded bg-muted animate-pulse" />
            <div class="h-4 w-36 rounded bg-muted animate-pulse" />
          </div>
        </div>
      </div>
    </PopoverContent>
  </Popover>
</template>
