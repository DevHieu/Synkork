<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoomsStore } from "@/stores/roomStore";
import { storeToRefs } from "pinia";

import Dialog from "@/components/ui/dialog/Dialog.vue";
import DialogContent from "@/components/ui/dialog/DialogContent.vue";
import DialogTitle from "@/components/ui/dialog/DialogTitle.vue";
import DialogHeader from "@/components/ui/dialog/DialogHeader.vue";
import { Settings, Users, Trash2 } from "lucide-vue-next";
import InfoTab from "./InfoTab.vue";
import MembersTab from "./MembersTab.vue";
import DeleteTab from "./DeleteTab.vue";

const props = defineProps<{ open: boolean }>();
const emit = defineEmits<{
  "update:open": [value: boolean];
}>();

const handleClose = () => {
  emit("update:open", false);
};

const roomStore = useRoomsStore();
const { currentRoom } = storeToRefs(roomStore);

// Tabs
type Tab = "info" | "members" | "danger";
const activeTab = ref<Tab>("info");

const tabs: { key: Tab; label: string; icon: any }[] = [
  { key: "info", label: "Thông tin", icon: Settings },
  { key: "members", label: "Thành viên", icon: Users },
  { key: "danger", label: "Nguy hiểm", icon: Trash2 },
];
</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent
      class="!max-w-[65vw] !w-[65vw] !max-h-[65vh] !h-[65vh] !p-0 overflow-hidden rounded-xl border border-border bg-background shadow-2xl"
    >
      <!-- Header -->
      <DialogHeader class="px-6 pt-6 pb-0">
        <DialogTitle class="text-lg font-semibold text-foreground">
          Cài đặt phòng
        </DialogTitle>
      </DialogHeader>

      <div
        class="flex flex-1 overflow-hidden"
        style="height: calc(85vh - 130px)"
      >
        <!-- Sidebar tabs -->
        <nav
          class="w-44 border-r border-border px-2 py-2 flex flex-col gap-0.5 shrink-0"
        >
          <button
            v-for="tab in tabs"
            :key="tab.key"
            @click="activeTab = tab.key"
            :class="[
              'flex items-center gap-2.5 w-full px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150',
              activeTab === tab.key
                ? 'bg-primary/10 text-primary'
                : 'text-muted-foreground hover:bg-muted hover:text-foreground',
              tab.key === 'danger' && activeTab !== 'danger'
                ? 'hover:text-destructive hover:bg-destructive/5'
                : '',
              tab.key === 'danger' && activeTab === 'danger'
                ? '!bg-destructive/10 !text-destructive'
                : '',
            ]"
          >
            <component :is="tab.icon" class="h-4 w-4 shrink-0" />
            {{ tab.label }}
          </button>
        </nav>

        <!-- Content -->
        <div class="flex-1 overflow-y-auto px-6 py-4">
          <!-- TAB: THÔNG TIN -->
          <div v-if="activeTab === 'info'" class="flex flex-col gap-5">
            <InfoTab :room="currentRoom" />
          </div>

          <!-- TAB: THÀNH VIÊN -->
          <div v-if="activeTab === 'members'" class="flex flex-col gap-3">
            <MembersTab />
          </div>

          <!-- TAB: NGUY HIỂM -->
          <div v-if="activeTab === 'danger'" class="flex flex-col gap-5">
            <DeleteTab
              :roomName="currentRoom?.name ?? ''"
              @delete="handleClose"
            />
          </div>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<style scoped></style>
