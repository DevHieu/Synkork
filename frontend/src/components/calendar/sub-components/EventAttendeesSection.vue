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
    <label class="block text-sm text-muted-foreground mb-1.5 font-medium">Người tham gia</label>
    <div class="flex flex-col gap-2">
      <div class="flex gap-2">
        <input v-model="attendeeInput" @keyup.enter="addAttendee" @keydown.enter.prevent type="text"
          placeholder="Nhập email và ấn Enter hoặc nút thêm..."
          class="flex-1 bg-muted border border-border rounded-lg px-3 py-2 text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all text-sm" />
        <button type="button" @click="addAttendee"
          class="bg-muted text-foreground px-3 py-2 rounded-lg hover:bg-muted/80 transition-all">
          <Plus :size="16" />
        </button>
      </div>
      <div v-if="attendees.length > 0" class="flex flex-wrap gap-2 mt-1">
        <div v-for="(email, idx) in attendees" :key="idx"
          class="flex items-center gap-1.5 bg-primary/20 text-primary px-2 py-1 rounded-md text-xs border border-primary/20">
          <span>{{ email }}</span>
          <button type="button" @click="removeAttendee(idx)" class="hover:text-primary/80 transition-colors">
            <X :size="10" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
