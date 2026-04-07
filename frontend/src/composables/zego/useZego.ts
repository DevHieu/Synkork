import { ZegoExpressEngine } from "zego-express-engine-webrtc";

import { zegoUtils } from "./zegoUtils";
import { zegoMedia } from "./zegoMedia";
import { zegoRemoteStream } from "./zegoRemoteStream";
import { zegoLocalStream } from "./zegoLocalStream";

import type { Participant } from "@/types/VoiceSpaceParticipant";
import type { Ref } from "vue";

interface ZegoState {
  zg: ZegoExpressEngine | null;
  localAudioStream: any;
  localAudioStreamID: string;
  localVideoStream: any;
  localVideoStreamID: string;
  localScreenStream: any;
  localScreenStreamID: string;
}

interface ZegoServiceOptions {
  state: ZegoState;
  appID: number;
  server: string;
  participants: Ref<Map<string, Participant>>;
  remoteStreams: Map<string, any>;
  videoOn: Ref<boolean>;
  micOn: Ref<boolean>;
  audioOn: Ref<boolean>;
  screenOn: Ref<boolean>;
}

export function useZego({
  state,
  appID,
  server,
  participants,
  remoteStreams,
  videoOn,
  micOn,
  audioOn,
  screenOn,
}: ZegoServiceOptions) {
  let isFirstLoad = true;

  const utils = zegoUtils(participants);
  const media = zegoMedia(
    state,
    remoteStreams,
    participants,
    micOn,
    audioOn,
    screenOn,
  );
  const remote = zegoRemoteStream(state, remoteStreams, participants);
  const local = zegoLocalStream(
    state,
    videoOn,
    micOn,
    audioOn,
    screenOn,
    participants,
  );

  const initEngine = () => {
    if (state.zg) return;
    state.zg = new ZegoExpressEngine(appID, server);
    state.zg.setLogConfig({ logLevel: "error", remoteLogLevel: "error" });
  };

  const destroyEngine = () => {
    if (!state.zg) return;
    isFirstLoad = true;
    state.zg.destroyEngine();
    state.zg = null;
  };

  const registerCallback = () => {
    if (!state.zg) return;

    state.zg.on("roomStateChanged", (_roomID, reason) => {
      console.log("[Room]", reason);
    });

    state.zg.on("roomUserUpdate", (_roomID, updateType, userList) => {
      if (updateType === "ADD") {
        // Khi user vào cuộc gọi
        userList.forEach((u) => {
          if (!participants.value.has(u.userID)) {
            // Thêm user vào list participants
            participants.value.set(u.userID, {
              userID: u.userID,
              userName: u.userName || u.userID,
              videoOn: false,
              micOn: true,
              audioOn: true,
              screenOn: false,
              isLocal: false,
            });
          }

          // vào lần đầu thì có cái tienegs vào space
          if (!isFirstLoad) {
            utils.playNotificationSound("join");
          }

          // Yêu cầu lấy mấy cái trạng thái mic, audio screen của những người vào trước
          media.broadcastMediaState(_roomID);
        });
      } else {
        if (userList.length > 0) {
          utils.playNotificationSound("leave");
        }

        // Xóa ra cái list participant
        userList.forEach((u) => participants.value.delete(u.userID));
      }

      // Cập nhập danh sách
      participants.value = new Map(participants.value);
    });

    state.zg.on("roomStreamUpdate", async (_roomID, updateType, streamList) => {
      if (!state.zg) return;

      for (const stream of streamList) {
        if (updateType === "ADD") {
          const userId = stream.user?.userID ?? "";
          const streamId = stream.streamID;
          if (streamId.startsWith("video_")) {
            await remote.playRemoteVideoStream(streamId, userId);
          } else if (streamId.startsWith("screen_")) {
            await remote.playRemoteScreenStream(streamId, userId);
          } else {
            await remote.playRemoteAudioStream(streamId);
          }
        } else {
          // Tắt cam/screen thì chạy
          remote.stopRemoteStream(stream.streamID);
        }
      }
    });

    state.zg.on("IMRecvCustomCommand", (_roomID, fromUser, command) => {
      // Cái command là cái dữ liệu mình gửi khi mình dùng mấy cái hàm custom trạng thái mà mình tạo bên zegoMedia á. (broadcastMediaState, requestMediaStates). Qua đấy để xem thêm

      try {
        const data = JSON.parse(command);

        if (data.type === "media_state") {
          const p = participants.value.get(fromUser.userID); // Tìm user

          if (p) {
            // Chỉnh trạng thái và cập nhập list participant
            p.micOn = data.micOn;
            p.audioOn = data.audioOn;
            p.screenOn = data.screenOn;
            participants.value = new Map(participants.value);
          }
        } else if (data.type === "request_state") {
          // Cái này là gửi yêu cầu lấy state của người trong phòng trước đó
          media.broadcastMediaState(_roomID);
        }
      } catch (e) {
        console.warn(e);
      }
    });

    state.zg.on("screenSharingEnded", () => {
      local.stopScreenStream();
      screenOn.value = false;
    });
  };

  return {
    initEngine,
    destroyEngine,
    registerCallback,
    utils,
    media,
    local,
    remote,
  };
}
