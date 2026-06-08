<script setup lang="ts">
import { useColorMode } from '@vueuse/core'

const mode = useColorMode()

import icon from "@/assets/icons/DauChimMatMat.svg"
</script>

<template>
  <h1 class="flex items-center gap-2 text-xl font-bold">
    <img :src="icon" class="w-8 h-8" :class="mode === 'dark' ? 'invert' : ''">
    <span>Synkork Admin</span>
  </h1>
</template>
