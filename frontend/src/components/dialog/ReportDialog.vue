<script setup lang="ts">
import { ref, computed } from 'vue'
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

const closeDialog = () => {
  emit('update:open')
  reason.value = ''
  description.value = ''
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

  try {
    if (props.user) {
      await createUserReport(data)
    } else {
      await createRoomReport(data)
    }

    closeDialog()
    toast.success("Đã gửi tố cáo. Cảm ơn bạn đã thông báo cho chúng tôi")
  } catch (error: any) {
    console.log(error.response);

    const message = error?.response?.data || "Gửi báo cáo thất bại, vui lòng thử lại"
    toast.error(message)
  }
}

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
          <Textarea v-model="description" placeholder="Hãy cung cấp thêm chi tiết để chúng tôi xử lý tốt hơn..."
            class="min-h-[100px] bg-background border-border focus:ring-primary/50 rounded-md resize-none" />
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
