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

async function handleRemove() {
  isLoading.value = true
  errorMessage.value = null
  try {
    await userService.delete(user.id)
    toast.success(`Đã xóa người dùng: ${user.username}`)
    emits('remove')
  }
  catch (err: any) {
    errorMessage.value = err?.response?.data?.message
      || err?.response?.data?.error
      || (typeof err?.response?.data === 'string' ? err.response.data : null)
      || err?.message
      || 'Xóa thất bại'
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
        Delete this user: {{ user.username }} ?
      </ModalTitle>

      <ModalDescription>
        You are about to delete a user with the ID {{ user.id }}. This action cannot be undone.
      </ModalDescription>
    </ModalHeader>
    <ModalFooter>
      <ModalClose as-child>
        <UiButton variant="outline" :disabled="isLoading">
          Cancel
        </UiButton>
      </ModalClose>

      <UiButton variant="destructive" :disabled="isLoading" @click="handleRemove">
        {{ isLoading ? 'Đang xóa...' : 'Delete' }}
      </UiButton>
    </ModalFooter>
  </div>
</template>
