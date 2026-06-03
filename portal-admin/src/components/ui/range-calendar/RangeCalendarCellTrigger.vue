<script lang="ts" setup>
import type { RangeCalendarCellTriggerProps } from "reka-ui"
import type { HTMLAttributes } from "vue"
import { reactiveOmit } from "@vueuse/core"
import { RangeCalendarCellTrigger, useForwardProps } from "reka-ui"
import { cn } from "@/lib/utils"
import { buttonVariants } from '@/components/ui/button'

const props = withDefaults(defineProps<RangeCalendarCellTriggerProps & { class?: HTMLAttributes["class"] }>(), {
  as: "button",
})

const delegatedProps = reactiveOmit(props, "class")

const forwardedProps = useForwardProps(delegatedProps)
</script>

<template>
  <RangeCalendarCellTrigger
    data-slot="range-calendar-trigger"
    :class="cn(
      buttonVariants({ variant: 'ghost' }),
      'h-8 w-8 p-0 font-normal data-selected:opacity-100',
      
      /* 1. Chỉ áp dụng nền xám, chữ thường cho các ô ở GIỮA (Có selected nhưng KHÔNG phải Start/End) */
      '[&[data-selected]:not([data-selection-start]):not([data-selection-end])]:bg-muted [&[data-selected]:not([data-selection-start]):not([data-selection-end])]:text-foreground [&[data-selected]:not([data-selection-start]):not([data-selection-end])]:rounded-none [&[data-selected]:not([data-selection-start]):not([data-selection-end]):hover]:bg-muted/50',
      
      /* 2. Ô của ngày hôm nay khi chưa được chọn */
      '[&[data-today]:not([data-selected])]:bg-muted [&[data-today]:not([data-selected])]:text-accent-foreground',
      
      /* 3. Ô ĐẦU RANGE (Selection Start): Nền đậm, chữ trắng, bo tròn */
      'data-selection-start:bg-primary data-selection-start:text-primary-foreground [&[data-selection-start]:hover]:bg-primary/85 data-selection-start:hover:text-primary-foreground data-selection-start:focus:bg-primary data-selection-start:focus:text-primary-foreground',
      
      /* 4. Ô CUỐI RANGE (Selection End): Nền đậm, chữ trắng, bo tròn */
      'data-selection-end:bg-primary data-selection-end:text-primary-foreground [&[data-selection-end]:hover]:bg-primary/85 data-selection-end:hover:text-primary-foreground data-selection-end:focus:bg-primary data-selection-end:focus:text-primary-foreground',
      
      // Outside months
      'data-outside-view:text-muted-foreground',
      // Disabled
      'data-disabled:text-muted-foreground data-disabled:opacity-50',
      // Unavailable
      'data-unavailable:text-destructive-foreground data-unavailable:line-through',
      props.class,
    )"
    v-bind="forwardedProps"
  >
    <slot />
  </RangeCalendarCellTrigger>
</template>
