<script setup lang="ts">
import { watch, ref } from "vue";
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
import { ChevronRight, Hash, Volume2 } from "lucide-vue-next";

import { useRouter } from "vue-router";
import { useRoomsStore } from "@/stores/roomStore";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import { createSpace, deleteSpace, renameSpace } from "@/services/spaceService";

const router = useRouter();
const roomStore = useRoomsStore();
const spaceStore = useSpaceStore();

const { currentRoom } = storeToRefs(roomStore);
const {
  currentSpace,
  chatSpaces,
  voiceSpaces,
  calendarSpaces,
  noteSpaces,
  taskSpaces,
} = storeToRefs(spaceStore);

// Watch trực tiếp vào hàm getter của props
watch(
  currentRoom,
  async (newRoom) => {
    if (!newRoom) return;

    console.log("Room ID changed:", newRoom.id);
    await spaceStore.fetchSpacesByRoomId(newRoom.id);
    changeSpace(0, "CHAT");
  },
  { immediate: true }
);

const changeSpace = async (
  index: number,
  type: "CHAT" | "VOICE" | "NOTE" | "CALENDAR" | "TASK"
) => {
  await spaceStore.changeSpace(index, type); // Assuming chatSpaces is used for CHAT type\

  router.push(
    `/rooms/${type.toLowerCase()}/${currentRoom.value?.id}/${
      currentSpace.value?.id
    }`
  );
};

// State cho việc tạo lịch
const showCreateDialog = ref(false);
const newCalendarName = ref("");
const isCreating = ref(false);

const openCreateDialog = (e: Event) => {
  e.stopPropagation(); // Prevent opening/closing the collapsible
  newCalendarName.value = "Lịch làm việc chung";
  showCreateDialog.value = true;
};

const handleCreateCalendar = async () => {
  if (!newCalendarName.value.trim() || !currentRoom.value) return;

  isCreating.value = true;
  try {
    await createSpace((currentRoom.value as any).id, {
      name: newCalendarName.value,
      type: "CALENDAR",
    });
    showCreateDialog.value = false;
    newCalendarName.value = "";
    // Refresh spaces
    await spaceStore.fetchSpacesByRoomId((currentRoom.value as any).id);
  } catch (err) {
    console.error("Error creating calendar space:", err);
    alert("Có lỗi xảy ra khi tạo kênh lịch trình!");
  } finally {
    isCreating.value = false;
  }
};

// State cho việc xoá lịch
const showDeleteDialog = ref(false);
const deletingSpaceId = ref<string | null>(null);
const isDeleting = ref(false);

const openDeleteDialog = (e: Event, spaceId: string) => {
  e.stopPropagation();
  deletingSpaceId.value = spaceId;
  showDeleteDialog.value = true;
};

const handleDeleteCalendar = async () => {
  if (!deletingSpaceId.value || !currentRoom.value) return;

  isDeleting.value = true;
  try {
    const wasSelected = currentSpace.value?.id === deletingSpaceId.value;
    await deleteSpace((currentRoom.value as any).id, deletingSpaceId.value);
    
    showDeleteDialog.value = false;
    deletingSpaceId.value = null;
    
    // Refresh spaces
    await spaceStore.fetchSpacesByRoomId((currentRoom.value as any).id);
    
    // Nếu đang select kênh vừa xoá → chuyển sang kênh chat đầu tiên
    if (wasSelected) {
      if (chatSpaces.value.length > 0) {
        await spaceStore.changeSpace(0, "CHAT");
        router.push(
          `/rooms/chat/${(currentRoom.value as any).id}/${(chatSpaces.value[0] as any).id}`
        );
      }
    }
  } catch (err) {
    console.error("Error deleting space:", err);
    alert("Có lỗi xảy ra khi xoá kênh lịch trình!");
  } finally {
    isDeleting.value = false;
  }
};

// State cho việc đổi tên lịch
const showRenameDialog = ref(false);
const renamingSpaceId = ref<string | null>(null);
const renameInput = ref("");
const isRenaming = ref(false);

const openRenameDialog = (e: Event, spaceId: string, currentName: string) => {
  e.stopPropagation();
  renamingSpaceId.value = spaceId;
  renameInput.value = currentName;
  showRenameDialog.value = true;
};

