<script setup lang="ts">
import { ref, watch } from "vue";
import type { CalendarEvent } from "@/types/CalendarEvent";

const props = defineProps<{
  show: boolean;
  isEditing: boolean;
  initialData: {
    title: string;
    description: string;
    eventDate: string;
    startTime: string;
    endTime: string;
    allowEditAll: boolean;
  };
  checkConflicts: (date: string, start: string, end: string, excludeId?: string) => Promise<CalendarEvent[]>;
  editingEventId?: string;
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "save", data: typeof props.initialData): void;
}>();

const formData = ref({ ...props.initialData });
const conflictEvents = ref<CalendarEvent[]>([]);
const isCheckingConflict = ref(false);

let conflictDebounce: ReturnType<typeof setTimeout> | null = null;

// Reset form data when dialog opens
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      formData.value = { ...props.initialData };
      conflictEvents.value = [];
    }
  }
);

// Check conflicts when date/time changes
watch(
  () => [formData.value.eventDate, formData.value.startTime, formData.value.endTime],
  ([date, start, end]) => {
    if (!props.show || !date || !start || !end) {
      conflictEvents.value = [];
      return;
    }
    if (conflictDebounce) clearTimeout(conflictDebounce);
    conflictDebounce = setTimeout(async () => {
      isCheckingConflict.value = true;
      try {
        conflictEvents.value = await props.checkConflicts(
          date as string,
          start as string,
          end as string,
          props.isEditing ? props.editingEventId : undefined
        );
      } catch {
        conflictEvents.value = [];
      } finally {
        isCheckingConflict.value = false;
      }
    }, 400);
  }
);

const handleSubmit = () => {
  if (!formData.value.title.trim()) return;
  emit("save", formData.value);
};
</script>

<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <!-- Overlay -->
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="emit('update:show', false)"
      ></div>

      <!-- Dialog Content -->
      <div
        class="relative bg-zinc-900 rounded-2xl shadow-2xl border border-white/10 w-full max-w-md mx-4 p-6"
      >
        <h2 class="text-lg font-semibold text-white mb-4">
          {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
        </h2>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Title -->
          <div>
            <label class="block text-sm text-gray-400 mb-1">Tiêu đề *</label>
            <input
              v-model="formData.title"
              type="text"
              required
              placeholder="Nhập tiêu đề sự kiện..."
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
            />
          </div>

          <!-- Description -->
          <div>
            <label class="block text-sm text-gray-400 mb-1">Mô tả</label>
            <textarea
              v-model="formData.description"
              rows="3"
              placeholder="Mô tả chi tiết sự kiện..."
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 resize-none"
            ></textarea>
          </div>

          <!-- Date -->
          <div>
            <label class="block text-sm text-gray-400 mb-1">Ngày *</label>
            <input
              v-model="formData.eventDate"
              type="date"
              required
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
            />
          </div>

          <!-- Time -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-sm text-gray-400 mb-1"
                >Giờ bắt đầu *</label
              >
              <input
                v-model="formData.startTime"
                type="time"
                required
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
              />
            </div>
            <div>
              <label class="block text-sm text-gray-400 mb-1"
                >Giờ kết thúc *</label
              >
              <input
                v-model="formData.endTime"
                type="time"
                required
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
              />
            </div>
          </div>

          <!-- Conflict Warning -->
          <div
            v-if="conflictEvents.length > 0"
            class="bg-amber-500/10 border border-amber-500/30 rounded-lg p-3"
          >
            <div class="flex items-center gap-2 text-amber-400 text-sm font-medium mb-1">
              <font-awesome-icon icon="exclamation-triangle" />
              Trùng giờ với {{ conflictEvents.length }} sự kiện:
            </div>
            <ul class="text-xs text-amber-300/80 space-y-0.5 ml-5">
              <li v-for="c in conflictEvents" :key="c.id">
                • {{ c.title }} ({{ c.startTime.substring(0, 5) }} - {{ c.endTime.substring(0, 5) }})
              </li>
            </ul>
          </div>

          <!-- Allow Edit All -->
          <div class="flex items-center gap-3">
            <label class="relative inline-flex items-center cursor-pointer">
              <input
                v-model="formData.allowEditAll"
                type="checkbox"
                class="sr-only peer"
              />
              <div
                class="w-9 h-5 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-teal-600"
              ></div>
            </label>
            <span class="text-sm text-gray-300"
              >Cho phép mọi người chỉnh sửa</span
            >
          </div>

          <!-- Actions -->
          <div class="flex gap-2 justify-end pt-2">
            <button
              type="button"
              @click="emit('update:show', false)"
              class="px-4 py-2 rounded-lg text-gray-300 hover:bg-white/10 transition-colors text-sm"
            >
              Hủy
            </button>
            <button
              type="submit"
              class="px-4 py-2 bg-teal-600 text-white rounded-lg hover:bg-teal-700 transition-colors text-sm font-medium"
            >
              {{ isEditing ? "Cập nhật" : "Tạo sự kiện" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
input[type="date"]::-webkit-calendar-picker-indicator,
input[type="time"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}
</style>
