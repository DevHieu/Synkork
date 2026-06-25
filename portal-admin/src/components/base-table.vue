<script setup lang="ts" generic="T extends { id?: number | string }">
import { computed, type VNode } from 'vue'

export interface TableColumn<T> {
  header: string | VNode
  group?: string | VNode
  accessor?: keyof T
  render?: (row: T) => string | VNode | number | boolean | null | undefined
  minWidth?: number
}

interface Props {
  columns: TableColumn<T>[]
  data: T[]
  className?: string
  footer?: any
}

const props = defineProps<Props>()

const columnWidths = computed(() => props.columns.map(column => column.minWidth || 160))

const minTableWidth = computed(() => {
  const totalWidth = columnWidths.value.reduce((total, width) => total + width, 0)
  return Math.max(totalWidth, 720)
})

const hasGroupedHeader = computed(() => props.columns.some(column => column.group))

const groupedBorderClass = 'border-r border-b border-border last:border-r-0'

const headerGroups = computed(() => {
  return props.columns.reduce<Array<{ label: string | VNode, colSpan: number }>>((groups, column) => {
    const previousGroup = groups[groups.length - 1]
    const label = column.group || ''

    if (previousGroup && previousGroup.label === label) {
      previousGroup.colSpan += 1
      return groups
    }

    groups.push({ label, colSpan: 1 })
    return groups
  }, [])
})

const emit = defineEmits<{
  scroll: [event: Event]
}>()

function onScroll(event: Event) {
  emit('scroll', event)
}
</script>

<template>
  <div :class="[`table-container relative`, className]">
    <div
      class="max-h-[60vh] w-full overflow-auto border rounded-md"
      @scroll="onScroll"
    >
      <!-- We use a raw table here to have full control over the container and stickiness -->
      <table
        class="w-full caption-bottom text-sm border-collapse"
        :class="[hasGroupedHeader ? 'border-separate border-spacing-0' : '']"
        :style="{ width: `max(100%, ${minTableWidth}px)` }"
      >
        <thead
          :class="[
            'z-10',
            hasGroupedHeader ? '' : '[&_tr]:border-b',
          ]"
        >
          <tr v-if="hasGroupedHeader">
            <UiTableHead
              v-for="(group, idx) in headerGroups"
              :key="idx"
              :colspan="group.colSpan"
              :class="[
                'sticky top-0 z-20 h-8 bg-muted/90 backdrop-blur-sm px-4 py-2 text-center text-xs font-semibold uppercase text-muted-foreground border-b border-border',
                groupedBorderClass,
              ]"
            >
              <component :is="group.label" v-if="typeof group.label !== 'string'" />
              <template v-else>
                {{ group.label }}
              </template>
            </UiTableHead>
          </tr>
          <tr>
            <UiTableHead
              v-for="(col, idx) in columns"
              :key="idx"
              :class="[
                'sticky z-10 h-10 text-center align-middle bg-background border-b border-border',
                hasGroupedHeader ? 'top-8' : 'top-0',
                hasGroupedHeader ? groupedBorderClass : '',
              ]"
              :style="{ minWidth: `${columnWidths[idx]}px`, width: `${columnWidths[idx]}px` }"
            >
              <component :is="col.header" v-if="typeof col.header !== 'string'" />
              <template v-else>
                {{ col.header }}
              </template>
            </UiTableHead>
          </tr>
        </thead>
        <tbody class="[&_tr:last-child]:border-0">
          <template v-if="data.length === 0">
            <UiTableRow>
              <UiTableCell
                :colspan="columns.length"
                class="py-10 text-center text-sm text-muted-foreground"
              >
                No data available
              </UiTableCell>
            </UiTableRow>
          </template>
          <template v-else>
            <UiTableRow
              v-for="(row, rowIdx) in data"
              :key="row.id || rowIdx"
              :class="[
                'transition-colors hover:bg-muted/50',
                hasGroupedHeader ? '' : 'border-b border-border',
              ]"
            >
              <UiTableCell
                v-for="(col, idx) in columns"
                :key="idx"
                :class="[
                  'text-center align-middle',
                  hasGroupedHeader ? groupedBorderClass : '',
                ]"
                :style="{ minWidth: `${columnWidths[idx]}px`, width: `${columnWidths[idx]}px` }"
              >
                <template v-if="col.render">
                  <component :is="col.render(row)" v-if="typeof col.render(row) === 'object'" />
                  <template v-else>
                    {{ col.render(row) }}
                  </template>
                </template>
                <template v-else-if="col.accessor">
                  {{ row[col.accessor] }}
                </template>
              </UiTableCell>
            </UiTableRow>
          </template>
        </tbody>
      </table>
      <slot name="footer" />
    </div>
  </div>
</template>
