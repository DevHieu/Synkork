import { useSpaceStore } from "../stores/spaceStore";
import { watch } from "vue";
import { useSpaceService } from "../services/spaceService";
import type { Space } from "../types/Space";
import { useRoomMemberStore } from "@/features/members/stores/roomMemberStore";
import router from "@/routers";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/features/users/stores/userStore";
import { toast } from "vue-sonner";
import { groupSpacesByType } from "../utils/spaceUtils";
import { useSpaceSocketComposable } from "./spaceSocketComposable";

let _joiningDMSpaceId = null as string | null;

export function useSpaceComposable() {
  const spaceService = useSpaceService();
  const spaceSocket = useSpaceSocketComposable();
  const spaceStore = useSpaceStore();
  const {
    loading,
    currentSpace,
    chatSpaces,
    voiceSpaces,
    noteSpaces,
    calendarSpaces,
    taskSpaces,
  } = storeToRefs(spaceStore);

  const fetchSpacesByRoomId = async (roomId: string) => {
    loading.value = true;
    try {
      const res = await spaceService.getAllSpacesFromRoomId(roomId);

      const result = await groupSpacesByType(res.data);

      chatSpaces.value = result.CHAT || [];
      voiceSpaces.value = result.VOICE || [];
      noteSpaces.value = result.NOTE || [];
      calendarSpaces.value = result.CALENDAR || [];
      taskSpaces.value = result.TASK || [];

      spaceSocket.subscribeSocket(roomId);
    } finally {
      loading.value = false;
    }
  };

  const changeSpace = async (index: number, type: string) => {
    let space: Space | null = null;

    switch (type) {
      case "CHAT":
        space = chatSpaces.value[index] ?? null;
        break;
      case "VOICE":
        space = voiceSpaces.value[index] ?? null;
        break;
      case "NOTE":
        space = noteSpaces.value[index] ?? null;
        break;
      case "CALENDAR":
        space = calendarSpaces.value[index] ?? null;
        break;
      case "TASK":
        space = taskSpaces.value[index] ?? null;
        break;
    }

    if (!checkPermission(space)) {
      toast.error("Bạn không có quyền truy cập vào space này.");
      return;
    }

    currentSpace.value = space;

    router.push(
      `/rooms/${type.toLowerCase()}/${router.currentRoute.value.params.roomId}/${space?.id}`,
    );
  };

  // Hàm này dùng để đổi space khi đã có spaceId (ví dụ khi đổi room mà URL đã có spaceId)
  const changeSpaceById = async (spaceId: string, spaceType: string) => {
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
        space = chatSpaces.value.find((s) => s.id === spaceId) ?? null;
        break;
      case "VOICE":
        space = voiceSpaces.value.find((s) => s.id === spaceId) ?? null;
        break;
      case "NOTE":
        space = noteSpaces.value.find((s) => s.id === spaceId) ?? null;
        break;
      case "CALENDAR":
        space = calendarSpaces.value.find((s) => s.id === spaceId) ?? null;
        break;
      case "TASK":
        space = taskSpaces.value.find((s) => s.id === spaceId) ?? null;
        break;
      default:
        space = null;
    }

    if (!checkPermission(space)) {
      toast.error("Bạn không có quyền truy cập vào space này.");

      if (!currentSpace.value || currentSpace.value.id === spaceId) {
        currentSpace.value = chatSpaces.value[0] ?? null;
        router.push(
          `/rooms/chat/${router.currentRoute.value.params.roomId}/${
            chatSpaces.value[0]?.id || ""
          }`,
        );
      }

      return;
    }

    if (space === null) {
      currentSpace.value = chatSpaces.value[0] ?? null;
      router.push(
        `/rooms/chat/${router.currentRoute.value.params.roomId}/${
          chatSpaces.value[0]?.id || ""
        }`,
      );
      return;
    }

    currentSpace.value = space;
    router.push(
      `/rooms/${spaceType.toLowerCase()}/${router.currentRoute.value.params.roomId}/${spaceId}`,
    );
  };

  const joinDMSpace = async (spaceId: string, path: string = "/me") => {
    if (_joiningDMSpaceId === spaceId) return;
    try {
      _joiningDMSpaceId = spaceId;
      loading.value = true;

      useRoomMemberStore().clearMembers();
      currentSpace.value = null;
      chatSpaces.value = [];
      voiceSpaces.value = [];
      noteSpaces.value = [];
      calendarSpaces.value = [];
      taskSpaces.value = [];

      const space = await spaceService.getSpaceById(spaceId);

      // Không hiểu tại sao hoạt động. Thứ tự 3 dòng này để im như này
      currentSpace.value = null;
      await router.push(`${path}/${spaceId}`);
      currentSpace.value = space;
    } catch (error) {
      console.log(error);

      toast.error("Không thể tham gia phòng chat này.");
    } finally {
      loading.value = false;
      _joiningDMSpaceId = null;
    }
  };

  const handleSpaceDeleted = async (spaceId: string) => {
    spaceStore.removeSpaceFromArray(spaceId);

    const { useVoiceSpaceStore } =
      await import("@/features/voice-chat/stores/voiceSpaceStore");
    const { currentSpaceId } = storeToRefs(useVoiceSpaceStore());

    if (currentSpaceId.value === spaceId) {
      await useVoiceSpaceStore().leaveRoom();
    }

    if (currentSpace.value?.id === spaceId) {
      changeSpaceById(spaceId, "CHAT");

      router.push(
        `/rooms/chat/${router.currentRoute.value.params.roomId}/${
          chatSpaces.value[0]?.id || ""
        }`,
      );
    }
  };

  const checkPermission = (space: Space | null) => {
    const { user } = storeToRefs(useUserStore());
    const { currentAuthority } = storeToRefs(useRoomMemberStore());

    if (!user.value || !space) return false;

    if (currentAuthority.value === "MEMBER" && space.restricted === true)
      return false;

    return true;
  };

  return {
    fetchSpacesByRoomId,
    changeSpace,
    changeSpaceById,
    joinDMSpace,
    handleSpaceDeleted,
  };
}
