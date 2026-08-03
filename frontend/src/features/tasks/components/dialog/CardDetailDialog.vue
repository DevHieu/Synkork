<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { Dialog, DialogContent, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar as CalendarIcon, UserPlus, Archive, AlignLeft, CreditCard, X, Check } from "lucide-vue-next";
import type { CardEvent } from "@/types/Task";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { useCardDetail } from "../../composables/card-detail";

const props = defineProps<{
  open: boolean;
  card: CardEvent;
  columnName: string;
}>();

const emit = defineEmits(["update:open", "save", "archive"]);

const {
  form,
  localAssignees,
  baseVersion,
  getStatus,
  handleSave,
  handleArchive,
  toggleAssignee,
  removeAssignee,
} = useCardDetail(props, emit);

const roomMemberStore = useRoomMemberStore();

const searchQuery = ref("");
const showDropdown = ref(false);

const status = computed(() => getStatus(form.value.dueDate));

const clearDueDate = () => {
  form.value.dueDate = ""
  handleSave()
};

const handleTitleKeydown = (e: KeyboardEvent) => {
  if (e.key === "Enter") (e.target as HTMLInputElement).blur();
};

const filteredMembers = computed(() => roomMemberStore.searchMembers(searchQuery.value));

const isAssigned = (memberId: string) => localAssignees.value.some((a) => a.id === memberId);

watch(
  () => props.open,
  (newVal) => {
    localAssignees.value = [...(props.card.assignees ?? [])];
    if (newVal && props.card) {
      let formattedDate = "";

      if (props.card.dueDate) {
        formattedDate = props.card.dueDate.slice(0, 16);
      }
      form.value = {
        title: props.card.title || "",
        description: props.card.description || "",
        dueDate: formattedDate,
      };
      baseVersion.value = props.card.version;
    }
    searchQuery.value = "";
    showDropdown.value = false;
  },
  { immediate: true },
);

