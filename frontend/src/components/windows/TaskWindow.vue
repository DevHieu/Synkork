<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import draggable from 'vuedraggable'
import { useRoute } from 'vue-router'
import { Plus, Hash } from 'lucide-vue-next'

import { taskSocket } from '@/services/websocket/taskSocket'
import { getAllColumns, createColumn, updateColumn, deleteColumn, moveColumn } from '@/services/task/columnService'
import { createCard, updateCard, deleteCard, moveCard } from '@/services/task/cardService'
import { useSpaceStore } from "@/stores/spaceStore";
// import { useUserStore } from '@/stores/userStore'
import { storeToRefs } from "pinia";
import { socketService } from '@/services/websocket/socketService';
import type { CardEvent, ColumnEvent, TaskMoveEvent, CardMovePayload } from "@/types/Task";

import TaskColumn from '@/components/windows/task/TaskColumn.vue'
import ColumnFormDialog from '../dialog/task/ColumnFormDialog.vue'
import DeleteConfirmDialog from '../dialog/DeleteConfirmDialog.vue'
import CardFormDialog from '@/components/dialog/task/CardFormDialog.vue'    

import { SidebarTrigger } from "@/components/ui/sidebar";

const columns = ref<ColumnEvent[]>([])
const isSocketConnected = ref(false)

const route = useRoute();
const spaceId = route.params.spaceId as string;

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);
// const userStore = useUserStore();
// const { user } = storeToRefs(userStore);

const isColumnDialogOpen = ref(false)
const editingCol = ref<ColumnEvent | null>(null)

const isCardDialogOpen = ref(false)
const editingCard = ref<CardEvent | null>(null)

const isSaving = ref(false)
const targetColumnId = ref<string>('')

const isDeleteOpen = ref(false)
const deleteType = ref('')
const deleteData = ref<{cardId: string, columnId: string} | null>(null)

const executeDelete = async () => {
    try {
        if (deleteType.value === 'column' && deleteData.value) {
            await deleteColumn(currentSpace.value.id, deleteData.value.columnId)
            columns.value = columns.value.filter(c => c.id !== deleteData.value?.columnId) // ← .columnId
        } else if (deleteType.value === 'card' && deleteData.value) {
            await deleteCard(currentSpace.value.id, deleteData.value.cardId)
        }
        isDeleteOpen.value = false
        deleteData.value = null
    } catch (e) {
        console.error("Lỗi:", e)
    }
}

//-------------- Column ---------------
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
    isSaving.value = true
    try {
        if (editingCol.value) await updateColumn(currentSpace.value.id, editingCol.value.id, data.title)
        else await createColumn(currentSpace.value.id, data.title)

        isColumnDialogOpen.value = false
    } catch (e) {
        console.error("Lỗi:", e)

        const error = e as any
        alert("Lỗi: " + (error.response?.data?.message || error.message))
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
    if(event.moved){
        const columnId = event.moved.element.id;
        const newPosition = event.moved.newIndex;

        try {
            await moveColumn(currentSpace.value.id, columnId, newPosition);
        } catch (error) {
            console.error("Lỗi di chuyển cột:", error);
        }
    }
}

const fetchColumns = async (spaceId: string) => {
    try {
        const res = await getAllColumns(spaceId);
        columns.value = res.data;
        console.log(res.data);
    } catch (e) {
        console.error("Lỗi tải cột:", e)
    }
}

// -------------- Card ---------------
const openAddCardDialog = (columnId: string) => {
    targetColumnId.value = columnId
    editingCard.value = null
    isCardDialogOpen.value = true
}

const openEditCardDialog = (columnId: string, card: any) => {
    targetColumnId.value = columnId
    editingCard.value = card
    isCardDialogOpen.value = true
}

