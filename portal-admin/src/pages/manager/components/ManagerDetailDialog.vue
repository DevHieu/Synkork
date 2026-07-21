<script setup lang="ts">
import { Calendar, LoaderIcon, Mail, Save, Shield, UserRound, WalletCards, X } from '@lucide/vue'
import { computed, reactive, ref, watch } from 'vue'
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
import {
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Select as UiSelect,
} from '@/components/ui/select'
import { formatTimestamp } from '@/utils/date.utils'

import type { ManagementRole, ManagerAccount, ManagerPlan, ManagerStatus, UpdateManagerPayload } from '../types/managerTypes'

import { managerService } from '../services/managerService'

const props = defineProps<{
  open: boolean
  account?: ManagerAccount | null
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
  'saved': [account: ManagerAccount]
}>()

const router = useRouter()

const isOpen = computed({
  get: () => props.open,
  set: value => emit('update:open', value),
})

const accountDetail = ref<ManagerAccount | null>(null)
const loading = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  displayName: '',
  username: '',
  email: '',
  status: 'active' as ManagerStatus,
  role: 'manager' as ManagementRole,
  plan: 'FREE' as ManagerPlan,
})

const displayAccount = computed(() => accountDetail.value || props.account || null)

const statusOptions = [
  { value: 'active', label: 'Hoạt động' },
  { value: 'inactive', label: 'Ngừng hoạt động' },
  { value: 'banned', label: 'Bị khóa' },
] as const

const roleOptions = [
  { value: 'manager', label: 'Quản lý' },
  { value: 'admin', label: 'Quản trị viên' },
] as const

const planOptions = ['FREE', 'TEAM', 'BUSINESS'] as const

function syncForm(account: ManagerAccount) {
  form.displayName = account.displayName || ''
  form.username = account.username || ''
  form.email = account.email || ''
  form.status = account.status || 'active'
  form.role = account.role || 'manager'
  form.plan = account.plan || 'FREE'
}

function statusLabel(status: ManagerStatus) {
  return statusOptions.find(option => option.value === status)?.label ?? status
}

function roleLabel(role: ManagementRole) {
  return roleOptions.find(option => option.value === role)?.label ?? role
}

function goToSubscriptionDetail() {
  if (!displayAccount.value?.email)
    return

  router.push({
    path: '/subscriptions',
    query: {
      tab: 'user-subscriptions',
      keyword: displayAccount.value.email,
    },
  })
}

function getErrorMessage(error: any) {
  const data = error?.response?.data
  if (typeof data === 'string')
    return data
  return data?.message || error?.message || 'Cập nhật tài khoản quản trị thất bại'
}

async function fetchAccountDetail() {
  if (!props.account?.id)
    return

  accountDetail.value = props.account
  syncForm(props.account)
  loading.value = true
  errorMessage.value = ''

  try {
    const account = await managerService.getById(props.account.id)
    accountDetail.value = account
    syncForm(account)
  }
  catch (error) {
    console.error('Failed to fetch manager detail:', error)
    errorMessage.value = 'Không thể tải chi tiết tài khoản này'
  }
  finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!displayAccount.value?.id)
    return

  if (!form.displayName.trim() || !form.email.trim()) {
    toast.error('Vui lòng nhập đầy đủ thông tin')
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const payload: UpdateManagerPayload = {
      displayName: form.displayName.trim(),
      email: form.email.trim(),
      status: form.status,
      role: form.role,
      plan: form.plan,
    }
    const saved = await managerService.update(displayAccount.value.id, payload)

    accountDetail.value = saved
    syncForm(saved)
    toast.success('Cập nhật tài khoản quản trị thành công')
    emit('saved', saved)
    isOpen.value = false
  }
  catch (error) {
    errorMessage.value = getErrorMessage(error)
    toast.error(errorMessage.value)
  }
  finally {
    isSubmitting.value = false
  }
}

