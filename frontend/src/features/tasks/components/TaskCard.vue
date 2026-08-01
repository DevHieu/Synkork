<script setup lang="ts">
import { ref, computed } from 'vue'
import { Archive, Calendar, AlignLeft } from 'lucide-vue-next'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
} from '@/components/ui/dialog'

import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";

import type { CardEvent } from '@/types/Task'

import CardDetailDialog from './dialog/CardDetailDialog.vue'
import { useTaskStore } from '@/features/tasks/stores/taskStore';
import { VersionConflictError } from '@/features/tasks/services/cardService'
import { toast } from 'vue-sonner'
import { useTaskAction } from '../composables/task-api.ts'

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const taskStore = useTaskStore();
const taskAction = useTaskAction();

const isCardDetailOpen = ref(false)
const isConflictDialogOpen = ref(false)
const conflictAttempted = ref<CardEvent | null>(null)
const isCreatingCopy = ref(false)

const props = defineProps<{ card: CardEvent, columnName: string, columnId: string }>()

const emit = defineEmits<{
    archive: [cardId: string]
}>()

const openDetail = () => {
    isCardDetailOpen.value = true
}

const saveInDetail = async (updatedCard: CardEvent) => {
    if (!currentSpace.value) return
    try {
        await taskAction.saveCard(
            currentSpace.value.id,
            updatedCard.id,
            props.columnId,
            updatedCard.title,
            updatedCard.description,
            updatedCard.assignees?.map(a => a.id) ?? [],
            updatedCard.dueDate,
            updatedCard.version
        )
    } catch (e) {
        console.log("CARD SAVE ERROR", e)
    console.log("instanceof VersionConflictError:", e instanceof VersionConflictError)
        if (e instanceof VersionConflictError) {
            toast.error("Thẻ này vừa được người khác cập nhật. Vui lòng kiểm tra lại nội dung mới nhất.")
            conflictAttempted.value = updatedCard
            isCardDetailOpen.value = false   // đóng dialog sửa, tránh user tưởng vẫn đang edit bản cũ
            isConflictDialogOpen.value = true
            return
        } else {
            console.error("Lỗi:", e)
            toast.error("Có lỗi xảy ra, vui lòng thử lại.")
        }
    }
}

const handleCreateCopy = async () => {
    if (!currentSpace.value || !conflictAttempted.value) return
    isCreatingCopy.value = true
    try {
        const attempted = conflictAttempted.value
        await taskAction.saveCard(
            currentSpace.value.id,
            '',                 // cardId rỗng => tạo mới, không phải update
            props.columnId,
            attempted.title,
            attempted.description,
            [],
            attempted.dueDate
        )
        toast.success("Đã tạo thẻ mới với nội dung bạn vừa nhập.")
    } catch (e) {
        console.error("Lỗi tạo thẻ mới:", e)
        toast.error("Không thể tạo thẻ mới, vui lòng thử lại.")
    } finally {
        isCreatingCopy.value = false
        isConflictDialogOpen.value = false
        conflictAttempted.value = null
    }
}

const handleDiscard = () => {
    isConflictDialogOpen.value = false
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
        <!-- để cho đẹp -->
            <div class="h-0.5 w-full bg-gradient-to-r from-primary/60 via-primary/30 to-transparent" />

            <div class="px-3 flex flex-col gap-1">
                <div class="flex items-start justify-between gap-1.5">
                    <h3 class="font-semibold text-[13px] leading-snug text-card-foreground break-words flex-1">
                        {{ card.title }}
                    </h3>
                    <Button
                        variant="ghost"
                        size="icon"
                        class="h-5 w-5 shrink-0 text-muted-foreground/40 hover:text-destructive hover:bg-destructive/10 opacity-0 group-hover:opacity-100 transition-all duration-150 rounded-md -mt-0.5 -mr-0.5"
                        @click.stop="emit('archive', card.id)"
                    >
                        <Archive class="w-3 h-3" />
                    </Button>
                </div>

                <p v-if="card.description"
                    class="text-[11px] text-muted-foreground line-clamp-1 leading-relaxed flex items-start gap-1">
                    <AlignLeft class="w-2.5 h-2.5 mt-0.5 shrink-0 opacity-50" />
                    {{ card.description }}
                </p>

                <div class="flex justify-between items-center pt-0.5">
                    <div class="flex items-center gap-1.5">
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
        <Dialog v-model:open="isConflictDialogOpen">
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Thẻ đã bị thay đổi bởi người khác</DialogTitle>
                    <DialogDescription>
                        Trong lúc bạn chỉnh sửa, một người khác đã lưu thay đổi cho thẻ
                        <strong>"{{ card.title }}"</strong>. Nếu lưu đè, nội dung của họ sẽ bị mất.
                        Bạn có muốn tạo một thẻ mới chứa nội dung bạn vừa nhập, để không mất dữ liệu không?
                    </DialogDescription>
                </DialogHeader>
                <DialogFooter>
                    <Button variant="outline" :disabled="isCreatingCopy" @click="handleDiscard">
                        Bỏ qua, xem bản mới nhất
                    </Button>
                    <Button :disabled="isCreatingCopy" @click="handleCreateCopy">
                        {{ isCreatingCopy ? 'Đang tạo...' : 'Tạo thẻ mới với nội dung của tôi' }}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    </div>
</template>

<style scoped>
.task-card {
    backdrop-filter: blur(2px);
}
</style>