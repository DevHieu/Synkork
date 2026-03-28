<script setup lang="ts">
import { useRouter } from 'vue-router'
import { SidebarHeader, SidebarContent } from "@/components/ui/sidebar" // Đảm bảo import đủ component
import { useFriendStore } from "@/stores/useFriendStore"
import { storeToRefs } from "pinia"
import { onMounted } from "vue"

const router = useRouter()
const store = useFriendStore()

// Sử dụng storeToRefs để lấy dữ liệu reactive
const { friends, loading, friendCount } = storeToRefs(store)

onMounted(() => {
  store.fetchFriends()
})

// Hàm bổ trợ để lấy 2 chữ cái đầu của tên làm Avatar
const getInitials = (name: string) => {
  return name ? name.substring(0, 2).toUpperCase() : '??'
}
</script>

<template>
  <SidebarHeader class="gap-3.5 border-b p-3">
    <div class="flex w-full items-center justify-between">
      <input
        type="text"
        placeholder="Tìm bạn..."
        class="w-full rounded-lg border px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
      />
    </div>
  </SidebarHeader>

  <SidebarContent class="mt-5 bg-[var(--color-sidebar)] text-[var(--color-sidebar-foreground)]">
    <div class="p-2 space-y-1">
      <div
        @click="router.push('/me/friends')"
        class="p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)]"
      >
        Bạn bè
      </div>
      <div class="p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)]">
        Ghi chú
      </div>
      <div class="p-2 rounded cursor-pointer hover:bg-[var(--color-sidebar-accent)]">
        Lịch
      </div>
    </div>

    <div class="p-2">
      <button
        class="w-full py-2 rounded-md text-sm font-medium
        bg-[var(--color-primary)] text-[var(--color-primary-foreground)]
        hover:opacity-90 transition"
      >
        + Thêm bạn
      </button>
    </div>

    <div class="px-3 mt-3 text-xs text-[var(--color-muted-foreground)] uppercase">
      Bạn bè — {{ friendCount }}
    </div>

    <div v-if="loading" class="px-5 py-3 text-sm text-muted-foreground italic">
      Đang tải danh sách...
    </div>

    <div v-else-if="friends.length === 0" class="px-5 py-3 text-sm text-muted-foreground">
      Chưa có bạn bè nào.
    </div>

    <div v-else class="px-2 mt-2 space-y-1">
      <div
        v-for="friend in friends"
        :key="friend.id"
        class="flex items-center gap-2 p-2 rounded cursor-pointer
        hover:bg-[var(--color-sidebar-accent)] transition"
      >
        <div class="relative">
          <div class="w-8 h-8 rounded-full bg-[var(--color-muted)] flex items-center justify-center text-xs font-bold uppercase">
            {{ getInitials(friend.name) }}
          </div>

          <div class="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full border-2 border-[var(--color-sidebar)] bg-green-500" />
        </div>

        <span class="text-sm truncate">{{ friend.name }}</span>
      </div>
    </div>
  </SidebarContent>
</template>