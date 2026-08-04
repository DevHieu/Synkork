import type { ColumnEvent, CardEvent } from "@/types/Task"
import { ref } from "vue"

export function useTaskDialogs() {

    const isColumnDialogOpen = ref(false)
    const editingCol = ref<ColumnEvent | null>(null)

    const isCardDialogOpen = ref(false)
    const editingCard = ref<CardEvent | null>(null)

    const targetColumnId = ref('')

    const openColumnDialog = (col: ColumnEvent | null) => {
        editingCol.value = col
        isColumnDialogOpen.value = true
    }

    const openCardDialog = (columnId:string) => {
        targetColumnId.value = columnId
        editingCard.value = null
        isCardDialogOpen.value = true
    }

    return {
        isColumnDialogOpen,
        editingCol,
        isCardDialogOpen,
        editingCard,

        targetColumnId,

        openColumnDialog,
        openCardDialog
    }
}