import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { ZegoExpressEngine } from "zego-express-engine-webrtc";
import type { Participant } from "@/types/VoiceSpaceParticipant";
import { useUserStore } from "@/stores/userStore";
import { getZegoToken } from "@/services/spaceService";
import { useZego } from "@/composables/useZego";

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
  const micOn = ref(true);
  const audioOn = ref(true);
  const isInRoom = ref(false);
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
    if (isInRoom.value && currentSpaceId.value === spaceId) return;
    if (isInRoom.value) await leaveRoom();

    const userStore = useUserStore();
    const userID = userStore.user?.id!;
    const userName = userStore.user?.username ?? userID;
    const token = await getZegoToken(userID);

    zego.initEngine();
    await zegoState.zg!.checkSystemRequirements();
    zego.registerCallbacks();

    participants.value.set(userID, {
      userID,
      userName,
      videoOn: false,
      isLocal: true,
    });

    const result = await zegoState.zg!.loginRoom(
      spaceId,
      token,
      { userID, userName },
      { userUpdate: true },
    );

    if (result) {
      currentSpaceId.value = spaceId;
      isInRoom.value = true;
      console.log("[Room] joined:", spaceId);
      await zego.publishAudioStream();
    }
  };

  const leaveRoom = async () => {
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
    micOn.value = true;
    audioOn.value = true;

    console.log("[Room] left");
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
  };

  const toggleAudio = () => {
    if (!zegoState.zg) return;
    audioOn.value = !audioOn.value;
    zego.muteAllRemoteAudio(!audioOn.value);
  };

  return {
    // State
    currentSpaceId,
    participants,
    participantList,
    videoOn,
    micOn,
    audioOn,
    isInRoom,
    // Actions
    joinRoom,
    leaveRoom,
    toggleVideo,
    toggleMic,
    toggleAudio,
  };
});
