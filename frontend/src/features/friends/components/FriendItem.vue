<script setup lang="ts">
defineProps<{
  id: string
  name: string
  avatarUrl?: string | null
  status: string
}>()
</script>

<template>
  <div class="flex items-center gap-3 p-2 hover:bg-[#35373c] rounded transition group">
    <!-- Avatar -->
    <div class="relative flex-shrink-0">
      <div class="w-10 h-10 rounded-full bg-[#4e5058] overflow-hidden flex items-center justify-center text-sm font-bold border border-[#202225]">
        <img 
          v-if="avatarUrl" 
          :src="avatarUrl" 
          class="w-full h-full object-cover"
          alt="avatar"
        />
        <span v-else>{{ name?.substring(0, 2).toUpperCase() }}</span>
      </div>

      <!-- Status -->
      <div 
        class="absolute bottom-0 right-0 w-3.5 h-3.5 rounded-full border-[2.5px] border-[#313338]"
        :class="status === 'online' ? 'bg-green-500' : 'bg-gray-500'"
      />
    </div>

    <!-- Info -->
    <div class="flex-1 min-w-0">
      <div class="font-semibold text-white truncate">{{ name }}</div>
      <div class="text-xs text-gray-400">{{ status === 'online' ? 'Đang hoạt động' : 'Ngoại tuyến' }}</div>
    </div>

    <!-- Nút xóa (hiện khi hover) -->
    <button 
      @click.stop="$emit('remove', id)"
      class="opacity-0 group-hover:opacity-100 text-red-400 hover:text-red-500 p-1 transition"
    >
      ✕
    </button>
  </div>
</template>
