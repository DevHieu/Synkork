<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import axiosClient from '@/lib/axiosClient'
import { toast } from 'vue-sonner'
import { UsersIcon, LayersIcon, CrownIcon, CalendarIcon, KeyRoundIcon } from '@lucide/vue'
import Badge from '@/components/ui/badge/Badge.vue'
import type { RoomDetail } from '../data/schema'

const props = defineProps<{ roomId: string }>()
defineEmits(['close'])

const loading = ref(false)
const room = ref<RoomDetail | null>(null)

async function fetchDetail() {
  try {
    loading.value = true
    const res = await axiosClient.get(`/manage/rooms/${props.roomId}`)
    room.value = res.data
  } catch (e: any) {
    toast.error(e.response?.data?.message || 'Lỗi khi tải chi tiết room')
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="max-h-[600px] overflow-y-auto space-y-6 px-1">
    <div v-if="loading" class="text-center py-8 text-muted-foreground">Loading...</div>

    <template v-else-if="room">
      <!-- Header info -->
      <div class="space-y-1">
        <h3 class="text-lg font-semibold">{{ room.name }}</h3>
        <p v-if="room.description" class="text-sm text-muted-foreground">{{ room.description }}</p>
        <div class="flex items-center gap-2 flex-wrap pt-1">
          <Badge variant="outline" class="bg-violet-100/40 text-violet-900 dark:text-violet-200 border-violet-300">
            {{ room.type }}
          </Badge>
          <Badge variant="outline" :class="room.status === 'OPEN'
            ? 'bg-teal-100/30 text-teal-900 dark:text-teal-200 border-teal-200'
            : 'bg-neutral-300/40 border-neutral-300'">
            {{ room.status }}
          </Badge>
        </div>
      </div>

      <!-- Invite code -->
      <div v-if="room.inviteCode" class="flex items-center gap-2 text-sm">
        <KeyRoundIcon class="h-4 w-4 text-muted-foreground" />
        <span class="text-muted-foreground">Invite Code:</span>
        <code class="font-mono font-semibold">{{ room.inviteCode }}</code>
      </div>

      <!-- Owner -->
      <div v-if="room.owner" class="space-y-2">
        <div class="flex items-center gap-2 text-sm font-medium">
          <CrownIcon class="h-4 w-4 text-amber-500" />
          Owner
        </div>
        <div class="flex items-center gap-3 p-2 rounded-md border bg-muted/30">
          <img v-if="room.owner.avatarUrl" :src="room.owner.avatarUrl" class="h-8 w-8 rounded-full object-cover" />
          <div v-else class="h-8 w-8 rounded-full bg-muted flex items-center justify-center text-xs font-bold uppercase">
            {{ room.owner.username?.charAt(0) }}
          </div>
          <div>
            <div class="text-sm font-medium">{{ room.owner.username }}</div>
            <div class="text-xs text-muted-foreground">{{ room.owner.email }}</div>
          </div>
        </div>
      </div>

      <!-- Members -->
      <div class="space-y-2">
        <div class="flex items-center gap-2 text-sm font-medium">
          <UsersIcon class="h-4 w-4 text-blue-500" />
          Members ({{ room.members.length }})
        </div>
        <div v-if="room.members.length === 0" class="text-sm text-muted-foreground">No members</div>
        <div v-else class="space-y-1 max-h-[160px] overflow-y-auto">
          <div
            v-for="member in room.members"
            :key="member.id"
            class="flex items-center gap-3 p-2 rounded-md border bg-muted/20"
          >
            <img v-if="member.avatarUrl" :src="member.avatarUrl" class="h-7 w-7 rounded-full object-cover" />
            <div v-else class="h-7 w-7 rounded-full bg-muted flex items-center justify-center text-xs font-bold uppercase">
              {{ member.username?.charAt(0) }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium truncate">{{ member.username }}</div>
              <div class="text-xs text-muted-foreground truncate">{{ member.email }}</div>
            </div>
            <Badge variant="outline" class="text-xs shrink-0">{{ member.role }}</Badge>
          </div>
        </div>
      </div>

      <!-- Spaces -->
      <div class="space-y-2">
        <div class="flex items-center gap-2 text-sm font-medium">
          <LayersIcon class="h-4 w-4 text-purple-500" />
          Spaces ({{ room.spaces.length }})
        </div>
        <div v-if="room.spaces.length === 0" class="text-sm text-muted-foreground">No spaces</div>
        <div v-else class="flex flex-wrap gap-2">
          <div
            v-for="space in room.spaces"
            :key="space.id"
            class="flex items-center gap-1.5 px-2 py-1 rounded-md border bg-muted/20 text-sm"
          >
            <span class="font-medium">{{ space.name }}</span>
            <Badge variant="outline" class="text-xs">{{ space.type }}</Badge>
          </div>
        </div>
      </div>

      <!-- Timestamps -->
      <div class="flex items-center gap-4 text-xs text-muted-foreground pt-2 border-t">
        <div class="flex items-center gap-1">
          <CalendarIcon class="h-3 w-3" />
          Created: {{ new Date(room.createdAt).toLocaleDateString() }}
        </div>
        <div class="flex items-center gap-1">
          <CalendarIcon class="h-3 w-3" />
          Updated: {{ new Date(room.updatedAt).toLocaleDateString() }}
        </div>
      </div>
    </template>
  </div>
</template>