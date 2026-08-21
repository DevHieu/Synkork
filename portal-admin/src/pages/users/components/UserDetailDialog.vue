<script setup lang="ts">
import { Calendar, DoorOpen, LoaderIcon, Mail, Save, Shield, TriangleAlert, WalletCards, X } from '@lucide/vue'
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { Badge } from '@/components/ui/badge'
import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input as UiInput } from '@/components/ui/input'
import { SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue, Select as UiSelect } from '@/components/ui/select'
import { useAuthStore } from '@/stores/auth'
import { formatTimestamp } from '@/utils/date.utils'

import type { User, UserPlan, UserRole, UserStatus } from '../types/userTypes'

import { userService } from '../services/userService'
import UserRoomsDialog from './UserRoomsDialog.vue'

const props = defineProps<{
  open: boolean
  user?: User | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'saved': [user: User]
}>()

const router = useRouter()
const authStore = useAuthStore()

const isOpen = computed({
  get: () => props.open,
  set: value => emit('update:open', value),
})

const userDetail = ref<User | null>(null)
const loading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')
const isRoomsOpen = ref(false)

const form = ref<{
  displayName: string
  email: string
  status: UserStatus
  plan: UserPlan
  role: UserRole
}>({
  displayName: '',
  email: '',
  status: 'ACTIVE',
  plan: 'FREE',
  role: 'user',
})

const displayUser = computed(() => userDetail.value || props.user || null)
const normalizedStatus = computed(() => form.value.status?.toUpperCase() || 'INACTIVE')
const normalizedPlan = computed(() => form.value.plan?.toUpperCase() || 'FREE')
const canChangeRole = computed(() => authStore.user?.role === 'ADMIN')

const statusOptions = [
  { value: 'ACTIVE', label: 'Hoạt động' },
  { value: 'BANNED', label: 'Bị chặn' },
  { value: 'NOT_VERIFIED', label: 'Chưa xác minh', disabled: true },
] as const

const planOptions = ['FREE', 'TEAM', 'BUSINESS'] as const

function statusLabel(status: string) {
  return {
    ACTIVE: 'Hoạt động',
    INACTIVE: 'Ngừng hoạt động',
    BANNED: 'Bị chặn',
  }[status] ?? status
}

function syncForm(user: User) {
  form.value = {
    displayName: user.displayName || '',
    email: user.email || '',
    status: (user.status?.toUpperCase() as UserStatus) || 'ACTIVE',
    plan: (user.plan?.toUpperCase() as UserPlan) || 'FREE',
    role: user.role || 'user',
  }
}

async function fetchUserDetail() {
  if (!props.user?.id)
    return

  userDetail.value = props.user
  syncForm(props.user)
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await userService.getById(props.user.id)
    const detail = response.data || response
    userDetail.value = detail
    syncForm(detail)
  }
  catch (error) {
    console.error('Failed to fetch user detail:', error)
    errorMessage.value = 'Không thể tải chi tiết user này'
  }
  finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!displayUser.value?.id)
    return

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const response = await userService.update(displayUser.value.id, {
      displayName: form.value.displayName,
      email: form.value.email,
      status: form.value.status,
      plan: form.value.plan,
      ...(canChangeRole.value ? { role: form.value.role } : {}),
    })

    const saved = response.data || response
    userDetail.value = saved
    syncForm(saved)
    toast.success('Cập nhật người dùng thành công')
    emit('saved', saved)
    isOpen.value = false
  }
  catch (err: any) {
    errorMessage.value = err?.response?.data?.message
      || err?.response?.data?.error
      || (typeof err?.response?.data === 'string' ? err.response.data : null)
      || err?.message
      || 'Cập nhật người dùng thất bại'
    toast.error(errorMessage.value)
  }
  finally {
    isSubmitting.value = false
  }
}

function goToSubscriptionDetail() {
  if (!displayUser.value?.email)
    return

  router.push({
    path: '/subscriptions',
    query: {
      tab: 'user-subscriptions',
      keyword: displayUser.value.email,
    },
  })
}

function showReportDetail(userEmail: string) {
  router.push(`/report?keyword=${userEmail}`)
}

