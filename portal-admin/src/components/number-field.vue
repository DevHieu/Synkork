<script setup lang="ts">
import {
  NumberField,
  NumberFieldContent,
  NumberFieldDecrement,
  NumberFieldIncrement,
  NumberFieldInput,
} from '@/components/ui/number-field'

const props = withDefaults(
  defineProps<{
    min?: number
    max?: number
    placeholder?: string
    class?: string
  }>(),
  {
    min: 0,
  },
)

const modelValue = defineModel<number>()
</script>

<template>
  <NumberField
    v-model="modelValue"
    :min="min"
    :max="max"
  >
    <NumberFieldContent :class="props.class">
      <NumberFieldDecrement />
      <NumberFieldInput :placeholder="placeholder" />
      <NumberFieldIncrement />
    </NumberFieldContent>
  </NumberField>
</template>
