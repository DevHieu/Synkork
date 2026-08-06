<script setup lang="ts">
import { ref } from 'vue'

import { BasicPage } from '@/components/global-layout'

import ManagerAccounts from './tabs/manager-accounts.vue'
import PasswordResetRequests from './tabs/password-reset-requests.vue'

const tabs = ref([
  { name: 'Quản lý tài khoản', value: 'accounts' },
  { name: 'Yêu cầu đổi mật khẩu', value: 'password-requests' },
])

const activeTab = ref(tabs.value[0].value)
</script>

<template>
  <BasicPage
    title="Quản lý tài khoản quản trị"
    description="Quản lý các tài khoản quản lý và quản trị viên trong hệ thống"
    sticky
  >
    <UiTabs :default-value="activeTab" class="w-full">
      <UiTabsList>
        <UiTabsTrigger
          v-for="tab in tabs"
          :key="tab.value"
          :value="tab.value"
        >
          {{ tab.name }}
        </UiTabsTrigger>
      </UiTabsList>

      <UiTabsContent value="accounts" class="space-y-4">
        <ManagerAccounts />
      </UiTabsContent>

      <UiTabsContent value="password-requests" class="space-y-4">
        <PasswordResetRequests />
      </UiTabsContent>
    </UiTabs>
  </BasicPage>
</template>
