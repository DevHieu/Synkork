<script lang="ts" setup>
import { ref } from 'vue'
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

import type { User } from '../types/userTypes'

import { userService } from '../services/userService'

const { user } = defineProps<{ user?: User }>()
const emits = defineEmits<{
  (e: 'close'): void
  (e: 'saved', user: User): void
}>()

const roles = ['admin', 'manager', 'user'] as const
const status = ['active', 'inactive', 'invited', 'suspended'] as const

const form = ref({
  displayName: user?.displayName || '',
  username: user?.username || '',
  email: user?.email || '',
  status: user?.status || 'active',
  role: user?.role || 'admin',
})

const isLoading = ref(false)

async function onSubmit() {
  isLoading.value = true
  try {
    let result: User
    if (user?.id) {
      result = await userService.update(user.id, form.value)
      toast.success('Cập nhật người dùng thành công')
    }
    else {
      result = await userService.create(form.value)
      toast.success('Tạo người dùng thành công')
    }
    emits('saved', result)
    emits('close')
  }
  catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || 'Có lỗi xảy ra'
    toast.error(msg)
  }
  finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="max-h-[500px] overflow-y-auto">
    <form class="space-y-4" @submit.prevent="onSubmit">
      <div class="space-y-2">
        <label class="text-sm font-medium">Display Name</label>
        <Input v-model="form.displayName" type="text" />
      </div>
      <div class="space-y-2">
        <label class="text-sm font-medium">Username</label>
        <Input v-model="form.username" type="text" :disabled="!!user?.id" />
      </div>
      <div class="space-y-2">
        <label class="text-sm font-medium">Email</label>
        <Input v-model="form.email" type="text" />
      </div>
      <div class="space-y-2">
        <label class="text-sm font-medium">Status</label>
        <Select v-model="form.status">
          <SelectTrigger class="w-full">
            <SelectValue placeholder="Select a status" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectItem v-for="s in status" :key="s" :value="s">
                {{ s }}
              </SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>
      <div class="space-y-2">
        <label class="text-sm font-medium">Role</label>
        <Select v-model="form.role">
          <SelectTrigger class="w-full">
            <SelectValue placeholder="Select a role" />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectItem v-for="r in roles" :key="r" :value="r">
                {{ r }}
              </SelectItem>
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>

      <Button type="submit" class="w-full" :disabled="isLoading">
        {{ isLoading ? 'Đang lưu...' : 'Save Changes' }}
      </Button>
    </form>
  </div>
</template>