const handleSaveCard = async (data: { title: string, description: string }) => {
    try {
        const payload = {
            columnId: targetColumnId.value,
            title: data.title,
            description: data.description,
        }

        if (editingCard.value) {
            await updateCard(currentSpace.value.id, editingCard.value.id, payload)
        } else {
            await createCard(currentSpace.value.id, payload)
        }

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
    try {
        let cardId = '';
        let newPosition = 0;

        if (event.moved) {
            // Di chuyển trong cùng 1 cột
            cardId = event.moved.element.id;
            newPosition = event.moved.newIndex;
        } else if (event.added) {
            // Kéo từ cột khác bỏ vào cột này
            cardId = event.added.element.id;
            newPosition = event.added.newIndex;
        } else {
            // Trường hợp 'removed' thì không cần gọi API move vì 'added' ở cột kia sẽ lo
            return;
        }

        const payload = {
            targetColumnId: currentColumnId,
            newPosition: newPosition
        };

        await moveCard(currentSpace.value.id, cardId, payload);
    } catch (error) {
        console.error("Lỗi di chuyển card: ", error);
        // Nếu lỗi, bạn nên fetch lại columns để UI đồng bộ lại với DB
        await fetchColumns(currentSpace.value.id);
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
    await fetchColumns(spaceId);
    await subscribeTospace(spaceId);

}

const subscribeTospace = async (spaceId: string) => {
    socketService.connect();

    taskSocket.subscribeCardCreate(spaceId, (card) => {
        console.log("Card mới:", card);

        const col = columns.value.find(c => c.id === card.columnId);
        if (col) {
            col.cards = col.cards || [];
            col.cards.push(card);
        }

    });

    taskSocket.subscribeCardUpdate(spaceId, (card) => {
        console.log("Card cập nhật:", card);
        const col = columns.value.find(c => c.id === card.columnId);
        if (col && col.cards) {
            const index = col.cards.findIndex(t => t.id === card.id);
            if (index !== -1) {
                col.cards[index] = card;
            }
        }
    });

    taskSocket.subscribeCardDelete(spaceId, (cardId) => {
        console.log("Card bị xóa:", cardId);
        columns.value.forEach(col => {
            if (col.cards) {
                const index = col.cards.findIndex(t => t.id === cardId);
                if (index !== -1) {
                    col.cards.splice(index, 1);
                }
            }
        });
    });

    taskSocket.subscribeCardMove(spaceId, (payload: CardMovePayload) => {
        console.log("Card di chuyển: ", payload);
        const targetCol = columns.value.find(c => c.id === payload.targetColumnId);
        if (targetCol) {
            targetCol.cards = payload.targetCards
        };

        if (payload.sourceColumnId && payload.sourceCards) {
            const sourceCol = columns.value.find(c => c.id === payload.sourceColumnId);
            if (sourceCol) {
                sourceCol.cards = payload.sourceCards;
            }
        };
    });

    taskSocket.subscribeColumnCreate(spaceId, (column) => {
        console.log("Cột mới:", column);
        columns.value.push(column);
    });

    taskSocket.subscribeColumnUpdate(spaceId, (column) => {
        console.log("Cột cập nhật:", column)
        const index = columns.value.findIndex(c => c.id === column.id);
        if (index !== -1) {
            columns.value[index] = column;
        }
    })

    taskSocket.subscribeColumnDelete(spaceId, (columnId) => {
        console.log("Cột bị xóa:", columnId)
        const index = columns.value.findIndex(c => c.id === columnId);
        if (index !== -1) {
            columns.value.splice(index, 1);
        }
    })

    taskSocket.subscribeColumnMove(spaceId, (col) => {
        console.log("Cột di chuyển:", col)
        
        const columnId = col.id
        const newPosition = col.position

        const oldIndex = columns.value.findIndex(c => c.id === columnId);
        if (oldIndex === -1) return;

        const [movedItem] = columns.value.splice(oldIndex, 1);
        if (movedItem) {
            columns.value.splice(newPosition, 0, movedItem);
        }
    });

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
                        <TaskColumn
                            :column="col"
                            :space-name="currentSpace?.name ?? ''"
                            @edit-column="openEditColumnDialog"
                            @delete-column="confirmDeleteColumn"
                            @add-card="openAddCardDialog"
                            @edit-card="openEditCardDialog"
                            @delete-card="confirmDeleteCard"
                            @card-move="onCardMove"
                        />
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

    <CardFormDialog v-model:open="isCardDialogOpen" :columnId="targetColumnId" :taskData="editingCard" @save="handleSaveCard" />
    <ColumnFormDialog v-model:open="isColumnDialogOpen" :column-data="editingCol" @save="handleSaveColumn" />
    <DeleteConfirmDialog
        v-model:open="isDeleteOpen"
        :title="deleteType === 'column' ? 'Xóa cột này?' : 'Xóa thẻ này?'"
        :description="deleteType === 'column' ? 'Toàn bộ thẻ trong cột này sẽ bị mất.' : 'Bạn không thể khôi phục thẻ này sau khi xóa.'"
        @confirm="executeDelete"
    />
</template>

<style scoped></style>