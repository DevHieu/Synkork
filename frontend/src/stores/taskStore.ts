import type { CardEvent, CardMovePayload, ColumnEvent, TaskMoveEvent } from "@/types/Task";
import { defineStore } from "pinia";
import { getAllColumns, createColumn, updateColumn, deleteColumn, moveColumn } from '@/services/task/columnService'
import { createCard, deleteCard, moveCard, updateCard } from "@/services/task/cardService";
import { socketService } from "@/services/websocket/socketService";
import { taskSocket } from "@/services/websocket/taskSocket";

export const useTaskStore = defineStore("task", {
    state: () => ({
        tasks: [] as CardEvent[],
        columns: [] as ColumnEvent[],
        loading: false,
    }),

    actions: {
        async subscribeTospace (spaceId: string) {
            socketService.connect();

            taskSocket.subscribeCardCreate(spaceId, (card) => {
                console.log("Card mới:", card);

                const col = this.columns.find(c => c.id === card.columnId);
                if (col) {
                    col.cards = col.cards || [];
                    col.cards.push(card);
                }
            });

            taskSocket.subscribeCardUpdate(spaceId, (card) => {
                console.log("Card cập nhật:", card);
                const col = this.columns.find(c => c.id === card.columnId);
                if (col && col.cards) {
                    const index = col.cards.findIndex(t => t.id === card.id);
                    if (index !== -1) {
                        col.cards[index] = card;
                    }
                }
            });

            taskSocket.subscribeCardDelete(spaceId, (cardId) => {
                console.log("Card bị xóa:", cardId);
                this.columns.forEach(col => {
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
                const targetCol = this.columns.find(c => c.id === payload.targetColumnId);
                if (targetCol) {
                    targetCol.cards = payload.targetCards
                };

                if (payload.sourceColumnId && payload.sourceCards) {
                    const sourceCol = this.columns.find(c => c.id === payload.sourceColumnId);
                    if (sourceCol) {
                        sourceCol.cards = payload.sourceCards;
                    }
                };
            });

            taskSocket.subscribeColumnCreate(spaceId, (column) => {
                console.log("Cột mới:", column);
                this.columns.push(column);
            });

            taskSocket.subscribeColumnUpdate(spaceId, (column) => {
                console.log("Cột cập nhật:", column)
                const index = this.columns.findIndex(c => c.id === column.id);
                if (index !== -1) {
                    this.columns[index] = column;
                }
            })

            taskSocket.subscribeColumnDelete(spaceId, (columnId) => {
                console.log("Cột bị xóa:", columnId)
                const index = this.columns.findIndex(c => c.id === columnId);
                if (index !== -1) {
                    this.columns.splice(index, 1);
                }
            })

            taskSocket.subscribeColumnMove(spaceId, (col) => {
                console.log("Cột di chuyển:", col)    
                const columnId = col.id
                const newPosition = col.position

                const oldIndex = this.columns.findIndex(c => c.id === columnId);
                if (oldIndex === -1) return;

                const [movedItem] = this.columns.splice(oldIndex, 1);
                if (movedItem) {
                    this.columns.splice(newPosition, 0, movedItem);
                }
            });
        },

        async fetchTasks(spaceId: string) {
            this.loading = true;
            try {
                const res = await getAllColumns(spaceId);
                this.columns = res.data;
                console.log(res.data);
            } catch (e) {
                console.error("Lỗi tải cột:", e)
            } finally {
                this.loading = false
            }
        },

        async saveColumn(spaceId: string, columnId: string, title: string) {
            if (columnId) await updateColumn(spaceId, columnId, title)
            else await createColumn(spaceId, title)
        },

        async moveColumn(spaceId: string, event: TaskMoveEvent) {
            if(!event.moved) return
    
            const columnId = event.moved.element.id;
            const newPosition = event.moved.newIndex;

            await moveColumn(spaceId, columnId, newPosition);        
        },

        async setCards(columnIndex: number, cards: CardEvent[]) {
            if (this.columns[columnIndex]) {
                this.columns[columnIndex].cards = cards
            }
        },

        async saveCard(spaceId: string, cardId: string, columnId: string, title: string, description: string, assigneeIds: string[] = [], dueDate?: string) {
            if(cardId) await updateCard(spaceId, cardId, { title, description, assigneeIds, dueDate })
            else await createCard(spaceId, { columnId, title, description })
        },
        
        async moveCard(spaceId: string, columnId: string, event: TaskMoveEvent) {
            const movedCard = event.moved || event.added
            if(!movedCard) return
            
            const payload = {
                targetColumnId: columnId,
                newPosition: movedCard.newIndex
            }
            
            await moveCard(spaceId, movedCard.element.id, payload);
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
    },

    
});