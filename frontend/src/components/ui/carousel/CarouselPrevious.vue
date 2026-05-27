<script setup lang="ts">
import type { WithClassAsProps } from "./interface"
import type { ButtonVariants } from '@/components/ui/button'
import { cn } from "@/lib/utils"
import { useCarousel } from "./useCarousel"

const props = withDefaults(defineProps<{
  variant?: ButtonVariants["variant"]
  size?: ButtonVariants["size"]
} & WithClassAsProps>(), {
  variant: "outline",
  size: "icon",
})

const { orientation, canScrollPrev, scrollPrev } = useCarousel()
</script>

<template>
  <button data-slot="carousel-previous" :disabled="!canScrollPrev" :class="cn(
    'absolute flex items-center justify-center',
    'w-10 h-10 rounded-full',
    'text-white/60 hover:text-white',
    'hover:bg-white/10',
    'transition-all duration-200 ease-out',
    'disabled:opacity-20 disabled:cursor-not-allowed',
    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white/40',
    orientation === 'horizontal'
      ? 'top-1/2 left-4 -translate-y-1/2'
      : '-top-12 left-1/2 -translate-x-1/2 rotate-90',
    props.class,
  )" @click="scrollPrev">
    <slot>
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
        stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M15 18l-6-6 6-6" />
      </svg>
      <span class="sr-only">Previous Slide</span>
    </slot>
  </button>
</template>