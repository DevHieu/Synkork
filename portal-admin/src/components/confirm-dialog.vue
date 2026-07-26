<script lang='ts' setup>
import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Textarea } from '@/components/ui/textarea'

interface ConfirmDialogProps {
  isLoading?: boolean
  disabled?: boolean
  cancelButtonText?: string
  confirmButtonText?: string
  destructive?: boolean
  requireReason?: boolean
  reasonLabel?: string
  reasonPlaceholder?: string
  reasonError?: string
  closeOnConfirm?: boolean
}

const {
  isLoading = false,
  disabled = false,
  destructive = false,
  requireReason = false,
  reasonLabel = 'Lý do',
  reasonPlaceholder = 'Nhập lý do...',
  reasonError = 'Vui lòng nhập lý do',
  closeOnConfirm = true,
  cancelButtonText = 'Cancel',
  confirmButtonText = 'Continue',
} = defineProps<ConfirmDialogProps>()

const emits = defineEmits<{
  (e: 'confirm', reason: string): void
}>()

const openModel = defineModel<boolean>('open', {
  default: false,
})
const reasonModel = defineModel<string>('reason', {
  default: '',
})

function handleConfirm() {
  emits('confirm', reasonModel.value.trim())
  if (closeOnConfirm)
    openModel.value = false
}
</script>

<template>
  <AlertDialog :open="openModel">
    <AlertDialogContent>
      <AlertDialogHeader class="text-start">
        <AlertDialogTitle>
          <slot name="title" />
        </AlertDialogTitle>
        <AlertDialogDescription as-child>
          <slot name="description" />
        </AlertDialogDescription>
      </AlertDialogHeader>

      <slot />

      <div v-if="requireReason" class="space-y-2">
        <label class="text-sm font-medium">{{ reasonLabel }}</label>
        <Textarea
          v-model="reasonModel"
          class="min-h-24"
          :placeholder="reasonPlaceholder"
        />
        <p v-if="!reasonModel.trim()" class="text-[12px] text-muted-foreground">
          {{ reasonError }}
        </p>
      </div>

      <AlertDialogFooter>
        <AlertDialogCancel :disabled="isLoading" @click="openModel = false">
          {{ cancelButtonText }}
        </AlertDialogCancel>

        <UiButton
          :variant="destructive ? 'destructive' : 'default'"
          :disabled="disabled || isLoading || (requireReason && !reasonModel.trim())"
          @click="handleConfirm"
        >
          {{ confirmButtonText }}
        </UiButton>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>