watch(
  () => props.card.version,
  (newVersion) => {
    if (!props.open) return;

    if (
      form.value.title === props.card.title &&
      form.value.description === props.card.description
    ) {
      baseVersion.value = newVersion;
    }
  }
);
</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="max-w-2xl p-0 overflow-hidden border-none shadow-2xl bg-background rounded-xl">
      <DialogTitle class="sr-only">Chi tiết thẻ</DialogTitle>
      <DialogDescription class="sr-only">Chỉnh sửa thông tin thẻ công việc</DialogDescription>
      <div class="flex items-center justify-between px-6 py-3 bg-muted/20 border-b border-border/50">
        <div class="flex items-center gap-2 text-muted-foreground">
          <CreditCard :size="16" />
          <span class="text-xs font-medium uppercase tracking-wider">Chi tiết thẻ</span>
        </div>
        <Button variant="ghost" size="sm" @click.stop="handleArchive"
          class="h-8 text-muted-foreground hover:text-amber-500 hover:bg-amber-50 transition-colors mr-5">
          <Archive :size="14" class="mr-1" />
          <span class="text-xs">Lưu trữ thẻ</span>
        </Button>
      </div>

      <div class="p-5 space-y-5">
        <!-- Title -->
        <div class="space-y-1">
          <input v-model="form.title"
            class="w-full text-2xl font-bold bg-transparent border-none p-1 focus:ring-0 focus:outline-none placeholder:text-muted-foreground/40"
            placeholder="Tiêu đề thẻ..." @blur="handleSave" @keydown="handleTitleKeydown" />
          <div class="flex items-center gap-2 text-sm text-muted-foreground">
            <span>Trong mục</span>
            <span class="px-2 py-0.5 rounded bg-secondary text-secondary-foreground font-medium text-xs">
              {{ columnName }}
            </span>
          </div>
        </div>

        <!-- Assignees -->
        <div class="grid grid-cols-2 gap-8 py-2">
          <div class="space-y-2">
            <Label class="text-[11px] font-semibold uppercase text-muted-foreground">Người thực hiện</Label>
            <div class="flex flex-wrap gap-2 mb-2">
              <div v-for="assignee in localAssignees" :key="assignee.id"
                class="flex items-center gap-1.5 bg-secondary rounded-full pl-1 pr-2 py-0.5">
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

            <!-- Dropdown chọn assignee -->
            <Popover v-model:open="showDropdown">
              <PopoverTrigger as-child>
                <div
                  class="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-dashed border-border hover:border-primary cursor-pointer transition-colors">
                  <UserPlus :size="13" class="text-muted-foreground" />
                  <span class="text-xs text-muted-foreground">
                    Thêm người...
                  </span>
                </div>
              </PopoverTrigger>

              <PopoverContent class="w-56 p-0 overflow-hidden" align="start">
                <div class="p-2 border-b border-border">
                  <input v-model="searchQuery" placeholder="Tìm tên..."
                    class="w-full text-xs bg-transparent outline-none placeholder:text-muted-foreground" />
                </div>

                <ul class="max-h-48 overflow-y-auto py-1">
                  <li v-for="member in filteredMembers" :key="member.memberId"
                    class="flex items-center gap-2 px-3 py-2 hover:bg-accent cursor-pointer transition-colors"
                    @click="toggleAssignee(member)">
                    <Avatar class="h-6 w-6">
                      <AvatarImage v-if="member.avatarUrl" :src="member.avatarUrl" />
                      <AvatarFallback class="text-[9px] bg-primary/10 text-primary font-bold">
                        {{ member.displayName?.charAt(0).toUpperCase() }}
                      </AvatarFallback>
                    </Avatar>

                    <span class="text-xs flex-1">
                      {{ member.displayName }}
                    </span>

                    <Check v-if="isAssigned(member.memberId)" :size="12" class="text-primary" />
                  </li>

                  <li v-if="filteredMembers.length === 0" class="px-3 py-2 text-xs text-muted-foreground">
                    Không tìm thấy
                  </li>
                </ul>
              </PopoverContent>
            </Popover>
          </div>

          <div class="space-y-2">
            <Label class="text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
              Ngày hết hạn
            </Label>
            <div class="flex items-center gap-1.5">
              <CalendarIcon class="w-3.5 h-3.5 text-muted-foreground shrink-0" />
              <input v-model="form.dueDate" type="datetime-local"
                class="text-xs bg-transparent border-none outline-none text-foreground/80 cursor-pointer hover:text-primary transition-colors w-full"
                @change="handleSave" />
            </div>

            <!-- Status badge -->
            <div class="flex items-center gap-1.5">
              <Badge v-if="status === 'OVERDUE'" variant="outline"
                class="text-[10px] px-1.5 py-0 h-4 gap-1 border-destructive/30 bg-destructive/10 text-destructive">
                <AlertCircle class="w-2.5 h-2.5" /> Quá hạn
              </Badge>
              <Badge v-else-if="status === 'DUE_SOON'" variant="outline"
                class="text-[10px] px-1.5 py-0 h-4 gap-1 border-amber-300 bg-amber-50 text-amber-600 dark:bg-amber-950/30 dark:text-amber-400 dark:border-amber-800/40">
                <Clock class="w-2.5 h-2.5" /> Sắp đến hạn
              </Badge>
              <button v-if="form.dueDate" @click="clearDueDate"
                class="text-[10px] text-muted-foreground/60 hover:text-destructive transition-colors flex items-center gap-0.5">
                <X class="w-2.5 h-2.5" /> Xóa
              </button>
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
            class="min-h-[100px] w-full text-base bg-transparent border-none focus-visible:ring-0 p-2 resize-none leading-relaxed placeholder:text-muted-foreground/30 shadow-none"
            @blur="handleSave" />
        </div>
      </div>

      <!-- Footer Info -->
      <div class="px-8 py-4 bg-muted/5 flex justify-between items-center border-t border-border/30">
        <p class="text-[10px] text-muted-foreground italic">
          * Tự động lưu khi bạn hoàn tất chỉnh sửa
        </p>
      </div>
    </DialogContent>
  </Dialog>
</template>