<script setup lang="ts">
const props = defineProps<{
  title: string
  data: string
  icon: any
  dayGrowth?: number
  monthGrowth?: number
}>()

function formatGrowth(value: number) {
  const sign = value > 0 ? '+' : ''
  return `${sign}${value.toFixed(1)}%`
}
</script>

<template>
  <UiCard>
    <UiCardHeader class="flex flex-row items-center justify-between pb-2 space-y-0">
      <UiCardTitle class="text-sm font-medium">{{ props.title }}</UiCardTitle>
      <component :is="props.icon" class="w-4 h-4 text-muted-foreground" />
    </UiCardHeader>

    <UiCardContent>
      <div class="text-2xl font-bold">{{ props.data }}</div>

      <div class="mt-1 flex flex-col gap-0.5">
        <p v-if="dayGrowth !== undefined" class="text-xs text-muted-foreground">
          <span :class="dayGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="font-medium">
            {{ formatGrowth(dayGrowth) }}
          </span>
          so với hôm qua
        </p>
        <p v-if="monthGrowth !== undefined" class="text-xs text-muted-foreground">
          <span :class="monthGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="font-medium">
            {{ formatGrowth(monthGrowth) }}
          </span>
          so với tháng trước
        </p>
      </div>
    </UiCardContent>
  </UiCard>
</template>