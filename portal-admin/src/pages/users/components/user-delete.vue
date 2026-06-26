<script lang="ts" setup>
import { ref } from 'vue'
import { toast } from 'vue-sonner'

import { ModalClose, ModalDescription, ModalFooter, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'
import { Button as UiButton } from '@/components/ui/button'

import type { User } from '../types/userTypes'

import { userService } from '../services/userService'

const { user } = defineProps<{
  user: User
}>()

const emits = defineEmits<{
  (e: 'remove'): void
}>()

const isLoading = ref(false)
const errorMessage = ref<string | null>(null)
const reason = ref('')

async function handleRemove() {
  if (!reason.value.trim()) {
    errorMessage.value = 'Vui lòng nhập lý do khóa tài khoản'
    toast.error(errorMessage.value)
    return
  }

  isLoading.value = true
  errorMessage.value = null
  try {
    await userService.delete(user.id, reason.value.trim())
    toast.success(`Đã khóa người dùng: ${user.username}`)
    emits('remove')
  }
  catch (err: any) {
    errorMessage.value = err?.response?.data?.message
      || err?.response?.data?.error
      || (typeof err?.response?.data === 'string' ? err.response.data : null)
      || err?.message
      || 'Khóa tài khoản thất bại'
    toast.error(errorMessage.value ?? 'Có lỗi xảy ra')
  }
  finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div>
    <ModalHeader>
      <ModalTitle>
        Khóa tài khoản: {{ user.username }}?
      </ModalTitle>

      <ModalDescription>
        User sẽ được chuyển sang INACTIVE, nhận email thông báo lý do, và bị xóa khỏi tất cả room đang tham gia.
      </ModalDescription>
    </ModalHeader>

    <div class="mt-4 space-y-2">
      <label class="text-sm font-medium">Lý do khóa/xóa user</label>
      <textarea
        v-model="reason"
        class="min-h-24 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm outline-none focus-visible:ring-1 focus-visible:ring-ring"
        placeholder="Nhập lý do để gửi mail cho user"
      />
      <p v-if="errorMessage" class="text-sm text-destructive">
        {{ errorMessage }}
      </p>
    </div>

    <ModalFooter>
      <ModalClose as-child>
        <UiButton variant="outline" :disabled="isLoading">
          Hủy
        </UiButton>
      </ModalClose>

      <UiButton variant="destructive" :disabled="isLoading || !reason.trim()" @click="handleRemove">
        {{ isLoading ? 'Đang khóa...' : 'Khóa user' }}
      </UiButton>
    </ModalFooter>
  </div>
</template>
