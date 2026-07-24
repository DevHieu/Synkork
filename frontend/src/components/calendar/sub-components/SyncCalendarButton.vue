<script setup lang="ts">
import axiosClient from '@/lib/axiosClient'
import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import PremiumFeatureDialog from '@/components/dialog/PremiumFeatureDialog.vue'

const userStore = useUserStore()
const showPremiumDialog = ref(false)
const isLoading = ref(false)
let popupCheckInterval: number | undefined

const handleSyncGoogleCalendar = async () => {
  if (userStore.userPlan === 'FREE') {
    showPremiumDialog.value = true
    return
  }

  isLoading.value = true
  try {
    const res = await axiosClient.get('/api/integrations/google-calendar/authorize-url')

    const authUrl = res.data.authorizeUrl

    const width = 500
    const height = 650
    const left = window.screenX + (window.outerWidth - width) / 2
    const top = window.screenY + (window.outerHeight - height) / 2

    const popup = window.open(
      authUrl,
      'google-calendar-link',
      `width=${width},height=${height},left=${left},top=${top}`
    )

    if (!popup) {
      isLoading.value = false
      return
    }

    const handleMessage = (event: MessageEvent) => {
      if (event.origin !== window.location.origin) return
      if (event.data?.type !== 'GOOGLE_CALENDAR_LINK') return

      window.removeEventListener('message', handleMessage)
      clearInterval(popupCheckInterval)
      isLoading.value = false

      if (event.data.status === 'success') {
        // toast thành công, refetch trạng thái liên kết
      } else {
        // toast lỗi
      }
    }

    window.addEventListener('message', handleMessage)

    popupCheckInterval = window.setInterval(() => {
      if (popup.closed) {
        clearInterval(popupCheckInterval)
        window.removeEventListener('message', handleMessage)
        isLoading.value = false
      }
    }, 500)
  } catch (e: any) {
    isLoading.value = false
    if (e.response?.status === 403) {
      showPremiumDialog.value = true
    }
  }
}

onUnmounted(() => {
  clearInterval(popupCheckInterval)
})
</script>

<template>
  <div>
    <button class="px-5 py-3 bg-red-300 disabled:opacity-50" :disabled="isLoading" @click="handleSyncGoogleCalendar">
      {{ isLoading ? 'Đang liên kết...' : 'Liên kết calendar' }}
    </button>

    <PremiumFeatureDialog 
      v-model:open="showPremiumDialog" 
      feature-name="Đồng bộ Google Calendar" 
      :business-only="false" 
    />
  </div>
</template>

<style scoped></style>