watch(
  [() => props.open, () => props.user?.id],
  ([opened]) => {
    if (opened)
      fetchUserDetail()
  },
  { immediate: true },
)
</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogContent class="max-w-[720px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-5">
        <DialogTitle class="text-[15px] font-semibold">
          Cập nhật người dùng
        </DialogTitle>
        <DialogDescription v-if="displayUser">
          {{ displayUser.email }}
        </DialogDescription>
      </DialogHeader>

      <div v-if="loading && !displayUser" class="flex items-center gap-2 px-6 py-8 text-sm text-muted-foreground">
        <LoaderIcon class="h-4 w-4 animate-spin" />
        Đang tải...
      </div>

      <div v-else-if="errorMessage && !displayUser" class="px-6 py-8 text-sm text-destructive">
        {{ errorMessage }}
      </div>

      <form v-else-if="displayUser" @submit.prevent="handleSubmit">
        <div class="flex max-h-[70vh] flex-col gap-5 overflow-y-auto px-6 py-5">
          <div class="flex flex-wrap items-start justify-between gap-4">
            <div class="min-w-0">
              <p class="truncate text-lg font-semibold">
                {{ form.displayName || displayUser.username }}
              </p>
              <p class="mt-1 font-mono text-[11px] text-muted-foreground">
                ID: {{ displayUser.id }}
              </p>
            </div>

            <div class="flex flex-wrap items-center gap-2">
              <Badge
                variant="outline"
                class="px-2.5"
                :class="normalizedStatus === 'ACTIVE'
                  ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'
                  : normalizedStatus === 'BANNED'
                    ? 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-800 dark:bg-rose-950 dark:text-rose-300'
                    : 'border-slate-200 bg-slate-50 text-slate-700 dark:border-slate-700 dark:bg-slate-950 dark:text-slate-300'"
              >
                {{ statusLabel(normalizedStatus) }}
              </Badge>
              <Badge variant="outline" class="px-2.5">
                {{ normalizedPlan }}
              </Badge>
            </div>
          </div>

          <div class="grid gap-3 md:grid-cols-2">
            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5 md:col-span-2">
              <label class="text-[12px] font-medium text-muted-foreground">Tên hiển thị</label>
              <UiInput v-model="form.displayName" required />
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <Mail class="h-3.5 w-3.5" />
                Email
              </label>
              <UiInput v-model="form.email" type="email" disabled />
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="text-[12px] font-medium text-muted-foreground">Username</label>
              <UiInput :model-value="displayUser.username" disabled />
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="text-[12px] font-medium text-muted-foreground">Trạng thái</label>
              <UiSelect v-model="form.status">
                <SelectTrigger class="w-full bg-background">
                  <SelectValue placeholder="Chọn trạng thái" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem v-for="status in statusOptions" :key="status.value" :value="status.value" :disabled="'disabled' in status && status.disabled">
                      {{ status.label }}
                    </SelectItem>
                  </SelectGroup>
                </SelectContent>
              </UiSelect>
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="text-[12px] font-medium text-muted-foreground">Gói dịch vụ</label>
              <UiSelect v-model="form.plan">
                <SelectTrigger class="w-full bg-background">
                  <SelectValue placeholder="Chọn gói dịch vụ" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem v-for="plan in planOptions" :key="plan" :value="plan">
                      {{ plan }}
                    </SelectItem>
                  </SelectGroup>
                </SelectContent>
              </UiSelect>
            </div>

            <div v-if="canChangeRole" class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <Shield class="h-3.5 w-3.5" />
                Vai trò
              </label>
              <UiSelect v-model="form.role">
                <SelectTrigger class="w-full bg-background">
                  <SelectValue placeholder="Chọn vai trò" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value="USER">
                      User
                    </SelectItem>
                    <SelectItem value="MANAGER">
                      Manager
                    </SelectItem>
                    <SelectItem value="ADMIN">
                      Admin
                    </SelectItem>
                  </SelectGroup>
                </SelectContent>
              </UiSelect>
            </div>

            <div class="rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <div class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <TriangleAlert class="h-3.5 w-3.5" />
                Cảnh báo
              </div>
              <div class="h-8 text-[13px] font-medium flex items-center justify-between">
                {{ displayUser.warning ?? 0 }}
                <UiButton v-if="displayUser.warning && displayUser.warning > 0" size="sm" variant="outline" @click="showReportDetail(displayUser.email)">
                  Chi tiết
                </UiButton>
              </div>
            </div>

            <div class="rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <div class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <Calendar class="h-3.5 w-3.5" />
                Ngày tạo
              </div>
              <p class="mt-1 text-[13px] font-medium">
                {{ formatTimestamp(displayUser.createdAt) }}
              </p>
            </div>
          </div>

          <div class="rounded-lg border border-border bg-muted/30 px-4 py-3">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div>
                <div class="flex items-center gap-2 text-[13px] font-semibold">
                  <WalletCards class="h-4 w-4 text-primary" />
                  Gói dịch vụ
                </div>
                <p class="mt-1 text-[12px] text-muted-foreground">
                  Tìm subscription hiện tại và lịch sử mua gói của user này
                </p>
              </div>
              <UiButton type="button" size="sm" class="h-8 gap-1.5 px-2.5 text-xs" @click="goToSubscriptionDetail">
                <WalletCards class="h-3.5 w-3.5" />
                Xem chi tiết
              </UiButton>
            </div>
          </div>

          <div class="rounded-lg border border-border bg-muted/30 px-4 py-3">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div>
                <div class="flex items-center gap-2 text-[13px] font-semibold">
                  <DoorOpen class="h-4 w-4 text-primary" />
                  Phòng đã tham gia
                </div>
                <p class="mt-1 text-[12px] text-muted-foreground">
                  Xem danh sách phòng và nhảy sang trang quản lí room
                </p>
              </div>
              <UiButton type="button" size="sm" variant="outline" class="h-8 gap-1.5 px-2.5 text-xs" @click="isRoomsOpen = true">
                <DoorOpen class="h-3.5 w-3.5" />
                Xem phòng
              </UiButton>
            </div>
          </div>

          <p v-if="errorMessage" class="text-[12px] text-destructive">
            {{ errorMessage }}
          </p>
        </div>

        <div class="flex justify-end gap-2 border-t border-border px-6 py-3">
          <UiButton type="button" variant="outline" size="sm" :disabled="isSubmitting" @click="isOpen = false">
            <X class="h-3.5 w-3.5" />
            Hủy
          </UiButton>
          <UiButton type="submit" size="sm" class="gap-1.5" :disabled="isSubmitting || loading">
            <LoaderIcon v-if="isSubmitting" class="h-3.5 w-3.5 animate-spin" />
            <Save v-else class="h-3.5 w-3.5" />
            {{ isSubmitting ? 'Đang lưu...' : 'Lưu thay đổi' }}
          </UiButton>
        </div>
      </form>
    </DialogContent>
  </Dialog>

  <UserRoomsDialog
    v-if="displayUser"
    v-model:open="isRoomsOpen"
    :user-id="displayUser.id"
    :username="displayUser.username"
  />
</template>
