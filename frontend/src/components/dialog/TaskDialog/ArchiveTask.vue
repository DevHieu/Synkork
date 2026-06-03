<script setup lang="ts">
import { ref, watch } from 'vue'
import { Archive, Trash2 } from 'lucide-vue-next'
import { storeToRefs } from 'pinia'
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from '@/components/ui/popover'

import { useTaskStore } from '@/stores/taskStore'
import { useSpaceStore } from '@/stores/spaceStore'
import { useRoute } from 'vue-router'

const props = defineProps<{
    open: boolean
}>()

const emit = defineEmits<{
    'update:open': [open: boolean]
    deleteColumn: [columnId: string]
    deleteCard: [columnId: string, cardId: string]
    deleteAllArchivedCards: []
    deleteAllArchivedColumns: []
}>()

const spaceStore = useSpaceStore()
const { currentSpace } = storeToRefs(spaceStore)

const taskStore = useTaskStore()
const { archivedColumns, archivedCards, columns } = storeToRefs(taskStore)

const archiveTab = ref<'columns' | 'cards'>('columns')
const unarchiveError = ref<string | null>(null)

const isDeleteOpen = ref(false)
const deleteType = ref<'column' | 'card'>('column')
const deleteData = ref<{ cardId: string, columnId: string } | null>(null)

const isDeleteAllOpen = ref(false)
const deleteAllType = ref<'columns' | 'cards'>('columns')

const route = useRoute();
const spaceId = route.params.spaceId as string;

const loadArchive = async () => {
    if (!currentSpace.value?.id) return

    try {
        await taskStore.fetchArchivedItems(currentSpace.value.id)
    } catch (err) {
        console.error(err)
    }
}

watch(
    () => props.open,
    async (open) => {
        if (open) {
            unarchiveError.value = null
            await loadArchive()
        }
    }
)

const handleDelete = () => {
    taskStore.delete(deleteType.value, spaceId, deleteData.value)
    isDeleteOpen.value = false
    deleteData.value = null
}

const handleDeleteAll = async () => {
    if (!currentSpace.value?.id) return

    try {
        if (deleteAllType.value === 'columns') {
            await Promise.all(
                archivedColumns.value.map(col =>
                    taskStore.delete('column', spaceId, { columnId: col.id, cardId: '' })
                )
            )
        } else {
            await Promise.all(
                archivedCards.value.map(card =>
                    taskStore.delete('card', spaceId, { columnId: card.columnId, cardId: card.id })
                )
            )
        }
        await loadArchive()
    } catch (err) {
        console.error(err)
    } finally {
        isDeleteAllOpen.value = false
    }
}

const confirmDeleteAll = (type: 'columns' | 'cards') => {
    deleteAllType.value = type
    isDeleteAllOpen.value = true
}

const handleUnarchiveColumn = async (columnId: string) => {
    if (!currentSpace.value?.id) return

    try {
        await taskStore.unarchiveColumn(currentSpace.value.id, columnId)
        await loadArchive()
    } catch (error: any) {
        unarchiveError.value =
            error?.response?.data?.message ?? 'Lỗi khôi phục cột'
    }
}

const handleUnarchiveCard = async (cardId: string) => {
    if (!currentSpace.value?.id) return

    try {
        await taskStore.unarchiveCard(currentSpace.value.id, cardId)
        await loadArchive()
    } catch (error: any) {
        unarchiveError.value =
            error?.response?.data?.message ?? 'Lỗi khôi phục thẻ'
    }
}
</script>

<template>
    <Popover :open="open" @update:open="emit('update:open', $event)">
        <PopoverTrigger as-child>
            <slot name="trigger" />
        </PopoverTrigger>

        <PopoverContent side="right" align="start" :side-offset="12" class="w-[420px] p-0">
            <!-- Header -->
            <div class="flex items-center gap-2 px-4 py-3 border-b">
                <Archive class="w-4 h-4 text-muted-foreground" />
                <h3 class="font-semibold text-sm">
                    Mục đã lưu trữ
                </h3>
            </div>

            <!-- Tabs -->
            <div class="flex border-b text-sm">
                <button @click="archiveTab = 'columns'" class="flex-1 py-2.5 font-medium" :class="archiveTab === 'columns'
                    ? 'border-b-2 border-primary text-primary'
                    : 'text-muted-foreground'
                    ">
                    Cột
                </button>

                <button @click="archiveTab = 'cards'" class="flex-1 py-2.5 font-medium" :class="archiveTab === 'cards'
                    ? 'border-b-2 border-primary text-primary'
                    : 'text-muted-foreground'
                    ">
                    Thẻ
                </button>
            </div>

            <!-- Error -->
            <div v-if="unarchiveError" class="mx-3 mt-2 p-2 rounded-md bg-destructive/10 text-destructive text-xs">
                {{ unarchiveError }}
            </div>

            <!-- Content -->
            <div class="max-h-[450px] overflow-y-auto p-3 space-y-2">
                <!-- Columns -->
                <template v-if="archiveTab === 'columns'">
                    <div v-if="archivedColumns.length === 0" class="text-sm text-muted-foreground text-center py-8">
                        Không có cột nào đang lưu trữ
                    </div>

                    <template v-else>
                        <!-- Delete All Button -->
                        <div class="flex justify-end pb-1">
                            <button
                                class="flex items-center gap-1 text-xs text-destructive hover:underline"
                                @click="emit('deleteAllArchivedColumns')"
                            >
                                <Trash2 class="w-3 h-3" />
                                Xóa tất cả
                            </button>
                        </div>

                        <div v-for="col in archivedColumns" :key="col.id"
                            class="p-3 rounded-lg bg-muted flex items-center justify-between">
                            <span class="text-sm truncate">
                                {{ col.name }}
                            </span>
                            <div>
                                <button class="text-xs text-primary hover:underline pe-3" @click="emit('deleteColumn', col.id)">
                                    Xóa
                                </button>
                                <button class="text-xs text-primary hover:underline" @click="handleUnarchiveColumn(col.id)">
                                    Khôi phục
                                </button>
                            </div>
                        </div>
                    </template>
                </template>

                <!-- Cards -->
                <template v-else>
                    <div v-if="archivedCards.length === 0" class="text-sm text-muted-foreground text-center py-8">
                        Không có thẻ nào đang lưu trữ
                    </div>

                    <template v-else>
                        <!-- Delete All Button -->
                        <div class="flex justify-end pb-1">
                            <button
                                class="flex items-center gap-1 text-xs text-destructive hover:underline"
                                @click="emit('deleteAllArchivedCards')"
                            >
                                <Trash2 class="w-3 h-3" />
                                Xóa tất cả
                            </button>
                        </div>

                        <div v-for="card in archivedCards" :key="card.id" class="p-3 rounded-lg bg-muted">
                            <div class="flex items-center justify-between">
                                <span class="text-sm font-medium truncate">
                                    {{ card.title }}
                                </span>
                                <div>
                                    <button class="text-xs text-primary hover:underline pe-3" @click="emit('deleteCard', card.columnId, card.id)">
                                        Xóa
                                    </button>
                                    <button class="text-xs text-primary hover:underline"
                                        @click="handleUnarchiveCard(card.id)">
                                        Khôi phục
                                    </button>
                                </div>
                            </div>
                            <div v-if="card.columnId" class="text-xs text-muted-foreground mt-1">
                                Cột: {{ columns.find(c => c.id === card.columnId)?.name ?? '—' }}
                            </div>
                        </div>
                    </template>
                </template>
            </div>
        </PopoverContent>
    </Popover>
</template>