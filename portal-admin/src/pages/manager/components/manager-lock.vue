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
const errorMessage = ref<string | null>(null)
const reason = ref('')

function getErrorMessage(error: any) {
  const data = error?.response?.data
  if (typeof data === 'string')
    return data
  return data?.message || error?.message || 'Khóa tài khoản thất bại'
}

async function handleLock() {
  if (!reason.value.trim()) {
    errorMessage.value = 'Vui lòng nhập lý do khóa tài khoản'
    toast.error(errorMessage.value)
    return
  }

  isLoading.value = true
  errorMessage.value = null
  try {
    await managerService.lock(props.account.id, reason.value.trim())
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

    <div class="mt-4 space-y-2">
      <label class="text-sm font-medium">Lý do khóa tài khoản</label>
      <textarea
        v-model="reason"
        class="min-h-24 w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm outline-none focus-visible:ring-1 focus-visible:ring-ring"
        placeholder="Nhập lý do để gửi mail cho manager/admin"
      />
      <p v-if="errorMessage" class="text-sm text-destructive">
        {{ errorMessage }}
      </p>
    </div>

    <ModalFooter>
      <UiButton variant="outline" :disabled="isLoading" @click="emit('close')">
        Hủy
      </UiButton>
      <UiButton variant="destructive" :disabled="isLoading || !reason.trim()" @click="handleLock">
        {{ isLoading ? 'Đang khóa...' : 'Khóa tài khoản' }}
      </UiButton>
    </ModalFooter>
  </div>
</template>
