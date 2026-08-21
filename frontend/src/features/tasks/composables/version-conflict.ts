import { reactive, ref } from "vue"
import { toast } from "vue-sonner"

export interface VersionConflict<T> {
    entityLabel: string
    createCopy: (data: T) => Promise<void>
}

export function useVersionConflict<T>({ entityLabel, createCopy }: VersionConflict<any>) {
    const isConflictOpen = ref(false)
    const isCreatingCopy = ref(false)
    const conflictData = ref<T | null>(null)

    const openConflict = (data: T) => {
        conflictData.value = data
        isConflictOpen.value = true
    }

    const handleCreateCopy = async () => {
        if (!conflictData.value) return
        isCreatingCopy.value = true
        try {
            await createCopy(conflictData.value)
            toast.success(`Đã tạo ${entityLabel} mới với nội dung bạn vừa nhập.`)
        } catch (e) {
            console.error(`Lỗi tạo ${entityLabel} mới:`, e)
            toast.error(`Không thể tạo ${entityLabel} mới, vui lòng thử lại.`)
        } finally {
            isCreatingCopy.value = false
            isConflictOpen.value = false
            conflictData.value = null
        }
    }

    const handleDiscard = () => {
        isConflictOpen.value = false
        conflictData.value = null
    }

    return reactive({
        isConflictOpen,
        isCreatingCopy,
        conflictData,
        openConflict,
        handleCreateCopy,
        handleDiscard
    })
}