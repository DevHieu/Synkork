import { defineStore } from "pinia";
import { getAllSpacesFromRoomId } from "@/services/spaceService";
import { spaceSocket } from "@/services/websocket/spaceSocket";
import router from "@/routers";
import { socketService } from "@/services/websocket/socketService";

export const useSpaceStore = defineStore("spaces", {
  state: () => ({
    currentSpace: null as any | null,
    chatSpaces: [] as any[],
    voiceSpaces: [] as any[],
    noteSpaces: [] as any[],
    calendarSpaces: [] as any[],
    taskSpaces: [] as any[],

    currentVoiceSpace: null as any | null,
    loading: false,
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
      switch (type) {
        case "CHAT":
          this.currentSpace = this.chatSpaces[index];
          break;
        case "VOICE":
          this.currentSpace = this.voiceSpaces[index];
          break;
        case "NOTE":
          this.currentSpace = this.noteSpaces[index];
          break;
        case "CALENDAR":
          this.currentSpace = this.calendarSpaces[index];
          break;
        case "TASK":
          this.currentSpace = this.taskSpaces[index];
          break;
        default:
          this.currentSpace = null;
      }

      router.push(
        `/rooms/${type.toLowerCase()}/${router.currentRoute.value.params.roomId}/${
          this.currentSpace?.id
        }`,
      );
    },

    // Hàm này dùng để đổi space khi đã có spaceId (ví dụ khi đổi room mà URL đã có spaceId)
    async changeSpaceById(spaceId: string, spaceType: string) {
      switch (spaceType.toUpperCase()) {
        case "CHAT":
          this.currentSpace =
            this.chatSpaces.find((space) => space.id === spaceId) || null;
          break;
        case "VOICE":
          this.currentSpace =
            this.voiceSpaces.find((space) => space.id === spaceId) || null;
          break;
        case "NOTE":
          this.currentSpace =
            this.noteSpaces.find((space) => space.id === spaceId) || null;
          break;
        case "CALENDAR":
          this.currentSpace =
            this.calendarSpaces.find((space) => space.id === spaceId) || null;
          break;
        case "TASK":
          this.currentSpace =
            this.taskSpaces.find((space) => space.id === spaceId) || null;
          break;
        default:
          this.currentSpace = null;
      }

      if (this.currentSpace === null) {
        this.currentSpace = this.chatSpaces[0];
      }

      router.push(
        `/rooms/chat/${router.currentRoute.value.params.roomId}/${
          this.chatSpaces[0]?.id || ""
        }`,
      );
    },

    addSpaceToArray(space: any) {
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

    removeSpaceFromArray(spaceId: string) {
      this.chatSpaces = this.chatSpaces.filter((s) => s.id !== spaceId);
      this.voiceSpaces = this.voiceSpaces.filter((s) => s.id !== spaceId);
      this.noteSpaces = this.noteSpaces.filter((s) => s.id !== spaceId);
      this.calendarSpaces = this.calendarSpaces.filter((s) => s.id !== spaceId);
      this.taskSpaces = this.taskSpaces.filter((s) => s.id !== spaceId);

      if (this.currentSpace?.id === spaceId) {
        this.currentSpace = null;
        router.push(
          `/rooms/chat/${router.currentRoute.value.params.roomId}/${
            this.chatSpaces[0]?.id || ""
          }`,
        );
      }
    },

    updateSpaceToArray(space: any) {
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
});
