<script setup lang="ts">
import { LoaderIcon, Pencil, Plus, Search, Trash2 } from '@lucide/vue'
import { refDebounced } from '@vueuse/core'
import { computed, h, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'

import type { TableColumn } from '@/components/base-table.vue'

import { BasicPage } from '@/components/global-layout'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import {
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { formatTimestamp } from '@/utils/date.utils'

import type {
  ManagerAccount,
  ManagerParams,
  ManagerStatus,
} from './types/managerTypes'

import ManagerDelete from './components/manager-delete.vue'
import ManagerResource from './components/manager-resource.vue'
import { managerService } from './services/managerService'

const loading = ref(false)
const accounts = ref<ManagerAccount[]>([])
const keyword = ref('')
const selectedStatus = ref('ALL')
const currentPage = ref(1)
const pageSize = 20
const totalCount = ref(0)

const debouncedKeyword = refDebounced(keyword, 400)
const totalPage = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize)))

const editTarget = ref<ManagerAccount>()
const deleteTarget = ref<ManagerAccount>()
const showResourceModal = ref(false)
const showDeleteModal = ref(false)

const statusOptions = [
  { value: 'ALL', label: 'Tất cả trạng thái' },
  { value: 'active', label: 'Hoạt động' },
  { value: 'inactive', label: 'Ngừng hoạt động' },
  { value: 'banned', label: 'Bị khóa' },
] as const

async function fetchData() {
  loading.value = true
  try {
    const params: ManagerParams = {
      page: currentPage.value - 1,
      size: pageSize,
    }

    if (debouncedKeyword.value.trim())
      params.keyword = debouncedKeyword.value.trim()
    if (selectedStatus.value !== 'ALL')
      params.status = selectedStatus.value as ManagerStatus

    const response = await managerService.getAll(params)
    accounts.value = response.content ?? []
    totalCount.value = response.totalElements ?? 0
  }
  catch (error: any) {
    console.error('Failed to fetch manager accounts:', error)
    const data = error?.response?.data
    const message = typeof data === 'string'
      ? data
      : data?.message || 'Không thể tải danh sách tài khoản quản trị'
    toast.error(message)
    accounts.value = []
    totalCount.value = 0
  }
  finally {
    loading.value = false
  }
}

function openCreateModal() {
  editTarget.value = undefined
  showResourceModal.value = true
}

function openEditModal(account: ManagerAccount) {
  editTarget.value = account
  showResourceModal.value = true
}

function openDeleteModal(account: ManagerAccount) {
  deleteTarget.value = account
  showDeleteModal.value = true
}

function refreshAfterChange() {
  showResourceModal.value = false
  showDeleteModal.value = false
  fetchData()
}

function renderStatus(status: ManagerStatus) {
  const config = {
    active: { label: 'Hoạt động', class: 'border-emerald-300 bg-emerald-50 px-3 text-emerald-700 dark:border-emerald-700 dark:bg-emerald-950 dark:text-emerald-300' },
    inactive: { label: 'Ngừng hoạt động', class: 'border-amber-300 bg-amber-50 px-3 text-amber-700 dark:border-amber-700 dark:bg-amber-950 dark:text-amber-300' },
    banned: { label: 'Bị khóa', class: 'border-red-300 bg-red-50 px-3 text-red-700 dark:border-red-700 dark:bg-red-950 dark:text-red-300' },
  }[status]

  return h(Badge, {
    variant: 'outline',
    class: config.class,
  }, () => config.label)
}

const columns = computed<TableColumn<ManagerAccount>[]>(() => [
  { header: 'Username', accessor: 'username', minWidth: 150 },
  { header: 'Tên hiển thị', accessor: 'displayName', minWidth: 180 },
  { header: 'Email', accessor: 'email', minWidth: 220 },
  {
    header: 'Vai trò',
    minWidth: 110,
    render: () => h(Badge, {
      variant: 'secondary',
      class: 'border-0 bg-neutral-100 px-3 text-neutral-950 dark:bg-neutral-800 dark:text-neutral-50',
    }, () => 'Manager'),
  },
  {
    header: 'Trạng thái',
    minWidth: 150,
    render: row => renderStatus(row.status),
  },
  {
    header: 'Ngày tạo',
    minWidth: 160,
    render: row => formatTimestamp(row.createdAt),
  },
  {
    header: 'Thao tác',
    minWidth: 180,
    render: row => h('div', { class: 'flex justify-center gap-1' }, [
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs',
        onClick: () => openEditModal(row),
      }, () => [h(Pencil, { class: 'h-3.5 w-3.5' }), 'Sửa']),
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10',
        onClick: () => openDeleteModal(row),
      }, () => [h(Trash2, { class: 'h-3.5 w-3.5' }), 'Xóa']),
    ]),
  },
])

watch(currentPage, fetchData)
watch([debouncedKeyword, selectedStatus], () => {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    return
  }
  fetchData()
})

onMounted(fetchData)
</script>

<template>
  <BasicPage
    title="Quản lý Manager"
    description="Quản lý các tài khoản Manager trong hệ thống"
    sticky
  >
    <template #actions>
      <UiButton @click="openCreateModal">
        <Plus />
        Tạo tài khoản
      </UiButton>
    </template>

    <div class="mb-4 flex flex-wrap items-center gap-3">
      <div class="relative w-full max-w-sm">
        <Search class="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
        <UiInput
          v-model="keyword"
          type="text"
          placeholder="Tìm theo username, email, tên..."
          class="h-9 pl-8"
        />
      </div>

      <div class="w-[190px]">
        <UiSelect v-model="selectedStatus">
          <SelectTrigger class="h-9 w-full">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </SelectItem>
          </SelectContent>
        </UiSelect>
      </div>
    </div>

    <div class="relative rounded-md border border-neutral-200 p-3 dark:border-neutral-800">
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center rounded-md bg-white/60 dark:bg-black/60"
      >
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <BaseTable :columns="columns" :data="accounts" />

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <Modal v-model:open="showResourceModal">
    <ModalContent class="sm:max-w-lg">
      <ManagerResource
        :account="editTarget"
        @close="showResourceModal = false"
        @saved="refreshAfterChange"
      />
    </ModalContent>
  </Modal>

  <Modal v-model:open="showDeleteModal">
    <ModalContent>
      <ManagerDelete
        v-if="deleteTarget"
        :account="deleteTarget"
        @close="showDeleteModal = false"
        @removed="refreshAfterChange"
      />
    </ModalContent>
  </Modal>
</template>
