<script setup lang="ts">
import { computed } from 'vue'
import { User, Home, ShieldAlert, Clock, CheckCircle2, XCircle, Eye } from '@lucide/vue'
import type { Report, ReportStatus } from '@/types/Reports.ts'

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { Button } from '@/components/ui/button'

const props = defineProps<{
  report: Report
  open: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'action', payload: { id: string, status: ReportStatus }): void
}>()

const isOpen = computed({
  get: () => props.open,
  set: (val) => emit('update:open', val),
})

const statusConfig = {
  PENDING: {
    label: 'Pending',
    variant: 'secondary' as const,
    icon: Clock,
  },
  REVIEWED: {
    label: 'Reviewed',
    variant: 'outline' as const,
    icon: Eye,
  },
  RESOLVED: {
    label: 'Resolved',
    variant: 'default' as const,
    icon: CheckCircle2,
  },
  DISMISSED: {
    label: 'Dismissed',
    variant: 'destructive' as const,
    icon: XCircle,
  },
}

const typeConfig = {
  USER: { label: 'User Report', icon: User },
  ROOM: { label: 'Room Report', icon: Home },
}

const statusInfo = computed(() => statusConfig[props.report.status])
const typeInfo   = computed(() => typeConfig[props.report.reportType])

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function handleAction(status: ReportStatus) {
  emit('action', { id: props.report.id, status })
}
</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogContent class="sm:max-w-lg">
      <DialogHeader>
        <div class="flex items-center gap-2">
          <ShieldAlert class="h-5 w-5 text-destructive" />
          <DialogTitle>Report Detail</DialogTitle>
        </div>
        <DialogDescription class="text-xs text-muted-foreground">
          ID: {{ report.id }}
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 pt-2">

        <!-- Type & Status -->
        <div class="flex items-center gap-3">
          <Badge variant="outline" class="gap-1.5">
            <component :is="typeInfo.icon" class="h-3.5 w-3.5" />
            {{ typeInfo.label }}
          </Badge>
          <Badge :variant="statusInfo.variant" class="gap-1.5">
            <component :is="statusInfo.icon" class="h-3.5 w-3.5" />
            {{ statusInfo.label }}
          </Badge>
        </div>

        <Separator />

        <!-- Target -->
        <div class="space-y-1">
          <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
            {{ report.reportType === 'USER' ? 'Reported User' : 'Reported Room' }}
          </p>
          <p class="text-sm font-semibold">
            {{ report.reportType === 'USER' ? (report.targetUserId) : (report.targetRoomId) }}
          </p>
          <p class="text-xs text-muted-foreground">
            {{ report.reportType === 'USER' ? report.targetName : report.targetName }}
          </p>
        </div>

        <Separator />

        <!-- Reporter -->
        <div class="space-y-1">
          <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">Reporter</p>
          <p class="text-sm font-semibold">{{ report.reporterId }}</p>
          <p class="text-xs text-muted-foreground">{{ report.reporterId }}</p>
        </div>

        <Separator />

        <!-- Reason & Description -->
        <div class="space-y-3">
          <div class="space-y-1">
            <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">Reason</p>
            <p class="text-sm font-semibold">{{ report.reason }}</p>
          </div>

          <div v-if="report.description" class="space-y-1">
            <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">Description</p>
            <p class="text-sm text-foreground leading-relaxed rounded-md bg-muted px-3 py-2">
              {{ report.description }}
            </p>
          </div>
        </div>

        <Separator />

        <!-- Timestamps -->
        <div class="flex gap-6 text-xs text-muted-foreground">
          <div>
            <span class="font-medium">Created:</span> {{ formatDate(report.createdAt) }}
          </div>
          <div>
            <span class="font-medium">Updated:</span> {{ formatDate(report.updatedAt) }}
          </div>
        </div>

      </div>
      <template v-if="report.status === 'PENDING' || report.status === 'REVIEWED'">
        <Separator class="my-4" />
        <div class="flex flex-col gap-3">
          <p class="text-sm font-medium text-muted-foreground uppercase">Admin Actions</p>
          <div class="flex gap-2">
            <!-- Nút Duyệt: Chấp nhận tố cáo -->
            <Button 
              variant="default" 
              class="flex-1 gap-2"
              @click="handleAction('RESOLVED')"
            >
              <CheckCircle2 class="h-4 w-4" />
              Resolve (Accept)
            </Button>

            <!-- Nút Bác bỏ: Tố cáo sai/không vi phạm -->
            <Button 
              variant="destructive" 
              class="flex-1 gap-2"
              @click="handleAction('DISMISSED')"
            >
              <XCircle class="h-4 w-4" />
              Dismiss (Reject)
            </Button>
            
            <!-- Nút Đánh dấu đã xem (nếu cần) -->
            <Button 
              v-if="report.status === 'PENDING'"
              variant="outline" 
              class="flex-1 gap-2"
              @click="handleAction('REVIEWED')"
            >
              <Eye class="h-4 w-4" />
              Mark Reviewed
            </Button>
          </div>
        </div>
      </template>

      <!-- Hiển thị khi đã xử lý xong -->
      <div v-else class="mt-4 p-3 bg-muted rounded-lg border border-dashed text-center">
        <p class="text-xs text-muted-foreground italic">
          This report has been finalized as <span class="font-bold">{{ report.status }}</span>
        </p>
      </div>
    </DialogContent>
  </Dialog>
</template>