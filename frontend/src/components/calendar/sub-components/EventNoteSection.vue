<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useSpaceStore } from "@/stores/spaceStore";
import { getAll } from "@/services/noteService";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { Note } from "@/types/NoteType";

const props = defineProps<{
  initialSpaceId?: string;
  initialNoteId?: string;
  show: boolean;
}>();



const emit = defineEmits<{
  (e: "change", data: { spaceId?: string; noteId?: string }): void;
}>();

const spaceStore = useSpaceStore();
const noteSpaces = computed(() => spaceStore.noteSpaces || []);

const selectedSpaceId = ref<string>("none");
const selectedNoteId = ref<string>("none");
const notes = ref<Note[]>([]);
const loadingNotes = ref(false);

const fetchNotesForSpace = async (spaceId: string) => {
  if (!spaceId || spaceId === "none") {
    notes.value = [];
    return;
  }
  loadingNotes.value = true;
  try {
    const res = await getAll(spaceId);
    notes.value = Array.isArray(res)
      ? res
      : (Array.isArray(res?.data) ? res.data : []);
  } catch (error) {
    console.error("Lỗi khi tải danh sách note:", error);
    notes.value = [];
  } finally {
    loadingNotes.value = false;
  }
};

// Khi chọn Space khác, fetch Note mới
watch(selectedSpaceId, async (newSpaceId) => {
  if (newSpaceId && newSpaceId !== "none") {
    await fetchNotesForSpace(newSpaceId);
  } else {
    notes.value = [];
  }
  
  // Nếu note hiện tại không nằm trong danh sách note của Space mới thì reset về none
  if (!notes.value.some(n => n.id === selectedNoteId.value)) {
    selectedNoteId.value = "none";
  }

  emitChange();
});

// Khi chọn Note khác
watch(selectedNoteId, () => {
  emitChange();
});

const emitChange = () => {
  emit("change", {
    spaceId: selectedSpaceId.value === "none" ? undefined : selectedSpaceId.value,
    noteId: selectedNoteId.value === "none" ? undefined : selectedNoteId.value
  });
};

// Đồng bộ khi mở Dialog
watch(() => props.show, async (isOpen) => {
  if (isOpen) {
    selectedSpaceId.value = props.initialSpaceId || "none";
    if (props.initialSpaceId) {
      await fetchNotesForSpace(props.initialSpaceId);
      selectedNoteId.value = props.initialNoteId || "none";
    } else {
      selectedNoteId.value = "none";
    }
  }
}, { immediate: true });
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <!-- Chọn Kênh Note -->
    <div class="space-y-1.5">
      <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Kênh Note</Label>
      <Select :model-value="selectedSpaceId || 'none'"
        @update:model-value="val => { selectedSpaceId = val as string; }">
        <SelectTrigger class="w-full rounded-md border-border/60 h-10 font-sans bg-transparent">
          <SelectValue placeholder="--- Chọn kênh ---" />
        </SelectTrigger>
        <SelectContent class="max-h-60 overflow-y-auto">
          <SelectGroup>
            <SelectItem value="none">---</SelectItem>
            <SelectItem v-for="space in noteSpaces" :key="space.id" :value="space.id">
              {{ space.name }}
            </SelectItem>
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>

    <!-- Chọn Note -->
    <div class="space-y-1.5">
      <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Liên kết Note</Label>
      <Select :model-value="selectedNoteId || 'none'"
        @update:model-value="val => selectedNoteId = val as string"
        :disabled="!selectedSpaceId || selectedSpaceId === 'none' || loadingNotes">
        <SelectTrigger class="w-full rounded-md border-border/60 h-10 font-sans bg-transparent">
          <SelectValue :placeholder="loadingNotes ? 'Đang tải...' : '--- Chọn note ---'" />
        </SelectTrigger>
        <SelectContent class="max-h-60 overflow-y-auto">
          <SelectGroup>
            <SelectItem value="none">---</SelectItem>
            <SelectItem v-for="note in notes" :key="note.id" :value="note.id">
              {{ note.title }}
            </SelectItem>
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  </div>
</template>
