<script setup lang="ts">
import { ref, computed, onBeforeUnmount, watch } from 'vue'
import { X, FileVideo, ImageIcon, Paperclip, UploadCloud } from "lucide-vue-next";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import Avatar from "@/components/ui/avatar/Avatar.vue"
import AvatarImage from "@/components/ui/avatar/AvatarImage.vue"
import AvatarFallback from "@/components/ui/avatar/AvatarFallback.vue"
import { Flag } from 'lucide-vue-next'
import type { User } from "@/types/User"
import type { Room } from "@/types/Room"
import { createRoomReport, createUserReport } from '@/services/reportService'
import type { ReportReason, ReportRequest } from '@/types/Report'
import { toast } from 'vue-sonner'

const props = defineProps<{
  open: boolean
  user?: User | null
  room?: Room | null
}>()

const emit = defineEmits(['update:open'])

const reason = ref<ReportReason | ''>('')
const description = ref('')
const submitting = ref(false)

const MAX_IMAGE_SIZE = 10 * 1024 * 1024
const MAX_VIDEO_SIZE = 50 * 1024 * 1024

const evidenceFile = ref<File | null>(null)
const evidencePreviewUrl = ref<string | null>(null)
const evidenceIsVideo = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const isUser = computed(() => !!props.user)
const targetName = computed(() => props.user?.displayName || props.room?.name || 'Đối tượng')
const targetAvatar = computed(() => props.user?.avatarUrl || props.room?.roomAvatar)
const targetSubInfo = computed(() => props.user ? `@${props.user.username}` : (props.room?.description || 'Phòng trò chuyện'))

const reasons: { value: ReportReason; label: string }[] = [
  { value: 'SPAM', label: 'Spam / Quảng cáo' },
  { value: 'HARASSMENT', label: 'Quấy rối / Đe dọa' },
  { value: 'INAPPROPRIATE', label: 'Nội dung không phù hợp' },
  { value: 'HATE_SPEECH', label: 'Ngôn từ thù ghét' },
  { value: 'OTHER', label: 'Lý do khác' },
]

const openFilePicker = () => fileInput.value?.click()

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

const clearEvidence = () => {
  if (evidencePreviewUrl.value) URL.revokeObjectURL(evidencePreviewUrl.value)
  evidenceFile.value = null
  evidencePreviewUrl.value = null
  evidenceIsVideo.value = false
}

const processFile = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isVideo = file.type.startsWith('video/')

  if (!isImage && !isVideo) {
    toast.error(`"${file.name}" không phải ảnh hoặc video`)
    return
  }

  const maxSize = isVideo ? MAX_VIDEO_SIZE : MAX_IMAGE_SIZE
  if (file.size > maxSize) {
    toast.error(`"${file.name}" vượt quá dung lượng cho phép (${isVideo ? '50MB' : '10MB'})`)
    return
  }

  clearEvidence()
  evidenceFile.value = file
  evidencePreviewUrl.value = URL.createObjectURL(file)
  evidenceIsVideo.value = isVideo
}

const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  processFile(file)
}

const isDragging = ref(false)
const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  isDragging.value = true
}
const handleDragLeave = () => {
  isDragging.value = false
}
const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  isDragging.value = false

  const file = e.dataTransfer?.files?.[0]
  if (file) processFile(file)
}

onBeforeUnmount(clearEvidence)

const closeDialog = () => {
  emit('update:open')
  reason.value = ''
  description.value = ''
  clearEvidence()
}

const handleSubmit = async () => {
  if (!reason.value || (!props.user && !props.room)) return

  const targetId = props.user?.id || props.room?.id
  if (!targetId) return

  const data: ReportRequest = {
    targetId,
    reason: reason.value,
    description: description.value,
  }

  submitting.value = true
  try {
    if (props.user) {
      await createUserReport(data, evidenceFile.value)
    } else {
      await createRoomReport(data, evidenceFile.value)
    }

    closeDialog()
    toast.success("Đã gửi tố cáo. Cảm ơn bạn đã thông báo cho chúng tôi")
  } catch (error: any) {
    console.log(error.response);

    const message = error?.response?.data || "Gửi báo cáo thất bại, vui lòng thử lại"
    toast.error(message)
  } finally {
    submitting.value = false
  }
}

const truncateMiddle = (name: string, maxLength = 38) => {
  if (name.length <= maxLength) return name

  const dotIndex = name.lastIndexOf('.')
  const ext = dotIndex > -1 ? name.slice(dotIndex) : ''
  const base = dotIndex > -1 ? name.slice(0, dotIndex) : name

  const keep = Math.max(4, Math.floor((maxLength - ext.length - 3) / 2))

  return `${base.slice(0, keep)}...${base.slice(-keep)}${ext}`
}

watch(() => props.open, (newVal) => {
  if(!newVal) {
    reason.value =  '',
    description.value = '',
    clearEvidence()
  }
})
</script>

