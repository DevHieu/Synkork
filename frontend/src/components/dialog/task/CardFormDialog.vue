<script setup>
import { ref, watch, nextTick } from 'vue'

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog'

import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'

const props = defineProps({
  open: Boolean,
  columnId: String,
  taskData: Object
})

const emit = defineEmits(['update:open', 'save'])

const form = ref({ title: '', description: '' })


const isOpen = ref(props.open)

watch(() => props.open, (val) => {
  isOpen.value = val
})

watch(isOpen, (val) => {
  emit('update:open', val)
})

// Reset form khi mở dialog
watch(() => props.open, (newVal) => {
  isOpen.value = newVal

  if (newVal) {
    if (props.taskData) {
      form.value = {
        title: props.taskData.title || '',
        description: props.taskData.description || ''
      }
    } else {
      form.value = { title: '', description: '' }
    }
  }
})

const closeDialog = () => {
  isOpen.value = false
}

const handleSave = () => {
  if (!form.value.title?.trim()) return

  emit('save', {
    title: form.value.title.trim(),
    description: form.value.description.trim() || ''
  })

  closeDialog()
}
</script>

<template>
  <Dialog :open="isOpen" @update:open="isOpen = $event">
    <DialogContent class="sm:max-w-[425px] bg-white border border-slate-200 shadow-2xl rounded-2xl [&>button]:hidden">
      <DialogHeader>
        <DialogTitle class="text-xl font-semibold text-slate-800">
          {{ props.taskData ? 'Chỉnh sửa công việc' : 'Thêm task mới' }}
        </DialogTitle>
      </DialogHeader>

      <div class="grid gap-6">
        <div class="py-3">
          <label class="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 mb-2 block">Tiêu đề nhiệm vụ</label>
          <Input v-model="form.title" placeholder="Nhập tiêu đề task..." class="h-12 rounded-xl border-slate-50 text-sm focus-visible:ring-orange-400 bg-slate-50/50" @keyup.enter="handleSave" />
        </div>

        <div class="py-3">
          <label class="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 mb-2 block">Mô tả</label>
          <textarea v-model="form.description" rows="4"
            class="w-full rounded-xl border border-slate-50 bg-slate-50/50 p-3 text-sm focus:outline-none focus:border-orange-400 focus-visible:ring-2 focus-visible:ring-orange-400 placeholder:text-slate-400 resize-y min-h-[120px]"
            placeholder="Mô tả chi tiết công việc..."></textarea>
        </div>
      </div>

      <DialogFooter class="gap-2 sm:justify-end">
        <Button type="button" variant="ghost" @click="closeDialog" class="rounded-xl hover:bg-slate-100 text-slate-500">
          Hủy
        </Button>
        <Button @click="handleSave" :disabled="!form.title.trim()"
          class="bg-orange-500 hover:bg-orange-600 text-white rounded-xl px-6 shadow-md shadow-orange-200 transition-all active:scale-95 disabled:opacity-50">
          {{ props.taskData ? 'Cập nhật' : 'Tạo task' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>