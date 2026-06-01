<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { 
  ChevronLeft, 
  ChevronRight, 
  ChevronsLeft, 
  ChevronsRight 
} from '@lucide/vue'

interface Props {
  total: number
  currentPage: number
  perPage?: number
  totalCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  perPage: 20,
})

const emit = defineEmits<{
  'update:currentPage': [page: number]
  'pageChange': [page: number]
}>()

const { t, locale } = useI18n()
const jumpPage = ref(String(props.currentPage))

const safeTotal = computed(() => Math.max(1, Number(props.total) || 1))
const safeCurrentPage = computed(() => Math.min(Math.max(1, props.currentPage), safeTotal.value))

const safeTotalCount = computed(() => {
  if (typeof props.totalCount === 'number') return props.totalCount
  return Math.max(0, (safeTotal.value - 1) * props.perPage + 1)
})

const showingFrom = computed(() => safeTotalCount.value === 0 ? 0 : (safeCurrentPage.value - 1) * props.perPage + 1)
const showingTo = computed(() => safeTotalCount.value === 0 ? 0 : Math.min(safeCurrentPage.value * props.perPage, safeTotalCount.value))

const numberLocale = computed(() => locale.value === 'vi' ? 'vi-VN' : 'en-US')

watch(() => props.currentPage, (newVal) => {
  jumpPage.value = String(newVal)
})

const pageNumbers = computed(() => {
  const pages: (number | '...')[] = []
  const maxVisiblePages = 10

  if (safeTotal.value <= maxVisiblePages) {
    for (let i = 1; i <= safeTotal.value; i++) pages.push(i)
    return pages
  }

  const middleSlots = maxVisiblePages - 4
  let rangeStart = Math.max(2, safeCurrentPage.value - Math.floor(middleSlots / 2))
  let rangeEnd = Math.min(safeTotal.value - 1, rangeStart + middleSlots - 1)

  if (rangeEnd - rangeStart + 1 < middleSlots) {
    rangeStart = Math.max(2, rangeEnd - middleSlots + 1)
  }

  pages.push(1)
  if (rangeStart > 2) pages.push('...')

  for (let i = rangeStart; i <= rangeEnd; i++) {
    pages.push(i)
  }

  if (rangeEnd < safeTotal.value - 1) pages.push('...')
  pages.push(safeTotal.value)

  return pages
})

function goToPage(page: number) {
  const nextPage = Math.min(Math.max(1, page), safeTotal.value)
  if (nextPage !== props.currentPage) {
    emit('update:currentPage', nextPage)
    emit('pageChange', nextPage)
  }
}

function handleJumpSubmit(event: Event) {
  event.preventDefault()
  const nextPage = Number(jumpPage.value)
  if (!Number.isNaN(nextPage)) goToPage(nextPage)
}
</script>

<template>
  <div class="mt-4 flex flex-col gap-3 border-t border-border pt-4 lg:flex-row lg:items-center lg:justify-between">
    <div class="text-sm text-muted-foreground">
      {{ t('pagination.currentRecord', {
        from: showingFrom.toLocaleString(numberLocale),
        to: showingTo.toLocaleString(numberLocale),
        total: safeTotalCount.toLocaleString(numberLocale),
      }) }}
    </div>
    <div class="flex flex-wrap items-center justify-start gap-1 lg:justify-end">
      <UiButton
        variant="outline"
        size="sm"
        :disabled="safeCurrentPage <= 1"
        class="h-8 w-8 p-0"
        :aria-label="t('pagination.first')"
        @click="goToPage(1)"
      >
        <ChevronsLeft class="h-4 w-4" />
      </UiButton>
      <UiButton
        variant="outline"
        size="sm"
        :disabled="safeCurrentPage <= 1"
        class="h-8 w-8 p-0"
        :aria-label="t('pagination.prev')"
        @click="goToPage(safeCurrentPage - 1)"
      >
        <ChevronLeft class="h-4 w-4" />
      </UiButton>

      <template v-for="(page, idx) in pageNumbers" :key="idx">
        <span v-if="page === '...'" class="px-2 text-sm text-muted-foreground/50">
          ...
        </span>
        <UiButton
          v-else
          :variant="safeCurrentPage === page ? 'default' : 'outline'"
          size="sm"
          class="h-8 min-w-[32px] px-2"
          @click="goToPage(page as number)"
        >
          {{ page }}
        </UiButton>
      </template>

      <UiButton
        variant="outline"
        size="sm"
        :disabled="safeCurrentPage >= safeTotal"
        class="h-8 w-8 p-0"
        :aria-label="t('pagination.next')"
        @click="goToPage(safeCurrentPage + 1)"
      >
        <ChevronRight class="h-4 w-4" />
      </UiButton>
      <UiButton
        variant="outline"
        size="sm"
        :disabled="safeCurrentPage >= safeTotal"
        class="h-8 w-8 p-0"
        :aria-label="t('pagination.last')"
        @click="goToPage(safeTotal)"
      >
        <ChevronsRight class="h-4 w-4" />
      </UiButton>

      <form
        class="ml-2 flex items-center gap-2 text-sm text-muted-foreground"
        @submit="handleJumpSubmit"
      >
        <span>{{ t('pagination.goTo') }}</span>
        <UiInput
          v-model="jumpPage"
          type="number"
          :min="1"
          :max="safeTotal"
          class="h-8 w-16 px-2 text-center"
        />
      </form>
    </div>
  </div>
</template>
