<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  Dialog,
  DialogContent,
} from '@/components/ui/dialog'

import type { CardEvent } from '@/types/Task'

import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'
import { useTemplateRef } from 'vue';

const props = defineProps<{
    open: Boolean,
    card: CardEvent
}>()

const emit = defineEmits(['update:open', 'save'])

const closeDialog = () => emit('update:open', false)
const saveStatus = ref('idle')

const saveTitle = () => {
    if(!form.value.title.trim()) return

    emit('save', {
        ...form.value,
        title: form.value.title.trim()
    })
}

const saveDescription = () => {
    emit('save', {
        ...form.value,
        description: form.value.description.trim()
    })
}

const handleTitleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    (e.target as HTMLInputElement).blur() 
}
}


const form = ref({ title: '', description: '' })

watch(() => props.open, (newVal) => {
  if (newVal && props.card) {
      form.value = {
        title: props.card.title || '',
        description: props.card.description || ''
      }
    } 
}, { immediate: true})

</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent
      class="card-dialog max-w-[700px] p-0 gap-0 overflow-y-auto max-h-[90vh]"
      @pointer-down-outside.prevent
    >
      <div class="p-5">
 
        <div class="flex gap-3 items-start mb-5">
          <span class="mt-0.5 text-muted-foreground shrink-0">
            <svg viewBox="0 0 16 16" width="18" height="18" fill="none">
              <rect x="1" y="1" width="14" height="14" rx="2" stroke="currentColor" stroke-width="1.2"/>
              <path d="M4 5h8M4 8h6M4 11h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
            </svg>
          </span>
          <div class="flex-1 min-w-0">
            <input
            v-model="form.title"
              class="card-title-input w-full text-foreground text-[17px] font-semibold leading-snug bg-transparent outline-none rounded-md px-2 py-1 -mx-2 border border-transparent hover:border-border focus:border-ring focus:bg-card transition-all"
              @blur="saveTitle"
              @keydown="handleTitleKeydown"
            />
          </div>
 
        </div>
 
        <!-- Two-column layout -->
        <div class="grid grid-cols-[1fr_160px] gap-6">
 
          <!-- ── Main ── -->
          <div class="min-w-0">
 
            <!-- Members -->
            <SectionTitle>Thành viên</SectionTitle>
            <div class="flex gap-1.5 flex-wrap mb-5">
              <Avatar
                
                class="size-8 cursor-pointer"
              >
                <AvatarFallback class="text-[11px] font-semibold">
                  
                </AvatarFallback>
              </Avatar>
              <button
                class="size-8 rounded-full border border-dashed border-border bg-muted text-muted-foreground hover:bg-accent text-lg flex items-center justify-center transition-colors"
              >+</button>
            </div>
 
            <!-- Labels -->
            <template>
              <SectionTitle>Nhãn</SectionTitle>
              <div class="flex gap-1.5 flex-wrap mb-5">
                <Badge
                  class="cursor-pointer font-semibold text-[11px] px-2.5 py-0.5 rounded-full border-0"
                ></Badge>
                <Badge class="cursor-pointer bg-muted text-muted-foreground hover:bg-accent border-0 text-[11px] font-medium">
                  + Thêm
                </Badge>
              </div>
            </template>
 
            <!-- Due date -->
            <template>
              <SectionTitle>Hạn hoàn thành</SectionTitle>
              <div class="mb-5">
                <span
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium cursor-pointer"
                  
                >
                  <svg viewBox="0 0 14 14" width="12" height="12" fill="none">
                    <rect x="1" y="2" width="12" height="11" rx="1.5" stroke="currentColor" stroke-width="1.2"/>
                    <path d="M4 1v2M10 1v2M1 6h12" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                  </svg>
                  
                </span>
              </div>
            </template>
 
            <!-- Description -->
            <SectionTitle>Mô tả</SectionTitle>
            <textarea 
                @blur="saveDescription"
                @keydown="handleTitleKeydown"
              v-model="form.description"
              class="w-full bg-muted/60 p-3 rounded-md min-h-[100px] outline-none"
              placeholder="Thêm mô tả chi tiết..."
            ></textarea>
 
          </div>

          </div>
        </div>
      
    </DialogContent>
  </Dialog>

</template>