<script lang="ts" setup>
import { MessagesSquareIcon } from '@lucide/vue'

import { Modal, ModalContent, ModalTrigger } from '@/components/prop-ui/modal'

import RoomResource from './room-resource.vue'

const isOpen = ref(false)
</script>

<template>
  <Modal v-model:open="isOpen">
    <ModalTrigger as-child>
      <UiButton>
        <MessagesSquareIcon />
        Create Room
      </UiButton>
    </ModalTrigger>
    <ModalContent>
      <RoomResource @close="isOpen = false" />
    </ModalContent>
  </Modal>
</template>