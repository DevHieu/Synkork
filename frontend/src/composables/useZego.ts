import { ZegoExpressEngine } from "zego-express-engine-webrtc";
import type { Participant } from "@/types/VoiceSpaceParticipant";
import type { Ref } from "vue";
import { useUserStore } from "@/stores/userStore";

interface ZegoState {
  zg: ZegoExpressEngine | null;
  localAudioStream: any;
  localAudioStreamID: string;
  localVideoStream: any;
  localVideoStreamID: string;
}

interface ZegoServiceOptions {
  state: ZegoState;
  appID: number;
  server: string;
  participants: Ref<Map<string, Participant>>;
  remoteStreams: Map<string, any>;
  videoOn: Ref<boolean>;
}

export function useZego({
  state,
  appID,
  server,
  participants,
  remoteStreams,
  videoOn,
}: ZegoServiceOptions) {
  // ─── Helpers ────────────────────────────────────────────────
  const isVideoStream = (streamID: string) => streamID.startsWith("video_");

  // ─── Engine ─────────────────────────────────────────────────
  const initEngine = () => {
    if (state.zg) return;
    state.zg = new ZegoExpressEngine(appID, server);
    state.zg.setLogConfig({ logLevel: "error", remoteLogLevel: "error" });
  };

  const destroyEngine = () => {
    if (!state.zg) return;
    state.zg.destroyEngine();
    state.zg = null;
  };

  // ─── Local streams ───────────────────────────────────────────
  const publishAudioStream = async () => {
    if (!state.zg) return;
    state.localAudioStream = await state.zg.createZegoStream({
      camera: { audio: true, video: false },
    });
    state.localAudioStreamID = "audio_" + Date.now();
    state.zg.startPublishingStream(
      state.localAudioStreamID,
      state.localAudioStream,
    );
  };

  const publishVideoStream = async () => {
    if (!state.zg) return;
    try {
      state.localVideoStream = await state.zg.createZegoStream({
        camera: { audio: false, video: true },
      });
      state.localVideoStreamID = "video_" + Date.now();
      state.zg.startPublishingStream(
        state.localVideoStreamID,
        state.localVideoStream,
      );

      await new Promise((r) => setTimeout(r, 100));
      const container = document.getElementById("local-video-container"); // Gắn stream vào trong thẻ có id là "local-video-container"
      if (container) {
        state.zg
          .createLocalStreamView(state.localVideoStream)
          .play("local-video-container");
      }

      const userStore = useUserStore();
      const me = participants.value.get(userStore.user?.id ?? "");
      if (me) {
        me.videoOn = true;
        me.videoStreamID = state.localVideoStreamID;
        participants.value = new Map(participants.value);
      }

      return state.localVideoStream;
    } catch (e) {
      console.warn("Không mở được cam:", e);
      videoOn.value = false;
      return null;
    }
  };

  const stopVideoStream = () => {
    if (!state.zg || !state.localVideoStream) return;
    state.zg.stopPublishingStream(state.localVideoStreamID);
    state.zg.destroyStream(state.localVideoStream);
    state.localVideoStream = null;
    state.localVideoStreamID = "";

    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id ?? "");
    if (me) {
      me.videoOn = false;
      me.videoStreamID = undefined;
      participants.value = new Map(participants.value);
    }
  };

  const stopAudioStream = () => {
    if (!state.zg || !state.localAudioStream) return;
    state.zg.stopPublishingStream(state.localAudioStreamID);
    state.zg.destroyStream(state.localAudioStream);
    state.localAudioStream = null;
    state.localAudioStreamID = "";
  };

  // ─── Remote streams ──────────────────────────────────────────
  const playRemoteVideoStream = async (streamID: string, userID: string) => {
    if (!state.zg) return;
    const remoteStream = await state.zg.startPlayingStream(streamID);
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
      state.zg
        .createRemoteStreamView(remoteStream)
        .play(`remote-video-${userID}`);
    }
  };

  const playRemoteAudioStream = async (streamID: string) => {
    if (!state.zg) return;
    const remoteStream = await state.zg.startPlayingStream(streamID);
    remoteStreams.set(streamID, remoteStream);

    await new Promise((r) => setTimeout(r, 100));

    const audioContainer = document.getElementById("audio-players");
    if (audioContainer) {
      state.zg.createRemoteStreamView(remoteStream).play("audio-players", {
        audio: true,
        video: false,
      });
    }
  };

  const stopRemoteStream = (streamID: string) => {
    if (!state.zg) return;
    state.zg.stopPlayingStream(streamID);
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

  // ─── Callbacks ───────────────────────────────────────────────
  const registerCallbacks = () => {
    if (!state.zg) return;

    state.zg.on("roomStateChanged", (_roomID, reason) => {
      console.log("[Room]", reason);
    });

    state.zg.on("roomUserUpdate", (_roomID, updateType, userList) => {
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

    state.zg.on("roomStreamUpdate", async (_roomID, updateType, streamList) => {
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

  // ─── Mic / Audio controls ────────────────────────────────────
  const muteMicrophone = (mute: boolean) => {
    if (!state.zg) return;
    state.zg.muteMicrophone(mute);
  };

  const muteAllRemoteAudio = (mute: boolean) => {
    if (!state.zg) return;
    for (const [streamID] of remoteStreams) {
      state.zg.mutePlayStreamAudio(streamID, mute);
    }
  };

  return {
    initEngine,
    destroyEngine,
    publishAudioStream,
    publishVideoStream,
    stopVideoStream,
    stopAudioStream,
    playRemoteVideoStream,
    playRemoteAudioStream,
    stopRemoteStream,
    registerCallbacks,
    muteMicrophone,
    muteAllRemoteAudio,
  };
}
