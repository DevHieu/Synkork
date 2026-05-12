<script setup>
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { AlertTriangle } from 'lucide-vue-next'

const props = defineProps({
  open: Boolean,
  title: {
    type: String,
    default: 'Xác nhận xóa'
  },
  description: {
    type: String,
    default: 'Hành động này không thể hoàn tác. Bạn có chắc chắn muốn xóa không?'
  }
})

const emit = defineEmits(['update:open', 'confirm'])

const closeDialog = () => {
  emit('update:open', false)
}

const handleConfirm = () => {
  emit('confirm')
  closeDialog()
}

</script>

<template>
  <Dialog :open="open" 
  @update:open="(val) => emit('update:open', val)">
    <DialogContent class="sm:max-w-md z-[9999]">
      <DialogHeader class="flex flex-col items-center gap-3">
        <div class="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center">
          <AlertTriangle class="w-6 h-6 text-red-500" />
        </div>
        <DialogTitle class="text-xl font-bold text-center">
          {{ title }}
        </DialogTitle>
        <DialogDescription class="text-center text-slate-400 text-sm leading-relaxed">
          {{ description }}
        </DialogDescription>
      </DialogHeader>

      <DialogFooter class="flex gap-2 mt-4 sm:justify-center">
        <Button 
          variant="outline" 
          @click="closeDialog" 
          class="flex-1 rounded-xl font-bold text-slate-400 hover:bg-slate-300"
        >
          Hủy bỏ
        </Button>
        <Button 
          variant="destructive"
          @click="handleConfirm" 
          class="flex-1 font-bold rounded-xl"
        >
          Xóa ngay
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>