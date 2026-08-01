<script setup>
import { ref, watch } from 'vue'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

const props = defineProps({
  open: Boolean,
  columnId: String,
  columnData: Object
})

const emit = defineEmits(['update:open', 'save'])

const form = ref({
  title: ''
})


watch(() => props.open, (newVal) => {
  if (newVal) {
    if (props.columnData) {
      form.value.title = props.columnData.name || ''
    } else {
      form.value.title = ''
    }
  }
})

const closeDialog = () => emit('update:open', false)

const handleSave = () => {
  if (!form.value.title.trim()) return

  emit('save', {
    columnId: props.columnId,
    title: form.value.title.trim()
  })
  closeDialog()
}

</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>{{ columnData ? 'Chỉnh sửa cột' : 'Thêm cột mới' }}
        </DialogTitle>
        <DialogDescription>
          <!-- {{ columnData ? 'Chỉnh sửa thông tin cột' : 'Tạo cột mới' }} -->
        </DialogDescription>
      </DialogHeader>

      <div class="grid gap-3">
        <label>
          Tên cột (Ví dụ: Đang đợi, Review...)
        </label>
        <Input v-model="form.title" placeholder="Nhập tên cột..." @keyup.enter="handleSave" maxlength="100"/>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="closeDialog">Hủy</Button>
        <Button @click="handleSave" :disabled="!form.title.trim()">
          {{ columnData ? 'Cập nhật' : 'Tạo cột' }} 
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>