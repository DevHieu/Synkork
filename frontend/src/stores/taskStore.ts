import type { CardEvent, ColumnEvent } from "@/types/Task";
import { defineStore } from "pinia";
import { getAllColumns, createColumn, updateColumn, deleteColumn, moveColumn } from '@/services/task/columnService'
import { deleteCard } from "@/services/task/cardService";

export const useTaskStore = defineStore("task", {
    state: () => ({
        tasks: [] as CardEvent[],
        columns: [] as ColumnEvent[],
        loading: false,
    }),

    actions: {
        async fetchTasks(spaceId: string) {
            this.loading = true;
            try {
                const res = await getAllColumns(spaceId);
                this.columns = res.data;
                console.log(res.data);
            } catch (e) {
                console.error("Lỗi tải cột:", e)
            }
        },

        async delete(deleteType: "column" | "card", spaceId: string, deleteData: { columnId?: string, cardId?: string } | null) {
            try {
                if (deleteType === 'column' && deleteData?.columnId) {
                    await deleteColumn(spaceId, deleteData.columnId)
                    this.columns = this.columns.filter(c => c.id !== deleteData.columnId)
                } else if (deleteType === 'card' && deleteData?.cardId) {
                    await deleteCard(spaceId, deleteData.cardId)
                }
                
            } catch (e) {
                console.error("Lỗi:", e)
            }
        },

        async setCards(columnIndex: number, cards: CardEvent[]) {
        if (this.columns[columnIndex]) {
            this.columns[columnIndex].cards = cards
        }
    }
    },

    
});