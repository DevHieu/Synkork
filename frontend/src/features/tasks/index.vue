<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import draggable from 'vuedraggable'
import { useRoute } from 'vue-router'
import { Plus, Hash, Archive } from 'lucide-vue-next'
import { taskSocket } from '@/features/tasks/services/taskSocket.ts'
import { useSpaceStore } from "@/features/spaces/stores/spaceStore";
import { useTaskStore } from "@/features/tasks/stores/taskStore";
import { storeToRefs } from "pinia";
import type { ColumnEvent, TaskMoveEvent } from "@/features/tasks/types/Task";
import TaskColumn from '@/features/tasks/components/TaskColumn.vue'
import ColumnFormDialog from '@/features/tasks/components/dialog/ColumnFormDialog.vue'
import DeleteConfirmDialog from '@/components/dialog/DeleteConfirmDialog.vue'
import CardFormDialog from '@/features/tasks/components/dialog/CardFormDialog.vue'
import ArchiveTask from '@/features/tasks/components/dialog/ArchiveTask.vue'
import ConflictDialog from '@/features/tasks/components/dialog/ConflictDialog.vue'
import { SidebarTrigger } from "@/components/ui/sidebar";
import { toast } from "vue-sonner"
import { VersionConflictError } from '@/features/tasks/services/cardService'
import { ColumnVersionConflictError } from '@/features/tasks/services/columnService'
import { subscribeToSpace } from './composables/task-subscriptions'
import { useTaskAction } from './composables/task-api'
import { useVersionConflict } from './composables/version-conflict'
import { useTaskDialogs } from './composables/task-dialog'

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);
const taskStore = useTaskStore();
const { columns } = storeToRefs(taskStore);

const taskAction = useTaskAction();
const taskConflict = useVersionConflict({
    entityLabel: 'cột',
    createCopy: async (attempted) => {
        if (!currentSpace.value?.id) return
        await taskAction.saveColumn(currentSpace.value.id, '', attempted.title)
    }
});
const taskDialog = useTaskDialogs();

const route = useRoute();
const spaceId = route.params.spaceId as string;

const isSocketConnected = ref(false)

const isSaving = ref(false)

const isDeleteOpen = ref(false)
const deleteType = ref<'column' | 'card'>('column')
const deleteData = ref<{ cardId: string, columnId: string } | null>(null)
const isDeleteAllOpen = ref(false)
const deleteAllType = ref<'columns' | 'cards'>('columns')

const isArchiveOpen = ref(false)

const props = defineProps<{
    spaceId: String,
    roomId: String,
}>()

const { isCardDialogOpen, isColumnDialogOpen, editingCard, editingCol, targetColumnId } = taskDialog

const handleSaveColumn = async (data: { title: string }) => {
    if (!currentSpace.value?.id) return;
    isSaving.value = true
    try {
        await taskAction.saveColumn(currentSpace.value.id, taskDialog.editingCol.value?.id ?? '', data.title, taskDialog.editingCol.value?.version)
        taskDialog.isColumnDialogOpen.value = false
    } catch (e) {
        if (e instanceof ColumnVersionConflictError) {
            toast.error("Cột này vừa được người khác cập nhật. Vui lòng kiểm tra lại nội dung mới nhất.")
            taskConflict.openConflict({ title: data.title })
        } else {
            console.error("Lỗi:", e)
            toast.error("Có lỗi xảy ra, vui lòng thử lại.")
        }
    } finally {
        isSaving.value = false
    }
}

const confirmDeleteColumn = (colId: string) => {
    console.log("CLICK DELETE", colId)
    deleteType.value = 'column'
    deleteData.value = { columnId: colId, cardId: '' }
    isDeleteOpen.value = true
}

const confirmDeleteAllArchivedColumns = () => {
    deleteAllType.value = 'columns'
    isDeleteAllOpen.value = true
}

