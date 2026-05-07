<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Dialog, DialogContent, DialogHeader } from '@/components/ui/dialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Calendar as CalendarIcon, UserPlus, Trash2, AlignLeft, CreditCard } from 'lucide-vue-next'
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
    <DialogContent class="max-w-[768px] p-0 gap-0 overflow-hidden border-border shadow-2xl bg-background text-foreground">
      
      <div class="px-6 py-5 bg-card dark:bg-muted/20 border-b border-border transition-colors">
        <div class="flex gap-4 items-start me-2">
          <CreditCard class="mt-1.5 text-muted-foreground" :size="20" />
          <div class="flex-1">
            <input 
              v-model="form.title"
              class="w-full text-xl font-bold bg-transparent border-2 border-transparent rounded-md px-1 -ml-1 focus:border-primary focus:bg-background/50 transition-all outline-none"
              @blur="saveTitle" 
              @keydown="handleTitleKeydown"
            />
            <p class="text-sm text-muted-foreground mt-1 px-1 font-medium">
              Trong danh sách <span class="underline cursor-pointer hover:text-primary transition-colors">{{ columnName ?? ' ' }}</span>
            </p>
          </div>
        </div>
      </div>

      <div class="p-6 grid grid-cols-1 md:grid-cols-[1fr_180px] gap-8 bg-background">
        
        <div class="space-y-6">
          <div class="flex flex-col gap-3">
            <Label class="text-[11px] font-bold uppercase tracking-wider text-muted-foreground ml-9">Thành viên</Label>
            <div class="flex items-center gap-2 ml-9">
              <Avatar class="h-8 w-8 ring-2 ring-background shadow-sm">
                <AvatarFallback class="text-[10px] bg-primary text-primary-foreground font-bold">JD</AvatarFallback>
              </Avatar>
              <Button variant="secondary" size="icon" class="h-8 w-8 rounded-full bg-secondary text-secondary-foreground hover:opacity-80">
                <UserPlus :size="14" />
              </Button>
            </div>
          </div>

          <div class="flex flex-col gap-3">
            <Label class="text-[11px] font-bold uppercase tracking-wider text-muted-foreground ml-9">Hạn hoàn thành</Label>
            <div class="ml-9 flex items-center">
               <span class="inline-flex items-center gap-2 px-3 py-1.5 rounded-md bg-secondary/30 text-xs font-semibold text-foreground border border-border cursor-pointer hover:bg-secondary/50 transition-colors">
                  <CalendarIcon :size="14" class="text-primary" />
                  Chưa thiết lập
               </span>
            </div>
          </div>

          <div class="flex flex-col gap-3 pt-2">
            <div class="flex items-center gap-3">
              <AlignLeft :size="20" class="text-muted-foreground" />
              <Label class="text-base font-bold">Mô tả</Label>
            </div>
            <div class="ml-8">
              <Textarea 
                v-model="form.description"
                placeholder="Thêm mô tả chi tiết hơn..."
                class="min-h-[150px] bg-card dark:bg-muted/30 border-border focus-visible:ring-2 focus-visible:ring-primary shadow-inner resize-none text-sm leading-relaxed"
                @blur="saveDescription"
              />
            </div>
          </div>
        </div>

        <div class="space-y-6">
          <div class="space-y-2">
            <Label class="text-[10px] font-bold uppercase tracking-widest text-muted-foreground block px-1">Thêm vào thẻ</Label>
            <Button variant="secondary" class="w-full justify-start gap-2 h-9 bg-secondary/50 hover:bg-secondary text-foreground font-semibold border-none">
              <UserPlus :size="14" class="text-primary" /> Thành viên
            </Button>
            <Button variant="secondary" class="w-full justify-start gap-2 h-9 bg-secondary/50 hover:bg-secondary text-foreground font-semibold border-none">
              <CalendarIcon :size="14" class="text-primary" /> Ngày hết hạn
            </Button>
          </div>

          <div class="space-y-2 pt-2">
            <Label class="text-[10px] font-bold uppercase tracking-widest text-muted-foreground block px-1">Thao tác</Label>
            <Button 
              variant="ghost" 
              class="w-full justify-start gap-2 h-9 text-red-500 hover:text-red-600 hover:bg-red-500/10 font-semibold transition-all"
            >
              <Trash2 :size="14" /> Xóa thẻ
            </Button>
          </div>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>