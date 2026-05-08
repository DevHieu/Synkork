<script setup lang="ts">
import { ref, watch } from 'vue'
import { Dialog, DialogContent, DialogHeader } from '@/components/ui/dialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { 
  Calendar as CalendarIcon, 
  UserPlus, 
  Trash2, 
  AlignLeft, 
  CreditCard,
  X,
  Settings2,
  Clock
} from 'lucide-vue-next'
import type { CardEvent } from '@/types/Task'

const props = defineProps<{
  open: boolean,
  card: CardEvent,
  columnName: string
}>()

const emit = defineEmits(['update:open', 'save', 'delete'])

const form = ref({ title: '', description: '' })

watch(() => props.open, (newVal) => {
  if (newVal && props.card) {
    form.value = {
      title: props.card.title || '',
      description: props.card.description || ''
    }
  }
}, { immediate: true })

const saveTitle = () => {
  if (!form.value.title.trim()) return
  emit('save', { ...props.card, title: form.value.title.trim() })
}

const saveDescription = () => {
  emit('save', { ...props.card, description: form.value.description.trim() })
}

const handleTitleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') (e.target as HTMLInputElement).blur()
}
</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="max-w-2xl p-0 overflow-hidden border-none shadow-2xl bg-background rounded-xl">
      
      <div class="flex items-center justify-between px-6 py-3 bg-muted/20 border-b border-border/50">
        <div class="flex items-center gap-2 text-muted-foreground">
          <CreditCard :size="16" />
          <span class="text-xs font-medium uppercase tracking-wider">Chi tiết thẻ</span>
        </div>
        <Button 
          variant="ghost" 
          size="sm"
          @click.stop="emit('delete', card)" 
          class="h-8 text-muted-foreground hover:text-red-500 hover:bg-red-50 transition-colors mr-5"
        >
          <Trash2 :size="14" class="mr-1" />
          <span class="text-xs">Xóa thẻ</span>
        </Button>
      </div>

      <div class="p-8 space-y-8">
        <!-- Title & Breadcrumb -->
        <div class="space-y-1">
          <input 
            v-model="form.title"
            class="w-full text-2xl font-bold bg-transparent border-none p-0 focus:ring-0 focus:outline-none placeholder:text-muted-foreground/40"
            placeholder="Tiêu đề thẻ..."
            @blur="saveTitle" 
            @keydown="handleTitleKeydown"
          />
          <div class="flex items-center gap-2 text-sm text-muted-foreground">
            <span>Trong mục</span>
            <span class="px-2 py-0.5 rounded bg-secondary text-secondary-foreground font-medium text-xs">
              {{ columnName }}
            </span>
          </div>
        </div>

        <!-- Metadata Grid (Gọn gàng trên 1 dòng) -->
        <div class="grid grid-cols-2 gap-8 py-2">
          <div class="space-y-2">
            <Label class="text-[11px] font-semibold uppercase text-muted-foreground">Người thực hiện</Label>
            <div class="flex items-center gap-3 group cursor-pointer">
              <Avatar class="h-8 w-8 ring-2 ring-offset-2 ring-transparent group-hover:ring-primary/20 transition-all">
                <AvatarFallback class="bg-primary/10 text-primary text-[10px] font-bold">JD</AvatarFallback>
              </Avatar>
              <span class="text-sm font-medium text-foreground/80 group-hover:text-primary transition-colors">John Doe</span>
              <UserPlus :size="14" class="text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
          </div>

          <div class="space-y-2">
            <Label class="text-[11px] font-semibold uppercase text-muted-foreground">Hạn chót</Label>
            <div class="flex items-center gap-2 text-sm font-medium text-foreground/80 hover:text-primary cursor-pointer group transition-colors">
              <CalendarIcon :size="16" class="text-muted-foreground group-hover:text-primary" />
              <span>Chưa thiết lập</span>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="space-y-3 pt-4 border-t border-border/50">
          <div class="flex items-center gap-2 text-foreground/70">
            <AlignLeft :size="18" />
            <span class="text-sm font-semibold">Mô tả</span>
          </div>
          <Textarea 
            v-model="form.description"
            placeholder="Nội dung chi tiết..."
            class="min-h-[180px] w-full text-base bg-transparent border-none focus-visible:ring-0 p-0 resize-none leading-relaxed placeholder:text-muted-foreground/30 shadow-none"
            @blur="saveDescription"
          />
        </div>
      </div>

      <!-- Footer Info -->
      <div class="px-8 py-4 bg-muted/5 flex justify-between items-center border-t border-border/30">
        <p class="text-[10px] text-muted-foreground italic">
          * Tự động lưu khi bạn hoàn tất chỉnh sửa
        </p>
        <div class="flex gap-2">
           <!-- Có thể thêm các tag nhỏ ở đây nếu cần -->
        </div>
      </div>

    </DialogContent>
  </Dialog>
</template>