<template>
  <Dialog :open="open" @update:open="() => emit('update:open')">
    <DialogContent class="sm:max-w-[425px] bg-background border-border text-foreground rounded-lg">
      <DialogHeader>
        <div class="flex items-center gap-2 text-destructive mb-1">
          <Flag class="w-5 h-5 fill-current" />
          <DialogTitle class="text-xl font-bold">Báo cáo vi phạm</DialogTitle>
        </div>
        <DialogDescription class="text-muted-foreground">
          Gửi báo cáo nếu bạn thấy nội dung hoặc hành vi vi phạm quy chuẩn cộng đồng.
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-6 py-4">
        <!-- Thông tin đối tượng bị báo cáo -->
        <div class="flex items-center gap-4 p-3 rounded-md bg-muted/50 border border-border">
          <Avatar class="h-12 w-12 border-2 border-primary/20">
            <AvatarImage v-if="targetAvatar" :src="targetAvatar" />
            <AvatarFallback />
          </Avatar>
          <div class="flex flex-col min-w-0">
            <span class="text-[10px] font-bold text-muted-foreground uppercase tracking-wider">
              Đang báo cáo {{ isUser ? 'người dùng' : 'phòng' }}
            </span>
            <span class="text-base font-bold truncate">{{ targetName }}</span>
            <span class="text-xs text-muted-foreground truncate">{{ targetSubInfo }}</span>
          </div>
        </div>

        <!-- Lý do báo cáo -->
        <div class="space-y-2">
          <Label class="text-sm font-semibold text-foreground/80">Lý do báo cáo</Label>
          <Select v-model="reason">
            <SelectTrigger class="w-full bg-background border-border focus:ring-primary/50 rounded-md">
              <SelectValue placeholder="Chọn lý do báo cáo" />
            </SelectTrigger>
            <SelectContent class="bg-popover border-border text-popover-foreground">
              <SelectItem v-for="item in reasons" :key="item.value" :value="item.value"
                class="focus:bg-accent focus:text-accent-foreground cursor-pointer">
                {{ item.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- Chi tiết -->
        <div class="space-y-2">
          <Label class="text-sm font-semibold text-foreground/80">Mô tả chi tiết (không bắt buộc)</Label>
          <Textarea 
            v-model="description" 
            placeholder="Hãy cung cấp thêm chi tiết để chúng tôi xử lý tốt hơn..."
            style="white-space: pre-wrap; overflow-wrap: anywhere; word-break: break-word;"
            class="min-h-[100px] max-h-[100px] w-full bg-background border-border rounded-md resize-none overflow-y-auto" 
          />
        </div>

        <!-- Bằng chứng đính kèm -->
        <div class="space-y-2">
          <Label class="text-sm font-semibold text-foreground/80">Bằng chứng (ảnh/video, không bắt buộc)</Label>

          <input ref="fileInput" type="file" accept="image/*,video/*" class="hidden" @change="handleFileChange" />

          <!-- Đã có file: hiển thị preview lớn hơn, kèm tên/dung lượng, hover để đổi file -->
          <div v-if="evidenceFile"
            class="relative flex items-center gap-3 p-2 rounded-md border border-border bg-muted/50 group">
            <div class="relative w-16 h-16 shrink-0 rounded-md overflow-hidden bg-background">
              <video v-if="evidenceIsVideo" :src="evidencePreviewUrl!" class="w-full h-full object-cover" muted />
              <img v-else :src="evidencePreviewUrl!" class="w-full h-full object-cover" />

              <div class="absolute top-1 left-1 bg-background/80 rounded p-0.5">
                <FileVideo v-if="evidenceIsVideo" class="w-3 h-3" />
                <ImageIcon v-else class="w-3 h-3" />
              </div>

              <button type="button" @click="openFilePicker"
                class="absolute inset-0 flex items-center justify-center bg-background/70 opacity-0 group-hover:opacity-100 transition-opacity text-[10px] font-semibold">
                Đổi file
              </button>
            </div>

            <div class="flex-1 min-w-0 max-w-[290px]">
              <p class="text-xs font-medium truncate" :title="evidenceFile.name">
                {{ truncateMiddle(evidenceFile.name) }}
              </p>
              <p class="text-[11px] text-muted-foreground">{{ formatFileSize(evidenceFile.size) }}</p>
            </div>

            <button type="button" @click="clearEvidence"
              class="shrink-0 w-7 h-7 rounded-full flex items-center justify-center text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors">
              <X class="w-4 h-4" />
            </button>
          </div>

          <!-- Chưa có file: khu vực chọn / kéo-thả -->
          <button v-else type="button" @click="openFilePicker" @dragover="handleDragOver" @dragleave="handleDragLeave"
            @drop="handleDrop"
            class="relative w-full h-24 rounded-md border border-dashed flex flex-col items-center justify-center gap-1 text-muted-foreground transition-colors"
            :class="isDragging
              ? 'border-primary/60 bg-primary/10 text-primary'
              : 'border-border hover:bg-muted/50 hover:text-foreground'">
            <UploadCloud v-if="isDragging" class="w-5 h-5" />
            <Paperclip v-else class="w-4 h-4" />
            <span class="text-[11px]">{{ isDragging ? 'Thả file vào đây' : 'Chọn hoặc kéo thả tệp' }}</span>
          </button>

          <p class="text-[11px] text-muted-foreground">Ảnh tối đa 10MB, video tối đa 50MB. Chỉ 1 tệp.</p>
        </div>
      </div>

      <DialogFooter class="gap-2">
        <Button variant="ghost" @click="closeDialog" class="flex-1 rounded-md hover:bg-muted text-muted-foreground">
          Hủy bỏ
        </Button>
        <Button :disabled="!reason" @click="handleSubmit"
          class="flex-1 rounded-md transition-all bg-destructive hover:bg-destructive/70 text-destructive-foreground font-bold">
          Gửi báo cáo
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>