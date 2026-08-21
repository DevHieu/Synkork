import { defineStore } from "pinia";
import type { Space, SpaceType } from "@/features/spaces/types/Space";

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
  }),

  actions: {
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
