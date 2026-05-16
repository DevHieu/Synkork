<script lang="ts" setup>
import Button from '../ui/button/Button.vue';
import { sidebarData } from './data/sidebar-data'
import NavFooter from './nav-footer.vue'
import NavTeam from './nav-team.vue'

const {logout} = useAuth()

</script>

<template>
  <UiSidebar collapsible="icon" class="z-50">
    <UiSidebarHeader>
      <NavFooter :user="sidebarData.user" />
    </UiSidebarHeader>

    <UiSidebarContent>
      <NavTeam :nav-main="sidebarData.navMain" />
    </UiSidebarContent>
    
    <UiSidebarFooter>
      <Button class="cursor-pointer" variant="destructive" @click="() => {
        logout()
      }">Log out</Button>
    </UiSidebarFooter>
    <UiSidebarRail />
  </UiSidebar>
</template>
