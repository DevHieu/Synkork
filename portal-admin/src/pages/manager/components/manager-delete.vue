<script setup lang="ts">
import { ref } from 'vue'
import { toast } from 'vue-sonner'

import {
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from '@/components/prop-ui/modal'

import type { ManagerAccount } from '../types/managerTypes'

import { managerService } from '../services/managerService'

const props = defineProps<{
  account: ManagerAccount
}>()

const emit = defineEmits<{
  close: []
  removed: []
}>()

const isLoading = ref(false)

function getErrorMessage(error: any) {
  const data = error?.response?.data
  if (typeof data === 'string')
    return data
  return data?.message || error?.message || 'Xóa tài khoản thất bại'
}

async function handleDelete() {
  isLoading.value = true
  try {
    await managerService.delete(props.account.id)
    toast.success(`Đã xóa tài khoản ${props.account.username}`)
    emit('removed')
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
  <div>
    <ModalHeader>
      <ModalTitle>Xóa tài khoản {{ account.username }}?</ModalTitle>
      <ModalDescription>
        Hành động này không thể hoàn tác. Tài khoản sẽ bị xóa khỏi hệ thống.
      </ModalDescription>
    </ModalHeader>

    <ModalFooter>
      <UiButton variant="outline" :disabled="isLoading" @click="emit('close')">
        Hủy
      </UiButton>
      <UiButton variant="destructive" :disabled="isLoading" @click="handleDelete">
        {{ isLoading ? 'Đang xóa...' : 'Xóa tài khoản' }}
      </UiButton>
    </ModalFooter>
  </div>
</template>
