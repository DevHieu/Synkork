<script lang="ts" setup>
import { ref } from 'vue'
import { toast } from 'vue-sonner'

import { ModalClose, ModalDescription, ModalFooter, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'

import type { User } from '../data/schema'

import { adminUserService } from '../data/userAdminService'

const { user } = defineProps<{
  user: User
}>()

const emits = defineEmits<{
  (e: 'remove'): void
}>()

const isLoading = ref(false)

async function handleRemove() {
  isLoading.value = true
  try {
    await adminUserService.delete(user.id)
    toast.success(`Đã xóa người dùng: ${user.username}`)
    emits('remove')
  }
  catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || 'Xóa thất bại'
    toast.error(msg)
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

      <ModalClose as-child>
        <UiButton variant="destructive" :disabled="isLoading" @click="handleRemove">
          {{ isLoading ? 'Đang xóa...' : 'Delete' }}
        </UiButton>
      </ModalClose>
    </ModalFooter>
  </div>
</template>
