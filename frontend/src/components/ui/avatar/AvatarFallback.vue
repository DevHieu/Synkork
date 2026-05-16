<script setup lang="ts">
import type { AvatarFallbackProps } from "reka-ui";
import type { HTMLAttributes } from "vue";
import { reactiveOmit } from "@vueuse/core";
import { AvatarFallback } from "reka-ui";
import { cn } from "@/lib/utils";
import { useThemeStore } from "@/stores/themeStore";

const themeStore = useThemeStore()

const props = defineProps<
  AvatarFallbackProps & { class?: HTMLAttributes["class"] }
>();

const delegatedProps = reactiveOmit(props, "class");
</script>

<template>
  <AvatarFallback data-slot="avatar-fallback" v-bind="delegatedProps" :class="cn(
    'bg-primary flex size-full items-center justify-center rounded-full',
    props.class,
  )
    ">
    <img src="/assets/DauChim.png" alt="dauchim" class="transition-all duration-200"
      :class="{ 'invert': themeStore.isDark }" />
  </AvatarFallback>
</template>
