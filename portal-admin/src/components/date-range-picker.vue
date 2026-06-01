<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  X,
} from '@lucide/vue'
import {
  RangeCalendarRoot,
} from 'reka-ui'
import {
  RangeCalendarCell,
  RangeCalendarCellTrigger,
  RangeCalendarGrid,
  RangeCalendarGridBody,
  RangeCalendarGridHead,
  RangeCalendarGridRow,
  RangeCalendarHeadCell,
} from '@/components/ui/range-calendar'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import { cn } from '@/lib/utils'
import {
  CalendarDate,
  getLocalTimeZone,
  today as getToday
} from '@internationalized/date'

interface AppDateRange {
  from: Date
  to: Date
}

interface DateRangePickerProps {
  modelValue: AppDateRange
  resetRange?: AppDateRange
  visibleRange?: AppDateRange
  maxRangeDays?: number
  showOneYearRange?: boolean
  class?: string
}

const props = withDefaults(defineProps<DateRangePickerProps>(), {
  showOneYearRange: false,
})

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()

function toCalendarDate(date: Date): CalendarDate {
  return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate())
}

function toDate(date: any): Date {
  return date.toDate(getLocalTimeZone())
}

const today = getToday(getLocalTimeZone())

const internalRange = ref<any>({
  start: props.modelValue?.from ? toCalendarDate(props.modelValue.from) : today,
  end: props.modelValue?.to ? toCalendarDate(props.modelValue.to) : today,
})

const placeholder = ref<any>(
  props.modelValue?.from ? toCalendarDate(props.modelValue.from) : today
)

const dateLabel = computed(() => {
  if (!props.modelValue?.from || !props.modelValue?.to) return t('common.selectDate', 'Select Date')
  return `${dayjs(props.modelValue.from).format('DD/MM/YYYY')} - ${dayjs(props.modelValue.to).format('DD/MM/YYYY')}`
})


watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    internalRange.value = {
      start: toCalendarDate(newVal.from),
      end: toCalendarDate(newVal.to),
    }
  }
}, { deep: true })

const quickRanges = computed(() => {
  const now = dayjs()
  const ranges = [
    {
      label: t('pages.014.ranges.7d', '7 days'),
      from: now.subtract(7, 'day').startOf('day').toDate(),
      to: now.endOf('day').toDate(),
    },
    {
      label: t('pages.014.ranges.30d', '30 days'),
      from: now.subtract(30, 'day').startOf('day').toDate(),
      to: now.endOf('day').toDate(),
    },
    {
      label: t('pages.014.ranges.90d', '90 days'),
      from: now.subtract(90, 'day').startOf('day').toDate(),
      to: now.endOf('day').toDate(),
    },
  ]

  if (props.showOneYearRange) {
    ranges.push({
      label: t('pages.014.ranges.1y', '1Y'),
      from: now.subtract(1, 'year').startOf('day').toDate(),
      to: now.endOf('day').toDate(),
    })
  }

  return ranges
})

function normalizeRange(range: AppDateRange): AppDateRange {
  if (!props.maxRangeDays) return range

  const from = dayjs(range.from).startOf('day')
  const to = dayjs(range.to).endOf('day')
  const maxTo = from.add(props.maxRangeDays, 'day').endOf('day')

  if (to.isAfter(maxTo)) {
    let finalTo = maxTo
    if (props.visibleRange) {
      const visibleTo = dayjs(props.visibleRange.to).endOf('day')
      if (finalTo.isAfter(visibleTo)) {
        finalTo = visibleTo
      }
    }
    return {
      from: from.toDate(),
      to: finalTo.toDate(),
    }
  }

  return range
}

function handleSelect(range: any) {
  if (!range?.start) return

  if (!range.end) {
    internalRange.value = {
      start: range.start,
      end: undefined,
    }
    return
  }

  const newRange: AppDateRange = {
    from: dayjs(toDate(range.start)).startOf('day').toDate(),
    to: dayjs(toDate(range.end)).endOf('day').toDate(),
  }

  const normalized = normalizeRange(newRange)
  
  internalRange.value = {
    start: toCalendarDate(normalized.from),
    end: toCalendarDate(normalized.to),
  }
  
  emit('update:modelValue', normalized)
}

function selectQuickRange(range: { from: Date; to: Date }) {
  const normalized = normalizeRange(range)
  placeholder.value = toCalendarDate(normalized.from)
  internalRange.value = {
    start: toCalendarDate(normalized.from),
    end: toCalendarDate(normalized.to),
  }
  emit('update:modelValue', normalized)
}

function reset() {
  if (props.resetRange) {
    selectQuickRange(props.resetRange)
  }
}

function movePlaceholder(unit: 'month' | 'year', value: number) {
  let nextDate = dayjs(toDate(placeholder.value))
  if (unit === 'month') {
    nextDate = nextDate.add(value, 'month')
  } else {
    nextDate = nextDate.add(value, 'year')
  }

  if (props.visibleRange) {
    const min = dayjs(props.visibleRange.from).startOf('month')
    const max = dayjs(props.visibleRange.to).endOf('month')
    if (nextDate.isBefore(min)) nextDate = min
    if (nextDate.isAfter(max)) nextDate = max
  }

  placeholder.value = toCalendarDate(nextDate.toDate())
}

