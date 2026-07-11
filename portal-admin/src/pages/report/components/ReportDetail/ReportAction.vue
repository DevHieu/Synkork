<script setup lang="ts">
import { AlertTriangle, CheckCircle2, Eye, Loader2, Lock, ShieldCheck, ShieldX, XCircle } from '@lucide/vue'
import { computed } from 'vue'

import type { Report } from '@/pages/report/types/Reports'

import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'

const props = defineProps<{
  report: Report
  targetWarning: number
  checkingTarget: boolean
  targetCheckError: boolean
  warnLoading: boolean
  lockLoading: boolean
  isTargetLocked: boolean
  hasWarned: boolean
  canResolve: boolean
}>()

const emit = defineEmits<{
  (e: 'warn'): void
  (e: 'lock'): void
  (e: 'resolve'): void
  (e: 'dismiss'): void
  (e: 'review'): void
}>()

const targetLabel = computed(() => (props.report.reportType === 'USER' ? 'Tài khoản người dùng' : 'Phòng'))
const lockButtonLabel = computed(() => (props.report.reportType === 'USER' ? 'Khoá người dùng' : 'Khoá phòng'))
</script>

<template>
  <div class="flex flex-col gap-3 px-4 pb-4">
    <p class="text-sm font-medium text-muted-foreground uppercase">
      Hành động quản trị
    </p>

    <!-- Cảnh cáo -->
    <div class="flex items-center justify-between gap-3 rounded-lg border px-3 py-2.5 border-amber-300 bg-amber-50 dark:bg-amber-950/20">
      <div class="flex items-center gap-2.5 min-w-0">
        <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-amber-100 text-amber-600 dark:bg-amber-900/40">
          <AlertTriangle class="h-4 w-4" />
        </div>
        <div class="min-w-0">
          <p class="text-sm font-medium leading-tight">
            Cảnh cáo
          </p>
          <p class="text-xs text-muted-foreground leading-tight">
            <span v-if="checkingTarget">Đang kiểm tra…</span>
            <span v-else>Tổng số lần đã cảnh cáo: <strong>{{ targetWarning }}</strong></span>
          </p>
        </div>
      </div>

      <Button
        variant="outline"
        size="sm"
        class="gap-1.5 shrink-0 border-amber-400 text-amber-700 hover:bg-amber-100"
        :disabled="checkingTarget || warnLoading || isTargetLocked || hasWarned"
        @click="emit('warn')"
      >
        <Loader2 v-if="warnLoading" class="h-3.5 w-3.5 animate-spin" />
        <AlertTriangle v-else class="h-3.5 w-3.5" />
        Cảnh cáo
      </Button>
    </div>

    <!-- Khoá đối tượng -->
    <div
      class="flex items-center justify-between gap-3 rounded-lg border px-3 py-2.5"
      :class="isTargetLocked ? 'border-destructive/30 bg-destructive/5' : 'border-border bg-muted/30'"
    >
      <div class="flex items-center gap-2.5 min-w-0">
        <div
          class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full"
          :class="isTargetLocked ? 'bg-destructive/10 text-destructive' : 'bg-muted text-muted-foreground'"
        >
          <component :is="isTargetLocked ? ShieldX : ShieldCheck" class="h-4 w-4" />
        </div>
        <div class="min-w-0">
          <p class="text-sm font-medium leading-tight">
            {{ targetLabel }}
          </p>
          <p class="text-xs text-muted-foreground leading-tight truncate">
            <span v-if="checkingTarget">Đang kiểm tra trạng thái…</span>
            <span v-else-if="targetCheckError">Không thể kiểm tra trạng thái</span>
            <span v-else-if="isTargetLocked">Đối tượng đã bị khoá</span>
            <span v-else>Đối tượng đang hoạt động bình thường</span>
          </p>
        </div>
      </div>

      <Button
        v-if="!isTargetLocked" variant="outline" size="sm"
        class="gap-1.5 shrink-0 border-destructive text-destructive hover:bg-destructive/10"
        :disabled="checkingTarget || lockLoading" @click="emit('lock')"
      >
        <Loader2 v-if="lockLoading" class="h-3.5 w-3.5 animate-spin" />
        <Lock v-else class="h-3.5 w-3.5" />
        {{ lockButtonLabel }}
      </Button>
      <Badge v-else variant="destructive" class="gap-1 shrink-0">
        <Lock class="h-3 w-3" />
        Đã khoá
      </Badge>
    </div>

    <!-- Nút hành động chính -->
    <div class="flex gap-2">
      <Button
        variant="default"
        class="flex-1 gap-2"
        :disabled="!canResolve"
        :title="!canResolve ? 'Vui lòng cảnh cáo hoặc khoá đối tượng trước khi giải quyết' : ''"
        @click="emit('resolve')"
      >
        <CheckCircle2 class="h-4 w-4" />
        Giải quyết
      </Button>

      <Button variant="destructive" class="flex-1 gap-2" :disabled="isTargetLocked" @click="emit('dismiss')">
        <XCircle class="h-4 w-4" />
        Bác bỏ
      </Button>

      <Button
        v-if="report.status === 'PENDING'" variant="outline" class="flex-1 gap-2"
        @click="emit('review')"
      >
        <Eye class="h-4 w-4" />
        Đánh dấu đã xem
      </Button>
    </div>

    <p v-if="!canResolve" class="text-[11px] text-amber-600 text-center">
      ⚠ Cần cảnh cáo hoặc khoá đối tượng trước khi có thể giải quyết báo cáo
    </p>
  </div>
</template>