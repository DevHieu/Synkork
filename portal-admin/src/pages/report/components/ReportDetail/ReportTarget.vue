<script setup lang="ts">
import { AlertTriangle, Loader2, Lock, Unlock } from '@lucide/vue'

import type { Report } from '@/pages/report/types/Reports'

import { Badge } from '@/components/ui/badge'

defineProps<{
  report: Report
  checkingTarget: boolean
  targetCheckError: boolean
  isTargetLocked: boolean
  targetWarning: number
  hasWarned: boolean
}>()
</script>

<template>
  <div class="space-y-1">
    <div class="flex items-center justify-between">
      <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
        {{ report.reportType === 'USER' ? 'Người dùng bị báo cáo' : 'Phòng bị báo cáo' }}
      </p>

      <div class="flex items-center gap-1.5 text-xs">
        <Loader2 v-if="checkingTarget" class="h-3.5 w-3.5 animate-spin text-muted-foreground" />
        <template v-else-if="!targetCheckError">
          <Badge v-if="isTargetLocked" variant="destructive" class="gap-1 text-[11px] py-0.5">
            <Lock class="h-3 w-3" />
            Đã khoá
          </Badge>
          <Badge v-else variant="outline" class="gap-1 text-[11px] py-0.5 text-emerald-600 border-emerald-300">
            <Unlock class="h-3 w-3" />
            Đang hoạt động
          </Badge>
        </template>
        <span v-else class="text-muted-foreground italic text-[11px]">Không thể kiểm tra</span>
      </div>
    </div>

    <p class="text-sm font-semibold">
      {{ report.targetName }}
    </p>
    <p v-if="report.targetEmail" class="text-xs text-muted-foreground">
      {{ report.targetEmail }}
    </p>

    <div v-if="!checkingTarget && !targetCheckError" class="flex items-center gap-1.5 mt-1">
      <AlertTriangle class="h-3.5 w-3.5 text-amber-500" />
      <span class="text-xs text-amber-600 font-medium">
        Số lần cảnh cáo: {{ targetWarning }}
      </span>
      <span v-if="hasWarned" class="text-[11px] text-emerald-600 font-medium ml-1">(+1 vừa cảnh cáo)</span>
    </div>
  </div>
</template>