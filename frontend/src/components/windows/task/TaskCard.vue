<script setup lang="ts">
import { Pencil, Trash2 } from 'lucide-vue-next'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import type { CardEvent } from '@/types/Task'

const props = defineProps<{ card: CardEvent }>()

const emit = defineEmits<{
    edit: [card: CardEvent]
    delete: [cardId: string]
}>()
</script>

<template>
    <Card class="p-4 border-none shadow-sm hover:shadow-md transition-all cursor-grab group relative rounded-2xl">
        <div class="flex flex-col gap-2">
            <p class="font-bold text-sm pr-10">{{ card.title }}</p>
            <p class="text-slate-500 text-xs line-clamp-2">{{ card.description }}</p>
            <div class="flex justify-between items-center mt-2">
                <div class="w-6 h-6 rounded-full bg-orange-100 flex items-center justify-center text-[10px] font-bold text-orange-600">
                    {{ card.user?.name || 'V' }}
                </div>
                <span class="text-[10px] text-slate-400 font-medium">
                    {{ card.createdAt || card.date }}
                </span>
            </div>
        </div>
        <div class="absolute top-3 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <Button variant="ghost" size="icon" class="h-6 w-6" @click="emit('edit', card)">
                <Pencil class="w-3 h-3" />
            </Button>
            <Button variant="ghost" size="icon" class="h-6 w-6 text-red-400" @click="emit('delete', card.id)">
                <Trash2 class="w-3 h-3" />
            </Button>
        </div>
    </Card>
</template>