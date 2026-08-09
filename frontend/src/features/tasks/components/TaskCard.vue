<script setup lang="ts">
import { ref, watch } from 'vue'
import { Archive, Calendar, AlignLeft, Check } from 'lucide-vue-next'
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
import { VersionConflictError } from '@/features/tasks/services/cardService'
import { toast } from 'vue-sonner'
import { useTaskAction } from '../composables/task-api.ts'
import { getAvatarColor, getInitials } from '@/features/tasks/utils/avatar'
import { formattedDate, checkDueSoon, checkOverdue } from '@/features/tasks/utils/task-date'
import { useVersionConflict } from '../composables/version-conflict.ts'

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const taskAction = useTaskAction();
const taskConflict = useVersionConflict({
    entityLabel: 'thẻ',
    createCopy: async (attempted) => {
        if (!currentSpace.value?.id) return
        await taskAction.saveCard(
            currentSpace.value.id,
            '',
            props.columnId,
            attempted.title,
            attempted.description,
            [],
            attempted.dueDate
        )
    }
});

const isCardDetailOpen = ref(false)

const props = defineProps<{ card: CardEvent, columnName: string, columnId: string }>()

const emit = defineEmits<{
    archive: [cardId: string]
    toggleComplete: [payload: { id: string; completed: boolean }]
}>()

const openDetail = () => {
    isCardDetailOpen.value = true
}

const isCompleted = ref(props.card.completed)

const handleToggleComplete = () => {
    if (!currentSpace.value) return

    const newStatus = !isCompleted.value;
    isCompleted.value = newStatus;
    props.card.completed = newStatus;

    taskAction.completeCardEvent(currentSpace.value.id, props.card.id, newStatus);
    
    emit("toggleComplete", {
        id: props.card.id,
        completed: newStatus,
    })
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
        if (e instanceof VersionConflictError) {
            toast.error("Thẻ này vừa được người khác cập nhật. Vui lòng kiểm tra lại nội dung mới nhất.")
            taskConflict.conflictData = updatedCard
            isCardDetailOpen.value = false
            taskConflict.isConflictOpen = true
            return
        } else {
            console.error("Lỗi:", e)
            toast.error("Có lỗi xảy ra, vui lòng thử lại.")
        }
    }
}

watch(
    () => props.card.completed,
    value => {
        isCompleted.value = value
    }
)

