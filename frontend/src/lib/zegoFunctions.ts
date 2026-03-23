import { ref, computed, onUnmounted, nextTick } from "vue";
import { ZegoExpressEngine } from "zego-express-engine-webrtc";
import { useUserStore } from "@/stores/userStore";
import { storeToRefs } from "pinia";
import { getZegoToken } from "@/services/spaceService";

export interface Participant {
  userID: string;
  userName: string;
  videoStreamID?: string;
  videoOn: boolean;
  isLocal: boolean;
}

export function zegoFunctions(spaceId: string) {
  const isVideoStream = (streamID: string) => streamID.startsWith("video_");
  const appID = Number(import.meta.env.VITE_ZEGO_APP_ID);
  const server = import.meta.env.VITE_ZEGO_SERVER_URL as string;

  const { user } = storeToRefs(useUserStore());

  let zg: ZegoExpressEngine;

  let localVideoStream: any = null;
  let localVideoStreamID = "";
  let localAudioStream: any = null;
  let localAudioStreamID = "";

  // streamID -> MediaStream
  const remoteStreams = new Map<string, any>();

  const participants = ref<Map<string, Participant>>(new Map());
  const videoOn = ref(false);
  const micOn = ref(true);
  const audioOn = ref(true);

  const participantList = computed(() =>
    Array.from(participants.value.values()),
  );

  // ─── Engine setup ──────────────────────────────────────────────
  const initEngine = () => {
    zg = new ZegoExpressEngine(appID, server);
    zg.setLogConfig({ logLevel: "error", remoteLogLevel: "error" });
  };

  // ─── Local streams ─────────────────────────────────────────────
  const publishAudioStream = async () => {
    localAudioStream = await zg.createZegoStream({
      camera: { audio: true, video: false },
    });
    localAudioStreamID = "audio_" + Date.now();
    zg.startPublishingStream(localAudioStreamID, localAudioStream);
  };

  const publishVideoStream = async () => {
    try {
      localVideoStream = await zg.createZegoStream({
        camera: { audio: false, video: true },
      });
      localVideoStreamID = "video_" + Date.now();
      zg.startPublishingStream(localVideoStreamID, localVideoStream);

      const me = participants.value.get(user.value?.id ?? "");
      if (me) {
        me.videoOn = true;
        me.videoStreamID = localVideoStreamID;
      }
      participants.value = new Map(participants.value); // trigger reactivity

      // Mount local video ngay tại đây, sau khi DOM cập nhật
      await nextTick();
      const container = document.getElementById("local-video-container");
      if (container) {
        zg.createLocalStreamView(localVideoStream).play(container);
      }

      return localVideoStream;
    } catch (e) {
      console.warn("Không mở được cam:", e);
      videoOn.value = false;
      return null;
    }
  };

  const stopVideoStream = () => {
    if (!localVideoStream) return;
    zg.stopPublishingStream(localVideoStreamID);
    zg.destroyStream(localVideoStream);
    localVideoStream = null;
    localVideoStreamID = "";

    const me = participants.value.get(user.value?.id ?? "");
    if (me) {
      me.videoOn = false;
      me.videoStreamID = undefined;
    }
  };

  // ─── Remote streams ────────────────────────────────────────────
  const playRemoteVideoStream = async (streamID: string, userID: string) => {
    const remoteStream = await zg.startPlayingStream(streamID);
    remoteStreams.set(streamID, remoteStream);

    const participant = participants.value.get(userID);
    if (participant) {
      participant.videoOn = true;
      participant.videoStreamID = streamID;
      participants.value = new Map(participants.value);
    }

    await new Promise((r) => setTimeout(r, 100));

    const container = document.getElementById(`remote-video-${userID}`);
    if (container) {
      zg.createRemoteStreamView(remoteStream).play(`remote-video-${userID}`);
    }
  };

  const playRemoteAudioStream = async (streamID: string) => {
    const remoteStream = await zg.startPlayingStream(streamID);
    remoteStreams.set(streamID, remoteStream);

    await new Promise((r) => setTimeout(r, 100));

    const audioContainer = document.getElementById("audio-players");
    if (audioContainer) {
      zg.createRemoteStreamView(remoteStream).play("audio-players", {
        audio: true,
        video: false,
      });
    }
  };

  const stopRemoteStream = (streamID: string) => {
    zg.stopPlayingStream(streamID);
    remoteStreams.delete(streamID);

    if (isVideoStream(streamID)) {
      for (const [, p] of participants.value) {
        if (p.videoStreamID === streamID) {
          p.videoOn = false;
          p.videoStreamID = undefined;
        }
      }
      participants.value = new Map(participants.value);
    }
  };

  // ─── Room callbacks ────────────────────────────────────────────
  const registerCallbacks = () => {
    zg.on("roomStateChanged", (_roomID, reason) => {
      console.log("[Room]", reason);
    });

    zg.on("roomUserUpdate", (_roomID, updateType, userList) => {
      for (const u of userList) {
        if (updateType === "ADD") {
          if (!participants.value.has(u.userID)) {
            participants.value.set(u.userID, {
              userID: u.userID,
              userName: u.userName || u.userID,
              videoOn: false,
              isLocal: false,
            });
          }
        } else {
          participants.value.delete(u.userID);
        }
      }
      participants.value = new Map(participants.value);
    });

    zg.on("roomStreamUpdate", async (_roomID, updateType, streamList) => {
      for (const stream of streamList) {
        if (updateType === "ADD") {
          const userID = stream.user?.userID ?? "";
          if (isVideoStream(stream.streamID)) {
            await playRemoteVideoStream(stream.streamID, userID);
          } else {
            await playRemoteAudioStream(stream.streamID);
          }
        } else {
          stopRemoteStream(stream.streamID);
        }
      }
    });
  };

  // ─── Login ─────────────────────────────────────────────────────
  const login = async () => {
    const userID = user.value?.id!;
    const userName = user.value?.username ?? userID;
    const token = await getZegoToken(userID);

    participants.value.set(userID, {
      userID,
      userName,
      videoOn: false,
      isLocal: true,
    });

    const result = await zg.loginRoom(
      spaceId,
      token,
      { userID, userName },
      { userUpdate: true },
    );

    if (result) {
      console.log("[Room] login success");
      await publishAudioStream();
    }
  };

  // ─── Actions ───────────────────────────────────────────────────
  const toggleVideo = async () => {
    videoOn.value = !videoOn.value;
    if (videoOn.value) {
      await publishVideoStream();
    } else {
      stopVideoStream();
    }
  };

  const toggleMic = () => {
    micOn.value = !micOn.value;
    zg.muteMicrophone(!micOn.value);
  };

  const toggleAudio = () => {
    audioOn.value = !audioOn.value;
    for (const [streamID] of remoteStreams) {
      zg.mutePlayStreamAudio(streamID, !audioOn.value);
    }
  };

  // ─── Lifecycle ─────────────────────────────────────────────────
  const setup = async () => {
    initEngine();
    await zg.checkSystemRequirements();
    registerCallbacks();
    await login();
  };

  const cleanup = () => {
    stopVideoStream();
    if (zg) {
      zg.logoutRoom(spaceId);
      zg.destroyEngine();
    }
  };

  onUnmounted(cleanup);

  return {
    // State
    user,
    participants,
    participantList,
    videoOn,
    micOn,
    audioOn,
    // Actions
    setup,
    toggleVideo,
    toggleMic,
    toggleAudio,
  };
}
