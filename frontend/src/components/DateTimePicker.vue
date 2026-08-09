<script setup lang="ts">
import { Calendar } from 'lucide-vue-next';
import { cn } from '@/lib/utils';
import { Input } from './ui/input';

interface DateTimePickerProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  class?: string;
}

const props = defineProps<DateTimePickerProps>();

const handleChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  props.onChange(target.value);
};
</script>

<template>
  <div :class="cn('relative w-full', props.class)">
    <Calendar class="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-primary" />

    <Input type="datetime-local" :value="props.value" :placeholder="props.placeholder" @change="handleChange" :class="cn(
      'pl-9',
      '[&::-webkit-calendar-picker-indicator]:absolute',
      '[&::-webkit-calendar-picker-indicator]:left-0',
      '[&::-webkit-calendar-picker-indicator]:top-0',
      '[&::-webkit-calendar-picker-indicator]:h-full',
      '[&::-webkit-calendar-picker-indicator]:w-full',
      '[&::-webkit-calendar-picker-indicator]:cursor-pointer',
      '[&::-webkit-calendar-picker-indicator]:opacity-0'
    )" />
  </div>
</template>

<style scoped></style>