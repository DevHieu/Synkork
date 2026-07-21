<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { BasicPage } from '@/components/global-layout'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'

import SubscriptionOrdersTab from './tabs/subscription-orders-tab.vue'
import SubscriptionPricingTab from './tabs/subscription-pricing-tab.vue'
import UserSubscriptionsTab from './tabs/user-subscriptions-tab.vue'

const route = useRoute()
const activeTab = ref('user-subscriptions')
const invoiceKeyword = ref('')
const userSubscriptionKeyword = ref('')

const validTabs = ['user-subscriptions', 'orders', 'pricing']

function handleViewInvoice(invoiceId: string) {
  invoiceKeyword.value = invoiceId
  activeTab.value = 'orders'
}

watch(
  [() => route.query.tab, () => route.query.keyword],
  ([tab, keyword]) => {
    const nextTab = typeof tab === 'string' && validTabs.includes(tab)
      ? tab
      : activeTab.value
    const nextKeyword = typeof keyword === 'string' ? keyword : ''

    activeTab.value = nextTab

    if (nextTab === 'user-subscriptions')
      userSubscriptionKeyword.value = nextKeyword

    if (nextTab === 'orders')
      invoiceKeyword.value = nextKeyword
  },
  { immediate: true },
)
</script>

<template>
  <BasicPage
    title="Gói đăng ký"
    description="Quản lý hóa đơn và lịch sử thanh toán theo kiểu bảng vận hành."
    sticky
  >
    <Tabs v-model="activeTab" class="space-y-4">
      <TabsList class="grid w-full max-w-2xl grid-cols-3">
        <TabsTrigger value="user-subscriptions">
          Gói người dùng
        </TabsTrigger>
        <TabsTrigger value="orders">
          Đơn mua gói
        </TabsTrigger>
        <TabsTrigger value="pricing">
          Quản lí giá
        </TabsTrigger>
      </TabsList>

      <TabsContent value="orders">
        <SubscriptionOrdersTab :keyword="invoiceKeyword" />
      </TabsContent>

      <TabsContent value="user-subscriptions">
        <UserSubscriptionsTab :keyword="userSubscriptionKeyword" @view-invoice="handleViewInvoice" />
      </TabsContent>

      <TabsContent value="pricing">
        <SubscriptionPricingTab />
      </TabsContent>
    </Tabs>
  </BasicPage>
</template>
