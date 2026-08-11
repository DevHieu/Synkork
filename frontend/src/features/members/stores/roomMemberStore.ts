import type { Member } from "@/features/members/types/Member";
import { defineStore } from "pinia";

export const useRoomMemberStore = defineStore("roomMember", {
  state: () => ({
    members: [] as Member[],
    loading: false,
    currentAuthority: null as string | null,
    isMuted: false,
    isDeafen: false,
    chatDisabledTime: null as string | null,
  }),

  actions: {
    async setInfo(username: string) {
      const current = this.members.find((m) => m.username === username);

      this.currentAuthority = current?.role ?? "MEMBER";
      this.isMuted = current?.muted ?? false;
      this.isDeafen = current?.deafen ?? false;
      this.chatDisabledTime = current?.chatDisableUntil ?? null;
    },

    async clearMembers() {
      this.members = [];
      this.currentAuthority = null;
    },
  },

  getters: {
    canManage: (state) =>
      state.currentAuthority === "OWNER" || state.currentAuthority === "ADMIN",

    isOwner: (state) => state.currentAuthority === "OWNER",

    owners: (state) => state.members.filter((m) => m.role === "OWNER"),
    admins: (state) => state.members.filter((m) => m.role === "ADMIN"),
    regularMembers: (state) => state.members.filter((m) => m.role === "MEMBER"),

    sortedMembers: (state) => {
      const order = ["OWNER", "ADMIN", "MEMBER"];
      return [...state.members].sort(
        (a, b) => order.indexOf(a.role) - order.indexOf(b.role),
      );
    },

    searchMembers: (state) => (query: string) => {
      if (!query.trim()) return state.members;
      const q = query.toLowerCase();
      return state.members.filter(
        (m) =>
          m.displayName?.toLowerCase().includes(q) ||
          m.username?.toLowerCase().includes(q),
      );
    },
  },
});