</script>
<template>
    <div>
        <Card
            :class="[
                'task-card group relative rounded-2xl border border-border/60 bg-card transition-all duration-300 cursor-grab active:cursor-grabbing overflow-hidden',
                isCompleted
                    ? 'opacity-60 saturate-50'
                    : 'hover:shadow-md hover:-translate-y-0.5'
            ]"
            @click="openDetail"
        >
            <!-- để cho đẹp -->
            <div class="h-0.5 w-full bg-gradient-to-r from-primary/60 via-primary/30 to-transparent" />

            <div class="px-3 flex flex-col gap-1">
                <div class="flex items-start gap-2">
                    <button
                        type="button"
                        role="checkbox"
                        :aria-checked="isCompleted"
                        aria-label="Đánh dấu hoàn thành"
                        class="task-check-btn relative mt-[1px] flex h-[18px] w-[18px] shrink-0 items-center justify-center rounded-full border-[1.5px] transition-all duration-200 ease-out"
                        :class="[
                            isCompleted
                                ? 'border-emerald-500 bg-emerald-500 shadow-[0_0_0_3px_rgba(16,185,129,0.16)]'
                                : 'border-muted-foreground/35 bg-background opacity-0 group-hover:opacity-100 hover:!border-emerald-500 hover:!opacity-100 hover:bg-emerald-50 dark:hover:bg-emerald-950/30'
                        ]" @click.stop="handleToggleComplete">
                        <Check v-if="isCompleted" class="check-pop h-2.5 w-2.5 text-white" :stroke-width="3" />
                    </button>
                    <h3
                        class="font-semibold text-[13px] line-clamp-1 leading-snug break-words flex-1 transition-colors duration-200"
                        :class="isCompleted
                            ? 'text-muted-foreground line-through decoration-[1.5px] decoration-muted-foreground/50'
                            : 'text-card-foreground'"
                    >
                        {{ card.title }}
                    </h3>
                    <Button variant="ghost" size="icon"
                        class="h-5 w-5 shrink-0 text-muted-foreground/40 hover:text-destructive hover:bg-destructive/10 opacity-0 group-hover:opacity-100 transition-all duration-150 rounded-md -mt-0.5 -mr-0.5"
                        @click.stop="emit('archive', card.id)">
                        <Archive v-if="isCompleted" class="w-3 h-3" />
                    </Button>
                </div>

                <p v-if="card.description"
                    class="flex items-start gap-1 text-[11px] text-muted-foreground leading-relaxed">
                    <AlignLeft class="w-2.5 h-2.5 mt-0.5 shrink-0 opacity-50" />
                    <span class="min-w-0 flex-1 line-clamp-2 break-words">
                        {{ card.description }}
                    </span>
                </p>

                <div class="flex justify-between items-center pt-0.5">
                    <div class="flex items-center gap-1.5">
                        <Badge v-if="card.dueDate" variant="outline" :class="[
                            'text-[10px] font-medium px-1.5 py-0 h-4 gap-0.5 rounded border',
                            isCompleted
                                ? 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/30 dark:text-emerald-400 dark:border-emerald-800/40'
                                : checkOverdue(card.dueDate)
                                    ? 'bg-destructive/10 text-destructive border-destructive/20'
                                    : checkDueSoon(card.dueDate)
                                        ? 'bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-950/30 dark:text-amber-400 dark:border-amber-800/40'
                                        : 'bg-muted text-muted-foreground border-border/50'
                        ]">
                            <Check v-if="isCompleted" class="w-2.5 h-2.5" />

                            <Calendar v-else class="w-2 h-2" />

                            {{
                                isCompleted
                                    ? "Hoàn thành"
                                    : new Date(card.dueDate).toLocaleDateString("vi-VN", {
                                        day: "2-digit",
                            month: "2-digit",
                            })
                            }}
                        </Badge>

                        <div class="flex items-center -space-x-1">
                            <div v-for="assignee in card.assignees?.slice(0, 3)" :key="assignee.id"
                                :title="assignee.name" :class="[
                                    'w-5.5 h-5.5 rounded-full border border-card flex items-center justify-center text-[8px] font-bold overflow-hidden',
                                    getAvatarColor(assignee.name)
                                ]">
                                <img v-if="assignee.avatarUrl" :src="assignee.avatarUrl"
                                    class="w-full h-full object-cover" />
                                <span v-else>{{ getInitials(assignee.name) }}</span>
                            </div>
                            <div v-if="(card.assignees?.length ?? 0) > 3"
                                class="w-4 h-4 rounded-full border border-card bg-muted flex items-center justify-center text-[8px] font-bold text-muted-foreground">
                                +{{ (card.assignees?.length ?? 0) - 3 }}
                            </div>
                        </div>
                    </div>

                    <span v-if="formattedDate && !isCompleted" class="text-[10px] text-muted-foreground/50 tabular-nums">
                        {{ formattedDate(card.dueDate) }}
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
        <Dialog v-model:open="taskConflict.isConflictOpen">
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
                    <Button variant="outline" :disabled="taskConflict.isCreatingCopy" @click="taskConflict.handleDiscard">
                        Bỏ qua, xem bản mới nhất
                    </Button>
                    <Button :disabled="taskConflict.isCreatingCopy" @click="taskConflict.handleCreateCopy">
                        {{ taskConflict.isCreatingCopy ? 'Đang tạo...' : 'Tạo thẻ mới với nội dung của tôi' }}
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

.task-check-btn:active {
    transform: scale(0.88);
}

.check-pop {
    animation: check-pop 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes check-pop {
    0% {
        transform: scale(0);
        opacity: 0;
    }
    60% {
        transform: scale(1.25);
    }
    100% {
        transform: scale(1);
        opacity: 1;
    }
}
</style>