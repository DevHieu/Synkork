<script setup lang="ts">
import { ref } from "vue"
import { useFriendStore } from "@/stores/useFriendStore"

const emit = defineEmits<{ (e: 'switchTab', tab: 'pending'): void }>()

const store = useFriendStore()

const username = ref("")
const isSending = ref(false)
const sendSuccess = ref(false)
const sendError = ref("")

const sendFriendRequest = async () => {
  if (!username.value.trim()) return

  isSending.value = true
  sendSuccess.value = false
  sendError.value = ""

  try {
    await store.sendRequest(username.value.trim())
    username.value = ""
    sendSuccess.value = true
    // Chuyển sang tab pending ngay lập tức — không delay
    emit('switchTab', 'pending')
  } catch (error: any) {
    sendError.value = typeof error === "string" ? error : "Gửi lời mời thất bại"
  } finally {
    isSending.value = false
  }
}
</script>

<template>
  <div class="flex-1 flex flex-col items-center justify-center p-8">
    <div class="max-w-md w-full text-center">
      <h1 class="text-3xl font-semibold mb-3">Thêm Bạn</h1>
      <p class="text-muted-foreground mb-8">
        Nhập tên người dùng của họ để gửi lời mời kết bạn.
      </p>

      <div class="flex gap-3">
        <input
          v-model="username"
          placeholder="Nhập tên người dùng"
          class="flex-1 bg-muted border border-border focus:border-primary rounded-md px-5 py-3 text-sm"
          @keyup.enter="sendFriendRequest"
        />
        <button
          @click="sendFriendRequest"
          :disabled="isSending || !username.trim()"
          class="bg-primary hover:bg-primary/90 disabled:bg-primary/70 disabled:cursor-not-allowed
                 text-primary-foreground px-6 py-3 rounded-md font-medium transition"
        >
          <span v-if="isSending">Đang gửi...</span>
          <span v-else>Gửi Yêu Cầu</span>
        </button>
      </div>

      <div v-if="sendSuccess" class="mt-4 text-sm text-green-500">
        ✓ Đã gửi lời mời kết bạn thành công!
      </div>
      <div v-if="sendError" class="mt-4 text-sm text-red-400">
        {{ sendError }}
      </div>
    </div>
  </div>
</template>