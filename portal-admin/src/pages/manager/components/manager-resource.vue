<script setup lang="ts">
import { computed } from 'vue'

import {
  ModalDescription,
  ModalHeader,
  ModalTitle,
} from '@/components/prop-ui/modal'

import type { ManagerAccount } from '../types/managerTypes'

import ManagerForm from './manager-form.vue'

const props = defineProps<{
  account?: ManagerAccount
}>()

defineEmits<{
  close: []
  saved: [account: ManagerAccount]
}>()

const title = computed(() => props.account ? 'Chỉnh sửa Manager' : 'Tạo tài khoản Manager')
const description = computed(() => props.account
  ? `Cập nhật thông tin của ${props.account.username}`
  : 'Tạo tài khoản Manager mới. Mật khẩu tạm thời sẽ được gửi qua email.')
</script>

<template>
  <div>
    <ModalHeader>
      <ModalTitle>{{ title }}</ModalTitle>
      <ModalDescription>{{ description }}</ModalDescription>
    </ModalHeader>
    <ManagerForm
      :account="account"
      @close="$emit('close')"
      @saved="value => $emit('saved', value)"
    />
  </div>
</template>
