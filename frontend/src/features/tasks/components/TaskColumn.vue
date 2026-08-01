<script setup lang="ts">
import { computed } from 'vue'
import { MoreHorizontal, Pencil, Trash2, Plus, GripVertical, Archive } from 'lucide-vue-next'
import draggable from 'vuedraggable'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
    DropdownMenu, DropdownMenuContent,
    DropdownMenuItem, DropdownMenuSeparator,
    DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import TaskCard from './TaskCard.vue'
import type { CardEvent, ColumnEvent, TaskMoveEvent } from '@/types/Task'

const props = defineProps<{ column: ColumnEvent, spaceName?: string }>()

const emit = defineEmits<{
    editColumn: [column: ColumnEvent]
    archiveColumn: [columnId: string]
    addCard: [columnId: string]
    archiveCard: [columnId: string, cardId: string]
    cardMove: [event: TaskMoveEvent, columnId: string]
}>()

const localCards = computed<CardEvent[]>({
    get: () => props.column.cards ?? [],
    set: (val) => {
        props.column.cards = val
    }
})
</script>

<template>
    <div class="task-column w-76 flex flex-col max-h-full rounded-2xl border border-border/70 bg-muted/40 backdrop-blur-sm overflow-hidden shadow-sm">

        <div class="flex items-center gap-2 px-4 pt-4 pb-3">
            <GripVertical class="column-handle w-4 h-4 text-muted-foreground/40 cursor-move hover:text-muted-foreground transition-colors shrink-0"/>

            <div class="flex-1 flex items-center gap-2 min-w-0">
                <h3 class="font-semibold text-sm text-foreground truncate leading-tight">
                    {{ column.name }}
                </h3>
                <Badge
                    variant="secondary"
                    class="text-[10px] font-semibold px-1.5 py-0 h-4 rounded-full shrink-0 bg-primary/10 text-primary border-0"
                > {{ localCards.length }}
                </Badge>
            </div>

            <DropdownMenu>
                <DropdownMenuTrigger as-child>
                    <Button
                        variant="ghost"
                        size="icon"
                        class="h-7 w-7 text-muted-foreground hover:text-foreground hover:bg-accent rounded-lg shrink-0"
                    >
                        <MoreHorizontal class="w-4 h-4" />
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" class="w-44 rounded-xl shadow-lg border border-border/60 bg-popover/95 backdrop-blur-md">
                    <DropdownMenuItem
                        @click="emit('editColumn', column)"
                        class="gap-2.5 cursor-pointer text-xs font-medium rounded-lg mx-1 my-0.5"
                    >
                        <Pencil class="w-3.5 h-3.5 text-muted-foreground" />
                        Đổi tên cột
                    </DropdownMenuItem>
                    <DropdownMenuSeparator class="mx-2 my-1" />
                    <DropdownMenuItem
                        @select="emit('archiveColumn', column.id)"
                        class="gap-2.5 cursor-pointer text-xs font-medium text-destructive focus:text-destructive focus:bg-destructive/10 rounded-lg mx-1 my-0.5"
                    >
                        <Archive class="w-3.5 h-3.5" />
                        Lưu trữ cột
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </div>

        <!-- để cho đẹp -->
        <div class="mx-4 h-px bg-border/60 mb-3" />

        <draggable
            v-model="localCards"
            group="tasks"
            item-key="id"
            :animation="200"
            ghost-class="dragging-ghost"
            @change="(e: TaskMoveEvent) => emit('cardMove', e, column.id)"
            class="flex-1 flex flex-col gap-2 overflow-y-auto px-3 pb-2 min-h-[120px] cards-scroll"
        >
            <template #item="{ element: card }">
                <TaskCard
                    :card="card"
                    :column-name="column.name"
                    @archive="emit('archiveCard', column.id, card.id)"
                    :column-id="column.id"
                />
            </template>

            <template #footer>
                <div
                    v-if="!localCards.length"
                    class="flex flex-col items-center justify-center py-8 gap-2 text-muted-foreground/50 select-none pointer-events-none"
                >
                    <div class="w-8 h-8 rounded-xl border-2 border-dashed border-muted-foreground/20 flex items-center justify-center">
                        <Plus class="w-4 h-4 opacity-40" />
                    </div>
                    <p class="text-[11px] font-medium">Chưa có thẻ nào</p>
                </div>
            </template>
        </draggable>

        <div class="px-3 pb-3 pt-1">
            <button
                @click="emit('addCard', column.id)"
                class="w-full py-2 flex items-center justify-center gap-1.5 text-muted-foreground hover:text-primary hover:bg-primary/8 text-xs font-medium cursor-pointer rounded-xl transition-all duration-150 border border-dashed border-border/60 hover:border-primary/40"
            >
                <Plus class="w-3.5 h-3.5" />
                Thêm thẻ
            </button>
        </div>
    </div>
</template>

<style scoped>
.task-column {
    min-width: 288px;
    max-width: 288px;
}

.cards-scroll {
    scrollbar-width: thin;
    scrollbar-color: var(--border) transparent;
}

.cards-scroll::-webkit-scrollbar {
    width: 4px;
}

.cards-scroll::-webkit-scrollbar-track {
    background: transparent;
}

.cards-scroll::-webkit-scrollbar-thumb {
    background-color: var(--border);
    border-radius: 999px;
}

:global(.dragging-ghost) {
    opacity: 0.4;
    border: 2px dashed var(--primary);
    border-radius: 16px;
    background: color-mix(in oklch, var(--primary) 8%, transparent);
}
</style>