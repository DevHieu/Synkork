import type { CardEvent, CardMovePayload, ColumnEvent, TaskMoveEvent } from "@/types/Task";
import { defineStore } from "pinia";
import { getAllColumns, createColumn, updateColumn, deleteColumn, moveColumn, unarchiveColumn, archiveColumn, getArchivedColumns, deleteAllArchivedColumns, ColumnVersionConflictError } from '@/features/tasks/services/columnService'
import { archiveCard, createCard, deleteAllArchivedCards, deleteCard, getArchivedCards, moveCard, unarchiveCard, updateCard, VersionConflictError } from "@/features/tasks/services/cardService";
import { socketService } from "@/services/websocket/socketService";
import { taskSocket } from "@/features/tasks/services/taskSocket";

export const useTaskStore = defineStore("task", {
    state: () => ({
        tasks: [] as CardEvent[],
        columns: [] as ColumnEvent[],
        archivedCards: [] as CardEvent[],
        archivedColumns: [] as ColumnEvent[],
        loading: false,
    }),

    actions: {
        // async subscribeToSpace(spaceId: string) {
        //     socketService.connect();

        //     taskSocket.subscribeCardCreate(spaceId, (card) => {
        //         console.log("Card mới:", card);

        //         const col = this.columns.find(c => c.id === card.columnId);
        //         if (col) {
        //             col.cards = col.cards || [];
        //             col.cards.push(card);
        //         }
        //     });

        //     taskSocket.subscribeCardUpdate(spaceId, (card) => {
        //         console.log("Card cập nhật:", card);
        //         const col = this.columns.find(c => c.id === card.columnId);
        //         if (col && col.cards) {
        //             const index = col.cards.findIndex(t => t.id === card.id);
        //             if (index !== -1) {
        //                 col.cards[index] = card;
        //             }
        //         }
        //     });

        //     taskSocket.subscribeCardDelete(spaceId, (cardId) => {
        //         console.log("Card bị xóa:", cardId);
        //         this.columns.forEach(col => {
        //             if (col.cards) {
        //                 const index = col.cards.findIndex(t => t.id === cardId);
        //                 if (index !== -1) {
        //                     col.cards.splice(index, 1);
        //                 }
        //             }
        //         });
        //     });

        //     taskSocket.subscribeCardMove(spaceId, (payload: CardMovePayload) => {
        //         console.log("Card di chuyển: ", payload);
        //         const targetCol = this.columns.find(c => c.id === payload.targetColumnId);
        //         if (targetCol) {
        //             targetCol.cards = payload.targetCards
        //         };

        //         if (payload.sourceColumnId && payload.sourceCards) {
        //             const sourceCol = this.columns.find(c => c.id === payload.sourceColumnId);
        //             if (sourceCol) {
        //                 sourceCol.cards = payload.sourceCards;
        //             }
        //         };
        //     });

        //     taskSocket.subscribeCardArchive(spaceId, (card) => {
        //         console.log("Card đã được lưu trữ: ", card)
        //         this.columns.forEach(col => {
        //             if (!col.cards) return
        //             const index = col.cards.findIndex(
        //                 c => c.id === card.id
        //             )
        //             if (index !== -1) {
        //                 col.cards.splice(index, 1)
        //             }
        //         })
        //         if (!this.archivedCards) {
        //             this.archivedCards = []
        //         }
        //         this.archivedCards.unshift(card)
        //     });

        //     taskSocket.subscribeCardUnarchive(spaceId, (card) => {
        //         console.log("Card đã được khôi phục: ", card)
        //         const archivedIndex = this.archivedCards.findIndex(c => c.id === card.id)
        //         if (archivedIndex !== -1) {
        //             this.archivedCards.splice(archivedIndex, 1)
        //         }
        //         const col = this.columns.find(c => c.id === card.columnId)
        //         if (col) {
        //             col.cards = col.cards || []
        //             col.cards.push(card)
        //             col.cards.sort((a, b) => a.position - b.position)
        //         }
        //     });

        //     taskSocket.subscribeCardDeleteArchived(spaceId, (card) => {
        //         console.log("Tất cả thẻ đã lưu trữ bị xóa: ", card)
        //         const index = this.archivedCards.findIndex(c => c.id === card.id)
        //         if (index !== -1) {
        //             this.archivedCards.splice(index, 1)
        //         }
        //     });

        //     taskSocket.subscribeColumnDeleteArchived(spaceId, (column) => {
        //         console.log("Tất cả cột đã lưu trữ bị xóa: ", column)
        //         const index = this.archivedColumns.findIndex(c => c.id === column.id)
        //         if (index !== -1) {
        //             this.archivedColumns.splice(index, 1)
        //         }
        //     });

        //     taskSocket.subscribeColumnCreate(spaceId, (column) => {
        //         console.log("Cột mới:", column);
        //         this.columns.push(column);
        //     });

        //     taskSocket.subscribeColumnUpdate(spaceId, (column) => {
        //         console.log("Cột cập nhật:", column)
        //         const index = this.columns.findIndex(c => c.id === column.id);
        //         if (index !== -1) {
        //             this.columns[index] = column;
        //         }
        //     });

        //     taskSocket.subscribeColumnDelete(spaceId, (columnId) => {
        //         console.log("Cột bị xóa:", columnId)
        //         const index = this.columns.findIndex(c => c.id === columnId);
        //         if (index !== -1) {
        //             this.columns.splice(index, 1);
        //         }
        //     });

        //     taskSocket.subscribeColumnMove(spaceId, (col) => {
        //         console.log("Cột di chuyển:", col)
        //         const columnId = col.id
        //         const newPosition = col.position

        //         const oldIndex = this.columns.findIndex(c => c.id === columnId);
        //         if (oldIndex === -1) return;

        //         const [movedItem] = this.columns.splice(oldIndex, 1);
        //         if (movedItem) {
        //             this.columns.splice(newPosition, 0, movedItem);
        //         }
        //     });


        //     taskSocket.subscribeColumnArchive(spaceId, (column) => {
        //         console.log("Cột đã được lưu trữ: ", column)
        //         const index = this.columns.findIndex(
        //             c => c.id === column.id
        //         )
        //         if (index !== -1) {
        //             this.columns.splice(index, 1)
        //         }
        //         this.archivedColumns.unshift(column)
        //     });

        //     taskSocket.subscribeColumnUnarchive(spaceId, (column) => {
        //         console.log("Cột đã được khôi phục: ", column)
        //         const archivedIndex = this.archivedColumns.findIndex(c => c.id === column.id)
        //         if (archivedIndex !== -1) {
        //             this.archivedColumns.splice(archivedIndex, 1)
        //         }
        //         this.columns.push(column)
        //         this.columns.sort((a, b) => a.position - b.position)
        //     })
        // },

        // async fetchTasks(spaceId: string) {
        //     this.loading = true;
        //     try {
        //         const res = await getAllColumns(spaceId);
        //         this.columns = res.data;
        //         console.log(res.data);
        //     } catch (e) {
        //         console.error("Lỗi tải cột:", e)
        //     } finally {
        //         this.loading = false
        //     }
        // },

        // async saveColumn(spaceId: string, columnId: string, title: string, version?: number) {
        //     if (!columnId) {
        //         await createColumn(spaceId, title)
        //         return
        //     }
        //     try {
        //         await updateColumn(spaceId, columnId, { name: title, version })
        //     } catch (e) {
        //         if (e instanceof ColumnVersionConflictError) {
        //             const idx = this.columns.findIndex(c => c.id === columnId)
        //             if (idx !== -1 && e.latest) {
        //                 this.columns[idx] = e.latest
        //             }
        //         }
        //         throw e
        //     }
        // },

        // async moveColumn(spaceId: string, event: TaskMoveEvent) {
        //     if (!event.moved) return

        //     const columnId = event.moved.element.id;
        //     const newPosition = event.moved.newIndex;

        //     await moveColumn(spaceId, columnId, newPosition);
        // },

        //hàm setCards để cập nhật danh sách thẻ trong một cột cụ thể
        // async setCards(columnIndex: number, cards: CardEvent[]) {
        //     if (this.columns[columnIndex]) {
        //         this.columns[columnIndex].cards = cards
        //     }
        // },

        // taskStore.ts
// async saveCard(spaceId: string, cardId: string, columnId: string, title: string, description: string, assigneeIds: string[] = [], dueDate?: string, version?: number) {
//     if (!cardId) {
//         await createCard(spaceId, { columnId, title, description })
//         return
//     }
//     try {
//         await updateCard(spaceId, cardId, {
//             title, description, assigneeIds, dueDate,
//             version   // ✅ dùng đúng version được truyền vào từ lúc mở form, không tra lại store
//         })
//     } catch (e) {
//         if(e instanceof VersionConflictError) {
//             //hàm này để xử lý khi có lỗi version conflict khi cập nhật thẻ, nó sẽ tìm kiếm thẻ trong danh sách cột và cập nhật thẻ mới nhất từ lỗi
//                 const i = this.tasks.findIndex(c => c.id === cardId);
                
//                 if(i !== -1 && e.latest) this.tasks[i]  = e.latest;
//             } 
//         throw e
//     }
// },

//         async moveCard(spaceId: string, columnId: string, event: TaskMoveEvent) {
//             //sao ở đây lại dùng event.moved || event.added để lấy thẻ di chuyển, vì có thể thẻ được thêm vào cột mới hoặc di chuyển trong cùng cột
//             const movedCard = event.moved || event.added
//             if (!movedCard) return

//             const payload = {
//                 targetColumnId: columnId,
//                 newPosition: movedCard.newIndex
//             }

//             await moveCard(spaceId, movedCard.element.id, payload);
//         },

        // async delete(deleteType: "column" | "card", spaceId: string, deleteData: { columnId?: string, cardId?: string } | null) {
        //     try {
        //         if (deleteType === 'column' && deleteData?.columnId) {
        //             await deleteColumn(spaceId, deleteData.columnId)
        //             this.columns = this.columns.filter(c => c.id !== deleteData.columnId)
        //             this.archivedColumns = this.archivedColumns.filter(c => c.id !== deleteData.columnId)
        //         } else if (deleteType === 'card' && deleteData?.cardId) {
        //             await deleteCard(spaceId, deleteData.cardId)
        //             this.archivedCards = this.archivedCards.filter(c => c.id !== deleteData.cardId)
        //         }

        //     } catch (e) {
        //         console.error("Lỗi:", e)
        //     }
        // },

//         async archiveCard(spaceId: string, cardId: string) {
//             await archiveCard(spaceId, cardId)
//         },

//         async archiveColumn(spaceId: string, columnId: string) {
//             await archiveColumn(spaceId, columnId)
//         },

//         async unarchiveCard(spaceId: string, cardId: string) {
//             await unarchiveCard(spaceId, cardId)
//         },

//         async unarchiveColumn(spaceId: string, columnId: string) {
//             await unarchiveColumn(spaceId, columnId)
//         },

//         async fetchArchivedItems(spaceId: string) {
//             const [cardsRes, colsRes] = await Promise.all([
//                 getArchivedCards(spaceId),
//                 getArchivedColumns(spaceId)
//             ])

//             this.archivedCards = cardsRes
//             this.archivedColumns = colsRes.data
//             return { cards: cardsRes.data, columns: colsRes.data }
            
//         },

        // async deleteAllArchived(deleteAllType: "columns" | "cards", spaceId: string) {
        //     try {
        //         if (deleteAllType === 'columns') {
        //             await deleteAllArchivedColumns(spaceId)
        //             this.archivedColumns = []
        //         } else if (deleteAllType === 'cards') {
        //             await deleteAllArchivedCards(spaceId)
        //             this.archivedCards = []
        //         }
        //     } catch (e) {
        //         console.error("Lỗi:", e)
        //     }
        // },
    }
});
                    