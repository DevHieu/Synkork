<script setup lang="ts">
import { Bell, Settings } from "lucide-vue-next"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import { ref } from "vue"
import SettingsModal from "@/components/sidebar/modals/SettingsModal.vue"
import NotificationBell from "../notification/NotificationBell.vue"

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
          class="h-fit rounded-full p-1 hover:bg-sidebar-accent/50 active:bg-sidebar-accent/90 transition-colors duration-200">
          <Avatar class="h-10 w-10 rounded-full shrink-0">
            <AvatarImage v-if="user.avatar" :src="user.avatar" :alt="user.name" />
            <AvatarFallback class="rounded-full bg-primary text-primary-foreground">
              {{ user.name?.charAt(0).toUpperCase() ?? "CN" }}
            </AvatarFallback>
          </Avatar>
        </div>

        <template v-if="!collapsed">
          <!-- Name + email -->
          <div class="grid flex-1 text-left text-sm leading-tight overflow-hidden">
            <span class="truncate font-semibold">{{ user.name }}</span>
            <span class="truncate text-xs text-muted-foreground">{{ user.email }}</span>
          </div>

          <!-- Settings gear — shown on hover -->
          <button @click.stop="showSettings = true" class="ml-auto p-1.5 rounded-md opacity-0 group-hover/navuser:opacity-100
                   hover:bg-sidebar-accent transition-all duration-200
                   text-sidebar-foreground hover:text-sidebar-foreground" title="Cài đặt người dùng">
          </button>
          <NotificationBell class="size-[18px]" />
        </template>

        <template v-else>
          <!-- Collapsed state: tiny gear on hover -->
          <button @click.stop="showSettings = true" class="absolute bottom-1 right-1 p-0.5 opacity-0 group-hover/navuser:opacity-100
                   transition-all duration-200 text-sidebar-foreground hover:text-sidebar-foreground" title="Cài đặt">
            <Settings class="size-3" />
          </button>
        </template>
      </SidebarMenuButton>
    </SidebarMenuItem>
  </SidebarMenu>

  <!-- Settings Modal -->
  <SettingsModal v-if="showSettings" @close="showSettings = false" />
</template>