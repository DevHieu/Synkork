<script lang="ts" setup>
import { LogOutIcon } from '@lucide/vue';
import { storeToRefs } from 'pinia';
import Button from '../ui/button/Button.vue';
import NavFooter from './nav-footer.vue';
import NavTeam from './nav-team.vue'
import { sidebarData } from './data/sidebar-data'

const {logout} = useAuth()

const { user, loading } = storeToRefs(useAuthStore())

const visibleNavMain = computed(() => {
  const isAdmin = user.value?.role === 'ADMIN'

  return sidebarData.navMain
    .filter(group => isAdmin || !group.items.some(item => item.url === '/manager'))
    .map(group => ({
      ...group,
      items: group.items.filter(item => isAdmin || item.url !== '/manager'),
    }))
})
</script>

<template>
  <UiSidebar collapsible="icon" class="z-50">
    <UiSidebarHeader>
      <NavFooter v-if="!loading && user" :user="user" />
      <div v-else-if="loading" class="flex items-center gap-2 p-2">
        <UiSkeleton class="size-8 rounded-lg" />
        <div class="grid gap-1">
          <UiSkeleton class="h-3 w-24" />
          <UiSkeleton class="h-3 w-32" />
        </div>
      </div>
    </UiSidebarHeader>

    <UiSidebarContent>
      <NavTeam :nav-main="visibleNavMain" />
    </UiSidebarContent>
    
    <UiSidebarFooter>
      <UiSidebarMenu>
        <UiSidebarMenuItem>
          <UiSidebarMenuButton tooltip="Đăng xuất" as-child>
            <Button
              class="w-full cursor-pointer justify-center group-data-[collapsible=icon]:size-8 group-data-[collapsible=icon]:p-0 group-data-[collapsible=icon]:justify-center overflow-hidden"
              variant="destructive"
              @click="() => {
                logout()
              }"
            >
              <LogOutIcon class="size-4 shrink-0" />
              <span class="group-data-[collapsible=icon]:hidden truncate">Đăng xuất</span>
            </Button>
          </UiSidebarMenuButton>
        </UiSidebarMenuItem>
      </UiSidebarMenu>
    </UiSidebarFooter>
    <UiSidebarRail />
  </UiSidebar>
</template>
