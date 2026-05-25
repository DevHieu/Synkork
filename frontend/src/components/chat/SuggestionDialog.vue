<script setup lang="ts">
import { nextTick, ref, watch } from "vue";
import ChooseSpaceDialog from "./sub-components/ChooseSpaceDialog.vue";
import NoteDialog from "@/components/dialog/NoteDialog/NoteDialog.vue";
import type {
  CalendarChannelOption,
  MessageEventSuggestion,
  SuggestedNoteDraft,
  SuggestedTaskDraft,
} from "@/types/CalendarSuggestion";
import { buildSuggestedEventDraft, buildSuggestedNoteDraft, buildSuggestedTaskDraft } from "@/utils/calendarSuggestion";
import ColumnListDialog from "../dialog/task/ColumnListDialog.vue";
import CardFormDialog from "../dialog/task/CardFormDialog.vue";

import { useSpaceStore } from "@/stores/spaceStore";
import { useTaskStore } from "@/stores/taskStore";
import { useSuggestionStore } from "@/stores/suggestionStore";

const spaceStore = useSpaceStore();
const suggestionStore = useSuggestionStore();
const taskStore = useTaskStore();

const props = defineProps<{
  open: boolean;
  roomId: string;
  messageInfo: MessageEventSuggestion | null;
}>();

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
  (e: "close"): void;
}>();


const dialogTargetType = ref<"CALENDAR" | "NOTE" | "TASK">("CALENDAR");
const spaceDialogOpen = ref(false);

const spaceChoosenId = ref("");

const noteDialogOpen = ref(false);
const noteDraft = ref<SuggestedNoteDraft | null>(null);

const taskColumnDialogOpen = ref(false);
const taskCardDialogOpen = ref(false);
const columnChoosenId = ref("");
const taskDraft = ref<SuggestedTaskDraft | null>(null);

const resolveTargetType = (suggestion: MessageEventSuggestion) => {
  return suggestion.suggestionType === "EVENT"
    ? "CALENDAR"
    : suggestion.suggestionType;
};

const closeAll = () => {
  spaceDialogOpen.value = false;
  noteDialogOpen.value = false;
  taskColumnDialogOpen.value = false;
  taskCardDialogOpen.value = false;
  noteDraft.value = null;
  taskDraft.value = null;
  columnChoosenId.value = "";
  spaceChoosenId.value = "";
  emit("update:open", false);
  emit("close");
};

watch(
  () => [props.open, props.messageInfo] as const,
  ([isOpen, suggestion]) => {
    if (!isOpen) {
      spaceDialogOpen.value = false;
      return;
    }

    if (!suggestion || suggestion.suggestionType === "NONE") {
      closeAll();
      return;
    }

    const targetType = resolveTargetType(suggestion);
    if (targetType !== "NONE") {
      dialogTargetType.value = targetType;
      spaceDialogOpen.value = true;
    }
  },
  { immediate: true },
);

const handleSelectSuggestionChannel = async (
  option: CalendarChannelOption,
  chosenType: "CALENDAR" | "NOTE" | "TASK",
) => {
  const suggestion = props.messageInfo;
  if (!suggestion || suggestion.suggestionType === "NONE") {
    closeAll();
    return;
  }

  spaceDialogOpen.value = false;
  await nextTick();

  if (chosenType === "CALENDAR") {
    suggestionStore.setPendingDraft(
      option.spaceId,
      buildSuggestedEventDraft(suggestion),
    );

    await spaceStore.changeSpaceById(option.spaceId, "CALENDAR");
  }

  if (chosenType === "NOTE") {
    spaceChoosenId.value = option.spaceId;
    noteDraft.value = buildSuggestedNoteDraft(suggestion);
    noteDialogOpen.value = true;
    return;
  }

  if (chosenType === "TASK") {
    spaceChoosenId.value = option.spaceId;
    taskDraft.value = buildSuggestedTaskDraft(suggestion);
    taskColumnDialogOpen.value = true;
    return;
  }

  closeAll();
};

const handleSpaceDialogOpenChange = (value: boolean) => {
  spaceDialogOpen.value = value;

  if (!value && !noteDialogOpen.value && !taskColumnDialogOpen.value && !taskCardDialogOpen.value) {
    closeAll();
  }
};

const handleSelectTaskColumn = async (columnId: string) => {
  columnChoosenId.value = columnId;
  taskColumnDialogOpen.value = false;
  await nextTick();
  taskCardDialogOpen.value = true;
};

const handleSaveTaskCard = async (data: { title: string; description: string }) => {
  await taskStore.saveCard(
    spaceChoosenId.value,
    "",
    columnChoosenId.value,
    data.title,
    data.description,
  );

  closeAll();
};
</script>

<template>
  <ChooseSpaceDialog :open="spaceDialogOpen" :target-type="dialogTargetType" @update:open="handleSpaceDialogOpenChange"
    @select="handleSelectSuggestionChannel" />

  <NoteDialog :space-id="spaceChoosenId" :open="noteDialogOpen" :note="null" :draft="noteDraft" @close="closeAll" />

  <ColumnListDialog v-model:open="taskColumnDialogOpen" :space-id="spaceChoosenId" @close="closeAll"
    @select="handleSelectTaskColumn" />

  <CardFormDialog v-model:open="taskCardDialogOpen" :columnId="columnChoosenId" :taskData="null" :draft="taskDraft"
    @save="handleSaveTaskCard" />
</template>

<style scoped></style>
