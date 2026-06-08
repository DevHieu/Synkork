<script lang="ts" setup>
import { ref } from 'vue'
import { toast } from 'vue-sonner'
import axiosClient from '@/lib/axiosClient'
import { ModalClose, ModalDescription, ModalFooter, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'
import type { Room } from '../data/schema'

const { room } = defineProps<{ room: Room }>()
const emits = defineEmits<{ (e: 'remove'): void }>()
const loading = ref(false)

async function handleRemove() {
  try {
    loading.value = true
    await axiosClient.delete(`/manage/rooms/${room.id}`)
    toast.success(`Room "${room.name}" đã được xóa`)
    emits('remove')
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi xóa room')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <ModalHeader>
      <ModalTitle>Delete room: {{ room.name }}?</ModalTitle>
      <ModalDescription>
        Xóa room <strong>{{ room.name }}</strong> sẽ không thể khôi phục lại. Bạn có chắc không?
      </ModalDescription>
    </ModalHeader>
    <ModalFooter>
      <ModalClose as-child>
        <UiButton variant="outline" :disabled="loading">Cancel</UiButton>
      </ModalClose>
      <UiButton variant="destructive" :disabled="loading" @click="handleRemove">
        {{ loading ? 'Deleting...' : 'Delete' }}
      </UiButton>
    </ModalFooter>
  </div>
</template>