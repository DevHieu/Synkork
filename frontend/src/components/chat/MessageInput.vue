<script setup lang="ts">
const newMessage = defineModel<string>({ required: true });

const emit = defineEmits<{
  send: [];
}>();

const handleSubmit = () => {
  emit("send");
};
</script>

<template>
  <div
    class="border-t border-white/10 px-4 py-3 bg-zinc-950/70 backdrop-blur-md"
  >
    <form @submit.prevent="handleSubmit" class="flex gap-2">
      <input
        v-model="newMessage"
        placeholder="Nhắn tin..."
        class="flex-1 bg-white/5 border border-white/10 px-3 py-2 rounded text-white placeholder-gray-400 focus:outline-none focus:ring-1 focus:ring-teal-500"
      />
      <button
        class="px-4 py-2 bg-teal-600 text-white rounded hover:bg-teal-500 transition-colors font-medium"
      >
        Gửi
      </button>
    </form>
  </div>
</template>
