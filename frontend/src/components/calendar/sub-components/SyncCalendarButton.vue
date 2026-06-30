<script setup lang="ts">
import axiosClient from '@/lib/axiosClient'
import { ref, onUnmounted } from 'vue'

const isLoading = ref(false)
let popupCheckInterval: number | undefined

const handleSyncGoogleCalendar = async () => {
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
      // toast: trình duyệt block popup, nhắc user cho phép popup
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

    // Phòng trường hợp user tự đóng popup giữa đường (không có postMessage)
    popupCheckInterval = window.setInterval(() => {
      if (popup.closed) {
        clearInterval(popupCheckInterval)
        window.removeEventListener('message', handleMessage)
        isLoading.value = false
      }
    }, 500)
  } catch (e) {
    isLoading.value = false
    // toast lỗi gọi API
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
  </div>
</template>

<style scoped></style>