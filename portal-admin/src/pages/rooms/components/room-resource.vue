<script lang="ts" setup>
import { computed } from 'vue'
import { ModalDescription, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'
import type { Room } from '../data/schema'
import RoomForm from './room-form.vue'

const props = defineProps<{ room?: Room }>()
defineEmits(['close', 'refresh'])

const title = computed(() => props.room?.id ? 'Edit Room' : 'New Room')
const description = computed(() => props.room?.id
  ? `Edit room "${props.room.name}"`
  : 'Create a new group room'
)
</script>

<template>
  <div>
    <ModalHeader>
      <ModalTitle>{{ title }}</ModalTitle>
      <ModalDescription>{{ description }}</ModalDescription>
    </ModalHeader>
    <RoomForm :room="room" @close="$emit('close')" @refresh="$emit('refresh')" />
  </div>
</template>