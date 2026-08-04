import { ZegoExpressEngine } from "zego-express-engine-webrtc";

import { zegoUtils } from "./zegoUtils";
import { zegoMedia } from "./zegoMedia";
import { zegoRemoteStream } from "./zegoRemoteStream";
import { zegoLocalStream } from "./zegoLocalStream";
import { useVoiceSpaceStore } from "@/features/voice-chat/stores/voiceSpaceStore";
import { toast } from "vue-sonner";
import type { ZegoServiceOptions } from "@/features/voice-chat/types/ZegoTypes";

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
  isMuted,
  isDeafen,
}: ZegoServiceOptions) {
  let isFirstLoad = true;

  const utils = zegoUtils(state, participants);
  const media = zegoMedia(
    state,
    remoteStreams,
    participants,
    micOn,
    audioOn,
    screenOn,
    isMuted,
    isDeafen,
  );
  const remote = zegoRemoteStream(state, remoteStreams, participants);
  const local = zegoLocalStream(state, videoOn, micOn, screenOn, participants);

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
              muted: false,
              deafen: false,
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

    state.zg.on("screenSharingEnded", () => {
      local.stopScreenStream();
      screenOn.value = false;
    });

    state.zg.on("IMRecvCustomCommand", (_roomID, fromUser, command) => {
      // Cái command là cái dữ liệu mình gửi khi mình dùng mấy cái hàm custom trạng thái mà mình tạo bên zegoMedia á. (broadcastMediaState, requestMediaStates). Qua đấy để xem thêm

      try {
        const data = JSON.parse(command);

        switch (data.type) {
          case "media_state": {
            const p = participants.value.get(fromUser.userID);

            if (p) {
              p.micOn = data.micOn;
              p.audioOn = data.audioOn;
              p.screenOn = data.screenOn;
              p.muted = data.muted ?? p.muted;
              p.deafen = data.deafen ?? p.deafen;
              p.audioStreamID = data.audioId ?? p.audioStreamID;
              participants.value = new Map(participants.value);
            }
            console.log(participants.value);
            break;
          }

          case "request_state": {
            media.broadcastMediaState(_roomID);
            break;
          }

          case "ROOM_MUTE":
          case "ROOM_DEAFEN": {
            if (data.muted !== null) {
              useVoiceSpaceStore().toggleMic(data.muted, true);
            } else if (data.deafen !== null) {
              useVoiceSpaceStore().toggleAudio(data.deafen, true);
            }

            break;
          }

          case "KICK_MEMBER": {
            toast.info("Bạn đã bị kick ra khỏi cuộc trò chuyện");
            useVoiceSpaceStore().leaveRoom();
            break;
          }

          case "STOP_SCREEN": {
            useVoiceSpaceStore().toggleShareScreen();
            break;
          }

          case "STOP_VIDEO": {
            useVoiceSpaceStore().toggleVideo();
            break;
          }

          default:
            break;
        }
      } catch (e) {
        console.warn(e);
      }
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
