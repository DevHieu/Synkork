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

const title = computed(() =>
  props.account ? 'Chỉnh sửa tài khoản quản trị' : 'Tạo tài khoản quản trị',
)
const description = computed(() => props.account
  ? `Cập nhật thông tin và vai trò của ${props.account.username}`
  : 'Tạo tài khoản quản lý hoặc quản trị viên mới. Mật khẩu tạm thời sẽ được gửi qua email.')
</script>

<template>
  <div>
    <ModalHeader class="border-b border-border bg-muted/30 px-6 py-5">
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
