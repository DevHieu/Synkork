<template>
  <div class="relative inline-block" ref="bellRef">
    <button
      @click="toggleDropdown"
      class="relative p-2 rounded-full hover:bg-gray-100 transition"
    >
      <svg xmlns="http://www.w3.org/2000/svg" class="w-6 h-6 text-gray-600"
        fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
          d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12
             0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
      </svg>

      <span
        v-if="unreadCount > 0"
        class="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold
               w-5 h-5 rounded-full flex items-center justify-center"
      >
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <Transition
      enter-active-class="transition ease-out duration-150"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
      <NotificationDropdown class="fixed right-4 top-16" v-if="isOpen" @close="isOpen = false" />
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useNotificationStore } from '@/stores/notificationStore'
import NotificationDropdown from '@/components/notification/NotificationDropDown.vue'

const store = useNotificationStore()
const { unreadCount } = storeToRefs(store)

const isOpen = ref<boolean>(false)
const bellRef = ref<HTMLElement | null>(null)

function toggleDropdown(): void {
  isOpen.value = !isOpen.value
}

// Click outside để đóng
function handleClickOutside(event: MouseEvent): void {
  if (bellRef.value && !bellRef.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('mousedown', handleClickOutside))
onUnmounted(() => document.removeEventListener('mousedown', handleClickOutside))
</script>