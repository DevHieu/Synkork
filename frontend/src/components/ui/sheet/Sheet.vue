<script setup lang="ts">
import { DialogRoot, useForwardProps } from "reka-ui"
import type { DialogRootEmits, DialogRootProps } from "reka-ui"
import { useVModel } from "@vueuse/core"

const props = defineProps<DialogRootProps>()
const emits = defineEmits<DialogRootEmits>()

const forwardedProps = useForwardProps(props)
const open = useVModel(props, "open", emits, {
  passive: (props.open === undefined) as false,
  defaultValue: props.defaultOpen,
})
</script>

<template>
  <DialogRoot v-bind="forwardedProps" v-model:open="open">
    <slot />
  </DialogRoot>
</template>
