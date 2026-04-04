import { defineStore } from "pinia";
import { createSpace, getAllSpacesFromRoomId } from "@/services/spaceService";
import router from "@/routers";

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
      console.log("changeigin: " + type);

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
        this.currentSpace = this.chatSpaces[0] || null;
        router.push(
          `/rooms/chat/${router.currentRoute.value.params.roomId}/${
            this.chatSpaces[0]?.id || ""
          }`,
        );
      }
    },

    async createSpace(name: string, type: string, roomId: string) {
      const spaceData = {
        name,
        type,
      };
      const newSpace = await createSpace(roomId, spaceData);

      switch (type.toUpperCase()) {
        case "CHAT":
          this.chatSpaces.unshift(newSpace);
          break;
        case "VOICE":
          this.voiceSpaces.unshift(newSpace);
          break;
        case "NOTE":
          this.noteSpaces.unshift(newSpace);
          break;
        case "CALENDAR":
          this.calendarSpaces.unshift(newSpace);
          break;
        case "TASK":
          this.taskSpaces.unshift(newSpace);
          break;
      }

      await this.changeSpaceById(newSpace.id, type);

      return newSpace.id;
    },
  },
});
