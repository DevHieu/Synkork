<script setup lang="ts">
import { ref } from "vue"
import { useFriendStore } from "@/stores/useFriendStore"

const store = useFriendStore()
const username = ref("")
const open = ref(false)
const loading = ref(false)
const error = ref("")

const emit = defineEmits(["close"])

const send = async () => {
  if (!username.value.trim()) return

  loading.value = true
  error.value = ""

  try {
    await store.sendRequest(username.value.trim())
    alert("Đã gửi lời mời kết bạn thành công!") // Có thể thay bằng toast sau
    username.value = ""
    open.value = false
    emit("close")
  } catch (e: any) {
    error.value = e || "Gửi lời mời thất bại"
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <button
    @click="open = true"
    class="w-full py-2 rounded-md text-sm font-medium
           bg-[var(--color-primary)] text-[var(--color-primary-foreground)]
           hover:opacity-90 transition"
  >
    + Thêm bạn
  </button>

  <!-- Modal -->
  <div v-if="open" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
    <div class="bg-[#36393f] p-6 rounded-lg w-96 space-y-4">
      <h2 class="text-lg font-semibold text-white">Thêm bạn bè</h2>
      
      <div>
        <p class="text-sm text-gray-300 mb-2">Nhập username của người bạn muốn kết bạn</p>
        <input
          v-model="username"
          placeholder="Nhập username..."
          class="w-full px-3 py-2.5 bg-[#202225] text-white rounded border border-[#202225] focus:outline-none focus:border-blue-500"
          :disabled="loading"
        />
      </div>

      <div v-if="error" class="text-red-400 text-sm">{{ error }}</div>

      <div class="flex justify-end gap-3 pt-2">
        <button 
          @click="open = false"
          class="px-4 py-2 text-sm text-gray-300 hover:text-white transition"
          :disabled="loading"
        >
          Hủy
        </button>
        <button 
          @click="send"
          :disabled="loading || !username.trim()"
          class="px-5 py-2 bg-[#5865f2] hover:bg-[#4752c4] text-white text-sm font-medium rounded transition disabled:opacity-50"
        >
          {{ loading ? "Đang gửi..." : "Gửi lời mời" }}
        </button>
      </div>
    </div>
  </div>
</template>