<script setup lang="ts">
import { AlertTriangle } from '@lucide/vue'
import { computed } from 'vue'

import type { Report } from '@/pages/report/types/Reports'

import { Badge } from '@/components/ui/badge'
import { SEVERITY_CONFIG } from '../../utils/report.utils.ts'
import { STATUS_CONFIG, TYPE_CONFIG } from '../../utils/report.utils.ts'

const props = defineProps<{ report: Report }>()

const statusInfo = computed(() => STATUS_CONFIG[props.report.status])
const typeInfo = computed(() => TYPE_CONFIG[props.report.reportType])
const severityInfo = computed(() => SEVERITY_CONFIG[props.report.severity])
</script>

<template>
  <div class="flex items-center gap-3 flex-wrap">
    <Badge variant="outline" class="gap-1.5">
      <component :is="typeInfo.icon" class="h-3.5 w-3.5" />
      {{ typeInfo.label }}
    </Badge>
    <Badge :variant="statusInfo.variant" class="gap-1.5">
      <component :is="statusInfo.icon" class="h-3.5 w-3.5" />
      {{ statusInfo.label }}
    </Badge>
    <Badge variant="outline" class="gap-1.5" :class="severityInfo?.class">
      <AlertTriangle class="h-3.5 w-3.5" />
      {{ severityInfo?.label ?? report.severity }}
    </Badge>
  </div>
</template>