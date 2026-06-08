import { UsersIcon, DoorClosedIcon } from '@lucide/vue'
import { h } from 'vue'

import type { FacetedFilterOption } from '@/components/data-table'

export const roomStatuses: (FacetedFilterOption & { style: string })[] = [
  {
    label: 'Open',
    value: 'OPEN',
    style: 'bg-teal-100/30 text-teal-900 dark:text-teal-200 border-teal-200',
  },
  {
    label: 'Closed',
    value: 'CLOSED',
    style: 'bg-neutral-300/40 border-neutral-300',
  },
]

export const roomTypes: (FacetedFilterOption & { style: string })[] = [
  {
    label: 'Group',
    value: 'GROUP',
    icon: h(UsersIcon),
    style: 'bg-violet-100/40 text-violet-900 dark:text-violet-200 border-violet-300',
  },
  {
    label: 'DM',
    value: 'DM',
    icon: h(DoorClosedIcon),
    style: 'bg-blue-100/40 text-blue-900 dark:text-blue-200 border-blue-300',
  },
]