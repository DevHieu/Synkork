<script setup lang="ts">
import { ref, computed, watch } from "vue";
import { useSpaceStore } from "@/stores/spaceStore";
import { getAllColumns } from "@/services/task/columnService";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

const props = defineProps<{
  initialSpaceId?: string;
  initialTaskId?: string;
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", data: { spaceId?: string; taskId?: string }): void;
}>();

const spaceStore = useSpaceStore();
const taskSpaces = computed(() => spaceStore.taskSpaces || []);

const selectedSpaceId = ref<string>("none");
const selectedTaskId = ref<string>("none");
const columns = ref<any[]>([]);
const loadingTasks = ref(false);

const fetchTasksForSpace = async (spaceId: string) => {
  if (!spaceId || spaceId === "none") {
    columns.value = [];
    return;
  }
  loadingTasks.value = true;
  try {
    const res = await getAllColumns(spaceId);
    console.log("getAllColumns response:", res);
    columns.value = res.data || [];
    console.log("Loaded columns with tasks:", columns.value);
  } catch (error) {
    console.error("Lỗi khi tải danh sách task:", error);
    columns.value = [];
  } finally {
    loadingTasks.value = false;
  }
};

// Khi chọn Space khác, fetch Task mới
watch(selectedSpaceId, async (newSpaceId) => {
  if (newSpaceId && newSpaceId !== "none") {
    await fetchTasksForSpace(newSpaceId);
  } else {
    columns.value = [];
  }
  
  // Kiểm tra xem task hiện tại có tồn tại trong bất cứ cột nào không
  const taskExists = columns.value.some(col => 
    col.cards && col.cards.some((t: any) => t.id === selectedTaskId.value)
  );
  if (!taskExists) {
    selectedTaskId.value = "none";
  }

  emitChange();
});

// Khi chọn Task khác
watch(selectedTaskId, () => {
  emitChange();
});

const emitChange = () => {
  emit("change", {
    spaceId: selectedSpaceId.value === "none" ? undefined : selectedSpaceId.value,
    taskId: selectedTaskId.value === "none" ? undefined : selectedTaskId.value
  });
};

// Đồng bộ khi mở Dialog
watch(() => props.show, async (isOpen) => {
  if (isOpen) {
    selectedSpaceId.value = props.initialSpaceId || "none";
    if (props.initialSpaceId) {
      await fetchTasksForSpace(props.initialSpaceId);
      selectedTaskId.value = props.initialTaskId || "none";
    } else {
      selectedTaskId.value = "none";
    }
  }
}, { immediate: true });
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <!-- Chọn Kênh Task -->
    <div class="space-y-1.5">
      <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Kênh Task</Label>


      <Select :model-value="selectedSpaceId || 'none'"
        @update:model-value="val => { selectedSpaceId = val as string; }">
        <SelectTrigger class="w-full rounded-md border-border/60 h-10 font-sans bg-transparent">
          <SelectValue placeholder="--- Chọn kênh ---" />
        </SelectTrigger>
        <SelectContent class="max-h-60 overflow-y-auto">
          <SelectGroup>
            <SelectItem value="none">---</SelectItem>
            <SelectItem v-for="space in taskSpaces" :key="space.id" :value="space.id">
              {{ space.name }}
            </SelectItem>
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>

    <!-- Chọn Task -->
    <div class="space-y-1.5">
      <Label class="text-[10px] font-sans font-bold text-muted-foreground uppercase tracking-wider">Liên kết Task</Label>
      <Select :model-value="selectedTaskId || 'none'"
        @update:model-value="val => selectedTaskId = val as string"
        :disabled="!selectedSpaceId || selectedSpaceId === 'none' || loadingTasks">
        <SelectTrigger class="w-full rounded-md border-border/60 h-10 font-sans bg-transparent">
          <SelectValue :placeholder="loadingTasks ? 'Đang tải...' : '--- Chọn task ---'" />
        </SelectTrigger>
        <SelectContent class="max-h-60 overflow-y-auto">
          <SelectItem value="none">---</SelectItem>
          <template v-for="col in columns" :key="col.id">
            <SelectGroup v-if="col.cards && col.cards.length > 0">
              <SelectLabel class="px-2.5 py-1.5 text-[9px] font-sans font-extrabold text-muted-foreground/70 uppercase tracking-widest border-b border-border/20 mb-1 select-none">
                {{ col.name }}
              </SelectLabel>
              <SelectItem v-for="task in col.cards" :key="task.id" :value="task.id" class="pl-5 cursor-pointer">
                {{ task.title }}
              </SelectItem>
            </SelectGroup>
          </template>
        </SelectContent>
      </Select>
    </div>
  </div>
</template>
