<script lang="ts" setup>
import { storeToRefs } from 'pinia';
import Button from '../ui/button/Button.vue';
import { sidebarData } from './data/sidebar-data'
import NavTeam from './nav-team.vue'
import NavFooter from './nav-footer.vue';

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
      <Button class="cursor-pointer" variant="destructive" @click="() => {
        logout()
      }">Log out</Button>
    </UiSidebarFooter>
    <UiSidebarRail />
  </UiSidebar>
</template>
