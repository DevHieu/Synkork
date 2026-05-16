<script setup lang="ts">
import { LoaderIcon } from '@lucide/vue'

import { BasicPage } from '@/components/global-layout'

const loading = ref(false)

function mockLoading() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 2000)
}
</script>

<template>
  <BasicPage
    title="Rooms & Space"
    description="Rooms & Space description"
    sticky
  >
    <template #actions>
      <UserInvite />
      <UserCreate />
      <UiButton variant="outline" @click="mockLoading">
        <LoaderIcon />Mock Loading
      </UiButton>
    </template>
    <div class="overflow-x-auto">
      <!-- <DataTable :loading :data="users" :columns="columns" /> -->
    </div>
  </BasicPage>
</template>
