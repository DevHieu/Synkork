<script setup lang="ts">
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
  MicOff,
  VolumeX,
} from "lucide-vue-next";

import { useRouter } from "vue-router";
import { useRoomsStore } from "@/stores/roomStore";
import { useSpaceStore } from "@/stores/spaceStore";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import { storeToRefs } from "pinia";
import CreateSpaceDialog from "../dialog/CreateSpaceDialog.vue";
import { ref } from "vue";
import RoomSettingDialog from "@/components/dialog/RoomSettingDialog/index.vue";
import InviteDialog from "@/components/dialog/InviteMemberDialog.vue";

const router = useRouter();
const roomStore = useRoomsStore();
const spaceStore = useSpaceStore();
const roomMemberStore = useRoomMemberStore();
const voiceSpaceStore = useVoiceSpaceStore();

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
const { canManage } = storeToRefs(roomMemberStore);

const changeSpace = async (
  index: number,
  type: "CHAT" | "VOICE" | "NOTE" | "CALENDAR" | "TASK",
) => {
  await spaceStore.changeSpace(index, type);
};

const joinVoiceSpace = async (spaceId: string) => {
  await voiceSpaceStore.joinRoom(spaceId);
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

const getInitials = (name: string) => {
  if (!name) return "?";
  return name
    .split(/[\s_-]/)
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
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
          <button
            @click.stop="openRoomSettingDialog()"
            class="transition duration-150 hover:text-foreground"
          >
            <Settings v-if="canManage" class="h-5 w-5" />
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
                v-if="canManage"
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
                v-if="canManage"
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
                v-if="canManage"
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
                v-if="canManage"
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
                v-if="canManage"
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
                <SidebarMenuItem v-for="item in voiceSpaces" :key="item.id">
                  <!-- Channel row -->
                  <SidebarMenuButton
                    @click="joinVoiceSpace(item.id)"
                    :isActive="currentSpace?.id === item.id"
                    class="group/voice"
                  >
                    <Volume2 class="mr-2 h-4 w-4 shrink-0" />
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>

                  <!-- Participants list (Discord style) -->
                  <div
                    v-if="
                      voiceSpaceStore.getParticipantsForSpace(item.id)?.length
                    "
                    class="ml-4 mt-0.5 mb-1 space-y-0.5"
                  >
                    <div
                      v-for="participant in voiceSpaceStore.getParticipantsForSpace(
                        item.id,
                      )"
                      :key="participant.userID"
                      class="flex items-center gap-2 px-2 py-0.5 rounded text-xs text-muted-foreground hover:text-foreground hover:bg-muted/50 transition-colors cursor-default"
                    >
                      <!-- Avatar -->
                      <div class="relative shrink-0">
                        <img
                          v-if="participant.avatarUrl"
                          :src="participant.avatarUrl"
                          class="w-5 h-5 rounded-full object-cover"
                        />
                        <div
                          v-else
                          class="w-5 h-5 rounded-full bg-primary/80 flex items-center justify-center text-[9px] font-bold text-primary-foreground"
                        >
                          {{ getInitials(participant.userName) }}
                        </div>
                      </div>

                      <!-- Tên -->
                      <span class="truncate flex-1">
                        {{ participant.userName }}
                        <span v-if="participant.isLocal" class="opacity-50"
                          >(Bạn)</span
                        >
                      </span>

                      <!-- Trạng thái mic + audio -->
                      <div class="flex items-center gap-1 shrink-0">
                        <MicOff
                          v-if="!participant.micOn"
                          class="h-3.5 w-3.5 text-red-500"
                        />
                        <VolumeX
                          v-if="!participant.audioOn"
                          class="h-3.5 w-3.5 text-red-500"
                        />
                      </div>
                    </div>
                  </div>
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
