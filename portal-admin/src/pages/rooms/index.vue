<script lang="ts" setup>
import { Eye, LoaderIcon, RefreshCwIcon, Search } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'

import BaseTable from '@/components/base-table.vue'
import { BasicPage } from '@/components/global-layout'
import Pagination from '@/components/pagination.vue'
import { Button as UiButton } from '@/components/ui/button'
import { Input as UiInput } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

import type {
  Room,
  RoomParams,
} from './types/RoomTypes'

import RoomDetailDialog from './components/RoomDetailDialog.vue'
import { roomService } from './service/roomService'

const selectedRoom = ref<Room | null>(null)
const isDetailOpen = ref(false)

const loading = ref(false)
const roomsData = ref<Room[]>([])
const totalCount = ref(0)

const currentPage = ref(1)
const pageSize = 20

const searchKeyword = ref('')
const selectedStatus = ref<string>('ALL')
const selectedType = ref<string>('ALL')

const debounceSearchKeyword = refDebounced(
  searchKeyword,
  500,
)

const totalPage = computed(() =>
  Math.ceil(totalCount.value / pageSize),
)

function handleViewDetail(room: Room) {
  selectedRoom.value = room
  isDetailOpen.value = true
}

const columns = computed<TableColumn<any>[]>(
  () => [
    {
      header: 'Name',
      accessor: 'name',
      minWidth: 200,
    },
    {
      header: 'Invite Code',
      accessor: 'inviteCode',
      minWidth: 140,
      render: row => row.inviteCode || '—',
    },
    {
      header: 'Type',
      accessor: 'type',
      minWidth: 120,
    },
    {
      header: 'Status',
      accessor: 'status',
      minWidth: 120,
    },
    {
      header: 'Members',
      accessor: 'memberCount',
      minWidth: 100,
    },
    {
      header: 'Action',
      minWidth: 120,
      render: row =>
        h(
          UiButton,
          {
            variant: 'outline',
            size: 'sm',
            class: 'h-8 gap-1 px-2 text-xs',
            onClick: () =>
              handleViewDetail(row),
          },
          () => [
            h(Eye, {
              class: 'h-3.5 w-3.5',
            }),
            'Chi tiết',
          ],
        ),
    },
  ],
)

async function fetchRooms() {
  loading.value = true

  try {
    const queryParams: RoomParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (
      searchKeyword.value.trim()
    ) {
      queryParams.search =
        searchKeyword.value.trim()
    }

    if (
      selectedStatus.value !== 'ALL'
    ) {
      queryParams.status =
        selectedStatus.value
    }

    if (
      selectedType.value !== 'ALL'
    ) {
      queryParams.type =
        selectedType.value
    }

    const response =
      await roomService.getRooms({
        params: queryParams,
      })

    roomsData.value =
      response.content || []

    totalCount.value =
      response.totalElements || 0
  }
  catch (error) {
    console.error(
      'Lỗi khi tải rooms:',
      error,
    )
  }
  finally {
    loading.value = false
  }
}

watch(
  [
    debounceSearchKeyword,
    selectedStatus,
    selectedType,
  ],
  () => {
    currentPage.value = 1
    fetchRooms()
  },
)

watch(currentPage, () => {
  fetchRooms()
})

onMounted(() => {
  fetchRooms()
})
</script>

<template>
  <BasicPage
    title="Rooms"
    description="Quản lý tất cả room trong hệ thống"
    sticky
  >
    <template #actions>
      <UiButton
        variant="outline"
        @click="fetchRooms"
      >
        <RefreshCwIcon
          class="mr-2 h-4 w-4"
        />
        Refresh
      </UiButton>
    </template>

    <!-- filters -->
    <div
      class="mb-4 flex flex-wrap items-center gap-3"
    >
      <div
        class="relative w-full max-w-sm"
      >
        <Search
          class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground"
        />

        <UiInput
          v-model="searchKeyword"
          type="text"
          placeholder="Tìm room..."
          class="h-9 pl-8"
        />
      </div>

      <div class="w-[180px]">
        <Select
          v-model="selectedStatus"
        >
          <SelectTrigger
            class="h-9 w-full"
          >
            <SelectValue
              placeholder="Status"
            />
          </SelectTrigger>

          <SelectContent>
            <SelectItem value="ALL">
              All Status
            </SelectItem>

            <SelectItem value="OPEN">
              OPEN
            </SelectItem>

            <SelectItem value="CLOSED">
              CLOSED
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div class="w-[180px]">
        <Select
          v-model="selectedType"
        >
          <SelectTrigger
            class="h-9 w-full"
          >
            <SelectValue
              placeholder="Type"
            />
          </SelectTrigger>

          <SelectContent>
            <SelectItem value="ALL">
              All Types
            </SelectItem>

            <SelectItem value="GROUP">
              GROUP
            </SelectItem>

            <SelectItem value="DM">
              DM
            </SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>

    <!-- table -->
    <div
      class="relative rounded-md border border-neutral-200 dark:border-neutral-800"
    >
      <!-- loading -->
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50"
      >
        <LoaderIcon
          class="animate-spin text-primary"
        />
      </div>

      <div
        class="overflow-x-auto"
      >
        <BaseTable
          :columns="columns"
          :data="roomsData"
        />
      </div>

      <Pagination
        v-model:current-page="
          currentPage
        "
        :total="totalPage"
        :total-count="
          totalCount
        "
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <RoomDetailDialog
    v-if="selectedRoom"
    v-model:open="
      isDetailOpen
    "
    :room-id="
      selectedRoom.id
    "
  />
</template>