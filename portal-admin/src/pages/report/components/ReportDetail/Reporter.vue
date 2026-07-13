<script setup lang="ts">
import type { Report } from '@/pages/report/types/Reports'

defineProps<{ report: Report }>()
</script>

<template>
  <div class="space-y-1">
    <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
      Người báo cáo
    </p>
    <p class="text-sm font-semibold">
      {{ report.reporterName || report.reporterEmail }}
    </p>
    <p v-if="report.reporterName" class="text-xs text-muted-foreground">
      {{ report.reporterEmail }}
    </p>
  </div>
</template>