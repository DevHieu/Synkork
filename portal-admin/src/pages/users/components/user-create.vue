<script lang="ts" setup>
import { UserRoundPlusIcon } from '@lucide/vue'
import { ref } from 'vue'

import { Modal, ModalContent, ModalTrigger } from '@/components/prop-ui/modal'

import type { User } from '../types/userTypes.ts'

import UserResource from './user-resource.vue'

const emits = defineEmits<{
  saved: [user: User]
}>()

const isOpen = ref(false)
</script>

<template>
  <Modal v-model:open="isOpen">
    <ModalTrigger as-child>
      <UiButton>
        <UserRoundPlusIcon />
        Tạo user
      </UiButton>
    </ModalTrigger>

    <ModalContent>
      <UserResource
        @close="isOpen = false"
        @saved="(u) => { isOpen = false; $emit('saved', u) }"
      />
    </ModalContent>
  </Modal>
</template>
