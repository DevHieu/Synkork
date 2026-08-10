import type { TaskMoveEvent } from "@/features/tasks/types/Task";
import { useTaskStore } from "../stores/taskStore";
import { storeToRefs } from "pinia";
import {
    ColumnVersionConflictError,
    createColumn,
    getAllColumns,
    updateColumn,
    moveColumn,
    deleteColumn,
    getArchivedColumns,
    archiveColumn,
    deleteAllArchivedColumns,
    unarchiveColumn,
} from "../services/columnService";
import { archiveCard, completeCard, createCard, deleteAllArchivedCards, deleteCard, getArchivedCards, moveCard, unarchiveCard, updateCard, VersionConflictError } from "../services/cardService";

export function useTaskAction() {
    const taskStore = useTaskStore();
    const { tasks, columns, archivedCards, archivedColumns, loading } = storeToRefs(taskStore);

    const fetchTasks = async (spaceId: string) => {
        loading.value = true;
        try {
            const res = await getAllColumns(spaceId);
            columns.value = res.data;
            console.log(res.data);
        } catch (e) {
            console.error("Error fetch column: ", e);
        } finally {
            loading.value = false;
        }
    }

    const saveColumn = async (spaceId: string, columnId: string, title: string, version?: number) => {
        loading.value = true;
        if (!columnId) {
            await createColumn(spaceId, title);
            return;
        }
        try {
            const res = await updateColumn(spaceId, columnId, { name: title, version });
            const i = columns.value.findIndex(c => c.id === columnId);
            if(i!== -1 && res?.data) columns.value[i] = { ...columns.value[i], ...res.data };
        } catch (e) {
            if (e instanceof ColumnVersionConflictError) {
                const i = columns.value.findIndex(c => c.id === columnId);

                if (i !== -1 && e.latest) columns.value[i] = e.latest;
            } throw e
        } finally {
            loading.value = false;
        }
    }

    const moveColumnEvent = async (spaceId: string, event: TaskMoveEvent) => {
        if (!event.moved) return;

        const columnId = event.moved.element.id;
        const newPosition = event.moved.newIndex;

        await moveColumn(spaceId, columnId, newPosition);
    }

    const saveCard = async (spaceId: string, cardId: string, columnId: string, title: string, description: string, assigneeIds: string[] = [], dueDate?: string, version?: number) => {
        loading.value = true;
        if (!cardId) {
            await createCard(spaceId, { columnId, title, description });
            return;
        }
        try {
            const res = await updateCard(spaceId, cardId, { title, description, assigneeIds, dueDate, version });
            if (res) {
                const col = columns.value.find(c => c.id === (res.columnId ?? columnId));
                if (col?.cards) {
                    const i = col.cards.findIndex(c => c.id === cardId);
                    if (i !== -1) col.cards[i] = { ...col.cards[i], ...res };
                }
            }
        } catch (e) {
            if (e instanceof VersionConflictError) {
                const i = tasks.value.findIndex(c => c.id === cardId);

                if (i !== -1 && e.latest) tasks.value[i] = e.latest;
            } throw e
        } finally {
            loading.value = false;
        }
    }

    const moveCardEvent = async (spaceId: string, targetColumnId: string, event: TaskMoveEvent) => {
        const movedCard = event.moved || event.added;
        if (!movedCard) return;

        const cardId = movedCard.element.id;
        const newPosition = movedCard.newIndex;

        await moveCard(spaceId, cardId, { targetColumnId, newPosition });
    }

    const deleteTask = async (deleteType: 'column' | 'card', spaceId: string, deleteData: { columnId?: string, cardId?: string } | null) => {
        try {
            if (deleteType === 'card' && deleteData?.cardId) {
                await deleteCard(spaceId, deleteData.cardId);
                archivedCards.value = archivedCards.value.filter(c => c.id !== deleteData.cardId);
            } else if (deleteType === 'column' && deleteData?.columnId) {
                await deleteColumn(spaceId, deleteData.columnId);
                columns.value = columns.value.filter(c => c.id !== deleteData.columnId);
                archivedColumns.value = archivedColumns.value.filter(c => c.id !== deleteData.columnId);
            }
        } catch (e) {
            console.error(`Error deleting ${deleteType}: `, e);
        }
    }

    const archiveCardEvent = async (spaceId: string, cardId: string) => {
        await archiveCard(spaceId, cardId)
    }

    const archiveColumnEvent = async (spaceId: string, columnId: string) => {
        await archiveColumn(spaceId, columnId)
    }

    const unarchiveCardEvent = async (spaceId: string, cardId: string) => {
        await unarchiveCard(spaceId, cardId)
    }

    const unarchiveColumnEvent = async (spaceId: string, columnId: string) => {
        await unarchiveColumn(spaceId, columnId)
    }

    const fetchArchivedItems = async (spaceId: string) => {
        if(!spaceId) return

        try {
            const [cardsRes, colsRes] = await Promise.all([
                getArchivedCards(spaceId),
                getArchivedColumns(spaceId)
            ])

            archivedCards.value = cardsRes
            archivedColumns.value = colsRes.data
            return { cards: cardsRes.data, columns: colsRes.data }
        } catch (error) {
            console.error("Lỗi load archive:", error)
        }
        
    }

    const deleteAllArchived = async (deleteAllType: "columns" | "cards", spaceId: string) => {
        try {
            if (deleteAllType === 'columns') {
                await deleteAllArchivedColumns(spaceId)
                archivedColumns.value = []
            } else if (deleteAllType === 'cards') {
                await deleteAllArchivedCards(spaceId)
                archivedCards.value = []
            }
        } catch (e) {
            console.error("Error:", e)
        }
    }

    const completeCardEvent = async (spaceId: string, cardId: string, completed: boolean) => {
        await completeCard(spaceId, cardId, completed);
    }

    return {
        fetchTasks,
        saveColumn,
        moveColumnEvent,
        saveCard,
        moveCardEvent,
        deleteTask,
        archiveCardEvent,
        archiveColumnEvent,
        unarchiveCardEvent,
        unarchiveColumnEvent,
        fetchArchivedItems,
        deleteAllArchived,
        completeCardEvent
    }

}