const handleRenameCalendar = async () => {
  if (!renameInput.value.trim() || !renamingSpaceId.value || !currentRoom.value) return;

  isRenaming.value = true;
  try {
    await renameSpace(
      (currentRoom.value as any).id,
      renamingSpaceId.value,
      renameInput.value.trim()
    );
    showRenameDialog.value = false;
    renamingSpaceId.value = null;
    renameInput.value = "";
    // Refresh spaces
    await spaceStore.fetchSpacesByRoomId((currentRoom.value as any).id);
  } catch (err) {
    console.error("Error renaming space:", err);
    alert("Có lỗi xảy ra khi đổi tên kênh lịch trình!");
  } finally {
    isRenaming.value = false;
  }
};
</script>

<template>
  <Sidebar collapsible="none" class="hidden flex-1 md:flex">
    <SidebarHeader class="gap-3.5 border-b p-4">
      <div class="flex w-full items-center justify-between">
        <div class="text-base font-medium text-foreground">
          {{ currentRoom?.name }}
        </div>
      </div>
    </SidebarHeader>
    <SidebarContent class="mt-5">
      <!-- TASK -->
      <Collapsible as-child default-open class="group/collapsible">
        <SidebarGroup>
          <SidebarGroupLabel as-child>
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH TASK
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in taskSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton @click="changeSpace(index, 'TASK')">
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
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH GHI CHÚ
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in noteSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton @click="changeSpace(index, 'NOTE')">
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
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              <span class="flex-1 text-left">KÊNH LỊCH TRÌNH</span>
              <button 
                @click="openCreateDialog" 
                class="ml-auto p-1 w-6 h-6 flex items-center justify-center rounded hover:bg-white/10 opacity-60 hover:opacity-100 transition-opacity"
                title="Tạo lịch mới"
              >
                <font-awesome-icon icon="plus" class="text-xs" />
              </button>
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in calendarSpaces"
                  :key="(item as any).id"
                  class="group/item relative"
                >
                  <SidebarMenuButton @click="changeSpace(index, 'CALENDAR')" class="w-full justify-between pr-16">
                    <div class="flex items-center truncate">
                      <font-awesome-icon icon="calendar-alt" class="mr-2 text-gray-400 group-hover:text-white shrink-0" />
                      <span class="truncate">{{ (item as any).name }}</span>
                    </div>
                  </SidebarMenuButton>
                  <div class="absolute right-2 top-1/2 -translate-y-1/2 flex gap-1 opacity-0 group-hover/item:opacity-100 transition-all z-10">
                    <button
                      @click="(e) => openRenameDialog(e, (item as any).id, (item as any).name)"
                      class="w-6 h-6 flex items-center justify-center rounded bg-zinc-800/80 text-gray-300 hover:bg-teal-600 hover:text-white transition-all shadow-sm"
                      title="Đổi tên"
                    >
                      <font-awesome-icon icon="edit" class="text-xs" />
                    </button>
                    <button
                      @click="(e) => openDeleteDialog(e, (item as any).id)"
                      class="w-6 h-6 flex items-center justify-center rounded bg-zinc-800/80 text-[#ef4444] hover:bg-red-500 hover:text-white transition-all shadow-sm"
                      title="Xoá lịch"
                    >
                      <font-awesome-icon icon="trash" class="text-xs" />
                    </button>
                  </div>
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
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH CHAT
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in chatSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton @click="changeSpace(index, 'CHAT')">
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
            <CollapsibleTrigger>
              <ChevronRight
                class="mr-2 h-4 w-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
              />
              KÊNH ĐÀM THOẠI
            </CollapsibleTrigger>
          </SidebarGroupLabel>
          <CollapsibleContent>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem
                  v-for="(item, index) in voiceSpaces"
                  :key="item.id"
                >
                  <SidebarMenuButton @click="changeSpace(index, 'VOICE')">
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

    <!-- Modal Tạo Lịch Trình -->
    <Teleport to="body">
      <div v-if="showCreateDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showCreateDialog = false"></div>
        
        <!-- Content -->
        <div class="relative bg-zinc-900 rounded-xl border border-white/10 w-full max-w-sm mx-4 p-5 shadow-2xl">
          <h3 class="text-lg font-semibold text-white mb-4">Tạo Kênh Lịch Trình</h3>
          
          <form @submit.prevent="handleCreateCalendar">
            <div class="mb-4">
              <label class="block text-sm text-gray-400 mb-1.5">Tên lịch trình</label>
              <input 
                v-model="newCalendarName"
                type="text" 
                required
                autofocus
                placeholder="VD: Lịch làm việc nhóm..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50"
              />
            </div>
            
            <div class="flex justify-end gap-2 mt-6">
              <button 
                type="button" 
                @click="showCreateDialog = false"
                class="px-4 py-2 rounded-lg text-sm text-gray-300 hover:bg-white/10 transition-colors"
                :disabled="isCreating"
              >
                Hủy
              </button>
              <button 
                type="submit"
                class="px-4 py-2 rounded-lg text-sm font-medium bg-teal-600 text-white hover:bg-teal-700 transition-colors disabled:opacity-50 flex items-center gap-2"
                :disabled="isCreating || !newCalendarName.trim()"
              >
                <span v-if="isCreating" class="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin"></span>
                Tạo kênh
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- Modal Xóa Lịch Trình -->
    <Teleport to="body">
      <div v-if="showDeleteDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showDeleteDialog = false"></div>
        
        <!-- Content -->
        <div class="relative bg-zinc-900 rounded-xl border border-red-500/20 w-full max-w-sm mx-4 p-5 shadow-2xl">
          <h3 class="text-lg font-semibold text-white mb-2 flex items-center gap-2">
            <span class="text-red-500"><font-awesome-icon icon="trash" /></span> 
            Xóa Lịch Trình
          </h3>
          
          <p class="text-sm text-gray-400 mb-6">
            Bạn có chắc chắn muốn xóa kênh lịch trình này? Toàn bộ sự kiện trong lịch này cũng sẽ bị xóa vĩnh viễn và không thể khôi phục.
          </p>
          
          <div class="flex justify-end gap-2">
            <button 
              type="button" 
              @click="showDeleteDialog = false"
              class="px-4 py-2 rounded-lg text-sm text-gray-300 hover:bg-white/10 transition-colors"
              :disabled="isDeleting"
            >
              Hủy
            </button>
            <button 
              @click="handleDeleteCalendar"
              class="px-4 py-2 rounded-lg text-sm font-medium bg-red-600 text-white hover:bg-red-700 transition-colors disabled:opacity-50 flex items-center gap-2"
              :disabled="isDeleting"
            >
              <span v-if="isDeleting" class="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin"></span>
              Xoá vĩnh viễn
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal Đổi Tên Lịch Trình -->
    <Teleport to="body">
      <div v-if="showRenameDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showRenameDialog = false"></div>
        
        <!-- Content -->
        <div class="relative bg-zinc-900 rounded-xl border border-white/10 w-full max-w-sm mx-4 p-5 shadow-2xl">
          <h3 class="text-lg font-semibold text-white mb-4 flex items-center gap-2">
            <span class="text-teal-400"><font-awesome-icon icon="edit" /></span>
            Đổi Tên Kênh Lịch
          </h3>
          
          <form @submit.prevent="handleRenameCalendar">
            <div class="mb-4">
              <label class="block text-sm text-gray-400 mb-1.5">Tên mới</label>
              <input 
                v-model="renameInput"
                type="text" 
                required
                autofocus
                placeholder="Nhập tên mới..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50"
              />
            </div>
            
            <div class="flex justify-end gap-2 mt-6">
              <button 
                type="button" 
                @click="showRenameDialog = false"
                class="px-4 py-2 rounded-lg text-sm text-gray-300 hover:bg-white/10 transition-colors"
                :disabled="isRenaming"
              >
                Hủy
              </button>
              <button 
                type="submit"
                class="px-4 py-2 rounded-lg text-sm font-medium bg-teal-600 text-white hover:bg-teal-700 transition-colors disabled:opacity-50 flex items-center gap-2"
                :disabled="isRenaming || !renameInput.trim()"
              >
                <span v-if="isRenaming" class="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin"></span>
                Lưu
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </Sidebar>
</template>

<style scoped></style>
