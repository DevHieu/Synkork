import type { Participant } from "@/types/VoiceSpaceParticipant";
import type { ZegoState } from "@/types/ZegoType";
import type { Ref } from "vue";
import { zegoUtils } from "./zegoUtils";
import globalAudio from "@/utils/appAudioManager";
import { useLocalStorage } from "@vueuse/core";

export function zegoRemoteStream(
  state: ZegoState,
  remoteStreamsList: Map<string, any>,
  participants: Ref<Map<string, Participant>>,
) {
  const utils = zegoUtils(state, participants);

  const playRemoteVideoStream = async (
    videoStreamId: string,
    userID: string,
  ) => {
    if (!state.zg) return;

    const remoteStream = await state.zg.startPlayingStream(videoStreamId);
    remoteStreamsList.set(videoStreamId, remoteStream); // Lưu vào trong list để frontend còn hiện

    // Chỉnh trạng thái video của participant (người cùng trong phòng)
    const participant = participants.value.get(userID);
    if (participant) {
      participant.videoOn = true;
      participant.videoStreamID = videoStreamId;
      participants.value = new Map(participants.value);
    }

    await new Promise((r) => setTimeout(r, 100));
    utils.createHiddenContainer(`remote-video-${userID}`); // Tạo container

    state.zg
      .createRemoteStreamView(remoteStream)
      .play(`remote-video-${userID}`);

    // Cái này sẽ gửi 1 cái dispatchEvent và bên vue sẽ nhận được cái event này
    window.dispatchEvent(
      new CustomEvent("zego:stream-ready", {
        detail: { containerId: `remote-video-${userID}` },
      }),
    );
  };

  const playRemoteAudioStream = async (streamId: string) => {
    if (!state.zg) return;

    const audioRemoteStream = await state.zg.startPlayingStream(streamId);
    remoteStreamsList.set(streamId, audioRemoteStream);

    globalAudio.connectRemoteStream(streamId, audioRemoteStream);

    // Apply callVolume hiện tại ngay lập tức
    const audio = useLocalStorage("app-audio-settings", {
      callVolume: 90,
      callMuted: false,
    });
    const vol = audio.value.callMuted ? 0 : audio.value.callVolume;
    globalAudio.setRemoteVolume(vol);
  };

  const playRemoteScreenStream = async (streamId: string, userId: string) => {
    if (!state.zg) return;

    const screenRemoteStream = await state.zg.startPlayingStream(streamId);
    remoteStreamsList.set(streamId, screenRemoteStream);

    const participant = participants.value.get(userId);
    if (participant) {
      participant.screenOn = true;
      participant.screenStreamID = streamId;
      participants.value = new Map(participants.value);
    }

    await new Promise((r) => setTimeout(r, 100));
    utils.createHiddenContainer(`remote-screen-${userId}`);

    state.zg
      .createRemoteStreamView(screenRemoteStream)
      .play(`remote-screen-${userId}`);

    // Cái này sẽ gửi 1 cái dispatchEvent và bên vue sẽ nhận được cái event này
    window.dispatchEvent(
      new CustomEvent("zego:stream-ready", {
        detail: { containerId: `remote-screen-${userId}` },
      }),
    );
  };

  // Như tên hàm đó, cái này dùng để dừng mấy cái remoteStream
  // Khi người khác nhấn tắt cam/screen thì cái này chạy. Thế thôi
  const stopRemoteStream = (streamId: string) => {
    if (!state.zg) return;

    state.zg.stopPlayingStream(streamId);
    remoteStreamsList.delete(streamId);

    if (streamId.startsWith("video_")) {
      // [, p] là để lấy phần tử thứ 2 của cái Map
      for (const [, p] of participants.value) {
        if (p.videoStreamID === streamId) {
          utils.clearHiddenContainer(`remote-video-${p.userID}`);
          p.videoOn = false;
          p.videoStreamID = undefined;
        }
      }
    } else if (streamId.startsWith("screen_")) {
      for (const [, p] of participants.value) {
        if (p.screenStreamID === streamId) {
          utils.clearHiddenContainer(`remote-screen-${p.userID}`);
          p.screenOn = false;
          p.screenStreamID = undefined;
        }
      }
    } else {
      // Audio stream → cleanup audio element
      globalAudio.disconnectRemoteStream(streamId);
    }

    participants.value = new Map(participants.value);
  };

  return {
    playRemoteVideoStream,
    playRemoteAudioStream,
    playRemoteScreenStream,
    stopRemoteStream,
  };
}