const onColumnMove = async (event: TaskMoveEvent) => {
    if (!currentSpace.value?.id) return;

    const movedCol = event.moved || event.added;
    const version = movedCol?.element?.version;
    try {
        await taskAction.moveColumnEvent(currentSpace.value.id, event, version)
    } catch (error) {
        console.error("Lỗi di chuyển cột: ", error);
        await taskAction.fetchTasks(currentSpace.value.id);
    }
}

const handleSaveCard = async (data: { title: string, description: string }) => {
    if (!currentSpace.value?.id) return;
    try {
        await taskAction.saveCard(
            currentSpace.value.id, taskDialog.editingCard.value?.id ?? '',
            taskDialog.targetColumnId.value, data.title, data.description, [], undefined, taskDialog.editingCard.value?.version
        )
        taskDialog.isCardDialogOpen.value = false
    } catch (e) {
        if (e instanceof VersionConflictError) {
            toast.error("Thẻ này vừa được người khác cập nhật. Vui lòng kiểm tra lại nội dung mới nhất.")
        } else {
            console.error("Lỗi:", e)
            toast.error("Có lỗi xảy ra, vui lòng thử lại.")
        }
    } finally {
        isSaving.value = false
    }
}

const confirmDeleteCard = (columnId: string, cardId: string) => {
    deleteType.value = 'card'
    deleteData.value = { columnId, cardId }
    isDeleteOpen.value = true
}

const confirmDeleteAllArchivedCards = () => {
    deleteAllType.value = 'cards'
    isDeleteAllOpen.value = true
}

const onCardMove = async (event: TaskMoveEvent, currentColumnId: string) => {
    if (!currentSpace.value?.id) return;

    const movedCard = event.moved || event.added;
    const version = movedCard?.element?.version;
    try {
        await taskAction.moveCardEvent(currentSpace.value.id, currentColumnId, event, version)
    } catch (error) {
        console.error("Lỗi di chuyển card: ", error);
        await taskAction.fetchTasks(currentSpace.value.id);
    }
}

const handleDeleteArchived = async () => {
    await taskAction.deleteTask(deleteType.value, spaceId, deleteData.value)
    isDeleteOpen.value = false
    deleteData.value = null
    isArchiveOpen.value = true
    taskAction.fetchArchivedItems(spaceId)
}

const handleDeleteAllArchived = async () => {
    await taskAction.deleteAllArchived(deleteAllType.value, spaceId)
    isDeleteAllOpen.value = false
    isArchiveOpen.value = true
    taskAction.fetchArchivedItems(spaceId)
}

const clearAll = async () => {
    columns.value = []
}

const joinspace = async (spaceId: string) => {
    if (!spaceId) return;

    taskSocket.leaveSpace(spaceId);

    await clearAll();
    await taskAction.fetchTasks(spaceId);
    await subscribeToSpace(spaceId);
}

watch(isArchiveOpen, async (open) => {
    if (open) {
        await taskAction.fetchArchivedItems(spaceId)
    }
})

onMounted(() => {
    if (spaceId) {
        isSocketConnected.value = true;
    }
})

onUnmounted(() => {
    taskSocket.leaveSpace(spaceId);
})

watch(
    [currentSpace, isSocketConnected],
    ([space, connected]) => {
        if (!space?.id || !connected) return;
        joinspace(space.id);
    },
    { immediate: true },
)
</script>

