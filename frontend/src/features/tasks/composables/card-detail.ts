import { useSpaceStore } from "@/features/spaces/stores/spaceStore.ts";
import { storeToRefs } from "pinia";
import { useTaskAction } from "./task-api";
import { ref, computed } from "vue";
import type { MemberSummary } from "@/features/tasks/types/Task";
import type { Member } from "@/features/members/types/Member";
import { checkOverdue } from "../utils/task-date";
import { toast } from "vue-sonner";

export function useCardDetail(
  props: { card: any; open: boolean; readOnly: boolean },
  emit: Function,
) {
  const spaceStore = useSpaceStore();
  const { currentSpace } = storeToRefs(spaceStore);

  const taskAction = useTaskAction();

  const status = computed(() => getStatus(props.card.dueDate));

  const form = ref({ title: "", description: "", dueDate: "" });

  const localAssignees = ref<MemberSummary[]>([]);

  const baseVersion = ref<number | undefined>(props.card.version);

  const isCompleted = ref<boolean>(props.card.completed ?? false);

  const getStatus = (dueDate?: string) => {
    if (!dueDate) return null;
    const now = new Date();
    const due = new Date(dueDate);
    const diff = due.getTime() - now.getTime();

    if (diff < 0) return "OVERDUE";
    if (diff <= 24 * 60 * 60 * 1000) return "DUE_SOON";

    return "NORMAL";
  };

  const emitSave = () => {
    if (!form.value.title.trim()) return;
    const formattedDueDate = form.value.dueDate
      ? `${form.value.dueDate}:00`
      : null;
    emit("save", {
      ...props.card,
      title: form.value.title.trim(),
      description: form.value.description.trim(),
      assignees: localAssignees.value,
      dueDate: formattedDueDate,
      version: baseVersion.value,
    });
  };

  const handleDueDateChange = () => {
    if (props.readOnly) return;
    if (form.value.dueDate) {
      const formattedDueDate = `${form.value.dueDate}:00`;

      if (checkOverdue(formattedDueDate)) {
        toast.error("Không thể đặt ngày hết hạn trong quá khứ.");
        form.value.dueDate = props.card.dueDate
          ? props.card.dueDate.slice(0, 16)
          : "";
        return;
      }
    }
    emitSave();
  };

  const handleSave = () => {
    if (props.readOnly) return;
    if (!form.value.title.trim()) return;
    emitSave();
  };

  const handleArchive = () => {
    if (!currentSpace.value) return;
    if (props.readOnly) return;

    taskAction.archiveCardEvent(currentSpace.value.id, props.card.id);

    emit("archive", props.card.id);
    emit("update:open", false);
  };

  const toggleAssignee = (member: Member) => {
    if (props.readOnly) return;
    const exists = localAssignees.value.some((a) => a.id === member.memberId);

    if (exists) {
      localAssignees.value = localAssignees.value.filter(
        (a) => a.id !== member.memberId,
      );
    } else {
      localAssignees.value.push({
        id: member.memberId,
        name: member.displayName,
        avatarUrl: member.avatarUrl,
      });
    }

    emitSave();
  };

  const removeAssignee = (id: string) => {
    if (props.readOnly) return;
    localAssignees.value = localAssignees.value.filter((a) => a.id !== id);
    emitSave();
  };

  const handleToggleComplete = () => {
    if (props.readOnly || !currentSpace.value) return;
    const newStatus = !isCompleted.value;
    isCompleted.value = newStatus;
    props.card.completed = newStatus;

    taskAction.completeCardEvent(
      currentSpace.value.id,
      props.card.id,
      newStatus,
    );

    emit("toggle-complete", { id: props.card.id, completed: newStatus });
  };

  return {
    form,
    status,
    localAssignees,
    baseVersion,
    isCompleted,
    getStatus,
    handleSave,
    handleArchive,
    toggleAssignee,
    removeAssignee,
    handleDueDateChange,
    handleToggleComplete,
  };
}
