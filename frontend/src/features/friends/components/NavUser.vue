<script setup lang="ts">
import { Settings } from "lucide-vue-next"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import { ref } from "vue"
import SettingsModal from "@/features/friend-settings/index.vue"
import NotificationBell from "@/features/notifications/components/NotificationBell.vue"

const props = defineProps<{
  user: { name: string; email: string; avatar: string | undefined }
  collapsed?: boolean
}>()

const showSettings = ref(false)
</script>

<template>
  <SidebarMenu>
    <SidebarMenuItem>
      <SidebarMenuButton size="lg" class="hover:bg-transparent active:bg-transparent group/navuser"
        :class="collapsed ? 'h-16.5 w-12 justify-center px-1 mx-auto' : 'h-16.5'">
        <!-- Avatar -->
        <div @click.stop="showSettings = true"
          class="relative h-fit rounded-full p-1 cursor-pointer group/avatar hover:bg-sidebar-accent/50 active:bg-sidebar-accent/90 transition-colors duration-200">
          <Avatar class="h-10 w-10 rounded-full shrink-0">
            <AvatarImage v-if="user.avatar" :src="user.avatar" :alt="user.name" />
            <AvatarFallback class="rounded-full bg-primary text-primary-foreground">
              {{ user.name?.charAt(0).toUpperCase() ?? "CN" }}
            </AvatarFallback>
          </Avatar>
          <!-- Overlay mờ khi hover -->
          <div
            class="absolute inset-0 rounded-full bg-black/40 opacity-0 group-hover/avatar:opacity-100 transition-opacity duration-200" />
          <!-- Icon Settings căn giữa -->
          <div
            class="absolute inset-0 flex items-center justify-center opacity-0 group-hover/avatar:opacity-100 transition-opacity duration-200 text-white">
            <Settings class="size-4" />
          </div>
        </div>

        <template v-if="!collapsed">
          <div class="grid flex-1 text-left text-sm leading-tight overflow-hidden">
            <span class="truncate font-semibold">{{ user.name }}</span>
            <span class="truncate text-xs text-muted-foreground">{{ user.email }}</span>
          </div>

          <NotificationBell class="size-4.5" />
        </template>
      </SidebarMenuButton>
    </SidebarMenuItem>
  </SidebarMenu>

  <!-- Settings Modal -->
  <SettingsModal v-if="showSettings" @close="showSettings = false" />
</template>
