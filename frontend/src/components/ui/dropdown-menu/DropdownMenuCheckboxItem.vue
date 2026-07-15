<script setup lang="ts">
import type { DropdownMenuCheckboxItemEmits, DropdownMenuCheckboxItemProps } from "reka-ui"
import type { HTMLAttributes } from "vue"
import { reactiveOmit } from "@vueuse/core"
import { Check } from "lucide-vue-next"
import {
  DropdownMenuCheckboxItem,
  useForwardPropsEmits,
} from "reka-ui"
import { cn } from "@/lib/utils"

const props = defineProps<DropdownMenuCheckboxItemProps & {
  class?: HTMLAttributes["class"]
  checked?: boolean
}
>()
const emits = defineEmits<DropdownMenuCheckboxItemEmits>()

const delegatedProps = reactiveOmit(props, "class")

const forwarded = useForwardPropsEmits(delegatedProps, emits)
</script>

<template>
  <DropdownMenuCheckboxItem data-slot="dropdown-menu-checkbox-item" v-bind="forwarded" :class="cn(
    'focus:bg-accent focus:text-accent-foreground relative flex cursor-default items-center gap-2 rounded-sm py-1.5 pr-2 pl-8 text-sm outline-hidden select-none data-[disabled]:pointer-events-none data-[disabled]:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*=\'size-\'])]:size-4',
    props.class,
  )">
    <span :class="cn(
      'pointer-events-none absolute left-2 flex size-4 shrink-0 items-center justify-center rounded-[4px] border shadow-xs transition-shadow',
      props.checked
        ? 'bg-primary text-primary-foreground border-primary'
        : 'border-input',
    )">
      <slot v-if="props.checked" name="indicator-icon">
        <Check class="size-3.5" />
      </slot>
    </span>
    <slot />
  </DropdownMenuCheckboxItem>
</template>