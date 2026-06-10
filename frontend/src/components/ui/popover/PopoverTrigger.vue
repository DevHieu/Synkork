<script setup lang="ts">
import type { PopoverTriggerProps } from "reka-ui"
import { PopoverTrigger } from "reka-ui"

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<PopoverTriggerProps>()
</script>

<template>
  <PopoverTrigger
    data-slot="popover-trigger"
    v-bind="{ ...props, ...$attrs }"
  >
    <slot />
  </PopoverTrigger>
</template>
