<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

import type {
  CreateManagerPayload,
  ManagementRole,
  ManagerAccount,
  ManagerStatus,
  UpdateManagerPayload,
} from '../types/managerTypes'

import { managerService } from '../services/managerService'

const props = defineProps<{
  account?: ManagerAccount
}>()

const emit = defineEmits<{
  close: []
  saved: [account: ManagerAccount]
}>()

const isEditing = computed(() => Boolean(props.account?.id))
const isLoading = ref(false)
const form = reactive({
  displayName: props.account?.displayName ?? '',
  username: props.account?.username ?? '',
  email: props.account?.email ?? '',
  status: (props.account?.status ?? 'active') as ManagerStatus,
  role: (props.account?.role ?? 'manager') as ManagementRole,
})

function getErrorMessage(error: any) {
  const data = error?.response?.data
  if (typeof data === 'string')
    return data
  return data?.message || error?.message || 'Có lỗi xảy ra'
}

async function onSubmit() {
  if (!form.displayName.trim() || !form.username.trim() || !form.email.trim()) {
    toast.error('Vui lòng nhập đầy đủ thông tin')
    return
  }

  isLoading.value = true
  try {
    let result: ManagerAccount
    if (props.account) {
      const payload: UpdateManagerPayload = {
        displayName: form.displayName.trim(),
        email: form.email.trim(),
        status: form.status,
        role: form.role,
      }
      result = await managerService.update(props.account.id, payload)
      toast.success('Cập nhật tài khoản quản trị thành công')
    }
    else {
      const payload: CreateManagerPayload = {
        displayName: form.displayName.trim(),
        username: form.username.trim(),
        email: form.email.trim(),
        status: form.status,
        role: form.role,
      }
      result = await managerService.create(payload)
      toast.success('Tạo tài khoản quản trị thành công')
    }
    emit('saved', result)
    emit('close')
  }
  catch (error) {
    toast.error(getErrorMessage(error))
  }
  finally {
    isLoading.value = false
  }
}
</script>

<template>
  <form class="space-y-4" @submit.prevent="onSubmit">
    <div class="space-y-2">
      <label class="text-sm font-medium">Tên hiển thị</label>
      <Input v-model="form.displayName" placeholder="Nguyễn Văn A" />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Tên đăng nhập</label>
      <Input
        v-model="form.username"
        placeholder="nguyenvana"
        :disabled="isEditing"
      />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Email</label>
      <Input v-model="form.email" type="email" placeholder="admin@synkork.com" />
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Vai trò</label>
      <Select v-model="form.role">
        <SelectTrigger class="w-full">
          <SelectValue placeholder="Chọn vai trò" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="manager">Quản lý</SelectItem>
          <SelectItem value="admin">Quản trị viên</SelectItem>
        </SelectContent>
      </Select>
    </div>

    <div class="space-y-2">
      <label class="text-sm font-medium">Trạng thái</label>
      <Select v-model="form.status">
        <SelectTrigger class="w-full">
          <SelectValue placeholder="Chọn trạng thái" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="active">Hoạt động</SelectItem>
          <SelectItem value="inactive">Ngừng hoạt động</SelectItem>
          <SelectItem value="banned">Bị khóa</SelectItem>
        </SelectContent>
      </Select>
    </div>

    <div class="flex justify-end gap-2 pt-2">
      <Button type="button" variant="outline" :disabled="isLoading" @click="emit('close')">
        Hủy
      </Button>
      <Button type="submit" :disabled="isLoading">
        {{ isLoading ? 'Đang lưu...' : isEditing ? 'Lưu thay đổi' : 'Tạo tài khoản' }}
      </Button>
    </div>
  </form>
</template>
