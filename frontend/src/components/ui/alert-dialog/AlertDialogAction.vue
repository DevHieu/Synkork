<script setup lang="ts">
import type { AlertDialogActionProps } from "reka-ui"
import type { HTMLAttributes } from "vue"
import type { ButtonVariants } from '@/components/ui/button'
import { reactiveOmit } from "@vueuse/core"
import { AlertDialogAction } from "reka-ui"
import { cn } from "@/lib/utils"
import { buttonVariants } from '@/components/ui/button'

const props = defineProps<AlertDialogActionProps & {
  class?: HTMLAttributes["class"]
  size?: ButtonVariants["size"]
}>()

const delegatedProps = reactiveOmit(props, "class", "size")
</script>

<template>
  <AlertDialogAction v-bind="delegatedProps" :class="cn(buttonVariants({ size: props.size }), props.class)">
    <slot />
  </AlertDialogAction>
</template>
