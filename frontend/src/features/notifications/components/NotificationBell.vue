<script setup lang="ts">
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import { Bell } from 'lucide-vue-next'
import { useNotificationStore } from '@/features/notifications/stores/notificationStore'
import NotificationDropdown from './NotificationDropDown.vue'

import { Button } from '@/components/ui/button'
import { Popover, PopoverTrigger, PopoverContent } from '@/components/ui/popover'

const store = useNotificationStore()
const { unreadCount } = storeToRefs(store)
const isOpen = ref(false)
</script>
<template>
  <Popover v-model:open="isOpen">
    <PopoverTrigger as-child>
      <Button
        variant="ghost"
        size="icon" 
        class="relative w-9 h-9 rounded-full cursor-pointer"
      >
        <Bell class="w-7 h-7 text-foreground" />

        <span
          v-if="unreadCount > 0"
          class="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-bold
                 min-w-[18px] h-[18px] rounded-full flex items-center justify-center px-1 leading-none"
        >
          {{ unreadCount > 9 ? '9+' : unreadCount }}
        </span>
      </Button>
    </PopoverTrigger>

    <PopoverContent
      class="w-auto p-0 border-0 shadow-none"
      align="end"
      :side-offset="8"
    >
      <NotificationDropdown @close="isOpen = false" />
    </PopoverContent>
  </Popover>
</template>