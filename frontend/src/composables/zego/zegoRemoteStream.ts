import type { Participant } from "@/types/VoiceSpaceParticipant";
import type { ZegoState } from "@/types/ZegoType";
import type { Ref } from "vue";
import { zegoUtils } from "./zegoUtils";

export function zegoRemoteStream(
  state: ZegoState,
  remoteStreamsList: Map<string, any>,
  participants: Ref<Map<string, Participant>>,
) {
  const utils = zegoUtils(participants);

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
    utils.ensureHiddenContainer(`remote-video-${userID}`); // Tạo container

    state.zg
      .createRemoteStreamView(remoteStream)
      .play(`remote-video-${userID}`);

    // Cái này sẽ gửi 1 cái dispatchEvent và bên vue sẽ nhận được cái event này
  };

  const playRemoteAudioStream = async (streamId: string) => {
    if (!state.zg) return;

    const audioRemoteStream = await state.zg.startPlayingStream(streamId);
    remoteStreamsList.set(streamId, audioRemoteStream);

    state.zg.createRemoteStreamView(audioRemoteStream).play("audio-players", {
      audio: true, // Phải setup lại ở đây để zego biết đường mà gắn. Tại vì mặc định nó sẽ gửi cả video cả audio
      video: false,
    } as any);
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
    utils.ensureHiddenContainer(`remote-screen-${userId}`);

    state.zg
      .createRemoteStreamView(screenRemoteStream)
      .play(`remote-screen-${userId}`);
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
