<script setup lang="ts">
const props = defineProps<{
  title: string
  data: string
  icon: any
  dataClass?: string
  description?: string
  dayGrowth?: number
  monthGrowth?: number
  dayGrowthLabel?: string
  monthGrowthLabel?: string
}>()

function formatGrowth(value: number) {
  const sign = value > 0 ? '+' : ''
  return `${sign}${value.toFixed(1)}%`
}
</script>

<template>
  <UiCard>
    <UiCardHeader class="flex flex-row items-center justify-between pb-2 space-y-0">
      <UiCardTitle class="text-sm font-medium">
        {{ props.title }}
      </UiCardTitle>
      <component :is="props.icon" class="w-4 h-4 text-muted-foreground" />
    </UiCardHeader>

    <UiCardContent>
      <div class="text-2xl font-bold" :class="props.dataClass">
        {{ props.data }}
      </div>
      <p v-if="props.description" class="mt-1 text-xs text-muted-foreground">
        {{ props.description }}
      </p>

      <div class="mt-1 flex flex-col gap-0.5">
        <p v-if="dayGrowth !== undefined" class="text-xs text-muted-foreground">
          <span :class="dayGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="font-medium">
            {{ formatGrowth(dayGrowth) }}
          </span>
          {{ props.dayGrowthLabel ?? 'so với hôm qua' }}
        </p>
        <p v-if="monthGrowth !== undefined" class="text-xs text-muted-foreground">
          <span :class="monthGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="font-medium">
            {{ formatGrowth(monthGrowth) }}
          </span>
          {{ props.monthGrowthLabel ?? 'so với tháng trước' }}
        </p>
      </div>
    </UiCardContent>
  </UiCard>
</template>
