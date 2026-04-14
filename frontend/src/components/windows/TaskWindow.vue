<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { Plus, MoreHorizontal, Search, Hash, Pencil, Trash2 } from 'lucide-vue-next'
import draggable from 'vuedraggable'


// đổi tên board
const boardName = ref('Synkork')
const isEditingName = ref(false)
const tempBoardName = ref('')

const startEditName = () => {
    tempBoardName.value = boardName.value
    isEditingName.value = true
}

const saveBoardName = async () => {
    if(tempBoardName.value.trim() && tempBoardName.value !== boardName.value){
        try {
            
        } catch (e) {
            console.error("Lỗi đổi tên:", e)
        }
    }
    isEditingName.value = false
}


</script>

<template>
    <div class="flex h-screen w-full bg-slate-50 overflow-hidden">
        <div class="flex-1 flex flex-col relative overflow-hidden background">
            <header class="p-6 flex justify-between items-center bg-transparent">
                <div class="flex items-center gap-2 font-semibold text-slate-700">
                    <Hash class="w-5 h-5 text-teal-600" />
                    <span 
                        class="cursor-pointer hover:bg-slate-200/50 px-1 rounded transition-colors"
                        title="Click để đổi tên"
                    >
                        {{ boardName }}
                    </span>
                </div>
            </header>

            <div class="flex-1 flex items-start gap-6 p-6 overflow-x-auto">
                <draggable group="columns" item-key="id" handle=".column-handle"
                        class="flex gap-6 items-start">
                    <template #item="{ element: col }">
                        <div class="w-80 flex-shrink-0 flex flex-col max-h-full border-2 border-slate-200 rounded-3xl p-4 bg-slate-50/50">
                            <div class="flex items-center justify-between mb-4 px-1">
                                <h3 class="column-handle cursor-move font-bold text-slate-700 text-sm uppercase tracking-wide flex items-center gap-2">
                                    {{ col.name }}
                                    <span class="text-slate-400 text-xs font-normal">
                                        ({{ col.cards?.length || 0 }})
                                    </span>
                                </h3>
                                <DropdownMenu>
                                    <DropdownMenuTrigger as-child>
                                        <Button variant="ghost" size="icon" class="h-8 w-8 text-slate-400">
                                            <MoreHorizontal class="w-4 h-4" />
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent align="end"
                                        class="rounded-xl border-none shadow-lg backdrop-blur-md">
                                        <DropdownMenuItem 
                                            class="gap-2 cursor-pointer text-xs">
                                            <Pencil class="w-3.5 h-3.5" /> Sửa tên cột
                                        </DropdownMenuItem>
                                        <DropdownMenuItem 
                                            class="gap-2 cursor-pointer text-xs text-red-500">
                                            <Trash2 class="w-3.5 h-3.5" /> Xóa cột
                                        </DropdownMenuItem>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            </div>

                            <draggable v-model="col.cards" group="tasks" item-key="id" :animation="200"
                                ghost-class="opacity-50" 
                                class="flex-1 flex flex-col gap-3 overflow-y-auto min-h-[150px] p-1">
                                <template #item="{ element: card }">
                                    <Card class="p-4 border-none shadow-sm hover:shadow-md transition-all cursor-grab group relative bg-white rounded-2xl">
                                        <div class="flex flex-col gap-2">
                                            <p class="font-bold text-slate-800 text-sm pr-10">{{ card.title }}</p>
                                            <p class="text-slate-500 text-xs line-clamp-2">{{ card.description }}</p>
                                            <div class="flex justify-between items-center mt-2">
                                                <div class="w-6 h-6 rounded-full bg-orange-100 flex items-center justify-center text-[10px] font-bold text-orange-600">
                                                    {{ card.user?.name || 'V' }}
                                                </div>
                                                <span class="text-[10px] text-slate-400 font-medium">
                                                    {{ card.createdAt || card.date }}
                                                </span>
                                            </div>
                                        </div>
                                        <div class="absolute top-3 right-2 flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                            <Button variant="ghost" size="icon" class="h-6 w-6"
                                                >
                                                <Pencil class="w-3 h-3" />
                                            </Button>
                                            <Button variant="ghost" size="icon" class="h-6 w-6 text-red-400"
                                                >
                                                <Trash2 class="w-3 h-3" />
                                            </Button>
                                        </div>
                                    </Card>
                                </template>
                            </draggable>

                            <button class="mt-3 w-full py-2 flex items-center justify-center gap-1 text-slate-400 hover:text-teal-600 text-sm font-medium">
                                <Plus class="w-4 h-4" /> Thêm task
                            </button>
                        </div>
                    </template>
                </draggable>

                <div class="flex-shrink-0 w-72 h-32 border-2 border-dashed border-slate-300 rounded-3xl flex flex-col items-center justify-center gap-2 group cursor-pointer hover:border-teal-400 transition-colors">
                    <div class="bg-slate-200 p-2 rounded-full group-hover:bg-teal-100">
                        <Plus class="w-5 h-5 text-slate-500 group-hover:text-teal-600" />
                    </div>
                    <p class="text-xs font-bold text-slate-500 uppercase tracking-widest group-hover:text-teal-600">
                        Thêm cột mới
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped></style>