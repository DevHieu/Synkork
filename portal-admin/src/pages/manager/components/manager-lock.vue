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
  locked: []
}>()

const isLoading = ref(false)

function getErrorMessage(error: any) {
  const data = error?.response?.data
  if (typeof data === 'string')
    return data
  return data?.message || error?.message || 'Khóa tài khoản thất bại'
}

async function handleLock() {
  isLoading.value = true
  try {
    await managerService.lock(props.account.id)
    toast.success(`Đã khóa tài khoản ${props.account.username}`)
    emit('locked')
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
      <ModalTitle>Khóa tài khoản {{ account.username }}?</ModalTitle>
      <ModalDescription>
        Tài khoản sẽ chuyển sang trạng thái bị khóa và không thể tiếp tục đăng nhập.
      </ModalDescription>
    </ModalHeader>

    <ModalFooter>
      <UiButton variant="outline" :disabled="isLoading" @click="emit('close')">
        Hủy
      </UiButton>
      <UiButton variant="destructive" :disabled="isLoading" @click="handleLock">
        {{ isLoading ? 'Đang khóa...' : 'Khóa tài khoản' }}
      </UiButton>
    </ModalFooter>
  </div>
</template>
