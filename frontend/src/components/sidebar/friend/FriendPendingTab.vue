<script setup lang="ts">
import { useFriendStore } from "@/stores/useFriendStore"

const store = useFriendStore()

const handleAccept = async (requestId: string) => {
  await store.acceptRequest(requestId)
}

const handleReject = async (requestId: string) => {
  await store.rejectRequest(requestId)
}

const handleCancel = async (requestId: string) => {
  await store.cancelRequest(requestId)
}
</script>

<template>
  <div class="flex-1 flex flex-col overflow-y-auto">

    <!-- ====== ĐÃ NHẬN ====== -->
    <template v-if="store.pendingRequests.length > 0">
      <div class="px-6 pt-4 pb-1 text-xs uppercase text-muted-foreground font-semibold">
        Đã nhận — {{ store.pendingRequests.length }}
      </div>

      <div class="px-2">
        <div
          v-for="req in store.pendingRequests"
          :key="req.id"
          class="group flex items-center gap-3 px-4 py-3 mx-2 rounded hover:bg-card transition"
        >
          <!-- Avatar -->
          <div class="relative w-10 h-10 flex-shrink-0">
            <div class="w-10 h-10 rounded-full bg-muted flex items-center justify-center text-sm font-bold overflow-hidden border border-border">
              {{ req.senderName?.slice(0, 2).toUpperCase() }}
            </div>
            <div class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full border-2 border-card bg-green-500" />
          </div>

          <!-- Info -->
          <div class="flex-1 min-w-0">
            <div class="font-medium truncate">{{ req.senderName }}</div>
            <div class="text-xs text-muted-foreground">Lời mời kết bạn đến từ họ</div>
          </div>

          <!-- Actions -->
          <div class="flex gap-2">
            <button
              @click="handleAccept(req.id)"
              class="w-8 h-8 rounded-full border border-border flex items-center justify-center text-green-500 hover:bg-green-500 hover:text-white hover:border-green-500 transition"
              title="Chấp nhận"
            >
              ✓
            </button>
            <button
              @click="handleReject(req.id)"
              class="w-8 h-8 rounded-full border border-border flex items-center justify-center text-muted-foreground hover:bg-red-500 hover:text-white hover:border-red-500 transition"
              title="Từ chối"
            >
              ✕
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- ====== ĐÃ GỬI ====== -->
    <template v-if="store.sentRequests.length > 0">
      <div class="px-6 pt-4 pb-1 text-xs uppercase text-muted-foreground font-semibold">
        Đã gửi — {{ store.sentRequests.length }}
      </div>

      <div class="px-2">
        <div
          v-for="req in store.sentRequests"
          :key="req.id"
          class="group flex items-center gap-3 px-4 py-3 mx-2 rounded hover:bg-card transition"
        >
          <!-- Avatar -->
          <div class="relative w-10 h-10 flex-shrink-0">
            <div class="w-10 h-10 rounded-full bg-muted flex items-center justify-center text-sm font-bold overflow-hidden border border-border">
              {{ req.receiverName?.slice(0, 2).toUpperCase() }}
            </div>
            <div class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full border-2 border-card bg-yellow-400" />
          </div>

          <!-- Info -->
          <div class="flex-1 min-w-0">
            <div class="font-medium truncate">{{ req.receiverName }}</div>
            <div class="text-xs text-muted-foreground">Đang chờ họ chấp nhận...</div>
          </div>

          <!-- Hủy lời mời -->
          <button
            @click="handleCancel(req.id)"
            class="w-8 h-8 rounded-full border border-border flex items-center justify-center text-muted-foreground hover:bg-red-500 hover:text-white hover:border-red-500 transition"
            title="Hủy lời mời"
          >
            ✕
          </button>
        </div>
      </div>
    </template>

    <!-- Trống -->
    <div
      v-if="store.pendingRequests.length === 0 && store.sentRequests.length === 0"
      class="text-center py-20 text-muted-foreground"
    >
      Không có lời mời kết bạn nào đang chờ.
    </div>

  </div>
</template>