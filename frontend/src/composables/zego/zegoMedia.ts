import { zegoUtils } from "./zegoUtils";
import type { ZegoState } from "@/types/ZegoType";
import type { Ref } from "vue";
import type { Participant } from "@/types/VoiceSpaceParticipant";

export function zegoMedia(
  state: ZegoState,
  remoteStreamsList: Map<string, any>,
  participants: Ref<Map<string, Participant>>,
  micOn: Ref<boolean>,
  audioOn: Ref<boolean>,
  screenOn: Ref<boolean>,
  isMuted: Ref<boolean>,
  isDeafen: Ref<boolean>,
) {
  const utils = zegoUtils(state, participants);

  // Tắt micro
  const muteMicro = async (mute: boolean) => {
    if (!state.zg) return;

    state.zg.muteMicrophone(mute);
  };

  // Tắt audio (tắt ko nghe ai nói nữa đó)
  const muteAllRemoteAudio = async (mute: boolean) => {
    if (!state.zg) return;

    for (const [streamID] of remoteStreamsList) {
      state.zg.mutePlayStreamAudio(streamID, mute);
    }
  };

  const muteAudioUser = async (audioId: string, isMute: boolean) => {
    if (!state.zg) return;

    state.zg.mutePlayStreamAudio(audioId, isMute);
  };

  const broadcastMediaState = (roomID: string) => {
    if (!state.zg) return;

    // Mình muốn người khác thấy gì gửi ở đây
    const payload = JSON.stringify({
      type: "media_state",
      micOn: micOn.value,
      audioOn: audioOn.value,
      screenOn: screenOn.value,
      muted: isMuted.value,
      deafen: isDeafen.value,
      audioId: state.localAudioStreamID,
    });
    state.zg.sendCustomCommand(roomID, payload, []); // param thứ 3 là userId muốn gửi tới. Để trống thì gửi hết mọi người
  };

  const requestMediaStates = (roomID: string) => {
    if (!state.zg) return;
    const payload = JSON.stringify({ type: "request_state" }); // Truyền type vậy để callback còn phân biệt mà xử lí
    state.zg.sendCustomCommand(roomID, payload, []);
  };

  const roomMutedUserRequest = (
    roomId: string,
    userId: string,
    payload: {
      type: "ROOM_MUTE" | "ROOM_DEAFEN";
      muted: boolean | null;
      deafen: boolean | null;
    },
  ) => {
    if (!state.zg) return;
    const data = JSON.stringify(payload);
    console.log("media activated ", roomId, userId, data);
    state.zg.sendCustomCommand(roomId, data, [userId]);
  };

  const stopUserScreen = (roomId: string, userId: string) => {
    if (!state.zg) return;
    const data = JSON.stringify({ type: "STOP_SCREEN" });
    state.zg.sendCustomCommand(roomId, data, [userId]);
  };

  const stopUserVideo = (roomId: string, userId: string) => {
    if (!state.zg) return;
    const data = JSON.stringify({ type: "STOP_VIDEO" });
    state.zg.sendCustomCommand(roomId, data, [userId]);
  };

  // Khi user từ giao diện call sang các space khác. Thì giao diện sẽ thay đổi -> các element, DOM cảu screen sẽ mất.
  // Vì zego render các cái thành phần stream bằng các id của element và mình đang làm khi publishStream mới thì mới tạo DOM -> zego mới render được

  // Nên Khi mình chuyển qua giao diện khác xong vào lại call -> các cái stream (video, screen sharing) sẽ biến mất

  // Và để hiện lại thì mình cần phải tạo lại các element và mình phải kêu zego stream lại
  // Đấy là lí do vì sao mình cần lưu các cái stream lại (để có thể gọi lại và stream lại)
  // Hàm này sẽ duyệt qua stream của mình và các participant và sẽ tạo lại các element và render lên lại
  const replayAllStreamToDOM = async () => {
    if (!state.zg) return;

    // Check xem trạng thái hiện tại của local
    if (state.localVideoStream) {
      utils.createHiddenContainer("local-video-container");
      state.zg
        .createLocalStreamView(state.localVideoStream)
        .play("local-video-container");
    }

    if (state.localScreenStream) {
      utils.createHiddenContainer("screen-sharing-container");
      state.zg
        .createLocalStreamView(state.localScreenStream)
        .play("screen-sharing-container");
    }

    // Lặp qua các participant check xem trạng thái của từng người và render lên
    for (const [streamId, remoteStream] of remoteStreamsList) {
      if (streamId.startsWith("video_")) {
        // Nếu id là video -> render video
        const userId = utils.findUserByStreamID(streamId, "video");
        if (userId) {
          utils.createHiddenContainer(`remote-video-${userId}`);
          state.zg
            .createRemoteStreamView(remoteStream)
            .play(`remote-video-${userId}`);
        }
      } else if (streamId.startsWith("screen_")) {
        // Và ngược lại (screen)
        const userId = utils.findUserByStreamID(streamId, "screen");
        if (userId) {
          utils.createHiddenContainer(`remote-screen-${userId}`);
          state.zg
            .createRemoteStreamView(remoteStream)
            .play(`remote-screen-${userId}`);
        }
      }
    }

    await new Promise((r) => setTimeout(r, 200));
  };

  return {
    muteMicro,
    muteAllRemoteAudio,
    muteAudioUser,
    broadcastMediaState,
    requestMediaStates,
    replayAllStreamToDOM,
    roomMutedUserRequest,
    stopUserScreen,
    stopUserVideo,
  };
}