<template>
    <div class="flex h-screen w-full overflow-hidden background">
        <div class="flex-1 flex flex-col relative overflow-hidden">
            <header class="flex items-center px-5 py-3.5 border-b border-border/50 bg-background/60 backdrop-blur-sm">
                <div class="flex items-center gap-2.5">
                    <SidebarTrigger class="-ml-1 shrink-0 text-muted-foreground hover:text-foreground" />
                    <div class="h-4 w-px bg-border/60" />
                    <div class="flex items-center gap-2">
                        <div class="w-7 h-7 rounded-lg bg-primary/15 flex items-center justify-center">
                            <Hash class="w-3.5 h-3.5 text-primary" />
                        </div>
                        <span class="font-semibold text-md text-foreground">{{ currentSpace?.name }}</span>
                    </div>
                </div>

                <ArchiveTask v-model:open="isArchiveOpen" @delete-column="confirmDeleteColumn"
                    @delete-card="confirmDeleteCard" @delete-all-archived-cards="confirmDeleteAllArchivedCards"
                    @delete-all-archived-columns="confirmDeleteAllArchivedColumns">
                    <template #trigger>
                        <button class="ms-5">
                            <Archive class="w-4 h-4" />
                        </button>
                    </template>
                </ArchiveTask>
            </header>

            <div class="flex-1 flex items-start gap-6 p-6 overflow-x-auto">
                <draggable v-model="columns" group="columns" item-key="id" handle=".column-handle"
                    @change="onColumnMove" class="flex gap-6 items-start h-full">
                    <template #item="{ element: col }">
                        <TaskColumn :column="col" :space-name="currentSpace?.name ?? ''" @edit-column="taskDialog.openColumnDialog"
                            @archive-column="taskAction.archiveColumnEvent(spaceId, col.id)" @add-card="taskDialog.openCardDialog" @archive-card="taskAction.archiveCardEvent(spaceId, $event)"
                            @card-move="onCardMove" />
                    </template>
                </draggable>

                <div @click="taskDialog.openColumnDialog(null)"
                    class="add-column-btn flex-shrink-0 w-72 h-28 border-2 border-dashed border-border/60 rounded-2xl flex flex-col items-center justify-center gap-2.5 group cursor-pointer hover:border-primary/50 hover:bg-primary/5 transition-all duration-200">
                    <div
                        class="w-8 h-8 rounded-xl bg-muted group-hover:bg-primary/15 flex items-center justify-center transition-colors">
                        <Plus class="w-4 h-4 text-muted-foreground group-hover:text-primary transition-colors" />
                    </div>
                    <p
                        class="text-xs font-semibold text-muted-foreground/60 group-hover:text-primary uppercase tracking-wider transition-colors">
                        Thêm cột mới
                    </p>
                </div>
            </div>
        </div>
    </div>

    <CardFormDialog v-model:open="isCardDialogOpen" :columnId="targetColumnId" :taskData="editingCard"
        :isSaving="isSaving" @save="handleSaveCard" />
    <ColumnFormDialog v-model:open="isColumnDialogOpen" :column-data="editingCol" @save="handleSaveColumn" />
    <DeleteConfirmDialog v-model:open="isDeleteOpen" :title="deleteType === 'column' ? 'Xóa cột này?' : 'Xóa thẻ này?'"
        :description="deleteType === 'column' ? 'Toàn bộ thẻ trong cột này sẽ bị mất.' : 'Bạn không thể khôi phục thẻ này sau khi xóa.'"
        @confirm="handleDeleteArchived" />
    <DeleteConfirmDialog v-model:open="isDeleteAllOpen"
        :title="deleteAllType === 'columns' ? 'Xóa tất cả cột?' : 'Xóa tất cả thẻ?'"
        :description="deleteAllType === 'columns' ? 'Toàn bộ các cột và thẻ bên trong sẽ bị xóa vĩnh viễn.' : 'Toàn bộ thẻ đã lưu trữ sẽ bị xóa vĩnh viễn.'"
        @confirm="handleDeleteAllArchived" />

        <ConflictDialog
            v-model:open="taskConflict.isConflictOpen"
            :is-creating-copy="taskConflict.isCreatingCopy"
            entity-label="cột"
            entity-title="Cột"
            :item-name="editingCol?.name"
            @discard="taskConflict.handleDiscard"
            @create-copy="taskConflict.handleCreateCopy"
        />

</template>

<style scoped></style>