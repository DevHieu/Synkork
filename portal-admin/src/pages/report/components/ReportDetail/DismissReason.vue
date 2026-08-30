<script setup lang="ts">
import { ref, watch } from 'vue'
import { XCircle } from '@lucide/vue'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import type { Report } from '@/pages/report/types/Reports'

const props = defineProps<{
    report: Report
    open: boolean
}>()

const emit = defineEmits<{
    (e: 'update:open', value: boolean): void
    (e: 'confirm', reasin: string): void
}>()

const isOpen = ref(props.open)
const reason = ref('')
const error = ref('')

watch(
  () => props.open,
  (value) => {
    isOpen.value = value

    if (value) {
      reason.value = ''
      error.value = ''
    }
  },
)

watch(isOpen, (value) => {
  emit('update:open', value)
})

function handleConfirm() {
  if (!reason.value.trim()) {
    error.value = 'Vui lòng nhập lý do để gửi cho người báo cáo.'
    return
  }

  emit('confirm', reason.value.trim())
  isOpen.value = false
}
</script>
<template>
    <Dialog v-model:open="isOpen">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <div class="flex items-center gap-2">
          <XCircle class="h-5 w-5 text-destructive" />
          <DialogTitle>Bác bỏ báo cáo</DialogTitle>
        </div>
        <DialogDescription class="text-sm text-muted-foreground">
          Vui lòng nhập lý do bác bỏ. Lý do này sẽ được gửi tới người báo cáo
          <span class="font-medium">({{ report.reporterEmail }})</span>.
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-2 py-2">
        <Label for="dismiss-reason" class="text-xs uppercase tracking-wide text-muted-foreground">
          Lý do bác bỏ
        </Label>
        <Textarea
          v-model="reason"
          placeholder="Ví dụ: Sau khi xem xét, chúng tôi không tìm thấy vi phạm nào liên quan đến nội dung bạn đã báo cáo"
          class="min-h-[100px] text-sm"
          @input="error = ''"
        />
        <p v-if="error" class="text-xs text-destructive">{{ error }}</p>
      </div>

      <DialogFooter class="gap-2 sm:gap-2">
        <Button variant="outline" @click="isOpen = false">
          Hủy
        </Button>
        <Button variant="destructive" class="gap-2" @click="handleConfirm">
          <XCircle class="h-4 w-4" />
          Gửi & Bác bỏ
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>