<script setup lang="ts">
import type { DataTableProps } from '@/components/data-table'

import { DataTable, useGenerateVueTable } from '@/components/data-table'

import type { Room } from '../data/schema'

import { roomColumns } from './room-columns'
import RoomTableToolbar from './room-table-toolbar.vue'

const props = defineProps<Omit<DataTableProps<Room>, 'columns'>>()
const table = useGenerateVueTable<Room>({ ...props, columns: roomColumns })
</script>

<template>
  <DataTable :columns="roomColumns" :data :loading :table>
    <template #toolbar>
      <RoomTableToolbar :table class="w-full overflow-x-auto" />
    </template>
  </DataTable>
</template>