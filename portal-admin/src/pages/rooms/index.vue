<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RefreshCwIcon } from '@lucide/vue'
import { BasicPage } from '@/components/global-layout'
import RoomTable from './components/room-data-table.vue'
import axiosClient from '@/lib/axiosClient.ts'

const loading = ref(false)
const rooms = ref<any[]>([])

async function fetchRooms() {
  try {
    loading.value = true
    const response = await axiosClient.get('/manage/rooms', {
      params: { page: 0, size: 50 },
    })
    rooms.value = response.data.content || []
  } catch (error) {
    console.error('Load rooms failed:', error)
    rooms.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchRooms())
</script>

<template>
  <BasicPage
    title="Rooms"
    description="Manage all group rooms in the system"
    sticky
  >
    <template #actions>
      <UiButton variant="outline" @click="fetchRooms">
        <RefreshCwIcon class="mr-2 h-4 w-4" />
        Refresh
      </UiButton>
    </template>

    <RoomTable
      :data="rooms"
      :loading="loading"
      @refresh="fetchRooms"
    />
  </BasicPage>
</template>