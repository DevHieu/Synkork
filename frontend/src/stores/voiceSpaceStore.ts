import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { ZegoExpressEngine } from "zego-express-engine-webrtc";
import type { Participant } from "@/types/VoiceSpaceParticipant";
import { useUserStore } from "@/stores/userStore";
import { getZegoToken } from "@/services/spaceService";
import { useZego } from "@/composables/useZego";
import router from "@/routers";
import { useLocalStorage } from "@vueuse/core";
import { useSpaceStore } from "@/stores/spaceStore";

export const useVoiceSpaceStore = defineStore("voiceSpace", () => {
  const appID = Number(import.meta.env.VITE_ZEGO_APP_ID);
  const server = import.meta.env.VITE_ZEGO_SERVER_URL as string;

  // state để tạo Zego service
  const zegoState = {
    zg: null as ZegoExpressEngine | null,
    localAudioStream: null as any,
    localAudioStreamID: "",
    localVideoStream: null as any,
    localVideoStreamID: "",
  };

  // Các state này cần để dạng reactive để tự động cập nhật khi có thay đổi
  const remoteStreams = new Map<string, any>();
  const currentSpaceId = ref<string | null>(null);
  const participants = ref<Map<string, Participant>>(new Map());
  const videoOn = ref(false);
  const micOn = useLocalStorage("voice-mic-on", false);
  const audioOn = ref(true);
  const isJoining = ref(false);
  const isInRoom = ref(false);
  const isExpanded = ref(false);
  const participantList = computed(() =>
    Array.from(participants.value.values()),
  );

  // các hàm xử lí của ZegoCloud
  const zego = useZego({
    state: zegoState,
    appID,
    server,
    participants,
    remoteStreams,
    videoOn,
    micOn,
    audioOn,
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
    await zego.registerCallbacks();

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

      participants.value.set(userID, {
        userID,
        userName,
        videoOn: false,
        micOn: micOn.value,
        audioOn: true,
        isLocal: true,
      });

      await zego.publishAudioStream();
      zego.playNotificationSound("join");
      zego.requestMediaStates(spaceId);
    }
  };

  const leaveRoom = async () => {
    isJoining.value = false;
    if (!zegoState.zg || !isInRoom.value) return;

    zego.stopVideoStream();
    zego.stopAudioStream();

    zegoState.zg.logoutRoom(currentSpaceId.value!);
    zego.destroyEngine();

    participants.value = new Map();
    remoteStreams.clear();
    currentSpaceId.value = null;
    isInRoom.value = false;
    videoOn.value = false;
    audioOn.value = true;

    zego.playNotificationSound("leave");

    if (router.currentRoute.value.path.includes("/rooms/voice")) {
      await useSpaceStore().changeSpace(0, "CHAT");
    }
  };

  const toggleVideo = async () => {
    videoOn.value = !videoOn.value;
    if (videoOn.value) {
      await zego.publishVideoStream();
    } else {
      zego.stopVideoStream();
    }
  };

  const toggleMic = () => {
    if (!zegoState.zg) return;
    micOn.value = !micOn.value;
    zego.muteMicrophone(!micOn.value);

    // Cập nhật trạng thái mic của chính mình trong participants để UI tự động cập nhật
    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id!);
    if (me) me.micOn = micOn.value;

    zego.broadcastMediaState(currentSpaceId.value!);
  };

  const toggleAudio = () => {
    if (!zegoState.zg) return;
    audioOn.value = !audioOn.value;
    zego.muteAllRemoteAudio(!audioOn.value);

    // Cập nhật trạng thái mic của chính mình trong participants để UI tự động cập nhật
    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id!);
    if (me) me.audioOn = audioOn.value;

    zego.broadcastMediaState(currentSpaceId.value!);
  };

  const getParticipantsForSpace = (spaceId: string): Participant[] => {
    if (currentSpaceId.value !== spaceId) return [];
    participants.value.forEach((p, key) => {
      console.log(key, p);
    });

    return Array.from(participants.value.values());
  };

  return {
    currentSpaceId,
    participants,
    participantList,
    videoOn,
    micOn,
    audioOn,
    isInRoom,
    isJoining,

    joinRoom,
    leaveRoom,
    toggleVideo,
    toggleMic,
    toggleAudio,
    getParticipantsForSpace,
  };
});
