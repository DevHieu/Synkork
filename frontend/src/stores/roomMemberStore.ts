import { getRoomMembers } from "@/services/roomMemberService";
import type { Member } from "@/types/Member";
import { defineStore } from "pinia";
import { roomMemberSocket } from "@/services/websocket/roomMemberSocket";
import { userSocket } from "./../services/websocket/userSocket";

export const useRoomMemberStore = defineStore("roomMember", {
  state: () => ({
    members: [] as Member[],
    loading: false,
    currentAuthority: null as string | null,
    isMuted: false,
    isDeafen: false,
  }),

  actions: {
    async subscribeSocket(roomId: string, username: string) {
      roomMemberSocket.subscribeAuthorityChange(roomId, (member) => {
        const idx = this.members.findIndex(
          (m) => m.memberId === member.memberId,
        );
        if (idx !== -1) {
          this.members[idx] = member;
          this.setInfo(username); // Có đổi quyền thì cái quyền hiện tại sẽ thay đổi
        }
      });

      roomMemberSocket.subscribeMemberKicked(roomId, (memberId) => {
        const kicked = this.members.find((m) => m.memberId === memberId);

        this.members = this.members.filter((m) => m.memberId !== memberId);

        if (kicked?.username === username) {
          this.currentAuthority = null;
        }
      });

      roomMemberSocket.subscribeMemberJoined(roomId, (member) => {
        this.members.push(member);
      });

      userSocket.subscribeKicked();
      userSocket.subscribeRoomDeleted();
    },

    async fetchMembers(roomId: string, username: string) {
      this.loading = true;
      this.clearMembers();
      try {
        console.log("Running");

        this.members = await getRoomMembers(roomId);

        // Set thông tin sau khi fetch xong
        this.setInfo(username);

        //subscribe socket
        this.subscribeSocket(roomId, username);
      } finally {
        this.loading = false;
      }
    },

    async setInfo(username: string) {
      const current = this.members.find((m) => m.username === username);

      this.currentAuthority = current?.role ?? "MEMBER";
      this.isMuted = current?.muted ?? false;
      this.isDeafen = current?.deafen ?? false;
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
        if (!query.trim()) return state.members
        const q = query.toLowerCase()
        return state.members.filter(m =>
            m.displayName?.toLowerCase().includes(q) ||
            m.username?.toLowerCase().includes(q)
        )
    },
  },
});
