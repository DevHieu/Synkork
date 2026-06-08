<script setup lang="ts">
import type { Table } from '@tanstack/vue-table'
import { XIcon } from '@lucide/vue'
import { DataTableFacetedFilter, DataTableViewOptions } from '@/components/data-table'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import type { Room } from '../data/schema'
import { roomStatuses, roomTypes } from '../data/data'
import RoomCreateButton from './room-create-button.vue'

interface Props { table: Table<Room> }
const props = defineProps<Props>()
const isFiltered = computed(() => props.table.getState().columnFilters.length > 0)
</script>

<template>
  <div class="flex items-center justify-between">
    <div class="flex items-center flex-1 space-x-2">
      <Input
        placeholder="Filter rooms by name..."
        :model-value="(table.getColumn('name')?.getFilterValue() as string) ?? ''"
        class="h-8 w-[150px] lg:w-[250px]"
        @input="table.getColumn('name')?.setFilterValue($event.target.value)"
      />
      <DataTableFacetedFilter
        v-if="table.getColumn('status')"
        :column="table.getColumn('status')"
        title="Status"
        :options="roomStatuses"
      />
      <DataTableFacetedFilter
        v-if="table.getColumn('type')"
        :column="table.getColumn('type')"
        title="Type"
        :options="roomTypes"
      />
      <Button
        v-if="isFiltered"
        variant="ghost"
        class="h-8 px-2 lg:px-3"
        @click="table.resetColumnFilters()"
      >
        Reset
        <XIcon class="size-4 ml-2" />
      </Button>
    </div>
    <div class="flex items-center gap-2">
      <DataTableViewOptions :table="table" />
      <RoomCreateButton />
    </div>
  </div>
</template>