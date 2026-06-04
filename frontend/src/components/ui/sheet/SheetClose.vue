<script setup lang="ts">
import type { DialogCloseProps } from "reka-ui"
import { DialogClose } from "reka-ui"

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<DialogCloseProps>()
</script>

<template>
  <DialogClose
    data-slot="sheet-close"
    v-bind="{ ...props, ...$attrs }"
  >
    <slot />
  </DialogClose>
</template>
