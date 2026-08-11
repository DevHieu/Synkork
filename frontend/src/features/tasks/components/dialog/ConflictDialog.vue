<script setup lang="ts">
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { computed } from 'vue'

const MAX_ITEM_NAME_LENGTH = 60

const props = withDefaults(defineProps<{
    open: boolean
    isCreatingCopy: boolean
    entityLabel: string
    entityTitle: string
    itemName?: string
}>(), {
    itemName: undefined,
})

const displayItemName = computed(() => {
    if (!props.itemName) return props.itemName
    return props.itemName.length > MAX_ITEM_NAME_LENGTH
        ? `${props.itemName.slice(0, MAX_ITEM_NAME_LENGTH)}…`
        : props.itemName
})

const emit = defineEmits<{
    'update:open': [value: boolean]
    discard: []
    createCopy: []
}>()
</script>

<template>
    <Dialog :open="open" @update:open="emit('update:open', $event)">
        <DialogContent>
            <DialogHeader>
                <DialogTitle>{{ entityTitle }} đã bị thay đổi bởi người khác</DialogTitle>
                <DialogDescription class="break-words">
                    Trong lúc bạn chỉnh sửa, một người khác đã lưu thay đổi cho {{ entityLabel }}
                    <strong v-if="itemName" class="break-all" :title="itemName">"{{ displayItemName }}"</strong>. Nếu lưu đè, nội dung của họ sẽ bị mất.
                    Bạn có muốn tạo một {{ entityLabel }} mới chứa nội dung bạn vừa nhập, để không mất dữ liệu không?
                </DialogDescription>
            </DialogHeader>
            <DialogFooter>
                <Button variant="outline" :disabled="isCreatingCopy" @click="emit('discard')">
                    Bỏ qua, xem bản mới nhất
                </Button>
                <Button :disabled="isCreatingCopy" @click="emit('createCopy')">
                    {{ isCreatingCopy ? 'Đang tạo...' : `Tạo ${entityLabel} mới với nội dung của tôi` }}
                </Button>
            </DialogFooter>
        </DialogContent>
    </Dialog>
</template>