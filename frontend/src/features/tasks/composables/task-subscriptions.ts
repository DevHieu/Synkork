import { taskSocket } from "@/features/tasks/services/taskSocket";
import type { CardEvent, CardMovePayload } from "@/types/Task";
import { useTaskStore } from "../stores/taskStore";
import { socketService } from "@/services/websocket/socketService";

export const subscribeToSpace = async (spaceId: string) => {
    const taskStore = useTaskStore();
    const { columns, archivedColumns, archivedCards } = taskStore;

    socketService.connect();
    taskSocket.subscribeCardCreate(spaceId, (card: CardEvent) => {
        console.log("Card created:", card);

        const col = columns.find(c => c.id === card.columnId);
        if (col) {
            col.cards = col.cards || [];
            col.cards.push(card);
        }
    });

    taskSocket.subscribeCardUpdate(spaceId, (card: CardEvent) => {
        console.log("Card updated:", card);

        const col = columns.find(c => c.id === card.columnId);
        if (col && col.cards) {
            const i = col.cards.findIndex(t => t.id === card.id);
            if (i !== -1) {
                col.cards[i] = card;
            }
        }
    });

    taskSocket.subscribeCardDelete(spaceId, (cardId: string) => {
        console.log("Card deleted:", cardId);

        columns.forEach(col => {
            if (col.cards) {
                const i = col.cards.findIndex(t => t.id === cardId);
                if (i !== -1) {
                    col.cards.splice(i, 1);
                }
            }
        })
    });

    taskSocket.subscribeCardMove(spaceId, (payload: CardMovePayload) => {
        console.log("Card moved:", payload);

        const targetCol = columns.find(c => c.id === payload.targetColumnId);
        if (targetCol) {
            targetCol.cards = payload.targetCards;
        }

        if (payload.sourceColumnId && payload.sourceCards) {
            const sourceCol = columns.find(c => c.id === payload.sourceColumnId);
            if (sourceCol) {
                sourceCol.cards = payload.sourceCards;
            }
        }
    });

    taskSocket.subscribeCardArchive(spaceId, (card: CardEvent) => {
        console.log("Card archived:", card);

        columns.forEach(col => {
            if (!col.cards) return;
            const i = col.cards.findIndex(t => t.id === card.id)
            if (i !== -1) {
                col.cards.splice(i, 1);
            }
        })

        archivedCards.unshift(card);
    });

    taskSocket.subscribeCardUnarchive(spaceId, (card: CardEvent) => {
        console.log("Card unarchived:", card);

        const i = archivedCards.findIndex(t => t.id === card.id)
        if (i !== -1) {
            archivedCards.splice(i, 1);
        }

        const col = columns.find(c => c.id === card.columnId)
        if (col) {
            col.cards = col.cards || []
            col.cards.push(card)
            col.cards.sort((a, b) => a.position - b.position)
        }
    });

    taskSocket.subscribeCardDeleteArchived(spaceId, (card) => {
        console.log("All archived card deleted: ", card)
        const i = archivedCards.findIndex(c => c.id === card.id)
        if (i !== -1) {
            archivedCards.splice(i, 1)
        }
    });

    taskSocket.subscribeColumnCreate(spaceId, (col) => {
        console.log("Column created:", col);
        columns.push(col);
    });

    taskSocket.subscribeColumnUpdate(spaceId, (col) => {
        console.log("Column updated:", col)
        const i = columns.findIndex(c => c.id === col.id);
        if (i !== -1) {
            columns[i] = col;
        }
    });

    taskSocket.subscribeColumnDelete(spaceId, (columnId) => {
        console.log("Column deleted:", columnId)
        const i = columns.findIndex(c => c.id === columnId);
        if (i !== -1) {
            columns.splice(i, 1);
        }
    });

    taskSocket.subscribeColumnMove(spaceId, (col) => {
        console.log("Column moved:", col)
        const columnId = col.id
        const newPosition = col.position

        const oldIndex = columns.findIndex(c => c.id === columnId);
        if (oldIndex === -1) return;

        const [movedItem] = columns.splice(oldIndex, 1);
        if (movedItem) {
            columns.splice(newPosition, 0, movedItem);
        }
    });


    taskSocket.subscribeColumnArchive(spaceId, (col) => {
        console.log("Column archired: ", col)
        const i = columns.findIndex(c => c.id === col.id)
        if (i !== -1) {
            columns.splice(i, 1)
        }
        archivedColumns.unshift(col)
    });

    taskSocket.subscribeColumnUnarchive(spaceId, (col) => {
        console.log("Column unarchived: ", col)
        const archivedIndex = archivedColumns.findIndex(c => c.id === col.id)
        if (archivedIndex !== -1) {
            archivedColumns.splice(archivedIndex, 1)
        }

        const archivedCardIds = new Set(archivedCards.map(c => c.id))
        columns.push({
            ...col,
            cards: (col.cards ?? []).filter((c: CardEvent) => !archivedCardIds.has(c.id)),
        })
        columns.sort((a, b) => a.position - b.position)
    });

    taskSocket.subscribeColumnDeleteArchived(spaceId, (col) => {
        console.log("All archived column deleted: ", col)
        const i = archivedColumns.findIndex(c => c.id === col.id)
        if (i !== -1) {
            archivedColumns.splice(i, 1)
        }
    });

    taskSocket.subscribeCardComplete(spaceId, (c) => {
        console.log("Card completed: ", c);
        columns.forEach(col => {
            const i = col.cards.findIndex(card => card.id === c.id)
            if (i !== -1) {
                col.cards[i] = c
            }
        });
    });

    taskSocket.subscribeCardUncomplete(spaceId, (c) => {
        console.log("Card uncompleted: ", c);
        taskStore.columns.forEach(col => {
            const i = col.cards.findIndex(card => card.id === c.id)

            if (i !== -1) {
                col.cards[i] = c
            }
        })
    });

}


