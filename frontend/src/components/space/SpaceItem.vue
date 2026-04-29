<script setup lang="ts">
import { ref } from "vue";
import { Hash, Lock, Settings } from "lucide-vue-next";
import { SidebarMenuButton, SidebarMenuItem } from "@/components/ui/sidebar";
import SpaceSettingDialog from "./SpaceSettingDialog.vue";

const props = defineProps<{
  spaceId: string;
  spaceName: string;
  isActive: boolean;
  canManage: boolean;
  restricted: boolean;
}>();

const emit = defineEmits<{
  click: [];
  save: [data: { name: string; restricted: boolean }];
  delete: [];
}>();

const settingOpen = ref(false);
</script>

<template>
  <SidebarMenuItem>
    <SidebarMenuButton
      @click="emit('click')"
      :isActive="isActive"
      class="group/item pr-1"
    >
      <!-- Icon khóa nếu restricted -->
      <Lock
        v-if="restricted"
        class="mr-2 h-4 w-4 shrink-0 text-muted-foreground"
      />
      <Hash v-else class="mr-2 h-4 w-4 shrink-0" />

      <span class="flex-1 truncate">{{ spaceName }}</span>

      <button
        v-if="canManage"
        @click.stop="settingOpen = true"
        class="opacity-0 group-hover/item:opacity-100 transition-opacity ml-1 p-0.5 rounded hover:bg-muted"
      >
        <Settings class="h-4 w-4 text-foreground" />
      </button>
    </SidebarMenuButton>

    <SpaceSettingDialog
      v-model:open="settingOpen"
      :space-id="spaceId"
      :space-name="spaceName"
      :restricted="restricted"
      @save="emit('save', $event)"
      @delete="emit('delete')"
    />
  </SidebarMenuItem>
</template>
