<script setup lang="ts">
import {
  BadgeCheck,
  Bell,
  CreditCard,
  LogOut,
  Sparkles,
  ChevronsUpDown,
} from "lucide-vue-next";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar";
import { logout } from "@/services/authService";

const props = defineProps<{
  user: { name: string; email: string; avatar: string };
  collapsed?: boolean;
}>();

const { isMobile } = useSidebar();
</script>

<template>
  <SidebarMenu>
    <SidebarMenuItem>
      <DropdownMenu>
        <DropdownMenuTrigger as-child>
          <SidebarMenuButton
            size="lg"
            class="hover:bg-transparent active:bg-transparent"
            :class="
              collapsed ? 'h-16.5 w-12 justify-center px-1 mx-auto' : 'h-16.5'
            "
          >
            <div
              class="h-fit rounded-full p-1 hover:bg-sidebar-accent/50 active:bg-sidebar-accent/90 transition-colors duration-200"
            >
              <Avatar class="h-10 w-10 rounded-full shrink-0">
                <AvatarImage :src="user.avatar || ''" :alt="user.name" />
                <AvatarFallback
                  class="rounded-full bg-primary text-primary-foreground"
                >
                  {{ user.name?.charAt(0).toUpperCase() ?? "CN" }}
                </AvatarFallback>
              </Avatar>
            </div>

            <template v-if="!collapsed">
              <div
                class="grid flex-1 text-left text-sm leading-tight overflow-hidden"
              >
                <span class="truncate font-semibold">{{ user.name }}</span>
                <span class="truncate text-xs text-muted-foreground">{{
                  user.email
                }}</span>
              </div>
              <ChevronsUpDown
                class="ml-auto size-4 shrink-0 text-muted-foreground"
              />
            </template>
          </SidebarMenuButton>
        </DropdownMenuTrigger>

        <DropdownMenuContent
          class="w-[--reka-dropdown-menu-trigger-width] min-w-56 rounded-lg"
          :side="isMobile ? 'bottom' : 'right'"
          align="end"
          :side-offset="4"
        >
          <DropdownMenuLabel class="p-0 font-normal">
            <div class="flex items-center gap-2 px-2 py-2.5 text-left text-sm">
              <Avatar class="h-10 w-10 rounded-full">
                <AvatarImage :src="user.avatar" :alt="user.name" />
                <AvatarFallback class="rounded-full">
                  {{ user.name?.charAt(0).toUpperCase() ?? "CN" }}
                </AvatarFallback>
              </Avatar>
              <div class="grid flex-1 text-left text-sm leading-tight">
                <span class="truncate font-medium">{{ user.name }}</span>
                <span class="truncate text-xs text-muted-foreground">{{
                  user.email
                }}</span>
              </div>
            </div>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuItem><Sparkles />Upgrade to Pro</DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuItem><BadgeCheck />Account</DropdownMenuItem>
            <DropdownMenuItem><CreditCard />Billing</DropdownMenuItem>
            <DropdownMenuItem><Bell />Notifications</DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuItem @click="logout"><LogOut />Log out</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </SidebarMenuItem>
  </SidebarMenu>
</template>
