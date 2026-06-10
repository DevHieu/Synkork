<script setup lang="ts">
import { PopoverRoot, useForwardProps } from "reka-ui"
import type { PopoverRootEmits, PopoverRootProps } from "reka-ui"
import { useVModel } from "@vueuse/core"

const props = defineProps<PopoverRootProps>()
const emits = defineEmits<PopoverRootEmits>()

const forwardedProps = useForwardProps(props)
const open = useVModel(props, "open", emits, {
  passive: (props.open === undefined) as false,
  defaultValue: props.defaultOpen,
})
</script>

<template>
  <PopoverRoot v-bind="forwardedProps" v-model:open="open">
    <slot />
  </PopoverRoot>
</template>