const disabledDates = computed(() => {
  if (!props.visibleRange) return undefined
  return {
    before: toCalendarDate(props.visibleRange.from),
    after: toCalendarDate(props.visibleRange.to),
  }
})
</script>

<template>
  <Popover>
    <PopoverTrigger asChild>
      <button
        type="button"
        :class="cn(
          'flex h-9 w-full items-center justify-between rounded-lg border border-muted-200 bg-muted/90 px-3 text-left text-sm text-muted-800 shadow-sm transition-colors hover:bg-surface-50 focus:outline-none focus:ring-1 focus:ring-primary dark:border-surface-300 dark:bg-surface-50 dark:text-muted-900 dark:hover:bg-surface-100',
          props.class
        )"
      >
        <span class="flex min-w-0 items-center gap-2">
          <CalendarDays class="h-4 w-4 shrink-0 text-primary" />
          <span class="truncate">{{ dateLabel }}</span>
        </span>
      </button>
    </PopoverTrigger>
    <PopoverContent class="w-auto max-w-[calc(100vw-2rem)] p-0 overflow-hidden" align="start">
      <div class="grid gap-0">
        <div class="border-b border-muted-100 bg-surface-50 p-2 dark:border-surface-200 dark:bg-surface-100">
          <div class="grid grid-cols-4 gap-1">
            <button
              v-for="range in quickRanges"
              :key="range.label"
              type="button"
              @click="selectQuickRange(range)"
              class="rounded-md px-2 py-1.5 text-center text-xs font-medium text-muted-600 transition-colors hover:bg-white hover:text-primary dark:hover:bg-surface-50"
            >
              {{ range.label }}
            </button>
            <button
              v-if="props.resetRange"
              type="button"
              @click="reset"
              class="inline-flex items-center justify-center gap-1 rounded-md px-2 py-1.5 text-xs font-medium text-muted-500 transition-colors hover:bg-white hover:text-muted-800 dark:hover:bg-surface-50"
            >
              <X class="h-3.5 w-3.5" />
              {{ t('common.reset', 'Reset') }}
            </button>
          </div>
        </div>
        <div class="cash-day-picker p-3">
          <div class="mb-2 flex items-center justify-between">
            <button
              type="button"
              class="inline-flex h-7 w-7 items-center justify-center rounded-md text-muted-500 hover:bg-surface-100 hover:text-muted-800"
              @click="movePlaceholder('year', -1)"
              :aria-label="t('pages.014.previousYear', 'Previous Year')"
            >
              <ChevronsLeft class="h-4 w-4" />
            </button>
            <button
              type="button"
              class="inline-flex h-7 w-7 items-center justify-center rounded-md text-muted-500 hover:bg-surface-100 hover:text-muted-800"
              @click="movePlaceholder('month', -1)"
              :aria-label="t('pages.014.previousMonth', 'Previous Month')"
            >
              <ChevronLeft class="h-4 w-4" />
            </button>
            <div class="text-xs font-semibold uppercase text-muted-500">
              {{ t('pages.014.jumpDate', 'Jump Date') }}
            </div>
            <button
              type="button"
              class="inline-flex h-7 w-7 items-center justify-center rounded-md text-muted-500 hover:bg-surface-100 hover:text-muted-800"
              @click="movePlaceholder('month', 1)"
              :aria-label="t('pages.014.nextMonth', 'Next Month')"
            >
              <ChevronRight class="h-4 w-4" />
            </button>
            <button
              type="button"
              class="inline-flex h-7 w-7 items-center justify-center rounded-md text-muted-500 hover:bg-surface-100 hover:text-muted-800"
              @click="movePlaceholder('year', 1)"
              :aria-label="t('pages.014.nextYear', 'Next Year')"
            >
              <ChevronsRight class="h-4 w-4" />
            </button>
          </div>
          
          <RangeCalendarRoot
            v-slot="{ grid, weekDays }"
            v-model="internalRange"
            v-model:placeholder="placeholder"
            @update:model-value="handleSelect"
            :disabled-dates="disabledDates"
            class="px-2"
          >
            <div class="flex flex-col gap-y-4 mt-4 sm:flex-row sm:gap-x-4 sm:gap-y-0">
              <RangeCalendarGrid v-for="month in grid" :key="month.value.toString()">
                <RangeCalendarGridHead>
                  <RangeCalendarGridRow>
                    <RangeCalendarHeadCell
                      v-for="day in weekDays" :key="day" class="mx-1"
                    >
                      {{ day }}
                    </RangeCalendarHeadCell>
                  </RangeCalendarGridRow>
                </RangeCalendarGridHead>
                <RangeCalendarGridBody>
                  <RangeCalendarGridRow v-for="(weekDates, index) in month.rows" :key="`weekDate-${index}`" class="mt-5 w-full">
                    <RangeCalendarCell
                      v-for="weekDate in weekDates"
                      :key="weekDate.toString()"
                      :date="weekDate"
                    >
                      <RangeCalendarCellTrigger
                        :day="weekDate"
                        :month="month.value"
                      />
                    </RangeCalendarCell>
                  </RangeCalendarGridRow>
                </RangeCalendarGridBody>
              </RangeCalendarGrid>
            </div>
          </RangeCalendarRoot>
        </div>
      </div>
    </PopoverContent>
  </Popover>
</template>
<style scoped>
:deep(.cash-day-picker table tbody tr td button) {
  padding: 20px 20px;
}
</style>