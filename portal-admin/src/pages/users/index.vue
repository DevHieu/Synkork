<script setup lang="ts">
import { Eye, LoaderIcon, Search, Trash2 } from '@lucide/vue'
import { computed, h, onMounted, ref, watch } from 'vue'

import type { TableColumn } from '@/components/base-table.vue'

import DateRangePicker from '@/components/date-range-picker.vue'
import { BasicPage } from '@/components/global-layout'
import { Modal, ModalContent } from '@/components/prop-ui/modal'
import { Button as UiButton } from '@/components/ui/button'

import type { User } from './data/schema'

import UserCreate from './components/user-create.vue'
import UserDelete from './components/user-delete.vue'
import UserResource from './components/user-resource.vue'
import { adminUserService } from './data/userAdminService'

// ── State ─────────────────────────────────────────────────────────────────────

const loading = ref(false)
const keyword = ref('')
const dateRange = ref<{ from: Date, to: Date } | undefined>(undefined)
const currentPage = ref(1)
const pageSize = 20
const allUsers = ref<User[]>([])
const totalCount = ref(0)

let searchTimer: ReturnType<typeof setTimeout> | null = null
function onSearch() {
  if (searchTimer)
    clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchData()
  }, 300)
}

const totalPage = computed(() => Math.ceil(totalCount.value / pageSize))

const editTarget = ref<User | null>(null)
const deleteTarget = ref<User | null>(null)
const showEditModal = ref(false)
const showDeleteModal = ref(false)

// ── Data fetching ─────────────────────────────────────────────────────────────

async function fetchData() {
  loading.value = true
  try {
    const { users, totalElements } = await adminUserService.getAll({
      keyword: keyword.value || undefined,
      page: currentPage.value - 1,
      size: pageSize,
    })
    allUsers.value = users
    totalCount.value = totalElements
  }
  catch (err) {
    console.error('Failed to fetch users:', err)
    allUsers.value = []
    totalCount.value = 0
  }
  finally {
    loading.value = false
  }
}

watch(currentPage, () => fetchData())

watch(dateRange, (newDate) => {
  console.log('Selected date range:', newDate)
})

onMounted(() => fetchData())

// ── Handlers ──────────────────────────────────────────────────────────────────

function handleViewDetail(user: User) {
  editTarget.value = user
  showEditModal.value = true
}

function handleDelete(user: User) {
  deleteTarget.value = user
  showDeleteModal.value = true
}

function onUserSaved() {
  showEditModal.value = false
  fetchData()
}

function onUserDeleted() {
  showDeleteModal.value = false
  fetchData()
}

// ── Table columns ─────────────────────────────────────────────────────────────

const columns = computed<TableColumn<User>[]>(() => [
  { header: 'ID', accessor: 'id', minWidth: 100 },
  { header: 'Username', accessor: 'username', minWidth: 150 },
  {
    header: 'Full Name',
    render: row => `${row.firstName} ${row.lastName}`,
    minWidth: 180,
  },
  { header: 'Role', accessor: 'role', minWidth: 120 },
  { header: 'Status', accessor: 'status', minWidth: 120 },
  { header: 'Email', accessor: 'email', minWidth: 220 },
  {
    header: 'Actions',
    minWidth: 160,
    render: row => h('div', { class: 'flex gap-1' }, [
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs',
        onClick: () => handleViewDetail(row),
      }, () => [h(Eye, { class: 'h-3.5 w-3.5' }), 'View Detail']),
      h(UiButton, {
        variant: 'outline',
        size: 'sm',
        class: 'h-8 gap-1 px-2 text-xs text-destructive hover:bg-destructive/10',
        onClick: () => handleDelete(row),
      }, () => [h(Trash2, { class: 'h-3.5 w-3.5' }), 'Delete']),
    ]),
  },
])
</script>

<template>
  <BasicPage
    title="Users"
    description="Users description"
    sticky
  >
    <template #actions>
      <div class="flex items-center gap-2">
        <div class="relative flex items-center gap-2 rounded-md border border-input bg-background px-3 py-1.5 text-sm shadow-sm w-72">
          <Search class="h-4 w-4 shrink-0 text-muted-foreground" />
          <input
            v-model="keyword"
            placeholder="Tìm theo username hoặc email..."
            class="flex-1 bg-transparent outline-none placeholder:text-muted-foreground text-sm"
            @input="onSearch"
          >
        </div>
        <div class="w-36 shrink-0">
          <DateRangePicker v-model="dateRange" />
        </div>
        <UserCreate @saved="onUserSaved" />
      </div>
    </template>

    <div class="relative">
      <div
        v-if="loading"
        class="absolute inset-0 z-20 flex items-center justify-center bg-white/50 dark:bg-black/50"
      >
        <LoaderIcon class="animate-spin text-primary" />
      </div>

      <BaseTable
        :columns="columns"
        :data="allUsers"
      />

      <Pagination
        v-model:current-page="currentPage"
        :total="totalPage"
        :total-count="totalCount"
        :per-page="pageSize"
      />
    </div>
  </BasicPage>

  <!-- Edit Modal -->
  <Modal v-model:open="showEditModal">
    <ModalContent>
      <UserResource
        :user="editTarget ?? undefined"
        @close="showEditModal = false"
        @saved="onUserSaved"
      />
    </ModalContent>
  </Modal>

  <!-- Delete Modal -->
  <Modal v-model:open="showDeleteModal">
    <ModalContent>
      <UserDelete
        v-if="deleteTarget"
        :user="deleteTarget"
        @remove="onUserDeleted"
      />
    </ModalContent>
  </Modal>
</template>
cl
