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

const props = defineProps<{ card: CardEvent, columnName: string, columnId: string }>()

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

const saveInDetail = async (updatedCard: CardEvent) => {
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

const handleUpdateAssignees = async ({
    cardId,
    assigneeIds
}: {
    cardId: string,
    assigneeIds: string[]
}) => {
    try {
        const payload = {
            title: null,
            description: null,
            assigneeIds
        }

        await updateCard(currentSpace.value.id, cardId, payload)

        // update local UI luôn
        props.card.assignees = props.card.assignees.filter(a =>
            assigneeIds.includes(a.id)
        )

        console.log(props.card)

    } catch (e) {
        console.error('Lỗi cập nhật assignees:', e)
    }
}
</script>

<template>
    <div>
        <Card class="p-4 border-none shadow-sm hover:shadow-md transition-all cursor-grab group relative rounded-2xl"
            @click="openDetail">
            <div class="flex flex-col gap-2">
                <div class="pr-10">
                    <h3 class="font-bold text-sm leading-tight text-foreground break-words">
                        {{ card.title }}
                    </h3>
                </div>
                <p v-if="card.description" class="text-slate-500 text-xs line-clamp-2 leading-relaxed">
                    {{ card.description }}
                </p>
                <div class="flex justify-between items-center mt-2">
                    <div class="flex items-center -space-x-2">
                        <div v-for="assignee in card.assignees" :key="assignee.id" :title="assignee.name"
                            class="w-6 h-6 rounded-full bg-orange-100 border-2 border-white flex items-center justify-center text-[10px] font-bold text-orange-600 cursor-pointer overflow-hidden">
                            <img v-if="assignee.avatarUrl" :src="assignee.avatarUrl"
                                class="w-full h-full object-cover" />

                            <span v-else>
                                {{ getInitials(assignee.name) }}
                            </span>
                        </div>

                        <!-- fallback nếu chưa assign ai -->
                       
                    </div>
                    <span class="text-[10px] text-slate-400 font-medium">
                        {{
                            card.createdAt
                                ? new Date(card.createdAt).toLocaleString('vi-VN', {
                                    day: '2-digit',
                                    month: '2-digit',
                                    year: 'numeric'
                                })
                                : 'Không rõ'
                        }}
                    </span>
                </div>
            </div>

            <div class="absolute top-3 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <!-- <Button
                variant="ghost" size="icon" class="h-6 w-6"
                @click.stop="emit('edit', card)"
            >
                <Pencil class="w-3 h-3" />
            </Button> -->
                <Button variant="ghost" size="icon" class="h-6 w-6 text-red-400" @click.stop="emit('delete', card.id)">
                    <Trash2 class="w-3 h-3" />
                </Button>
            </div>
        </Card>

        <CardDetailDialog v-model:open="isCardDetailOpen" :card="props.card" @save="saveInDetail"
            :column-name="props.columnName" :column-id="props.columnId" @update-assignees="handleUpdateAssignees" />
    </div>

</template>