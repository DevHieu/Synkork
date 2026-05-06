import { useRoomMemberStore } from "./roomMemberStore";
import { storeToRefs } from "pinia";
import { defineStore } from "pinia";
import { ref, computed, watch, reactive } from "vue";
import { ZegoExpressEngine } from "zego-express-engine-webrtc";
import type { Participant } from "@/types/VoiceSpaceParticipant";
import { useUserStore } from "@/stores/userStore";
import { getZegoToken } from "@/services/spaceService";
import router from "@/routers";
import { useLocalStorage } from "@vueuse/core";
import { useSpaceStore } from "@/stores/spaceStore";

import { useZego } from "@/composables/zego/useZego";
import { muteAudio } from "@/services/roomMemberService";
import { toast } from "vue-sonner";

export const useVoiceSpaceStore = defineStore("voiceSpace", () => {
  const appID = Number(import.meta.env.VITE_ZEGO_APP_ID);
  const server = import.meta.env.VITE_ZEGO_SERVER_URL as string;

  const zegoState = {
    zg: null as ZegoExpressEngine | null,
    localAudioStream: null as any,
    localAudioStreamID: "",
    localVideoStream: null as any,
    localVideoStreamID: "",
    localScreenStream: null as any,
    localScreenStreamID: "",
  };

  const remoteStreams = new Map<string, any>();
  const participants = ref<Map<string, Participant>>(new Map());

  const currentRoomId = ref(router.currentRoute.value.params.roomId as string);
  const currentSpaceId = ref<string | null>(null);
  const videoOn = ref(false);
  const micOn = useLocalStorage("voice-mic-on", false);
  const audioOn = ref(true);
  const screenOn = ref(false);
  const isMuted = ref(false);
  const isDeafen = ref(false);

  const isJoining = ref(false);
  const isInRoom = ref(false);
  const isExpanded = ref(false);
  const participantList = computed(() =>
    Array.from(participants.value.values()),
  );
  const mutedList = reactive(new Map<string, boolean>());

  const roomMemberStore = useRoomMemberStore();
  const { isMuted: mutedStore, isDeafen: deafenStore } =
    storeToRefs(roomMemberStore);

  // Vì thông tin ở roomMember load nó sẽ chậm nên để watch ở đây để khi nó load xong sẽ thay đổi luôn
  watch(mutedStore, (val) => {
    isMuted.value = val;
    if (isInRoom.value) toggleMic(val, true);
  });

  watch(deafenStore, (val) => {
    isDeafen.value = val;
    if (isInRoom.value) toggleAudio(val, true);
  });

  const zego = useZego({
    state: zegoState,
    appID,
    server,
    participants,
    remoteStreams,
    videoOn,
    micOn,
    audioOn,
    screenOn,
    isMuted,
    isDeafen,
  });

  // Cần thêm roomId để làm chức năng mute tiếng user toàn phòng
  const joinRoom = async (spaceId: string) => {
    isJoining.value = true;
    if (isInRoom.value && currentSpaceId.value === spaceId) {
      isExpanded.value = true;
      router.push(
        `/rooms/voice/${router.currentRoute.value.params.roomId}/${spaceId}`,
      );
      return;
    }

    // Đang trong space khác thì out ra đã rồi vào
    if (isInRoom.value) {
      await leaveRoom();
    }

    const userStore = useUserStore();
    const userID = userStore.user?.id!;
    const userName = userStore.user?.username ?? userID;

    if (!userID || !userName) {
      console.warn("[Room] user chưa load xong, thử lại sau...");
      return;
    }

    isMuted.value = mutedStore.value;
    isDeafen.value = deafenStore.value;

    const token = await getZegoToken(userID);

    await zego.initEngine();
    await zegoState.zg!.checkSystemRequirements();
    await zego.registerCallback();

    const result = await zegoState.zg!.loginRoom(
      spaceId,
      token,
      { userID, userName },
      { userUpdate: true },
    );

    if (result) {
      currentSpaceId.value = spaceId;
      isInRoom.value = true;
      isJoining.value = false;

      // Add bản thân vào trong list participant
      participants.value.set(userID, {
        userID,
        userName,
        videoOn: false,
        micOn: micOn.value,
        audioOn: true,
        screenOn: false,
        isLocal: true,
        muted: isMuted.value,
        deafen: isDeafen.value,
      });

      await zego.local.publishAudioStream();
      zego.utils.playNotificationSound("join");
      zego.media.requestMediaStates(spaceId);
    }
  };

  const leaveRoom = async () => {
    isJoining.value = false;
    if (!zegoState.zg || !isInRoom.value) return;

    zego.local.stopVideoStream();
    zego.local.stopAudioStream();

    zegoState.zg.logoutRoom(currentSpaceId.value!);
    zego.destroyEngine();

    participants.value = new Map();
    remoteStreams.clear();
    currentSpaceId.value = null;
    isInRoom.value = false;
    videoOn.value = false;
    screenOn.value = false;
    audioOn.value = true;

    zego.utils.playNotificationSound("leave");

    if (router.currentRoute.value.path.includes("/rooms/voice")) {
      await useSpaceStore().changeSpace(0, "CHAT");
    }
  };

  const toggleVideo = async () => {
    videoOn.value = !videoOn.value;
    if (videoOn.value) {
      await zego.local.publishVideoStream();
    } else {
      zego.local.stopVideoStream();
    }
  };

  const toggleMic = (state?: boolean, isAdmin?: boolean) => {
    if (!zegoState.zg) return;

    // state dduwwocj truyền vào khi admin chặn/mở. Nên là nếu state không có -> user đang nhấn -> chặn
    if (isMuted.value === true && isAdmin === undefined) {
      toast.error("Bạn đã bị chủ phòng tắt mic. Hãy liên hệ đễ được gỡ");
      return;
    }

    if (state !== undefined && isAdmin === true) {
      isMuted.value = state;
      // Admin khóa → tắt mic luôn
      if (state === true) {
        micOn.value = false;
        zego.media.muteMicro(true);
        const userStore = useUserStore();
        const me = participants.value.get(userStore.user?.id!);
        if (me) {
          me.micOn = false;
          me.muted = true;
        }
      } else {
        // Admin mở → chỉ gỡ khóa, không tự bật mic. Người dùng tự quyết
        const userStore = useUserStore();
        const me = participants.value.get(userStore.user?.id!);
        if (me) me.muted = false;
      }
      zego.media.broadcastMediaState(currentSpaceId.value!);
      return;
    }

    micOn.value = state !== undefined ? state : !micOn.value;
    zego.media.muteMicro(!micOn.value); // Phải đảo lại vì cách hiểu true false lúc làm nó cứ sai sai. Mà lười sửa quá nên để tạm vậy

    // Cái này để cập nhập trạng thái mic của user trong participant. Trong mấy hàm nhỏ ko ghi nên ghi ngoài đây
    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id!);
    if (me) me.micOn = micOn.value;

    zego.media.broadcastMediaState(currentSpaceId.value!);
  };

  const toggleAudio = (state?: boolean, isAdmin?: boolean) => {
    if (!zegoState.zg) return;

    if (isDeafen.value === true && isAdmin === undefined) {
      toast.error("Bạn đã bị chủ phòng tắt âm thanh. Hãy liên hệ đễ được gỡ");
      return;
    }

    if (state !== undefined && isAdmin === true) {
      isDeafen.value = state;
    } else if (audioOn.value === true) {
      // Tắt loa thì tắt mic luôn, so sánh state để admin tắt loa thì ko cần tắt mic
      toggleMic(false);
    }

    audioOn.value = state !== undefined ? !state : !audioOn.value;
    zego.media.muteAllRemoteAudio(!audioOn.value);

    // Cái này y chang như trên mic
    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id!);
    if (me) {
      me.audioOn = audioOn.value;
      me.audioStreamID = zegoState.localAudioStreamID;
    }

    zego.media.broadcastMediaState(currentSpaceId.value!);
  };

  const toggleShareScreen = async () => {
    screenOn.value = !screenOn.value;
    if (screenOn.value) {
      await zego.local.publishScreenStream();
    } else {
      zego.local.stopScreenStream();
    }
  };

  const replayAllStreamsToDOM = async () => {
    await zego.media.replayAllStreamToDOM();
  };

  const getParticipantsForSpace = (spaceId: string): Participant[] => {
    if (currentSpaceId.value !== spaceId) return [];
    return Array.from(participants.value.values());
  };

  const getAudioTracks = (): MediaStreamTrack[] => {
    const tracks: MediaStreamTrack[] = [];

    // Remote audio từ div#audio-players
    const audioPlayers = document.getElementById("audio-players");
    if (audioPlayers) {
      audioPlayers.querySelectorAll("audio, video").forEach((el) => {
        const mediaEl = el as HTMLMediaElement;
        if (mediaEl.srcObject instanceof MediaStream) {
          mediaEl.srcObject.getAudioTracks().forEach((t) => tracks.push(t));
        }
      });
    }

    // Local mic
    if (zegoState.localAudioStream) {
      zegoState.localAudioStream
        .getAudioTracks()
        .forEach((t: MediaStreamTrack) => tracks.push(t));
    }

    return tracks;
  };

  const toggleMuteUser = (
    userId: string,
    type: "ROOM_MUTE" | "ROOM_DEAFEN",
    data: {
      muted: boolean | null;
      deafen: boolean | null;
    },
  ) => {
    console.log("what the fack - voiceSpaceStore");
    console.log(currentRoomId.value);

    if (!zegoState.zg || !currentRoomId.value) return;

    const payload = {
      type: type,
      ...data,
    };

    muteAudio(currentRoomId.value, userId, data);
    zego.media.roomMutedUserRequest(currentSpaceId.value!, userId, payload);
  };

  const kickMember = (userId: string) => {
    if (!zegoState.zg || !currentSpaceId.value) return;

    zego.media.kickMember(currentSpaceId.value, userId);
  };

  const stopUserScreen = (userId: string) => {
    if (!zegoState.zg || !currentSpaceId.value) return;
    zego.media.stopUserScreen(currentSpaceId.value, userId);
  };

  // User A ko muốn nghe tiếng của User B -> Ý là vậy á. Mà ko biết đặt tên sao cho hợp lí
  const toggleAudioUser = (audioId: string) => {
    if (!zegoState.zg || !currentSpaceId.value) return;

    let isMute = true;
    if (mutedList.has(audioId)) {
      isMute = false;
      mutedList.delete(audioId);
    } else {
      mutedList.set(audioId, true);
    }

    zego.media.muteAudioUser(audioId, isMute);
  };

  return {
    currentSpaceId,
    participants,
    participantList,
    mutedList,
    videoOn,
    micOn,
    audioOn,
    screenOn,
    isInRoom,
    isJoining,

    joinRoom,
    leaveRoom,
    toggleVideo,
    toggleMic,
    toggleAudio,
    toggleShareScreen,
    replayAllStreamsToDOM,
    getParticipantsForSpace,
    getAudioTracks,
    toggleMuteUser,
    kickMember,
    stopUserScreen,
    toggleAudioUser,
  };
});
