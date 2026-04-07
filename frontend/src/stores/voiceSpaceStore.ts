import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { ZegoExpressEngine } from "zego-express-engine-webrtc";
import type { Participant } from "@/types/VoiceSpaceParticipant";
import { useUserStore } from "@/stores/userStore";
import { getZegoToken } from "@/services/spaceService";
import router from "@/routers";
import { useLocalStorage } from "@vueuse/core";
import { useSpaceStore } from "@/stores/spaceStore";

import { useZego } from "@/composables/zego/useZego";
import { zegoLocalStream } from "@/composables/zego/zegoLocalStream";
import { zegoMedia } from "@/composables/zego/zegoMedia";
import { zegoUtils } from "@/composables/zego/zegoUtils";

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

  const currentSpaceId = ref<string | null>(null);
  const videoOn = ref(false);
  const micOn = useLocalStorage("voice-mic-on", false);
  const audioOn = ref(true);
  const screenOn = ref(false);
  const isJoining = ref(false);
  const isInRoom = ref(false);
  const isExpanded = ref(false);
  const participantList = computed(() =>
    Array.from(participants.value.values()),
  );

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
  });

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

  const toggleMic = () => {
    if (!zegoState.zg) return;
    micOn.value = !micOn.value;
    zego.media.muteMicro(!micOn.value);

    // Cái này để cập nhập trạng thái mic của user trong participant. Trong mấy hàm nhỏ ko ghi nên ghi ngoài đây
    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id!);
    if (me) me.micOn = micOn.value;

    zego.media.broadcastMediaState(currentSpaceId.value!);
  };

  const toggleAudio = () => {
    if (!zegoState.zg) return;
    audioOn.value = !audioOn.value;
    zego.media.muteAllRemoteAudio(!audioOn.value);

    // Cái này y chang như trên mic
    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id!);
    if (me) me.audioOn = audioOn.value;

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

  return {
    currentSpaceId,
    participants,
    participantList,
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
  };
});
