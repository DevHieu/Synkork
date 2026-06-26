<script lang="ts" setup>
import { computed, ref } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

import type { User, UserPlan, UserRole, UserStatus } from '../types/userTypes'

import { userService } from '../services/userService'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ user?: User }>()
const emits = defineEmits<{
  (e: 'close'): void
  (e: 'saved', user: User): void
}>()

const isEdit = computed(() => !!props.user?.id)
const authStore = useAuthStore()
const canChangeRole = computed(() => authStore.user?.role === 'ADMIN')

const statusOptions = [
  { value: 'ACTIVE', label: 'Hoạt động' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
  { value: 'BANNED', label: 'Bị khóa' },
] as const
const planOptions = ['FREE', 'TEAM', 'BUSINESS'] as const

const form = ref<{
  firstName: string
  lastName: string
  displayName: string
  username: string
  email: string
  status: UserStatus
  plan: UserPlan
  role: UserRole
}>({
  firstName: '',
  lastName: '',
  displayName: props.user?.displayName || '',
  username: props.user?.username || '',
  email: props.user?.email || '',
  status: (props.user?.status as UserStatus) || 'ACTIVE',
  plan: (props.user?.plan as UserPlan) || 'FREE',
  role: props.user?.role || 'user',
})

const isLoading = ref(false)
const errorMessage = ref<string | null>(null)

async function onSubmit() {
  isLoading.value = true
  errorMessage.value = null
  try {
    let result: User
    if (isEdit.value && props.user?.id) {
      result = await userService.update(props.user.id, {
        displayName: form.value.displayName,
        email: form.value.email,
        status: form.value.status,
        plan: form.value.plan,
        ...(canChangeRole.value ? { role: form.value.role } : {}),
      })
      toast.success('Cập nhật người dùng thành công')
    }
    else {
      result = await userService.create({
        firstName: form.value.firstName,
        lastName: form.value.lastName,
        username: form.value.username,
        email: form.value.email,
        status: form.value.status,
        plan: form.value.plan,
        role: 'user',
      })
      toast.success('Tạo người dùng thành công')
    }
    emits('saved', result)
    emits('close')
  }
  catch (err: any) {
    errorMessage.value = err?.response?.data?.message
      || err?.response?.data?.error
      || (typeof err?.response?.data === 'string' ? err.response.data : null)
      || err?.message
      || 'Có lỗi xảy ra'
    toast.error(errorMessage.value ?? 'Có lỗi xảy ra')
  }
  finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="max-h-[500px] overflow-y-auto">
    <form class="space-y-4" @submit.prevent="onSubmit">
      <!-- Họ tên (chỉ khi tạo mới) -->
      <template v-if="!isEdit">
        <div class="space-y-2">
          <label class="text-sm font-medium">Tên</label>
          <Input v-model="form.firstName" type="text" placeholder="Nhập tên" required />
        </div>
        <div class="space-y-2">
          <label class="text-sm font-medium">Họ</label>
          <Input v-model="form.lastName" type="text" placeholder="Nhập họ" required />
        </div>
      </template>

      <!-- Tên hiển thị (chỉ khi cập nhật) -->
      <div v-else class="space-y-2">
        <label class="text-sm font-medium">Tên hiển thị</label>
        <Input v-model="form.displayName" type="text" placeholder="Nhập tên hiển thị" required />
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium">Username</label>
        <Input v-model="form.username" type="text" required />
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium">Email</label>
        <Input v-model="form.email" type="email" :disabled="isEdit" required />
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium">Trạng thái</label>
        <Select v-model="form.status">
          <SelectTrigger class="w-full">
            <SelectValue placeholder="Chọn trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectItem v-for="s in statusOptions" :key="s.value" :value="s.value">
                {{ s.label }}
              </SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div class="space-y-2">
        <label class="text-sm font-medium">Gói dịch vụ</label>
        <Select v-model="form.plan">
          <SelectTrigger class="w-full">
            <SelectValue placeholder="Chọn gói dịch vụ" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectItem v-for="p in planOptions" :key="p" :value="p">
                {{ p }}
              </SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <div v-if="isEdit && canChangeRole" class="space-y-2">
        <label class="text-sm font-medium">Vai trò</label>
        <Select v-model="form.role">
          <SelectTrigger class="w-full">
            <SelectValue placeholder="Chọn vai trò" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectItem value="user">User</SelectItem>
              <SelectItem value="manager">Manager</SelectItem>
              <SelectItem value="admin">Admin</SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
        <p class="text-xs text-muted-foreground">
          Khi nâng lên Manager hoặc Admin, tài khoản sẽ chuyển sang trang Manager & Admin.
        </p>
      </div>

      <Button type="submit" class="w-full" :disabled="isLoading">
        {{ isLoading ? 'Đang lưu...' : (isEdit ? 'Lưu thay đổi' : 'Tạo user') }}
      </Button>
    </form>
  </div>
</template>
