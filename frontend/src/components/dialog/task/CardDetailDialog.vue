<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Dialog, DialogContent } from '@/components/ui/dialog'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import {
  Calendar as CalendarIcon,
  UserPlus,
  Trash2,
  AlignLeft,
  CreditCard,
  X,
  Check
} from 'lucide-vue-next'
import type { CardEvent, SpaceMemberDTO, MemberSummary } from '@/types/Task'
import type { Member } from '@/types/Member'

import { useTaskStore } from '@/stores/taskStore'
import { useSpaceStore } from '@/stores/spaceStore'
import { useRoomMemberStore } from '@/stores/roomMemberStore'
import { storeToRefs } from 'pinia'


const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

// State assignees
const members = ref<SpaceMemberDTO[]>([])
const searchQuery = ref('')
const showDropdown = ref(false)

// Danh sách assignees hiện tại của card (local copy để thao tác)
const localAssignees = ref<MemberSummary[]>([])

const props = defineProps<{
  open: boolean,
  card: CardEvent,
  columnName: string
}>()

const emit = defineEmits(['update:open', 'save', 'update-assignees'])

const form = ref({ title: '', description: '' })

watch(() => props.open, (newVal) => {
  if (newVal && props.card) {
    form.value = {
      title: props.card.title || '',
      description: props.card.description || ''
    }
  }
}, { immediate: true })

const saveTitle = () => {
  if (!form.value.title.trim()) return
  emit('save', { ...props.card, title: form.value.title.trim() })
}

const saveDescription = () => {
  emit('save', { ...props.card, description: form.value.description.trim() })
}

const handleTitleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') (e.target as HTMLInputElement).blur()
}

// mới 


const roomMemberStore = useRoomMemberStore()

// Bỏ: members ref, getSpaceMembers call
// Thay filteredMembers bằng:
const filteredMembers = computed(() =>
  roomMemberStore.searchMembers(searchQuery.value)
)

watch(() => props.open, (newVal) => {
  if (newVal) {
    localAssignees.value = [...(props.card.assignees ?? [])]
    // Không cần fetch gì thêm — store đã có members rồi
  }
})

// const toggleAssignee = (member: Member) => {
//     if (isAssigned(member.memberId)) {
//         localAssignees.value = localAssignees.value.filter(a => a.id !== member.memberId)
//     } else {
//         localAssignees.value.push({ id: member.memberId, name: member.displayName, avatarUrl: member.avatarUrl })
//     }
//     // Emit ngay để auto-save
//     emit('update-assignees', {
//         cardId: props.card.id,
//         assigneeIds: localAssignees.value.map(a => a.id)
//     })
// }

const handleDelete = () => {
  const data = {
    columnId: props.card.columnId,
    cardId: props.card.id
  }
  useTaskStore().delete("card", currentSpace.value.id, data)
  emit('update:open', false) // Đóng dialog
}

const emitUpdate = () => {
  emit('update-assignees', {
    cardId: props.card.id,
    assigneeIds: localAssignees.value.map(a => a.id)
  })
}

const removeAssignee = (id: string) => {
  localAssignees.value = localAssignees.value.filter(a => a.id !== id)
  emitUpdate()
}

const addAssignee = (member: Member) => {
  if (!localAssignees.value.some(a => a.id === member.memberId)) {
    localAssignees.value.push({
      id: member.memberId,
      name: member.displayName,
      avatarUrl: member.avatarUrl
    })
    emitUpdate()
  }
}

// isAssigned giữ nguyên
const isAssigned = (memberId: string) =>
  localAssignees.value.some(a => a.id === memberId)
</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="max-w-2xl p-0 overflow-hidden border-none shadow-2xl bg-background rounded-xl">

      <div class="flex items-center justify-between px-6 py-3 bg-muted/20 border-b border-border/50">
        <div class="flex items-center gap-2 text-muted-foreground">
          <CreditCard :size="16" />
          <span class="text-xs font-medium uppercase tracking-wider">Chi tiết thẻ</span>
        </div>
        <Button variant="ghost" size="sm" @click.stop="handleDelete()"
          class="h-8 text-muted-foreground hover:text-red-500 hover:bg-red-50 transition-colors mr-5">
          <Trash2 :size="14" class="mr-1" />
          <span class="text-xs">Xóa thẻ</span>
        </Button>
      </div>

      <div class="p-8 space-y-8">
        <!-- Title & Breadcrumb -->
        <div class="space-y-1">
          <input v-model="form.title"
            class="w-full text-2xl font-bold bg-transparent border-none p-0 focus:ring-0 focus:outline-none placeholder:text-muted-foreground/40"
            placeholder="Tiêu đề thẻ..." @blur="saveTitle" @keydown="handleTitleKeydown" />
          <div class="flex items-center gap-2 text-sm text-muted-foreground">
            <span>Trong mục</span>
            <span class="px-2 py-0.5 rounded bg-secondary text-secondary-foreground font-medium text-xs">
              {{ columnName }}
            </span>
          </div>
        </div>

        <!-- Metadata Grid (Gọn gàng trên 1 dòng) -->
        <div class="grid grid-cols-2 gap-8 py-2">
          <div class="space-y-2">
            <Label class="text-[11px] font-semibold uppercase text-muted-foreground">Người thực hiện</Label>
            <div class="flex flex-wrap gap-2 mb-2">
              <div
    v-for="assignee in localAssignees"
    :key="assignee.id"
    class="flex items-center gap-1.5 bg-secondary rounded-full pl-1 pr-2 py-0.5"
