<script setup lang="ts">
import { ref } from "vue";
import { Volume2, MicOff, VolumeX, Settings } from "lucide-vue-next";
import { SidebarMenuButton, SidebarMenuItem } from "@/components/ui/sidebar";
import SpaceSettingDialog from "./SpaceSettingDialog.vue";

const props = defineProps<{
  spaceId: string;
  spaceName: string;
  isActive: boolean;
  canManage: boolean;
  restricted: boolean;
  participants: {
    userID: string;
    userName: string;
    avatarUrl?: string;
    micOn: boolean;
    audioOn: boolean;
    isLocal: boolean;
  }[];
}>();

const emit = defineEmits<{
  join: [];
  save: [data: { name: string; restricted: boolean }];
  delete: [];
}>();

const settingOpen = ref(false);

const getInitials = (name: string) =>
  name
    ?.split(/[\s_-]/)
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2) ?? "?";
</script>

<template>
  <SidebarMenuItem>
    <SidebarMenuButton
      @click="emit('join')"
      :isActive="isActive"
      class="group/item pr-1"
    >
      <Volume2 class="mr-2 h-4 w-4 shrink-0" />
      <span class="flex-1 truncate">{{ spaceName }}</span>

      <button
        v-if="canManage"
        @click.stop="settingOpen = true"
        class="opacity-0 group-hover/item:opacity-100 transition-opacity ml-1 p-0.5 rounded hover:bg-muted"
      >
        <Settings class="h-4 w-4 text-foreground" />
      </button>
    </SidebarMenuButton>

    <!-- Participants -->
    <div v-if="participants.length" class="ml-4 mt-0.5 mb-1 space-y-0.5">
      <div
        v-for="p in participants"
        :key="p.userID"
        class="flex items-center gap-2 px-2 py-0.5 rounded text-xs text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-colors cursor-default"
      >
        <div class="relative shrink-0">
          <img
            v-if="p.avatarUrl"
            :src="p.avatarUrl"
            class="w-5 h-5 rounded-full object-cover"
          />
          <div
            v-else
            class="w-5 h-5 rounded-full bg-primary/80 flex items-center justify-center text-[9px] font-bold text-primary-foreground"
          >
            {{ getInitials(p.userName) }}
          </div>
        </div>
        <span class="truncate flex-1">
          {{ p.userName }}
          <span v-if="p.isLocal" class="opacity-50">(Bạn)</span>
        </span>
        <div class="flex items-center gap-1 shrink-0">
          <MicOff v-if="!p.micOn" class="h-3.5 w-3.5 text-red-500" />
          <VolumeX v-if="!p.audioOn" class="h-3.5 w-3.5 text-red-500" />
        </div>
      </div>
    </div>

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
