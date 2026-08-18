<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from "vue";
import { Plus, X, Check } from "lucide-vue-next";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import type { Member } from "@/types/Member";
import { useUserStore } from "@/stores/userStore";

const props = defineProps<{
  initialAttendeeIds?: string[];
  roomMembers: Member[];
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", attendeeIds: string[]): void;
}>();

const selectedIds = ref<string[]>([...(props.initialAttendeeIds || [])]);
const search = ref("");
const isOpen = ref(false);
const containerRef = ref<HTMLElement | null>(null);

const selectedMembers = computed(() =>
  selectedIds.value
    .map((id) => props.roomMembers.find((member) => member.memberId === id))
    .filter(Boolean) as Member[],
);

const userStore = useUserStore();

const filteredMembers = computed(() => {
  const query = search.value.trim().toLowerCase();
  return props.roomMembers

    .filter((member) => member.memberId && member.username !== userStore.user?.username)
    .filter((member) => {
      if (!query) return true;
      return [member.displayName, member.username]
        .filter(Boolean)
        .some((value) => value!.toLowerCase().includes(query));
    })
    .slice(0, 50);
});

const toggleMember = (member: Member) => {
  if (!member.memberId) return;
  if (selectedIds.value.includes(member.memberId)) {
    selectedIds.value = selectedIds.value.filter((id) => id !== member.memberId);
  } else {
    selectedIds.value.push(member.memberId);
  }
};

const removeMember = (memberId?: string) => {
  if (!memberId) return;
  selectedIds.value = selectedIds.value.filter((id) => id !== memberId);
};

const handleClickOutside = (event: MouseEvent) => {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
});

// Section này chỉ quản lý danh sách thành viên tham gia cục bộ của form hiện tại.
// Đồng bộ với component cha khi danh sách người tham gia thay đổi
watch(selectedIds, (newList) => {
  emit("change", newList);
});

// Làm mới khi Dialog mở ra hoặc initialAttendeeIds thay đổi
watch(() => props.initialAttendeeIds, (newIds) => {
  selectedIds.value = [...(newIds || [])];
}, { deep: true });

watch(() => props.show, (isOpened) => {
  if (isOpened) {
    selectedIds.value = [...(props.initialAttendeeIds || [])];
    search.value = "";
    isOpen.value = false;
  }
});
</script>

<template>
  <div ref="containerRef" class="relative w-full">
    <div class="flex flex-col gap-2">
      <div class="flex gap-1.5 rounded-md border border-border/60 bg-background p-1.5 shadow-sm cursor-default">
        <input v-model="search" type="text" placeholder="TÌM THÀNH VIÊN..." @focus="isOpen = true"
          class="flex-1 rounded-md border border-border/60 bg-background px-3 py-2 font-sans text-xs uppercase text-foreground placeholder-muted-foreground/75 transition-colors focus:outline-none focus:border-foretext-foreground" />
      </div>

      <!-- Suggestion Dropdown (Absolute positioned) -->
      <div v-if="isOpen"
        class="absolute z-50 top-full left-0 right-0 mt-1 rounded-md border border-border/60 bg-popover p-1.5 shadow-md max-h-60 overflow-y-auto animate-in fade-in-50 zoom-in-95 duration-100">
        <template v-if="filteredMembers.length > 0">
          <div v-for="member in filteredMembers" :key="member.memberId" @click="toggleMember(member)"
            class="flex w-full items-center justify-between rounded-sm px-3 py-2 text-left font-sans text-xs hover:bg-muted transition-colors text-popover-foreground cursor-pointer">
            <div class="flex items-center gap-3 min-w-0">
              <Avatar class="size-6 border border-border/60 shrink-0 rounded-sm">
                <AvatarImage v-if="member.avatarUrl" :src="member.avatarUrl" />
                <AvatarFallback />
              </Avatar>
              <div class="min-w-0">
                <p class="font-bold uppercase truncate text-[11px]">{{ member.displayName || member.username }}</p>
                <p class="text-muted-foreground truncate text-[9px]">@{{ member.username }}</p>
              </div>
            </div>

            <Button type="button" :variant="selectedIds.includes(member.memberId) ? 'secondary' : 'outline'"
              class="h-7 px-2.5 text-[9px] font-bold rounded-sm pointer-events-none">
              <span v-if="selectedIds.includes(member.memberId)" class="flex items-center gap-1 text-white">
                <Check :size="12" /> ĐÃ THÊM
              </span>
              <span v-else class="flex items-center gap-1 text-foreground">
                <Plus :size="12" /> THÊM
              </span>
            </Button>
          </div>
        </template>
        <template v-else>
          <div class="px-3 py-4 text-center text-xs font-sans text-muted-foreground select-none">
            KHÔNG TÌM THẤY THÀNH VIÊN
          </div>
        </template>
      </div>

      <!-- Selected Members -->
      <div v-if="selectedMembers.length > 0" class="flex flex-wrap gap-2 mt-2">
        <div v-for="member in selectedMembers" :key="member.memberId"
          class="flex items-center gap-2 rounded-md border border-border/60 bg-muted/20 pl-1.5 pr-2.5 py-1 text-xs font-sans text-foreground">
          <Avatar class="size-5 border border-border/60 shrink-0 rounded-sm">
            <AvatarImage v-if="member.avatarUrl" :src="member.avatarUrl" />
            <AvatarFallback />
          </Avatar>
          <span class="font-medium text-[11px]">{{ member.displayName || member.username }}</span>
          <button type="button" @click="removeMember(member.memberId)"
            class="text-muted-foreground hover:text-destructive transition-colors ml-0.5">
            <X :size="12" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
