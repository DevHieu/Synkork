<script setup lang="ts">
import { ref } from "vue";
import { Settings, UserRoundPlus } from "lucide-vue-next";
import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
} from "@/components/ui/sidebar";
import { createSpace, updateSpace, deleteSpace } from "@/services/spaceService";
import { useRoomsStore } from "@/stores/roomStore";
import { useSpaceStore } from "@/stores/spaceStore";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { storeToRefs } from "pinia";
import { toast } from "vue-sonner";

import SpaceGroup from "@/components/space/SpaceGroup.vue";
import SpaceItem from "@/components/space/SpaceItem.vue";
import VoiceSpaceItem from "@/components/space/VoiceSpaceItem.vue";
import CreateSpaceDialog from "@/components/dialog/CreateSpaceDialog.vue";
import RoomSettingDialog from "@/components/dialog/RoomSettingDialog/index.vue";
import InviteDialog from "@/components/dialog/InviteMemberDialog.vue";


const roomStore = useRoomsStore();
const spaceStore = useSpaceStore();
const voiceSpaceStore = useVoiceSpaceStore();
const { canManage } = storeToRefs(useRoomMemberStore());
const { currentRoom } = storeToRefs(roomStore);
const {
  currentSpace,
  chatSpaces,
  voiceSpaces,
  calendarSpaces,
  noteSpaces,
  taskSpaces,
  loading
} = storeToRefs(spaceStore);

const showRoomSettingDialog = ref(false);
const showInviteDialog = ref(false);
const showAddSpaceDialog = ref(false);
const selectedSpaceType = ref("CHAT");

const openAddSpaceDialog = (type: string) => {
  selectedSpaceType.value = type;
  showAddSpaceDialog.value = true;
};

const handleCreateSpace = async (name: string, type: string) => {
  const roomId = currentRoom.value?.id ?? "";

  const res = await createSpace(roomId, {
    name,
    type,
  });

  if (res.status === 200) {
    toast.success("Tạo kênh thành công");
    spaceStore.changeSpaceById(res.data.id, type);
  }
};

const handleUpdateSpace = async (
  spaceId: string,
  name: string,
  restricted: boolean,
) => {
  const res = await updateSpace(currentRoom.value?.id ?? "", spaceId, {
    name,
    restricted,
  });

  if (res.status === 200) {
    toast.success("Cập nhật kênh thành công");
  }
};

const handleDelete = async (spaceId: string) => {
  const res = await deleteSpace(currentRoom.value?.id ?? "", spaceId);

  if (res.status === 200) {
    toast.success("Xóa kênh thành công");
  }
};
</script>

<template>
  <Sidebar collapsible="none" class="hidden flex-1 md:flex">
    <SidebarHeader class="gap-3.5 border-b px-4 py-3">
      <div class="flex w-full items-center justify-between">
        <div class="text-base font-medium text-foreground">
          {{ currentRoom?.name }}
        </div>
        <div class="flex gap-3.5">
          <button @click.stop="showRoomSettingDialog = true" class="transition duration-150 hover:text-foreground">
            <Settings class="h-5 w-5" />
          </button>
          
          <button
            @click="showInviteDialog = true"
            class="p-1.5 rounded-lg hover:bg-muted transition text-muted-foreground hover:text-foreground"
          ></button>
          <button @click="showInviteDialog = true"
            class="p-1.5 rounded-lg hover:bg-muted transition text-muted-foreground hover:text-foreground">
            <UserRoundPlus class="h-5 w-5" />
          </button>
        </div>
      </div>
    </SidebarHeader>

    <SidebarContent class="mt-5">
      <SpaceGroup label="KÊNH TASK" :can-manage="canManage" @add="openAddSpaceDialog('TASK')">
        <SpaceItem v-for="(item, index) in taskSpaces" :key="item.id" :space-id="item.id" :space-name="item.name"
          :is-active="currentSpace?.id === item.id" :can-manage="canManage" :restricted="item.restricted"
          @click="spaceStore.changeSpace(index, 'TASK')"
          @save="handleUpdateSpace(item.id, $event.name, $event.restricted)" @delete="handleDelete(item.id)" />
      </SpaceGroup>

      <SpaceGroup label="KÊNH GHI CHÚ" :can-manage="canManage" @add="openAddSpaceDialog('NOTE')">
        <SpaceItem v-for="(item, index) in noteSpaces" :key="item.id" :space-id="item.id" :space-name="item.name"
          :is-active="currentSpace?.id === item.id" :can-manage="canManage" :restricted="item.restricted"
          @click="spaceStore.changeSpace(index, 'NOTE')"
          @save="handleUpdateSpace(item.id, $event.name, $event.restricted)" @delete="handleDelete(item.id)" />
      </SpaceGroup>

      <SpaceGroup label="KÊNH LỊCH TRÌNH" :can-manage="canManage" @add="openAddSpaceDialog('CALENDAR')">
        <SpaceItem v-for="(item, index) in calendarSpaces" :key="item.id" :space-id="item.id" :space-name="item.name"
          :is-active="currentSpace?.id === item.id" :can-manage="canManage" :restricted="item.restricted"
          @click="spaceStore.changeSpace(index, 'CALENDAR')"
          @save="handleUpdateSpace(item.id, $event.name, $event.restricted)" @delete="handleDelete(item.id)" />
      </SpaceGroup>

      <SpaceGroup label="KÊNH CHAT" :can-manage="canManage" @add="openAddSpaceDialog('CHAT')">
        <SpaceItem v-for="(item, index) in chatSpaces" :key="item.id" :space-id="item.id" :space-name="item.name"
          :is-active="currentSpace?.id === item.id" :can-manage="canManage" :restricted="item.restricted"
          @click="spaceStore.changeSpace(index, 'CHAT')"
          @save="handleUpdateSpace(item.id, $event.name, $event.restricted)" @delete="handleDelete(item.id)" />
      </SpaceGroup>

      <SpaceGroup label="KÊNH ĐÀM THOẠI" :can-manage="canManage" @add="openAddSpaceDialog('VOICE')">
        <VoiceSpaceItem v-for="item in voiceSpaces" :key="item.id" :space-id="item.id" :space-name="item.name"
          :is-active="currentSpace?.id === item.id" :can-manage="canManage" :restricted="item.restricted"
          :participants="voiceSpaceStore.getParticipantsForSpace(item.id) ?? []"
          @join="voiceSpaceStore.joinRoom(item.id, loading)"
          @save="handleUpdateSpace(item.id, $event.name, $event.restricted)" @delete="handleDelete(item.id)" />
      </SpaceGroup>
    </SidebarContent>
  </Sidebar>

  <CreateSpaceDialog v-model:open="showAddSpaceDialog" :type="selectedSpaceType"
    @created="({ name, type }) => handleCreateSpace(name, type)" />
  <RoomSettingDialog v-model:open="showRoomSettingDialog" />
  <InviteDialog v-model:open="showInviteDialog" />
</template>
