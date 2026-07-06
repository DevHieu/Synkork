<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { LayoutGrid, Lock, Users } from '@lucide/vue'
import { Badge } from '@/components/ui/badge'
import { roomService } from '@/pages/rooms/service/roomService'
import type { Room } from '@/pages/rooms/types/RoomTypes'

const rooms = ref<Room[]>([])
const loading = ref(false)

async function fetchTopRooms() {
  loading.value = true
  try {
    // type=GROUP cần truyền vì endpoint /manage/rooms dùng chung với trang
    // quản lý Rooms (hiển thị cả DM/PERSONAL), không thể ép cứng GROUP ở BE
    const res = await roomService.getRooms({ page: 0, size: 5, type: 'GROUP' })
    // Sort by memberCount descending
    const data: Room[] = res.data ?? []
    rooms.value = data.sort((a, b) => b.memberCount - a.memberCount).slice(0, 5)
  } catch (err) {
    console.error('Failed to fetch top rooms:', err)
  } finally {
    loading.value = false
  }
}

onMounted(fetchTopRooms)
</script>

<template>
  <div class="space-y-1">
    <!-- Loading skeleton -->
    <div v-if="loading" class="flex flex-col gap-3 py-2">
      <div v-for="i in 5" :key="i" class="flex items-center gap-3 animate-pulse">
        <div class="h-9 w-9 rounded-full bg-muted shrink-0" />
        <div class="flex-1 space-y-1.5">
          <div class="h-3 w-2/3 rounded bg-muted" />
          <div class="h-2.5 w-1/3 rounded bg-muted" />
        </div>
        <div class="h-5 w-12 rounded bg-muted" />
      </div>
    </div>

    <!-- Empty -->
    <div
      v-else-if="rooms.length === 0"
      class="flex flex-col items-center justify-center py-10 text-muted-foreground gap-2"
    >
      <LayoutGrid class="h-8 w-8 opacity-40" />
      <p class="text-sm">Không có dữ liệu room.</p>
    </div>

    <!-- List -->
    <div v-else class="space-y-1">
      <div
        v-for="(room, index) in rooms"
        :key="room.id"
        class="flex w-full items-center gap-3 rounded-lg px-2 py-2"
      >
        <!-- Rank -->
        <span class="w-5 text-center text-xs font-semibold text-muted-foreground shrink-0">
          {{ index + 1 }}
        </span>

        <!-- Avatar -->
        <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-muted overflow-hidden">
          <img v-if="room.avatarUrl" :src="room.avatarUrl" :alt="room.name" class="h-full w-full object-cover" />
          <LayoutGrid v-else class="h-4 w-4 text-muted-foreground" />
        </div>

        <!-- Info -->
        <div class="flex-1 min-w-0 space-y-0.5">
          <p class="text-sm font-medium leading-tight truncate">{{ room.name }}</p>
          <p class="text-xs text-muted-foreground leading-tight truncate">
            {{ room.ownerUsername ?? 'Unknown' }}
          </p>
        </div>

        <!-- Member count -->
        <div class="flex items-center gap-1 text-xs text-muted-foreground shrink-0">
          <Users class="h-3 w-3" />
          {{ room.memberCount }}
        </div>

        <!-- Status badge -->
        <Badge
          :variant="room.status === 'OPEN' ? 'default' : 'destructive'"
          class="text-[11px] shrink-0 gap-1"
        >
          <Lock v-if="room.status !== 'OPEN'" class="h-3 w-3" />
          {{ room.status }}
        </Badge>
      </div>
    </div>

    <div class="pt-2 text-center">
      <RouterLink
        to="/rooms"
        class="text-sm text-muted-foreground underline-offset-4 hover:underline hover:text-foreground transition-colors"
      >
        Xem tất cả rooms
      </RouterLink>
    </div>
  </div>
</template>