<script setup lang="ts">
import { MoreHorizontal, Pencil, Trash2, Plus } from 'lucide-vue-next'
import draggable from 'vuedraggable'
import { Button } from '@/components/ui/button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from '@/components/ui/dropdown-menu'
import TaskCard from './TaskCard.vue'
import type { CardEvent, ColumnEvent, TaskMoveEvent } from '@/types/Task'

const props = defineProps<{ column: ColumnEvent }>()

const emit = defineEmits<{
    editColumn: [column: ColumnEvent]
    deleteColumn: [columnId: string]
    addCard: [columnId: string]
    editCard: [columnId: string, card: CardEvent]
    deleteCard: [columnId: string, cardId: string]
    cardMove: [event: TaskMoveEvent, columnId: string]
}>()
</script>

<template>
    <div class="w-80 flex flex-col max-h-full border-3 border-slate-400 rounded-3xl p-4 overflow-hidden">
        <!-- Header -->
        <div class="flex items-start justify-between mb-4 px-1">
            <h3 class="column-handle cursor-move font-bold text-sm uppercase tracking-wide break-words min-w-0 flex-1 mr-2">
                {{ column.name }}
                <span class="text-slate-400 text-xs font-normal">
                    ({{ column?.cards?.length || 0 }})
                </span>
            </h3>
            <DropdownMenu>
                <DropdownMenuTrigger as-child>
                    <Button variant="ghost" size="icon" class="h-8 w-8 text-slate-500">
                        <MoreHorizontal class="w-4 h-4" />
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" class="rounded-xl border-none shadow-lg backdrop-blur-md">
                    <DropdownMenuItem @click="emit('editColumn', column)" class="gap-2 cursor-pointer text-xs">
                        <Pencil class="w-3.5 h-3.5" /> Sửa tên cột
                    </DropdownMenuItem>
                    <DropdownMenuItem @select="emit('deleteColumn', column.id)" class="gap-2 cursor-pointer text-xs text-red-500">
                        <Trash2 class="w-3.5 h-3.5" /> Xóa cột
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </div>

        <!-- Cards -->
        <draggable
            v-model="column.cards"
            group="tasks"
            item-key="id"
            :animation="200"
            ghost-class="opacity-50"
            @change="(e: TaskMoveEvent) => emit('cardMove', e, column.id)"
            class="flex-1 flex flex-col gap-3 overflow-y-auto min-h-[150px] p-1"
        >
            <template #item="{ element: card }">
        
                <TaskCard
                    :card="card"
                    @edit="emit('editCard', column.id, card)"
                    @delete="emit('deleteCard', column.id, card.id)"
                />
            </template>
        </draggable>

        <!-- Add card button -->
        <button
            @click="emit('addCard', column.id)"
            class="mt-3 w-full py-2 flex items-center justify-center gap-1 text-slate-400 hover:text-teal-600 text-sm font-medium cursor-pointer"
        >
            <Plus class="w-4 h-4" /> Thêm thẻ
        </button>
    </div>
</template>