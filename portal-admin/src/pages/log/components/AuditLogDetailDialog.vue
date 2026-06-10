<script lang="ts" setup>
import { FileText } from '@lucide/vue'
import { computed } from 'vue'

import { Button as UiButton } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { formatTimestamp } from '@/utils/date.utils'

import type { AuditLogDetail } from '../types/LogTypes'

import { logService } from '../service/logService'

const props = defineProps<{
  logId: string
  open: boolean
}>()

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

const isOpen = computed({
  get: () => props.open,
  set: val => emit('update:open', val),
})

const log = ref<AuditLogDetail | null>(null)
const isLoading = ref(false)

const formattedMetadata = computed(() => {
  if (!log.value?.metadata)
    return null
  try {
    return JSON.stringify(JSON.parse(log.value?.metadata), null, 2)
  }
  catch {
    return log.value?.metadata
  }
})

watch(() => props.logId, async (newId) => {
  if (!newId)
    return
  if (!newId)
    return
  isLoading.value = true
  log.value = await logService.getLogDetails(newId)
  isLoading.value = false
}, { immediate: true })
</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogContent class="max-w-[560px] gap-0 p-0 overflow-hidden">
      <DialogHeader class="flex flex-row items-start justify-between gap-3 px-6 py-5 border-b border-border">
        <div class="flex items-center gap-3">
          <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/10">
            <FileText class="h-4.5 w-4.5 text-primary" />
          </div>
          <div>
            <DialogTitle class="text-[15px] font-semibold leading-tight">
              Chi tiết nhật ký
            </DialogTitle>
            <p v-if="log" class="mt-0.5 font-mono text-[11px] text-muted-foreground">
              ID: {{ log.id }}
            </p>
          </div>
        </div>
      </DialogHeader>

      <div v-if="isLoading" class="flex flex-col gap-4 px-6 py-5">
        <div class="grid grid-cols-2 gap-2.5">
          <div v-for="i in 4" :key="i" class="h-14 animate-pulse rounded-lg bg-muted" />
        </div>
        <div class="h-px bg-border" />
        <div class="grid grid-cols-2 gap-2.5">
          <div v-for="i in 4" :key="i" class="h-14 animate-pulse rounded-lg bg-muted" />
        </div>
      </div>

      <template v-else-if="log">
        <div class="flex flex-col gap-5 overflow-y-auto px-6 py-5 max-h-[70vh]">
          <!-- Status & Entity Type badges -->
          <div class="flex items-center gap-2">
            <span
              v-if="log.entityType"
              class="inline-flex items-center rounded-full border border-primary/20 bg-primary/10 px-2.5 py-0.5 text-[11px] font-semibold text-primary"
            >
              {{ log.entityType }}
            </span>
          </div>

          <!-- Thông tin hành động -->
          <div>
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Thông tin hành động
            </p>
            <div class="grid grid-cols-2 gap-2.5">
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Hành động
                </p>
                <p class="text-[13px] font-medium text-foreground break-all">
                  {{ log.action }}
                </p>
              </div>
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Thời gian
                </p>
                <p class="text-[13px] font-medium text-foreground">
                  {{ formatTimestamp(log.createdAt) }}
                </p>
              </div>
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Người thực hiện
                </p>
                <p class="text-[13px] font-medium text-foreground break-all">
                  {{ log.actorEmail ?? '—' }}
                </p>
              </div>
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Actor ID
                </p>
                <p class="font-mono text-[13px] font-medium text-foreground">
                  {{ log.actorId ?? '—' }}
                </p>
              </div>
            </div>
          </div>

          <div class="border-t border-border" />

          <!-- Đối tượng liên quan -->
          <div>
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Đối tượng liên quan
            </p>
            <div class="grid grid-cols-2 gap-2.5">
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Loại đối tượng
                </p>
                <p class="text-[13px] font-medium text-foreground">
                  {{ log.entityType ?? '—' }}
                </p>
              </div>
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Tên đối tượng
                </p>
                <p class="text-[13px] font-medium text-foreground break-all">
                  {{ log.entityName ?? '—' }}
                </p>
              </div>
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Entity ID
                </p>
                <p class="font-mono text-[13px] font-medium text-foreground break-all">
                  {{ log.entityId ?? '—' }}
                </p>
              </div>
              <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5">
                <p class="mb-1 text-[11px] text-muted-foreground">
                  Workspace ID
                </p>
                <p class="font-mono text-[13px] font-medium text-foreground">
                  {{ log.workspaceId ?? '—' }}
                </p>
              </div>
            </div>
          </div>

          <!-- Mô tả -->
          <div v-if="log.description">
            <div class="border-t border-border mb-5" />
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Mô tả
            </p>
            <div class="rounded-lg border border-border bg-muted/40 px-3 py-2.5 text-[13px] leading-relaxed text-foreground">
              {{ log.description }}
            </div>
          </div>

          <!-- Metadata -->
          <div v-if="formattedMetadata">
            <p class="mb-2.5 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
              Metadata
            </p>
            <pre class="max-h-[160px] overflow-y-auto rounded-lg border border-border bg-muted/40 px-3 py-2.5 font-mono text-[12px] leading-relaxed text-emerald-500 dark:text-emerald-400 whitespace-pre-wrap break-all">{{ formattedMetadata }}</pre>
          </div>
        </div>

        <div class="flex justify-end border-t border-border px-6 py-4">
          <UiButton variant="outline" size="sm" @click="isOpen = false">
            Đóng
          </UiButton>
        </div>
      </template>
    </DialogContent>
  </Dialog>
</template>
