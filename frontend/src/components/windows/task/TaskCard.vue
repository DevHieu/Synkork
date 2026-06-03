<script setup lang="ts">
import { ref, computed } from 'vue'
import { Trash2, Calendar, AlignLeft } from 'lucide-vue-next'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'

import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";

import type { CardEvent } from '@/types/Task'

import CardDetailDialog from '@/components/dialog/TaskDialog/CardDetailDialog.vue'
import { useTaskStore } from '@/stores/taskStore';

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const taskStore = useTaskStore();

const isCardDetailOpen = ref(false)

const props = defineProps<{ card: CardEvent, columnName: string, columnId: string }>()

const emit = defineEmits<{
    delete: [cardId: string]
}>()

const openDetail = () => {
    isCardDetailOpen.value = true
}

const saveInDetail = async (updatedCard: CardEvent) => {
    if (!currentSpace.value) return
    try {
        await taskStore.saveCard(
            currentSpace.value.id,
            updatedCard.id,
            props.columnId,
            updatedCard.title,
            updatedCard.description,
            updatedCard.assignees?.map(a => a.id) ?? [],
            updatedCard.dueDate
        )
    } catch (error) {
        console.error("Lỗi:", error)
    }
}

const getInitials = (name?: string) => {
    if (!name?.trim()) return '?'
    const parts = name.trim().split(' ')
    if (parts.length === 1) return parts[0]?.substring(0, 2).toUpperCase() ?? '?'
    return ((parts[0]?.[0] ?? '') + (parts[parts.length - 1]?.[0] ?? '')).toUpperCase()
}

const avatarColors = [
    'bg-rose-100 text-rose-600',
    'bg-sky-100 text-sky-600',
    'bg-violet-100 text-violet-600',
    'bg-amber-100 text-amber-600',
    'bg-emerald-100 text-emerald-600',
    'bg-pink-100 text-pink-600',
]

const getAvatarColor = (name?: string) => {
    if (!name) return avatarColors[0]
    const idx = name.charCodeAt(0) % avatarColors.length
    return avatarColors[idx]
}

const formattedDate = computed(() => {
    if (!props.card.createdAt) return null
    return new Date(props.card.createdAt).toLocaleDateString('vi-VN', {
        day: '2-digit', month: '2-digit', year: 'numeric'
    })
})

const isDueSoon = computed(() => {
    if (!props.card.dueDate) return false
    const due = new Date(props.card.dueDate)
    const now = new Date()
    const diff = (due.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
    return diff <= 2 && diff >= 0
})

const isOverdue = computed(() => {
    if (!props.card.dueDate) return false
    return new Date(props.card.dueDate) < new Date()
})
</script>

<template>
    <div>
        <Card
            class="task-card group relative rounded-2xl border border-border/60 bg-card shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 cursor-grab active:cursor-grabbing overflow-hidden"
            @click="openDetail"
        >
            <!-- Top accent line using primary color -->
            <div class="h-0.5 w-full bg-gradient-to-r from-primary/60 via-primary/30 to-transparent" />

            <div class="px-3 flex flex-col gap-1">
                <!-- Title row -->
                <div class="flex items-start justify-between gap-1.5">
                    <h3 class="font-medium text-[13px] leading-snug text-card-foreground break-words flex-1">
                        {{ card.title }}
                    </h3>
                    <!-- Delete button, appears on hover -->
                    <Button
                        variant="ghost"
                        size="icon"
                        class="h-5 w-5 shrink-0 text-muted-foreground/40 hover:text-destructive hover:bg-destructive/10 opacity-0 group-hover:opacity-100 transition-all duration-150 rounded-md -mt-0.5 -mr-0.5"
                        @click.stop="emit('delete', card.id)"
                    >
                        <Trash2 class="w-3 h-3" />
                    </Button>
                </div>

                <!-- Description preview -->
                <p v-if="card.description"
                    class="text-[11px] text-muted-foreground line-clamp-1 leading-relaxed flex items-start gap-1">
                    <AlignLeft class="w-2.5 h-2.5 mt-0.5 shrink-0 opacity-50" />
                    {{ card.description }}
                </p>

                <!-- Footer: due date + assignees + date -->
                <div class="flex justify-between items-center pt-0.5">
                    <div class="flex items-center gap-1.5">
                        <!-- Due date badge -->
                        <Badge
                            v-if="card.dueDate"
                            variant="outline"
                            :class="[
                                'text-[10px] font-medium px-1.5 py-0 h-4 gap-0.5 rounded border',
                                isOverdue
                                    ? 'bg-destructive/10 text-destructive border-destructive/20'
                                    : isDueSoon
                                        ? 'bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-950/30 dark:text-amber-400 dark:border-amber-800/40'
                                        : 'bg-muted text-muted-foreground border-border/50'
                            ]"
                        >
                            <Calendar class="w-2 h-2" />
                            {{ new Date(card.dueDate).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }) }}
                        </Badge>

                        <!-- Assignee avatars -->
                        <div class="flex items-center -space-x-1">
                            <div
                                v-for="assignee in card.assignees?.slice(0, 3)"
                                :key="assignee.id"
                                :title="assignee.name"
                                :class="[
                                    'w-5.5 h-5.5 rounded-full border border-card flex items-center justify-center text-[8px] font-bold overflow-hidden',
                                    getAvatarColor(assignee.name)
                                ]"
                            >
                                <img v-if="assignee.avatarUrl" :src="assignee.avatarUrl" class="w-full h-full object-cover" />
                                <span v-else>{{ getInitials(assignee.name) }}</span>
                            </div>
                            <div
                                v-if="(card.assignees?.length ?? 0) > 3"
                                class="w-4 h-4 rounded-full border border-card bg-muted flex items-center justify-center text-[8px] font-bold text-muted-foreground"
                            >
                                +{{ (card.assignees?.length ?? 0) - 3 }}
                            </div>
                        </div>
                    </div>

                    <!-- Created date -->
                    <span v-if="formattedDate" class="text-[10px] text-muted-foreground/50 tabular-nums">
                        {{ formattedDate }}
                    </span>
                </div>
            </div>
        </Card>

        <CardDetailDialog
            v-model:open="isCardDetailOpen"
            :card="props.card"
            :column-name="props.columnName"
            :column-id="props.columnId"
            @save="saveInDetail"
        />
    </div>
</template>

<style scoped>
.task-card {
    /* Subtle backdrop so card stands out from column bg */
    backdrop-filter: blur(2px);
}
</style>