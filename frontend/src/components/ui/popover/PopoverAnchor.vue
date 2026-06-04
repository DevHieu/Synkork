<script setup lang="ts">
import type { PopoverAnchorProps } from "reka-ui"
import { PopoverAnchor } from "reka-ui"

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<PopoverAnchorProps>()
</script>

<template>
  <PopoverAnchor
    data-slot="popover-anchor"
    v-bind="{ ...props, ...$attrs }"
  >
    <slot />
  </PopoverAnchor>
</template>
