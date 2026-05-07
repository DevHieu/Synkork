<script setup lang="ts">
import { ref } from 'vue'
import { Pencil, Trash2 } from 'lucide-vue-next'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";

import type { CardEvent } from '@/types/Task'

import { updateCard } from '@/services/task/cardService'

import CardDetailDialog from '@/components/dialog/task/CardDetailDialog.vue'

const targetCard = ref<CardEvent | null>(null)

const props = defineProps<{ card: CardEvent, columnName: string }>()

const emit = defineEmits<{
    edit: [card: CardEvent]
    delete: [cardId: string]
}>()

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);
const isCardDetailOpen = ref(false)

const openDetail = () => {
    isCardDetailOpen.value = true
}

const saveInDetail = async (updatedCard: CardEvent ) => {
    try {
        const payload = {
            columnId: props.card.columnId,
            title: updatedCard.title,
            description: updatedCard.description,
        }
        await updateCard(currentSpace.value.id, props.card.id, payload)
   
    } catch (error) {
        console.error("Lỗi:", error)
    }
}

const getInitials = (name: string | undefined) => {
  if (!name) return '?'
  
  const parts = name.trim().split(' ')
  if (parts.length === 1) {
    return parts[0].substring(0, 2).toUpperCase() // Trả về 2 chữ đầu nếu tên chỉ có 1 từ
  }
  
  // Lấy chữ cái đầu của từ đầu tiên và từ cuối cùng
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}
</script>

<template>
    <Card
        class="p-4 border-none shadow-sm hover:shadow-md transition-all cursor-grab group relative rounded-2xl"
        @click="openDetail"
    >
        <div class="flex flex-col gap-2">
            <p class="font-bold text-sm pr-10">{{ card.title }}</p>
            <p class="text-slate-500 text-xs line-clamp-2">{{ card.description }}</p>
            <div class="flex justify-between items-center mt-2">
                <div class="w-6 h-6 rounded-full bg-orange-100 flex items-center justify-center text-[10px] font-bold text-orange-600">
                    {{ getInitials(card.createdBy?.name) }}
                </div>
                <span class="text-[10px] text-slate-400 font-medium">
                    {{ card.createdBy?.name ?? 'Không rõ' }}
                </span>
            </div>
        </div>

        <div class="absolute top-3 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <Button
                variant="ghost" size="icon" class="h-6 w-6"
                @click.stop="emit('edit', card)"
            >
                <Pencil class="w-3 h-3" />
            </Button>
            <Button
                variant="ghost" size="icon" class="h-6 w-6 text-red-400"
                @click.stop="emit('delete', card.id)"
            >
                <Trash2 class="w-3 h-3" />
            </Button>
        </div>
    </Card>

    <CardDetailDialog v-model:open="isCardDetailOpen" :card="props.card" @save="saveInDetail" :column-name="props.columnName"/>
</template>