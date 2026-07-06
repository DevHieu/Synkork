<script setup lang="ts">
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  Eye,
  Home,
  Loader2,
  Lock,
  ShieldAlert,
  ShieldCheck,
  ShieldX,
  Unlock,
  User,
  XCircle,
} from '@lucide/vue'
import { computed, ref, watch } from 'vue'

import type { Report, ReportStatus } from '@/pages/report/types/Reports'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Separator } from '@/components/ui/separator'
import { roomService } from '@/pages/rooms/service/roomService'
import { userService } from '@/pages/users/services/userService'

import { REASON_LABEL_MAP, SEVERITY_CONFIG } from '../utils/report.utils.ts'
import DismissReason from './DismissReason.vue'

const props = defineProps<{
  report: Report
  open: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'action', payload: { id: string, status: ReportStatus, note?: string }): void
  (e: 'locked', payload: { reportType: 'USER' | 'ROOM', targetId: string }): void
}>()

const isOpen = computed({
  get: () => props.open,
  set: val => emit('update:open', val),
})

const STATUS_CONFIG = {
  PENDING: { label: 'Chờ xử lý', variant: 'secondary' as const, icon: Clock },
  REVIEWED: { label: 'Đang xem xét', variant: 'outline' as const, icon: Eye },
  RESOLVED: { label: 'Đã giải quyết', variant: 'default' as const, icon: CheckCircle2 },
  DISMISSED: { label: 'Đã bác bỏ', variant: 'destructive' as const, icon: XCircle },
}

const TYPE_CONFIG = {
  USER: { label: 'Báo cáo người dùng', icon: User },
  ROOM: { label: 'Báo cáo phòng', icon: Home },
}

const statusInfo = computed(() => STATUS_CONFIG[props.report.status])
const typeInfo = computed(() => TYPE_CONFIG[props.report.reportType])

const targetId = computed(() =>
  props.report.reportType === 'USER'
    ? props.report.targetUserId
    : props.report.targetRoomId,
)

const isUserReport = computed(() => props.report.reportType === 'USER')
const LOCKED_STATUS = { USER: 'BANNED', ROOM: 'LOCKED' } as const

const targetStatus = ref<string | null>(null)
const targetWarning = ref<number>(0)
const checkingTarget = ref(false)
const targetCheckError = ref(false)
const lockLoading = ref(false)
const warnLoading = ref(false)
const warnedReportIds = ref<Set<string>>(new Set())
const hasWarned = computed(() => warnedReportIds.value.has(props.report.id))
const reasonLabel = computed(() => REASON_LABEL_MAP[props.report.reason] ?? props.report.reason)
const severityInfo = computed(() => SEVERITY_CONFIG[props.report.severity])

const isTargetLocked = computed(() =>
  !!targetStatus.value
  && targetStatus.value.toUpperCase() === LOCKED_STATUS[props.report.reportType],
)

const canResolve = computed(() => hasWarned.value || isTargetLocked.value)

async function checkTargetStatus() {
  if (!targetId.value)
    return

  checkingTarget.value = true
  targetCheckError.value = false

  try {
    const data = isUserReport.value
      ? await userService.getById(targetId.value)
      : await roomService.getRoomDetail(targetId.value)

    targetStatus.value = data.status || data.data?.status || null
    targetWarning.value = data.warning || data.data?.warning || 0
  }
  catch (error) {
    targetCheckError.value = true
  }
  finally {
    checkingTarget.value = false
  }
}

watch(
  () => [props.open, props.report.id] as const,
  ([open]) => {
    if (open)
      checkTargetStatus()
  },
  { immediate: true },
)

function handleAction(status: ReportStatus) {
  emit('action', { id: props.report.id, status })
}

async function handleLockTarget() {
  if (!targetId.value)
    return

  const confirmMsg = isUserReport.value
    ? 'Bạn có chắc muốn khoá người dùng này?'
    : 'Bạn có chắc muốn khoá phòng này?'
  if (!confirm(confirmMsg))
    return

  lockLoading.value = true
  try {
    if (isUserReport.value) {
      await userService.updateStatus(targetId.value, 'BANNED')
    }
    else {
      await roomService.lockRoom(targetId.value, 'LOCKED')
    }
    targetStatus.value = LOCKED_STATUS[props.report.reportType]
    emit('locked', { reportType: props.report.reportType, targetId: targetId.value })
  }
  catch (error) {
    console.error('Lỗi khoá đối tượng:', error)
  }
  finally {
    lockLoading.value = false
  }
}