>
    <Avatar class="h-5 w-5">
        <AvatarImage v-if="assignee.avatarUrl" :src="assignee.avatarUrl" />
        <AvatarFallback class="text-[9px] bg-primary/10 text-primary font-bold">
            {{ assignee.name?.charAt(0).toUpperCase() }}
        </AvatarFallback>
    </Avatar>
    <span class="text-xs font-medium">{{ assignee.name }}</span>
    <button @click="removeAssignee(assignee.id)" class="text-muted-foreground hover:text-red-500">
        <X :size="10" />
    </button>
</div>
            </div>

            <!-- Dropdown chọn -->
            <div class="relative">
              <div
                class="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-dashed border-border hover:border-primary cursor-pointer transition-colors"
                @click="showDropdown = !showDropdown">
                <UserPlus :size="13" class="text-muted-foreground" />
                <span class="text-xs text-muted-foreground">Thêm người...</span>
              </div>

              <!-- Dropdown list -->
              <div v-if="showDropdown" v-click-outside="() => showDropdown = false"
                class="absolute top-full mt-1 left-0 w-56 bg-popover border border-border rounded-xl shadow-lg z-50 overflow-hidden">
                <div class="p-2 border-b border-border">
                  <input v-model="searchQuery" placeholder="Tìm tên..."
                    class="w-full text-xs bg-transparent outline-none placeholder:text-muted-foreground" @click.stop />
                </div>
                <ul class="max-h-48 overflow-y-auto py-1">
                  <li
    v-for="member in filteredMembers"
    :key="member.memberId"
    class="flex items-center gap-2 px-3 py-2 hover:bg-accent cursor-pointer transition-colors"
    @click="addAssignee(member)"
>
    <Avatar class="h-6 w-6">
        <AvatarImage v-if="member.avatarUrl" :src="member.avatarUrl" />
        <AvatarFallback class="text-[9px] bg-primary/10 text-primary font-bold">
            {{ member.displayName?.charAt(0).toUpperCase() }}
        </AvatarFallback>
    </Avatar>
    <span class="text-xs flex-1">{{ member.displayName }}</span>
    <Check v-if="isAssigned(member.memberId)" :size="12" class="text-primary" />
</li>
                  <li v-if="filteredMembers.length === 0" class="px-3 py-2 text-xs text-muted-foreground">
                    Không tìm thấy
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <div class="space-y-2">
            <Label class="text-[11px] font-semibold uppercase text-muted-foreground">Hạn chót</Label>
            <div
              class="flex items-center gap-2 text-sm font-medium text-foreground/80 hover:text-primary cursor-pointer group transition-colors">
              <CalendarIcon :size="16" class="text-muted-foreground group-hover:text-primary" />
              <span>Chưa thiết lập</span>
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="space-y-3 pt-4 border-t border-border/50">
          <div class="flex items-center gap-2 text-foreground/70">
            <AlignLeft :size="18" />
            <span class="text-sm font-semibold">Mô tả</span>
          </div>
          <Textarea v-model="form.description" placeholder="Nội dung chi tiết..."
            class="min-h-[180px] w-full text-base bg-transparent border-none focus-visible:ring-0 p-0 resize-none leading-relaxed placeholder:text-muted-foreground/30 shadow-none"
            @blur="saveDescription" />
        </div>
      </div>

      <!-- Footer Info -->
      <div class="px-8 py-4 bg-muted/5 flex justify-between items-center border-t border-border/30">
        <p class="text-[10px] text-muted-foreground italic">
          * Tự động lưu khi bạn hoàn tất chỉnh sửa
        </p>
        <div class="flex gap-2">
          <!-- Có thể thêm các tag nhỏ ở đây nếu cần -->
        </div>
      </div>

    </DialogContent>
  </Dialog>
</template>