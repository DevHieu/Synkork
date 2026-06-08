<script lang="ts" setup>
import { ModalDescription, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'

import type { User } from '../data/schema'

import UserForm from './user-form.vue'

const props = defineProps<{
  user?: User
}>()

defineEmits<{
  (e: 'close'): void
  (e: 'saved', user: User): void
}>()

const user = computed(() => props.user)
const title = computed(() => user.value?.id ? `Edit User` : 'New User')
const description = computed(() => user.value?.id ? `Edit user ${user.value.username}` : 'Create new user')
</script>

<template>
  <div>
    <ModalHeader>
      <ModalTitle>
        {{ title }}
      </ModalTitle>
      <ModalDescription>
        {{ description }}
      </ModalDescription>
    </ModalHeader>

    <UserForm :user="user" @close="$emit('close')" @saved="(u) => $emit('saved', u)" />
  </div>
</template>
