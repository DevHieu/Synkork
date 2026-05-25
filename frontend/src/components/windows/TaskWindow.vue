<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import draggable from 'vuedraggable'
import { useRoute } from 'vue-router'
import { Plus, Hash } from 'lucide-vue-next'

import { taskSocket } from '@/services/websocket/taskSocket'
import { useSpaceStore } from "@/stores/spaceStore";
import { useTaskStore } from "@/stores/taskStore";
import { storeToRefs } from "pinia";

import type { CardEvent, ColumnEvent, TaskMoveEvent } from "@/types/Task";

import TaskColumn from '@/components/windows/task/TaskColumn.vue'
import ColumnFormDialog from '@/components/dialog/task/ColumnFormDialog.vue'
import DeleteConfirmDialog from '@/components/dialog/DeleteConfirmDialog.vue'
import CardFormDialog from '@/components/dialog/task/CardFormDialog.vue'

import { SidebarTrigger } from "@/components/ui/sidebar";

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);
const taskStore = useTaskStore();
const { columns } = storeToRefs(taskStore);

const route = useRoute();
const spaceId = route.params.spaceId as string;

const isSocketConnected = ref(false)

const isColumnDialogOpen = ref(false)
const editingCol = ref<ColumnEvent | null>(null)

const isCardDialogOpen = ref(false)
const editingCard = ref<CardEvent | null>(null)

const isSaving = ref(false)
const targetColumnId = ref<string>('')

const isDeleteOpen = ref(false)
const deleteType = ref<'column' | 'card'>('column')
const deleteData = ref<{ cardId: string, columnId: string } | null>(null)

const executeDelete = async () => {
    taskStore.delete(deleteType.value, spaceId, deleteData.value)

    isDeleteOpen.value = false
    deleteData.value = null
}

const openAddColumnDialog = () => {
    editingCol.value = null
    isColumnDialogOpen.value = true
}

const openEditColumnDialog = async (col: ColumnEvent) => {
    editingCol.value = col
    await nextTick()
    isColumnDialogOpen.value = true
}

const handleSaveColumn = async (data: { title: string }) => {
    if (!currentSpace.value?.id) return;
    isSaving.value = true
    try {
        await taskStore.saveColumn(currentSpace.value.id, editingCol.value?.id ?? '', data.title)
        isColumnDialogOpen.value = false
    } catch (e) {
        console.error("Lỗi:", e)
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

const onColumnMove = async (event: TaskMoveEvent) => {
    if (!currentSpace.value?.id) return;
    try {
        await taskStore.moveColumn(currentSpace.value.id, event)
    } catch (error) {
        console.error("Lỗi di chuyển cột: ", error);
        await taskStore.fetchTasks(currentSpace.value.id);
    }
}

const openAddCardDialog = (columnId: string) => {
    targetColumnId.value = columnId
    editingCard.value = null
    isCardDialogOpen.value = true
}

const handleSaveCard = async (data: { title: string, description: string }) => {
    if (!currentSpace.value?.id) return;
    try {
        await taskStore.saveCard(
            currentSpace.value.id, editingCard.value?.id ?? '',
            targetColumnId.value, data.title, data.description
        )
        isCardDialogOpen.value = false
    } catch (error) {
        console.error("Lỗi:", error)
    } finally {
        isSaving.value = false
    }
}

const confirmDeleteCard = (columnId: string, cardId: string) => {
    deleteType.value = 'card'
    deleteData.value = { columnId, cardId }
    isDeleteOpen.value = true
}

const onCardMove = async (event: TaskMoveEvent, currentColumnId: string) => {
    if (!currentSpace.value?.id) return;
    try {
        await taskStore.moveCard(currentSpace.value.id, currentColumnId, event)
    } catch (error) {
        console.error("Lỗi di chuyển card: ", error);
        await taskStore.fetchTasks(currentSpace.value.id);
    }
}

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

const joinspace = async (spaceId: string) => {
    if (!spaceId) return;

    if (currentSpace.value?.id && currentSpace.value.id !== spaceId) {
        taskSocket.leaveSpace(currentSpace.value.id);
    }

    await clearAll();
    await taskStore.fetchTasks(spaceId);
    await taskStore.subscribeTospace(spaceId);
}

const clearAll = async () => {
    columns.value = []
}
</script>

<template>
    <div class="flex h-screen w-full overflow-hidden background">
        <div class="flex-1 flex flex-col relative overflow-hidden">
            <header class="p-6 flex items-center gap-2 font-semibold">
                <SidebarTrigger class="-ml-1 shrink-0" />
                <Hash class="w-5 h-5 text-teal-600" />
                <span>{{ currentSpace?.name }}</span>
            </header>

            <div class="flex-1 flex items-start gap-6 p-6 overflow-x-auto">
                <draggable v-model="columns" group="columns" item-key="id" handle=".column-handle"
                    @change="onColumnMove" class="flex gap-6 items-start h-full">
                    <template #item="{ element: col }">
                        <TaskColumn :column="col" :space-name="currentSpace?.name ?? ''"
                            @edit-column="openEditColumnDialog" @delete-column="confirmDeleteColumn"
                            @add-card="openAddCardDialog" @delete-card="confirmDeleteCard" @card-move="onCardMove" />
                    </template>
                </draggable>

                <div @click="openAddColumnDialog"
                    class="flex-shrink-0 w-72 h-32 border-2 border-dashed rounded-3xl flex flex-col items-center justify-center gap-2 group cursor-pointer hover:border-teal-700 transition-colors">
                    <div class="bg-slate-200 p-2 rounded-full group-hover:bg-teal-100">
                        <Plus class="w-5 h-5 text-slate-500 group-hover:text-teal-600" />
                    </div>
                    <p class="text-xs font-bold text-slate-200 uppercase tracking-widest group-hover:text-teal-600">
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
        @confirm="executeDelete" />
</template>

<style scoped></style>