async function handleWarnTarget() {
  if (!targetId.value)
    return

  const confirmMsg = isUserReport.value
    ? 'Bạn có chắc muốn gửi cảnh cáo đến người dùng này?'
    : 'Bạn có chắc muốn khoá phòng này?'
  if (!confirm(confirmMsg))
    return

  warnLoading.value = true
  try {
    if (isUserReport.value) {
      await userService.warnUser(targetId.value)
    }
    else {
      await roomService.warnRoom(targetId.value)
    }
    targetWarning.value += 1
    warnedReportIds.value = new Set(warnedReportIds.value).add(props.report.id)
  }
  catch (error) {
    console.error('Lỗi gửi cảnh cáo:', error)
  }
  finally {
    warnLoading.value = false
  }
}

const isDismissDialogOpen = ref(false)

function handleDismiss(reason: string) {
  emit('action', { id: props.report.id, status: 'DISMISSED', note: reason })
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogContent class="sm:max-w-lg">
      <DialogHeader>
        <div class="flex items-center gap-2">
          <ShieldAlert class="h-5 w-5 text-destructive" />
          <DialogTitle>Chi tiết báo cáo</DialogTitle>
        </div>
        <DialogDescription class="text-xs text-muted-foreground">
          Xem xét và xử lý báo cáo vi phạm
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 pt-2">
        <!-- Loại & Trạng thái -->
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

        <Separator />

        <!-- Đối tượng bị báo cáo -->
        <div class="space-y-1">
          <div class="flex items-center justify-between">
            <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
              {{ report.reportType === 'USER' ? 'Người dùng bị báo cáo' : 'Phòng bị báo cáo' }}
            </p>

            <!-- Trạng thái khoá -->
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

          <!-- Tên / email — không hiển thị ID -->
          <p class="text-sm font-semibold">
            {{ report.targetName }}
          </p>
          <p v-if="report.targetEmail" class="text-xs text-muted-foreground">
            {{ report.targetEmail }}
          </p>

          <!-- Số lần cảnh cáo -->
          <div v-if="!checkingTarget && !targetCheckError" class="flex items-center gap-1.5 mt-1">
            <AlertTriangle class="h-3.5 w-3.5 text-amber-500" />
            <span class="text-xs text-amber-600 font-medium">
              Số lần cảnh cáo: {{ targetWarning }}
            </span>
            <span v-if="hasWarned" class="text-[11px] text-emerald-600 font-medium ml-1">(+1 vừa cảnh cáo)</span>
          </div>
        </div>

        <Separator />

        <!-- Người báo cáo -->
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

        <Separator />

        <!-- Lý do & Mô tả -->
        <div class="space-y-3">
          <div class="space-y-1">
            <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
              Lý do
            </p>
            <p class="text-sm font-semibold">
              {{ reasonLabel }}
            </p>
          </div>

          <div v-if="report.description" class="space-y-1">
            <p class="text-xs font-medium text-muted-foreground uppercase tracking-wide">
              Mô tả
            </p>
            <p class="text-sm text-foreground leading-relaxed rounded-md bg-muted px-3 py-2">
              {{ report.description }}
            </p>
          </div>
        </div>

        <Separator />

        <!-- Thời gian -->
        <div class="flex gap-6 text-xs text-muted-foreground">
          <div>
            <span class="font-medium">Ngày tạo:</span> {{ formatDate(report.createdAt) }}
          </div>
        </div>
      </div>

      <template v-if="report.status === 'PENDING' || report.status === 'REVIEWED'">
        <Separator class="my-4" />
        <div class="flex flex-col gap-3">
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
              @click="handleWarnTarget"
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
                  {{ report.reportType === 'USER' ? 'Tài khoản người dùng' : 'Phòng' }}
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
              :disabled="checkingTarget || lockLoading" @click="handleLockTarget"
            >
              <Loader2 v-if="lockLoading" class="h-3.5 w-3.5 animate-spin" />
              <Lock v-else class="h-3.5 w-3.5" />
              {{ report.reportType === 'USER' ? 'Khoá người dùng' : 'Khoá phòng' }}
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
              @click="handleAction('RESOLVED')"
            >
              <CheckCircle2 class="h-4 w-4" />
              Giải quyết
            </Button>

            <Button variant="destructive" class="flex-1 gap-2" :disabled="isTargetLocked" @click="isDismissDialogOpen = true">
              <XCircle class="h-4 w-4" />
              Bác bỏ
            </Button>

            <Button
              v-if="report.status === 'PENDING'" variant="outline" class="flex-1 gap-2"
              @click="handleAction('REVIEWED')"
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

      <div v-else class="mt-4 p-3 bg-muted rounded-lg border border-dashed text-center">
        <p class="text-xs text-muted-foreground italic">
          Báo cáo này đã được xử lý: <span class="font-bold">{{ statusInfo.label }}</span>
        </p>
      </div>
    </DialogContent>
  </Dialog>
  <DismissReason v-model:open="isDismissDialogOpen" :report="report" @confirm="handleDismiss" />
</template>
