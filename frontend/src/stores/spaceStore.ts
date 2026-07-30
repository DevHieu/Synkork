import { defineStore } from "pinia";
import { getAllSpacesFromRoomId, getSpaceById } from "@/services/spaceService";
import { spaceSocket } from "@/services/websocket/spaceSocket";
import router from "@/routers";
import { socketService } from "@/services/websocket/socketService";

import { useUserStore } from "./userStore";
import { useRoomMemberStore } from "./roomMemberStore";
import { storeToRefs } from "pinia";
import type { Space, SpaceType } from "@/types/Space";
import { toast } from "vue-sonner";
import { watch } from "vue";

export const useSpaceStore = defineStore("spaces", {
  state: () => ({
    currentSpace: null as Space | null,
    chatSpaces: [] as Space[],
    voiceSpaces: [] as Space[],
    noteSpaces: [] as Space[],
    calendarSpaces: [] as Space[],
    taskSpaces: [] as Space[],

    currentVoiceSpace: null as Space | null,
    loading: true,
    _joiningDMSpaceId: null as string | null,
  }),

  actions: {
    async fetchSpacesByRoomId(roomId: string) {
      this.loading = true;
      try {
        const res = await getAllSpacesFromRoomId(roomId);

        await this.filterSpacesByType(res.data);

        this.connectSpaceSocket(roomId);
      } finally {
        this.loading = false;
      }
    },

    connectSpaceSocket(roomId: string) {
      socketService.connect();

      spaceSocket.subscribeSpaceCreated(roomId, (space) => {
        this.addSpaceToArray(space);
      });

      spaceSocket.subscribeSpaceUpdated(roomId, (updatedSpace) => {
        this.updateSpaceToArray(updatedSpace);
      });

      spaceSocket.subscribeSpaceDeleted(roomId, (spaceId) => {
        this.removeSpaceFromArray(spaceId);
      });
    },

    async filterSpacesByType(rooms: any[]) {
      const result = {
        CHAT: [],
        VOICE: [],
        NOTE: [],
        CALENDAR: [],
        TASK: [],
      } as Record<string, any[]>;

      rooms.forEach((space) => {
        result[space.type]?.push(space);
      });

      this.chatSpaces = result.CHAT || [];
      this.voiceSpaces = result.VOICE || [];
      this.noteSpaces = result.NOTE || [];
      this.calendarSpaces = result.CALENDAR || [];
      this.taskSpaces = result.TASK || [];
    },

    async changeSpace(index: number, type: string) {
      let space: Space | null = null;

      switch (type) {
        case "CHAT":
          space = this.chatSpaces[index] ?? null;
          break;
        case "VOICE":
          space = this.voiceSpaces[index] ?? null;
          break;
        case "NOTE":
          space = this.noteSpaces[index] ?? null;
          break;
        case "CALENDAR":
          space = this.calendarSpaces[index] ?? null;
          break;
        case "TASK":
          space = this.taskSpaces[index] ?? null;
          break;
      }

      if (!checkPermission(space)) {
        toast.error("Bạn không có quyền truy cập vào space này.");
        return;
      }

      this.currentSpace = space;

      router.push(
        `/rooms/${type.toLowerCase()}/${router.currentRoute.value.params.roomId}/${space?.id}`,
      );
    },

    // Hàm này dùng để đổi space khi đã có spaceId (ví dụ khi đổi room mà URL đã có spaceId)
    async changeSpaceById(spaceId: string, spaceType: string) {
      // Đợi cái quyền hiện tại của user load xong thì mới chạy tiếp. Vì bên dưới có check quyền vào phòng
      const memberStore = useRoomMemberStore();
      if (memberStore.loading || !memberStore.currentAuthority) {
        await new Promise<void>((resolve) => {
          const unwatch = watch(
            () => memberStore.currentAuthority,
            (val) => {
              if (val) {
                unwatch();
                resolve();
              }
            },
          );
        });
      }

      let space: Space | null = null;

      switch (spaceType.toUpperCase()) {
        case "CHAT":
          space = this.chatSpaces.find((s) => s.id === spaceId) ?? null;
          break;
        case "VOICE":
          space = this.voiceSpaces.find((s) => s.id === spaceId) ?? null;
          break;
        case "NOTE":
          space = this.noteSpaces.find((s) => s.id === spaceId) ?? null;
          break;
        case "CALENDAR":
          space = this.calendarSpaces.find((s) => s.id === spaceId) ?? null;
          break;
        case "TASK":
          space = this.taskSpaces.find((s) => s.id === spaceId) ?? null;
          break;
        default:
          space = null;
      }

      if (!checkPermission(space)) {
        toast.error("Bạn không có quyền truy cập vào space này.");

        if (!this.currentSpace || this.currentSpace.id === spaceId) {
          this.currentSpace = this.chatSpaces[0] ?? null;
          router.push(
            `/rooms/chat/${router.currentRoute.value.params.roomId}/${
              this.chatSpaces[0]?.id || ""
            }`,
          );
        }

        return;
      }

      if (space === null) {
        this.currentSpace = this.chatSpaces[0] ?? null;
        router.push(
          `/rooms/chat/${router.currentRoute.value.params.roomId}/${
            this.chatSpaces[0]?.id || ""
          }`,
        );
        return;
      }

      this.currentSpace = space;
      router.push(
        `/rooms/${spaceType.toLowerCase()}/${router.currentRoute.value.params.roomId}/${spaceId}`,
      );
    },

    async joinDMSpace(spaceId: string, path: string = "/me") {
      if (this._joiningDMSpaceId === spaceId) return;
      try {
        this._joiningDMSpaceId = spaceId;
        this.loading = true;
        useRoomMemberStore().clearMembers();

        const space = await getSpaceById(spaceId);

        // Không hiểu tại sao hoạt động. Thứ tự 3 dòng này để im như này
        this.currentSpace = null;
        await router.push(`${path}/${spaceId}`);
        this.currentSpace = space;
      } catch (error) {
        console.log(error);

        toast.error("Không thể tham gia phòng chat này.");
      } finally {
        this.loading = false;
        this._joiningDMSpaceId = null;
      }
    },

    async addSpaceToArray(space: any) {
      switch (space.type.toUpperCase()) {
        case "CHAT":
          this.chatSpaces.push(space);
          break;
        case "VOICE":
          this.voiceSpaces.push(space);
          break;
        case "NOTE":
          this.noteSpaces.push(space);
          break;
        case "CALENDAR":
          this.calendarSpaces.push(space);
          break;
        case "TASK":
          this.taskSpaces.push(space);
          break;
      }
    },

    async removeSpaceFromArray(spaceId: string) {
      this.chatSpaces = this.chatSpaces.filter((s) => s.id !== spaceId);
      this.voiceSpaces = this.voiceSpaces.filter((s) => s.id !== spaceId);
      this.noteSpaces = this.noteSpaces.filter((s) => s.id !== spaceId);
      this.calendarSpaces = this.calendarSpaces.filter((s) => s.id !== spaceId);
      this.taskSpaces = this.taskSpaces.filter((s) => s.id !== spaceId);

      const { useVoiceSpaceStore } =
        await import("@/features/voice-chat/stores/voiceSpaceStore");
      const { currentSpaceId } = storeToRefs(useVoiceSpaceStore());

      if (currentSpaceId.value === spaceId) {
        await useVoiceSpaceStore().leaveRoom();
      }

      if (this.currentSpace?.id === spaceId) {
        this.changeSpaceById(spaceId, "CHAT");

        router.push(
          `/rooms/chat/${router.currentRoute.value.params.roomId}/${
            this.chatSpaces[0]?.id || ""
          }`,
        );
      }
    },

    async updateSpaceToArray(space: any) {
      switch (space.type.toUpperCase()) {
        case "CHAT":
          const chatIndex = this.chatSpaces.findIndex((s) => s.id === space.id);
          if (chatIndex !== -1) {
            this.chatSpaces[chatIndex] = space;
          }
          break;
        case "VOICE":
          const voiceIndex = this.voiceSpaces.findIndex(
            (s) => s.id === space.id,
          );
          if (voiceIndex !== -1) {
            this.voiceSpaces[voiceIndex] = space;
          }
          break;
        case "NOTE":
          const noteIndex = this.noteSpaces.findIndex((s) => s.id === space.id);
          if (noteIndex !== -1) {
            this.noteSpaces[noteIndex] = space;
          }
          break;
        case "CALENDAR":
          const calendarIndex = this.calendarSpaces.findIndex(
            (s) => s.id === space.id,
          );
          if (calendarIndex !== -1) {
            this.calendarSpaces[calendarIndex] = space;
          }
          break;
        case "TASK":
          const taskIndex = this.taskSpaces.findIndex((s) => s.id === space.id);
          if (taskIndex !== -1) {
            this.taskSpaces[taskIndex] = space;
          }
          break;
      }
    },
  },

  getters: {
    isExisted: (state) => (spaceId: string) => {
      return (
        state.chatSpaces.some((s) => s.id === spaceId) ||
        state.voiceSpaces.some((s) => s.id === spaceId) ||
        state.noteSpaces.some((s) => s.id === spaceId) ||
        state.calendarSpaces.some((s) => s.id === spaceId) ||
        state.taskSpaces.some((s) => s.id === spaceId)
      );
    },

    getSpaceTypeSize: (state) => (type: SpaceType) => {
      switch (type) {
        case "CHAT":
          return state.chatSpaces.length;
        case "VOICE":
          return state.voiceSpaces.length;
        case "NOTE":
          return state.noteSpaces.length;
        case "CALENDAR":
          return state.calendarSpaces.length;
        case "TASK":
          return state.taskSpaces.length;
        default:
          return 0;
      }
    },

    isPersonalSpace: (state) => {
      return state.currentSpace?.roomType === "PERSONAL";
    },
  },
});

function checkPermission(space: Space | null) {
  const { user } = storeToRefs(useUserStore());
  const { currentAuthority } = storeToRefs(useRoomMemberStore());

  if (!user.value || !space) return false;

  if (currentAuthority.value === "MEMBER" && space.restricted === true)
    return false;

  return true;
}
