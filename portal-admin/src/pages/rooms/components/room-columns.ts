import type { ColumnDef } from '@tanstack/vue-table'
import { h } from 'vue'

import { DataTableColumnHeader, SelectColumn } from '@/components/data-table'
import { Copy } from '@/components/prop-ui/copy'
import Badge from '@/components/ui/badge/Badge.vue'

import type { Room } from '../data/schema'
import { roomStatuses, roomTypes } from '../data/data'
import DataTableRowActions from './room-table-row-actions.vue'

export const roomColumns: ColumnDef<Room>[] = [
  SelectColumn as ColumnDef<Room>,
  {
    accessorKey: 'inviteCode',
    header: ({ column }) => h(DataTableColumnHeader<Room>, { column, title: 'Invite Code' }),
    cell: ({ row }) => {
      const code = row.getValue('inviteCode') as string | null
      if (!code) return h('span', { class: 'text-muted-foreground text-sm' }, '—')
      return h('div', { class: 'flex items-center gap-1' }, [
        h('span', {}, code),
        h(Copy, { class: 'ml-1', size: 'sm', content: code }),
      ])
    },
    enableSorting: false,
    enableResizing: true,
  },
  {
    accessorKey: 'name',
    header: ({ column }) => h(DataTableColumnHeader<Room>, { column, title: 'Name' }),
    cell: ({ row }) => h('div', { class: 'font-medium' }, row.getValue('name')),
    enableResizing: true,
  },
  {
    accessorKey: 'memberCount',
    header: ({ column }) => h(DataTableColumnHeader<Room>, { column, title: 'Members' }),
    cell: ({ row }) => h('div', { class: 'text-center' }, row.getValue('memberCount')),
    enableResizing: true,
  },
  {
    accessorKey: 'status',
    header: ({ column }) => h(DataTableColumnHeader<Room>, { column, title: 'Status' }),
    cell: ({ row }) => {
      const s = roomStatuses.find(s => s.value === row.getValue('status'))
      if (!s) return h('span', { class: 'text-muted-foreground text-sm' }, '—')
      return h(Badge, { class: s.style, variant: 'outline' }, () => s.label)
    },
    filterFn: (row, id, value) => value.includes(row.getValue(id)),
    enableResizing: true,
  },
  {
    accessorKey: 'type',
    header: ({ column }) => h(DataTableColumnHeader<Room>, { column, title: 'Type' }),
    cell: ({ row }) => {
      const t = roomTypes.find(t => t.value === row.getValue('type'))
      if (!t) return null
      return h(Badge, { class: t.style, variant: 'outline' }, () => [
        t.icon && h(t.icon, { class: 'mr-1 h-3 w-3' }),
        t.label,
      ])
    },
    filterFn: (row, id, value) => value.includes(row.getValue(id)),
    enableResizing: true,
  },
  {
    id: 'actions',
    cell: ({ row }) => h(DataTableRowActions, { row }),
  },
]