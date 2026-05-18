<script setup lang="ts">
import { watch } from "vue";
import { Plus, X } from "lucide-vue-next";
import { useAttendees } from "../composables/useAttendees";

const props = defineProps<{
  initialAttendees?: string[];
  show: boolean;
}>();

const emit = defineEmits<{
  (e: "change", attendees: string[]): void;
}>();

const { attendees, attendeeInput, addAttendee, removeAttendee, resetAttendees } =
  useAttendees(props.initialAttendees || []);

// Section này chỉ quản lý danh sách email cục bộ của form hiện tại.
// Đồng bộ với component cha khi danh sách người tham gia thay đổi
watch(attendees, (newList) => {
  emit("change", newList);
}, { deep: true });

// Làm mới khi Dialog mở ra
watch(() => props.show, (isOpen) => {
  if (isOpen) resetAttendees(props.initialAttendees || []);
});
</script>

<template>
  <div>
    <label class="block text-[10px] font-mono font-bold text-muted-foreground uppercase tracking-widest mb-2">Người tham gia</label>
    <div class="flex flex-col gap-2">
      <div class="flex gap-2 rounded-xl border-2 border-border bg-background p-2 shadow-[0_16px_34px_-30px_var(--color-foreground)]">
        <input v-model="attendeeInput" @keyup.enter="addAttendee" @keydown.enter.prevent type="text"
          placeholder="NHẬP EMAIL VÀ ẤN ENTER..."
          class="flex-1 rounded-lg border-2 border-border bg-background px-4 py-3 font-mono text-xs uppercase text-foreground placeholder-muted-foreground transition-colors focus:outline-none focus:border-primary" />
        <button type="button" @click="addAttendee"
          class="rounded-full border-2 border-primary bg-primary px-4 py-2 text-primary-foreground transition-colors hover:bg-background hover:text-primary">
          <Plus :size="16" />
        </button>
      </div>
      <div v-if="attendees.length > 0" class="flex flex-wrap gap-2 mt-2">
        <div v-for="(email, idx) in attendees" :key="idx"
          class="flex items-center gap-1.5 rounded-full border-2 border-border bg-muted/30 px-3 py-1.5 text-xs font-mono text-foreground">
          <span>{{ email }}</span>
          <button type="button" @click="removeAttendee(idx)" class="text-muted-foreground hover:text-destructive transition-colors ml-1">
            <X :size="12" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
