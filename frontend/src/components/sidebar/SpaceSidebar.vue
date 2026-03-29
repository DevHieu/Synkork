<script setup lang="ts">
// import { watch } from "vue";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
  ChevronRight,
  Hash,
  Volume2,
  Plus,
  UserRoundPlus,
  Settings,
} from "lucide-vue-next";

import { useRouter } from "vue-router";
import { useRoomsStore } from "@/stores/roomStore";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import CreateSpaceDialog from "../dialog/CreateSpaceDialog.vue";
import { ref } from "vue";
import RoomSettingDialog from "@/components/dialog/RoomSettingDialog/index.vue";
import InviteDialog from "@/components/dialog/InviteMemberDialog.vue";

const router = useRouter();
const roomStore = useRoomsStore();
const spaceStore = useSpaceStore();

const showRoomSettingDialog = ref(false);
const showInviteDialog = ref(false);

const showAddSpaceDialog = ref(false);
const selectedSpaceType = ref<string>("CHAT");

const { currentRoom } = storeToRefs(roomStore);
const {
  currentSpace,
  chatSpaces,
  voiceSpaces,
  calendarSpaces,
  noteSpaces,
  taskSpaces,
} = storeToRefs(spaceStore);

const changeSpace = async (
  index: number,
  type: "CHAT" | "VOICE" | "NOTE" | "CALENDAR" | "TASK",
) => {
  await spaceStore.changeSpace(index, type);

  router.push(
    `/rooms/${type.toLowerCase()}/${currentRoom.value?.id}/${
      currentSpace.value?.id
    }`,
  );
};

const openAddSpaceDialog = (type: string) => {
  selectedSpaceType.value = type;
  showAddSpaceDialog.value = true;
};

const openRoomSettingDialog = () => {
  showRoomSettingDialog.value = true;
};

const handleCreateSpace = async (name: string, type: string) => {
  const spaceId = await spaceStore.createSpace(
    name,
    type,
    currentRoom.value?.id || "",
  );

  router.push(
    `/rooms/${type.toLowerCase()}/${spaceId}/${currentSpace.value?.id}`,
  );
};
</script>

<template>
  <Sidebar collapsible="none" class="hidden flex-1 md:flex">
    <SidebarHeader class="gap-3.5 border-b p-4">
      <div class="flex w-full items-center justify-between">
        <div class="text-base font-medium text-foreground">
          {{ currentRoom?.name }}
        </div>
        <div class="flex gap-3.5">
          <button
            @click.stop="openRoomSettingDialog()"
            class="transition duration-150 hover:text-foreground"
          >
            <Settings class="h-5 w-5" />
          </button>
          <button
            @click="showInviteDialog = true"
            class="p-1.5 rounded-lg hover:bg-muted transition text-muted-foreground hover:text-foreground"
          >
            <UserRoundPlus class="h-5 w-5" />
          </button>
        </div>
      </div>
    </SidebarHeader>
    <SidebarContent class="mt-5">
      <!-- TASK -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger
              class="flex items-center justify-between w-full group/label text-muted-foreground hover:text-foreground transition-colors duration-200"
            >
              <div class="flex items-center">
                <ChevronRight
                  class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
                />
                KÊNH TASK
              </div>
              <button
                @click.stop="openAddSpaceDialog('TASK')"
                class="opacity-0 group-hover/label:opacity-100 transition-opacity duration-200 hover:bg-muted rounded p-0.5"
              >
                <Plus class="h-3.5 w-3.5" />
              </button>
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in taskSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'TASK')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- NOTE -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger
              class="flex items-center justify-between w-full group/label text-muted-foreground hover:text-foreground transition-colors duration-200"
            >
              <div class="flex items-center">
                <ChevronRight
                  class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
                />
                KÊNH GHI CHÚ
              </div>
              <button
                @click.stop="openAddSpaceDialog('NOTE')"
                class="opacity-0 group-hover/label:opacity-100 transition-opacity duration-200 hover:bg-muted rounded p-0.5"
              >
                <Plus class="h-3.5 w-3.5" />
              </button>
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in noteSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'NOTE')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- CALENDAR -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger
              class="flex items-center justify-between w-full group/label text-muted-foreground hover:text-foreground transition-colors duration-200"
            >
              <div class="flex items-center">
                <ChevronRight
                  class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
                />
                KÊNH LỊCH TRÌNH
              </div>
              <button
                @click.stop="openAddSpaceDialog('CALENDAR')"
                class="opacity-0 group-hover/label:opacity-100 transition-opacity duration-200 hover:bg-muted rounded p-0.5"
              >
                <Plus class="h-3.5 w-3.5" />
              </button>
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in calendarSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'CALENDAR')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- CHAT -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger
              class="flex items-center justify-between w-full group/label text-muted-foreground hover:text-foreground transition-colors duration-200"
            >
              <div class="flex items-center">
                <ChevronRight
                  class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
                />
                KÊNH CHAT
              </div>
              <button
                @click.stop="openAddSpaceDialog('CHAT')"
                class="opacity-0 group-hover/label:opacity-100 transition-opacity duration-200 hover:bg-muted rounded p-0.5"
              >
                <Plus class="h-3.5 w-3.5" />
              </button>
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in chatSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'CHAT')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Hash class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>

      <!-- VOICE -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger
              class="flex items-center justify-between w-full group/label text-muted-foreground hover:text-foreground transition-colors duration-200"
            >
              <div class="flex items-center">
                <ChevronRight
                  class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
                />
                KÊNH ĐÀM THOẠI
              </div>
              <button
                @click.stop="openAddSpaceDialog('VOICE')"
                class="opacity-0 group-hover/label:opacity-100 transition-opacity duration-200 hover:bg-muted rounded p-0.5"
              >
                <Plus class="h-3.5 w-3.5" />
              </button>
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in voiceSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton
                    @click="changeSpace(index, 'VOICE')"
                    :isActive="currentSpace?.id === item.id"
                  >
                    <Volume2 class="mr-2 h-4 w-4" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </SidebarMenu>
            </SidebarGroupContent>
          </CollapsibleContent>
        </SidebarGroup>
      </Collapsible>
    </SidebarContent>
  </Sidebar>

  <CreateSpaceDialog
    v-model:open="showAddSpaceDialog"
    :type="selectedSpaceType"
    @created="({ name, type }) => handleCreateSpace(name, type)"
  />

  <RoomSettingDialog v-model:open="showRoomSettingDialog" />
  <InviteDialog v-model:open="showInviteDialog" />
</template>

<style scoped></style>
