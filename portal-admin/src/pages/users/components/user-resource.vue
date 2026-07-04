<script lang="ts" setup>
import { computed } from 'vue'

import { ModalDescription, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'

import type { User } from '../types/userTypes.ts'

import UserForm from './user-form.vue'

const props = defineProps<{
  user?: User
}>()

defineEmits<{
  (e: 'close'): void
  (e: 'saved', user: User): void
}>()

const user = computed(() => props.user)
const title = computed(() => user.value?.id ? 'Cập nhật người dùng' : 'Tạo người dùng mới')
const description = computed(() => user.value?.id ? `Chỉnh sửa người dùng ${user.value.username}` : 'Tạo tài khoản người dùng mới')
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
