<script setup lang="ts">
import { ShieldAlert } from '@lucide/vue'
import { computed, ref, watch } from 'vue'

import type { Report, ReportStatus, UpdateReportStatusPayload } from '@/pages/report/types/Reports'

import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Separator } from '@/components/ui/separator'
import { roomService } from '@/pages/rooms/service/roomService'
import { userService } from '@/pages/users/services/userService'

import { LOCKED_STATUS, REASON_LABEL_MAP, STATUS_CONFIG } from '../utils/report.utils.ts'

import DismissReason from '../components/ReportDetail/DismissReason.vue'
import ReportActions from '../components/ReportDetail/ReportAction.vue'
import ReportBadges from '../components/ReportDetail/ReportBadges.vue'
import ReportReason from '../components/ReportDetail/ReportReason.vue'
import ReportReporter from '../components/ReportDetail/Reporter.vue'
import ReportTarget from '../components/ReportDetail/ReportTarget.vue'
import ReportEvidence from '../components/ReportDetail/ReportEvidence.vue'

const props = defineProps<{
  report: Report
  open: boolean
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'action', payload: UpdateReportStatusPayload): void
  (e: 'locked', payload: { reportType: 'USER' | 'ROOM', targetId: string }): void
}>()

const isOpen = computed({
  get: () => props.open,
  set: val => emit('update:open', val),
})

const statusLabel = computed(() => STATUS_CONFIG[props.report.status]?.label ?? props.report.status)

const targetId = computed(() =>
  props.report.reportType === 'USER'
    ? props.report.targetUserId
    : props.report.targetRoomId,
)

const isUserReport = computed(() => props.report.reportType === 'USER')

const targetStatus = ref<string | null>(null)
const targetWarning = ref<number>(0)
const checkingTarget = ref(false)
const targetCheckError = ref(false)
const lockLoading = ref(false)
const warnLoading = ref(false)
const hasWarned = computed(() => !!props.report.hasWarn)
const reasonLabel = computed(() => REASON_LABEL_MAP[props.report.reason] ?? props.report.reason)

const isTargetLocked = computed(() =>
  !!targetStatus.value
  && targetStatus.value.toUpperCase() === LOCKED_STATUS[props.report.reportType],
)

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
      await roomService.changeRoomStatus(targetId.value, 'LOCKED')
    }
    targetStatus.value = LOCKED_STATUS[props.report.reportType]
    emit('locked', { reportType: props.report.reportType, targetId: targetId.value })
    emit('action', { id: props.report.id, status: 'RESOLVED' })
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
    : 'Bạn có chắc muốn gửi cảnh cáo đến phòng này?'
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

    emit('action', { id: props.report.id, status: 'RESOLVED', hasWarn: true })
  }
  catch (error) {
    console.error('Lỗi gửi cảnh cáo:', error)
  }
  finally {
    warnLoading.value = false
  }
}

const isDismissDialogOpen = ref(false)
const isImageZoomed = ref(false)

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

watch(
  () => [props.open, props.report.id] as const,
  ([open]) => {
    isImageZoomed.value = false
    if (open)
      checkTargetStatus()
  },
  { immediate: true },
)

</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogContent class="sm:max-w-lg max-h-[90vh] p-0 overflow-y-auto">
      <DialogHeader class="flex flex-col gap-1.5 px-4 pt-4">
        <div class="flex items-center gap-2">
          <ShieldAlert class="h-5 w-5 text-destructive" />
          <DialogTitle>Chi tiết báo cáo</DialogTitle>
        </div>
        <DialogDescription class="text-xs text-muted-foreground">
          Xem xét và xử lý báo cáo vi phạm
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 pt-2 px-4 pb-4">
        <ReportBadges :report="report" />

        <Separator />

        <ReportTarget
          :report="report"
          :checking-target="checkingTarget"
          :target-check-error="targetCheckError"
          :is-target-locked="isTargetLocked"
          :target-warning="targetWarning"
          :has-warned="hasWarned"
        />

        <Separator />

        <ReportReporter :report="report" />

        <Separator />

        <ReportReason :reason-label="reasonLabel" :description="report.description || ''" />

        <Separator />

        <div class="flex gap-6 text-xs text-muted-foreground">
          <div>
            <span class="font-medium">Ngày tạo:</span> {{ formatDate(report.createdAt) }}
          </div>
        </div>  

        <ReportEvidence
          v-if="report.evidenceUrl"
          :src="report.evidenceUrl"
          :name="report.evidenceName || ''"
          :resource-type="report.evidenceResourceType || ''"
          v-model:open="isImageZoomed"
        />
      </div>

      <template v-if="report.status === 'PENDING' || report.status === 'REVIEWED'">
        <Separator class="my-4" />
        <ReportActions
          :report="report"
          :target-warning="targetWarning"
          :checking-target="checkingTarget"
          :target-check-error="targetCheckError"
          :warn-loading="warnLoading"
          :lock-loading="lockLoading"
          :is-target-locked="isTargetLocked"
          :has-warned="hasWarned"
          @warn="handleWarnTarget"
          @lock="handleLockTarget"
          @dismiss="isDismissDialogOpen = true"
          @review="handleAction('REVIEWED')"
        />
      </template>

      <div v-else class="mt-4 p-3 bg-muted rounded-lg border border-dashed text-center">
        <p class="text-xs text-muted-foreground italic">
          Báo cáo này đã được xử lý: <span class="font-bold">{{ statusLabel }}</span>
        </p>
      </div>
    </DialogContent>
  </Dialog>
  <DismissReason v-model:open="isDismissDialogOpen" :report="report" @confirm="handleDismiss" />
</template>