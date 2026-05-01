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
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'

const props = defineProps({
  open: Boolean,
  columnId: String,
  taskData: Object
})

const emit = defineEmits(['update:open', 'save'])

const form = ref({ title: '', description: '' })

watch(() => props.open, (newVal) => {
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

const closeDialog = () => emit('update:open', false)

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
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-md ">
      <DialogHeader>
        <DialogTitle class="mb-5">
          {{ taskData ? 'Chỉnh sửa thẻ' : 'Thêm thẻ mới' }}
        </DialogTitle>
      </DialogHeader>

      <div class="grid gap-6">
        <div class="flex flex-col">
          <label class="mb-3">Tiêu đề</label>
          <Input v-model="form.title" placeholder="Nhập tiêu đề ..." @keyup.enter="handleSave" />
        </div>

        <div class="flex flex-col py-3">
          <label class="mb-3">Mô tả</label>
          <Textarea v-model="form.description" class="min-h-[120px] resize-none"
            placeholder="Mô tả chi tiết..."></Textarea>
        </div>
      </div>

      <DialogFooter class="gap-2">
        <Button type="button" variant="outline" @click="closeDialog">
          Hủy
        </Button>
        <Button @click="handleSave" :disabled="isSaving || !form.title.trim()">
          {{ isSaving ? 'Đang lưu...' : taskData ? 'Cập nhật' : 'Tạo thẻ' }} 
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>