watch(
  [() => props.open, () => props.account?.id],
  ([opened]) => {
    if (opened)
      fetchAccountDetail()
  },
  { immediate: true },
)
</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogContent class="max-w-[680px] gap-0 overflow-hidden p-0">
      <DialogHeader class="border-b border-border px-6 py-5">
        <DialogTitle class="text-[15px] font-semibold">
          Cập nhật tài khoản quản trị
        </DialogTitle>
        <DialogDescription v-if="displayAccount">
          {{ displayAccount.email }}
        </DialogDescription>
      </DialogHeader>

      <div v-if="loading && !displayAccount" class="flex items-center gap-2 px-6 py-8 text-sm text-muted-foreground">
        <LoaderIcon class="h-4 w-4 animate-spin" />
        Đang tải...
      </div>

      <div v-else-if="errorMessage && !displayAccount" class="px-6 py-8 text-sm text-destructive">
        {{ errorMessage }}
      </div>

      <form v-else-if="displayAccount" @submit.prevent="handleSubmit">
        <div class="flex max-h-[70vh] flex-col gap-5 overflow-y-auto px-6 py-5">
          <div class="flex flex-wrap items-start justify-between gap-4">
            <div class="min-w-0">
              <p class="truncate text-lg font-semibold">
                {{ form.displayName || displayAccount.username }}
              </p>
              <p class="mt-1 font-mono text-[11px] text-muted-foreground">
                ID: {{ displayAccount.id }}
              </p>
            </div>

            <div class="flex flex-wrap items-center gap-2">
              <Badge
                variant="outline"
                class="px-2.5"
                :class="form.status === 'active'
                  ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-300'
                  : form.status === 'banned'
                    ? 'border-rose-200 bg-rose-50 text-rose-700 dark:border-rose-800 dark:bg-rose-950 dark:text-rose-300'
                    : 'border-slate-200 bg-slate-50 text-slate-700 dark:border-slate-700 dark:bg-slate-950 dark:text-slate-300'"
              >
                {{ statusLabel(form.status) }}
              </Badge>
              <Badge variant="outline" class="px-2.5">
                {{ roleLabel(form.role) }}
              </Badge>
              <Badge variant="outline" class="px-2.5">
                {{ form.plan }}
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
                <UserRound class="h-3.5 w-3.5" />
                Tên đăng nhập
              </label>
              <UiInput v-model="form.username" disabled />
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <Mail class="h-3.5 w-3.5" />
                Email
              </label>
              <UiInput v-model="form.email" type="email" required />
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <Shield class="h-3.5 w-3.5" />
                Vai trò
              </label>
              <UiSelect v-model="form.role">
                <SelectTrigger class="w-full bg-background">
                  <SelectValue placeholder="Chọn vai trò" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="role in roleOptions" :key="role.value" :value="role.value">
                    {{ role.label }}
                  </SelectItem>
                </SelectContent>
              </UiSelect>
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="text-[12px] font-medium text-muted-foreground">Trạng thái</label>
              <UiSelect v-model="form.status">
                <SelectTrigger class="w-full bg-background">
                  <SelectValue placeholder="Chọn trạng thái" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="status in statusOptions" :key="status.value" :value="status.value">
                    {{ status.label }}
                  </SelectItem>
                </SelectContent>
              </UiSelect>
            </div>

            <div class="space-y-2 rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <label class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <WalletCards class="h-3.5 w-3.5" />
                Gói dịch vụ
              </label>
              <UiSelect v-model="form.plan">
                <SelectTrigger class="w-full bg-background">
                  <SelectValue placeholder="Chọn gói dịch vụ" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="plan in planOptions" :key="plan" :value="plan">
                    {{ plan }}
                  </SelectItem>
                </SelectContent>
              </UiSelect>
            </div>

            <div class="rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <div class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <Calendar class="h-3.5 w-3.5" />
                Ngày tạo
              </div>
              <p class="mt-1 text-[13px] font-medium">
                {{ formatTimestamp(displayAccount.createdAt) }}
              </p>
            </div>

            <div class="rounded-lg border border-border bg-muted/30 px-3 py-2.5">
              <div class="flex items-center gap-2 text-[12px] font-medium text-muted-foreground">
                <Calendar class="h-3.5 w-3.5" />
                Cập nhật
              </div>
              <p class="mt-1 text-[13px] font-medium">
                {{ formatTimestamp(displayAccount.updatedAt) }}
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
                  Tìm subscription hiện tại và lịch sử mua gói của tài khoản này
                </p>
              </div>
              <UiButton type="button" size="sm" class="h-8 gap-1.5 px-2.5 text-xs" @click="goToSubscriptionDetail">
                <WalletCards class="h-3.5 w-3.5" />
                Xem chi tiết
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
</template>
