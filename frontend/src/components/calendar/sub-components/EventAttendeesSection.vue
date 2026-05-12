<script setup lang="ts">
import { watch } from "vue";
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
    <label class="block text-sm text-gray-400 mb-1.5 font-medium">Người tham gia</label>
    <div class="flex flex-col gap-2">
      <div class="flex gap-2">
        <input v-model="attendeeInput" @keyup.enter="addAttendee" @keydown.enter.prevent type="text"
          placeholder="Nhập email và ấn Enter hoặc nút thêm..."
          class="flex-1 bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm" />
        <button type="button" @click="addAttendee"
          class="bg-white/10 text-white px-3 py-2 rounded-lg hover:bg-white/20 transition-all">
          <i class="pi pi-plus" />
        </button>
      </div>
      <div v-if="attendees.length > 0" class="flex flex-wrap gap-2 mt-1">
        <div v-for="(email, idx) in attendees" :key="idx"
          class="flex items-center gap-1.5 bg-teal-500/20 text-teal-300 px-2 py-1 rounded-md text-xs border border-teal-500/20">
          <span>{{ email }}</span>
          <button type="button" @click="removeAttendee(idx)" class="hover:text-white transition-colors">
            <i class="pi pi-times text-[10px]" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
