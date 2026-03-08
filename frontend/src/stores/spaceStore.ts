import { defineStore } from "pinia";
import { getAllSpacesFromRoomId } from "@/services/spaceService";

export const useSpaceStore = defineStore("spaces", {
  state: () => ({
    currentSpace: null as any | null,
    chatSpaces: [] as any[],
    voiceSpaces: [] as any[],
    noteSpaces: [] as any[],
    calendarSpaces: [] as any[],
    taskSpaces: [] as any[],
    loading: false,
  }),

  actions: {
    async fetchSpacesByRoomId(roomId: string) {
      this.loading = true;
      try {
        const res = await getAllSpacesFromRoomId(roomId);
        await this.filterSpacesByType(res.data);
      } finally {
        this.loading = false;
      }
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
      console.log("spaceStore changinf");

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

      console.log("Current Space:", this.currentSpace);
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
    },
  },
});
