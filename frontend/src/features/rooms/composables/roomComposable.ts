import { storeToRefs } from "pinia";
import { useRoomsStore } from "../stores/roomStore";
import type { Room } from "../types/Room";
import { useRoomService } from "../services/roomService";
import { socketService } from "@/services/socketService";
import { useUserStore } from "@/features/users/stores/userStore";
import { useSpaceStore } from "@/features/spaces/stores/spaceStore";
import { useRoomSocketComposable } from "./roomSocketSomposable";
import { useMemberComposable } from "@/features/members/composables/memberComposable";
import router from "@/routers";
import { useSpaceComposable } from "@/features/spaces/composables/spaceComposable";

export function useRoomComposable() {
  const roomStore = useRoomsStore();
  const { loading, rooms, currentRoom } = storeToRefs(roomStore);

  const roomService = useRoomService();
  const spaceComposable = useSpaceComposable();
  const roomSocket = useRoomSocketComposable();
  const memberComposable = useMemberComposable();

  const fetchRooms = async () => {
    loading.value = true;
    try {
      rooms.value = await roomService.getUserRooms();
      console.trace(rooms.value);
    } finally {
      loading.value = false;
    }
  };

  // Nhận spaceId để check xem khi đổi room có cần redirect đến space nào không
  const changeRoom = async (
    room: Room,
    spaceId?: string,
    spaceType?: string,
  ) => {
    currentRoom.value = room;
    socketService.unsubscribeAll(); // Hủy tất cả subscription cũ khi đổi room để tránh nhận dữ liệu của phòng trước đó vào

    // Cần nối lại socket của room mới trước khi điều hướng sang space bên trong.
    roomSocket.subscribeSocket(room.id);

    const spaceStore = useSpaceStore();
    await spaceComposable.fetchSpacesByRoomId(room.id);

    const { user } = storeToRefs(useUserStore());

    memberComposable.fetchMembers(room.id, user.value?.username as string);

    // Nếu không có spaceId, mặc định redirect đến CHAT space đầu tiên của room mới
    if (spaceId === undefined) {
      await spaceComposable.changeSpace(0, "CHAT");
      router.push(`/rooms/chat/${room.id}/${spaceStore.currentSpace?.id}`);
    } else {
      // Cho phép caller chỉ định rõ loại space để điều hướng sang đúng màn hình.
      const targetSpaceType =
        spaceType ?? (router.currentRoute.value.meta.spaceType as string);

      await spaceComposable.changeSpaceById(spaceId, targetSpaceType);
    }
  };

  const createRoom = async (roomData: { name: string; imageFile?: File }) => {
    const userStore = useUserStore();
    const { user } = storeToRefs(userStore);

    if (!user.value) return;

    const newRoom = await roomService.createRoom(roomData);

    rooms.value.unshift(newRoom);
    changeRoom(newRoom, undefined);
  };

  const joinRoom = async (inviteCode: string) => {
    const roomInvited = await roomService.joinRoom(inviteCode);

    await fetchRooms();
    await changeRoom(roomInvited);
  };

  return {
    fetchRooms,
    changeRoom,
    createRoom,
    joinRoom,
  };
}
