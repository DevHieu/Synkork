<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { ArrowRight, Columns3, LoaderCircle } from "lucide-vue-next";
import { toast } from "vue-sonner";
import { getAllColumnsWithoutCard } from "@/features/tasks/services/columnService";
import type { ColumnEvent } from "@/types/Task";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

const props = defineProps<{
  open: boolean;
  spaceId: string;
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "close"): void;
  (e: "select", columnId: string): void;
}>();

const loading = ref(false);
const columnList = ref<ColumnEvent[]>([]);

const sortedColumns = computed(() =>
  [...columnList.value].sort((a, b) => (a.position ?? 0) - (b.position ?? 0)),
);

const closeDialog = () => {
  emit("update:open", false);
  emit("close");
};

const loadColumns = async () => {
  if (!props.spaceId) {
    columnList.value = [];
    return;
  }

  loading.value = true;

  try {
    const response = await getAllColumnsWithoutCard(props.spaceId);
    columnList.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    toast.error("Không thể tải danh sách cột.");
    columnList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSelect = (column: ColumnEvent) => {
  emit("select", column.id);
  emit("update:open", false);
};

watch(
  () => [props.open, props.spaceId] as const,
  ([isOpen]) => {
    if (isOpen) {
      loadColumns();
    }
  },
  { immediate: true },
);
</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="sm:max-w-lg">
      <DialogHeader>
        <DialogTitle class="flex items-center gap-2">
          <Columns3 class="h-5 w-5 text-primary" />
          Chọn cột task
        </DialogTitle>
        <DialogDescription>
          Chọn cột bạn muốn tạo thẻ mới trong space task này.
        </DialogDescription>
      </DialogHeader>

      <div v-if="loading" class="flex min-h-40 items-center justify-center">
        <LoaderCircle class="h-5 w-5 animate-spin text-primary" />
      </div>

      <div v-else class="flex flex-col gap-3">
        <div class="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
          Danh sách cột
        </div>

        <div v-if="sortedColumns.length" class="flex max-h-80 flex-col gap-2 overflow-y-auto pr-1">
          <button v-for="column in sortedColumns" :key="column.id" type="button"
            class="flex items-center justify-between rounded-xl border border-border bg-background px-4 py-3 text-left transition-colors hover:border-primary hover:bg-primary/5"
            @click="handleSelect(column)">
            <div class="flex min-w-0 flex-col">
              <span class="truncate text-sm font-medium text-foreground">
                {{ column.name }}
              </span>
              <span class="text-xs text-muted-foreground">
                Vị trí {{ (column.position ?? 0) + 1 }}
              </span>
            </div>
            <ArrowRight class="h-4 w-4 shrink-0 text-primary" />
          </button>
        </div>

        <div v-else class="rounded-xl border border-dashed border-border px-4 py-6 text-sm text-muted-foreground">
          Chưa có cột nào trong space task này.
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="closeDialog">
          Hủy
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
