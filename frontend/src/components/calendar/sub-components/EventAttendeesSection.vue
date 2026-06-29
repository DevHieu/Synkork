<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { Plus, X } from "lucide-vue-next";
import type { Member } from "@/types/Member";

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

const selectedMembers = computed(() =>
  selectedIds.value
    .map((id) => props.roomMembers.find((member) => member.userId === id))
    .filter(Boolean) as Member[],
);

const availableMembers = computed(() => {
  const query = search.value.trim().toLowerCase();
  return props.roomMembers
    .filter((member) => member.userId && !selectedIds.value.includes(member.userId))
    .filter((member) => {
      if (!query) return true;
      return [member.displayName, member.username, member.email]
        .filter(Boolean)
        .some((value) => value!.toLowerCase().includes(query));
    })
    .slice(0, 6);
});

const addMember = (member: Member) => {
  if (!member.userId || selectedIds.value.includes(member.userId)) return;
  selectedIds.value.push(member.userId);
  search.value = "";
};

const removeMember = (userId?: string) => {
  if (!userId) return;
  selectedIds.value = selectedIds.value.filter((id) => id !== userId);
};

// Section này chỉ quản lý danh sách email cục bộ của form hiện tại.
// Đồng bộ với component cha khi danh sách người tham gia thay đổi
watch(selectedIds, (newList) => {
  emit("change", newList);
}, { deep: true });

// Làm mới khi Dialog mở ra
watch(() => props.show, (isOpen) => {
  if (isOpen) {
    selectedIds.value = [...(props.initialAttendeeIds || [])];
    search.value = "";
  }
});
</script>

<template>
  <div>
    <div class="flex flex-col gap-2">
      <div class="flex gap-2 rounded-xl border-2 border-border bg-background p-2 shadow-[0_16px_34px_-30px_var(--color-foreground)] cursor-default">
        <input v-model="search" type="text"
          placeholder="TÌM THÀNH VIÊN..."
          class="flex-1 rounded-lg border-2 border-border bg-background px-4 py-3 font-mono text-xs uppercase text-foreground placeholder-muted-foreground transition-colors focus:outline-none focus:border-primary" />
        <button type="button" :disabled="availableMembers.length === 0" @click="availableMembers[0] && addMember(availableMembers[0])"
          class="rounded-full border-2 border-primary bg-primary px-4 py-2 text-primary-foreground transition-colors hover:bg-background hover:text-primary">
          <Plus :size="16" />
        </button>
      </div>
      <div v-if="availableMembers.length > 0 && search.trim()" class="rounded-xl border-2 border-border bg-background p-2">
        <button v-for="member in availableMembers" :key="member.memberId" type="button" @click="addMember(member)"
          class="flex w-full items-center justify-between rounded-lg px-3 py-2 text-left font-mono text-xs hover:bg-muted">
          <span class="font-bold uppercase">{{ member.displayName || member.username }}</span>
          <span class="text-muted-foreground">{{ member.email || `@${member.username}` }}</span>
        </button>
      </div>
      <div v-if="selectedMembers.length > 0" class="flex flex-wrap gap-2 mt-2">
        <div v-for="member in selectedMembers" :key="member.userId"
          class="flex items-center gap-1.5 rounded-full border-2 border-border bg-muted/30 px-3 py-1.5 text-xs font-mono text-foreground">
          <span>{{ member.displayName || member.username }}</span>
          <button type="button" @click="removeMember(member.userId)" class="text-muted-foreground hover:text-destructive transition-colors ml-1">
            <X :size="12" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
