<script lang="ts" setup>
import { BasicPage } from '@/components/global-layout'

import OverviewContent from './tabs/overview-content.vue'
import ReportOverview from './tabs/report-overview.vue'
import RoomOverview from './tabs/room-overview.vue'
import SubscriptionOverview from './tabs/subscription-overview.vue'
import UserOverview from './tabs/user-overview.vue'

const tabs = ref([
  { name: 'Tổng quát', value: 'overview' },
  { name: 'Người dùng', value: 'users' },
  { name: 'Phòng & Không gian', value: 'rooms' },
  { name: 'Gói đăng ký', value: 'subscriptions' },
  { name: 'Tố cáo', value: 'report' },
])

const activeTab = ref(tabs.value[0].value)
</script>

<template>
  <BasicPage
    title="Thống kê tổng quát"
    description="Theo dõi tổng quan các chỉ số và tình hình hoạt động của hệ thống."
    sticky
  >
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
      <UiTabsContent value="subscriptions" class="space-y-4">
        <SubscriptionOverview />
      </UiTabsContent>
      <UiTabsContent value="report" class="space-y-4">
        <ReportOverview />
      </UiTabsContent>
    </UiTabs>
  </BasicPage>
</template>
