<script setup lang="ts">
import type { DialogTriggerProps } from "reka-ui"
import { DialogTrigger } from "reka-ui"

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<DialogTriggerProps>()
</script>

<template>
  <DialogTrigger
    data-slot="dialog-trigger"
    v-bind="{ ...props, ...$attrs }"
  >
    <slot />
  </DialogTrigger>
</template>
