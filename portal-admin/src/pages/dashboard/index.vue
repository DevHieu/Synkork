<script lang="ts" setup>
import { toast } from 'vue-sonner'

import { BasicPage } from '@/components/global-layout'
import { Button } from '@/components/ui/button'

import OverviewContent from './tabs/overview-content.vue'
import UserOverview from './tabs/user-overview.vue'
import RoomOverview from './tabs/room-overview.vue'
import MessageOverview from './tabs/message-overview.vue'

const tabs = ref([
  { name: 'Overview', value: 'overview' },
  { name: 'Rooms & Spaces', value: 'rooms' },
  { name: 'Messages', value: 'messages' },
  { name: 'Users', value: 'users' },
])

const activeTab = ref(tabs.value[0].value)
</script>

<template>
  <BasicPage
    title="workspace"
    description="workspace description"
    sticky
  >
    <template #actions>
      <Button
        @click="() => toast('hello', {
          position: 'top-center',
        })"
      >
        {{ $t('download') }}
      </Button>
    </template>

    <UiTabs :default-value="activeTab" class="w-full">
      <UiTabsList>
        <UiTabsTrigger
          v-for="tab in tabs" :key="tab.value"
          :value="tab.value"
        >
          {{ tab.name }}
        </UiTabsTrigger>
      </UiTabsList>
      <UiTabsContent value="overview" class="space-y-4">
        <OverviewContent />
      </UiTabsContent>
      <UiTabsContent value="users" class="space-y-4">
        <UserOverview />
      </UiTabsContent>
      <UiTabsContent value="rooms" class="space-y-4">
        <RoomOverview />
      </UiTabsContent>
      <UiTabsContent value="messages" class="space-y-4">
        <MessageOverview />
      </UiTabsContent>
    </UiTabs>
  </BasicPage>
</template>
