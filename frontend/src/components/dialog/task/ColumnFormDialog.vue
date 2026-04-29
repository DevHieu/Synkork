<script setup>
import { ref, watch } from 'vue'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
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
    <DialogContent class="sm:max-w-[425px] bg-white border border-slate-200 shadow-2xl rounded-2xl [&>button]:hidden">
      <DialogHeader>
        <DialogTitle class="text-xl font-semibold text-slate-800">{{ columnData ? 'Chỉnh sửa cột' : 'Thêm cột mới' }}
        </DialogTitle>
      </DialogHeader>

      <div class="py-6">
        <label class="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 mb-2 block">
          Tên cột (Ví dụ: Đang đợi, Review...)
        </label>
        <Input v-model="form.title" placeholder="Nhập tên cột..." @keyup.enter="handleSave"
          class="h-12 rounded-2xl border-slate-200 focus-visible:ring-orange-400 bg-slate-50/50" maxlength="100"/>
      </div>

      <DialogFooter>
        <Button variant="ghost" @click="closeDialog" class="rounded-xl hover:bg-slate-100 text-slate-500">Hủy</Button>
        <Button @click="handleSave" :disabled="isSaving" 
          class="bg-orange-500 hover:bg-orange-600 text-white rounded-xl px-6 shadow-md shadow-orange-200 transition-all active:scale-95 disabled:opacity-50">
          {{ isSaving ? 'Đang lưu...' : columnData ? 'Cập nhật' : 'Tạo cột' }} 
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>