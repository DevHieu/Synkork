import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import { useTaskAction } from "./task-api";
import { ref, computed } from "vue";
import type { MemberSummary } from "@/types/Task";
import type { Member } from "@/types/Member";

export function useCardDetail(
    props: { card: any; open: boolean },
    emit: Function,
) {
    const spaceStore = useSpaceStore();
    const { currentSpace } = storeToRefs(spaceStore);

    const taskAction = useTaskAction();

    const status = computed(() => getStatus(props.card.dueDate));

    const form = ref({ title: "", description: "", dueDate: "" });

    const localAssignees = ref<MemberSummary[]>([]);

    const baseVersion = ref<number | undefined>(props.card.version);

    const getStatus = (dueDate?: string) => {
        if (!dueDate) return null;
        const now = new Date();
        const due = new Date(dueDate);
        const diff = due.getTime() - now.getTime();

        if (diff < 0) return "OVERDUE";
        if (diff <= 24 * 60 * 60 * 1000) return "DUE_SOON";

        return "NORMAL";
    }

    const clearDueDate = () => {
        form.value.dueDate = ""
        handleSave()
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

    const handleSave = () => {
        if (!form.value.title.trim()) return;
        emitSave();
    };

    const handleArchive = () => {
        if (!currentSpace.value) return;

        taskAction.archiveCardEvent(currentSpace.value.id, props.card.id);

        emit("archive", props.card.id);
        emit("update:open", false);
    };

    const toggleAssignee = (member: Member) => {
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
        localAssignees.value = localAssignees.value.filter((a) => a.id !== id);
        emitSave();
    };

    return {
        form,
        status,
        localAssignees,
        baseVersion,
        getStatus,
        clearDueDate,
        handleSave,
        handleArchive,
        toggleAssignee,
        removeAssignee
    }
}