import { useUserStore } from "@/stores/userStore";
import type { Participant } from "@/types/VoiceSpaceParticipant";
import type { ZegoState } from "@/types/ZegoType";
import type { Ref } from "vue";

import { zegoUtils } from "./zegoUtils";

export function zegoLocalStream(
  state: ZegoState,
  videoOn: Ref<boolean>,
  micOn: Ref<boolean>,
  audioOn: Ref<boolean>,
  screenOn: Ref<boolean>,
  participants: Ref<Map<string, Participant>>,
) {
  const utils = zegoUtils(participants);

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

      // Có cái setTimeout này là để cách thời gian 1 chút để chắc chắn là video đã sẵn sàng
      await new Promise((r) => setTimeout(r, 100));

      utils.createHiddenContainer("local-video-container"); // Tạo container ẩn

      // Sau đó cho play cái stream mới tạo bên trên trong element vừa tạo
      state.zg
        .createLocalStreamView(state.localVideoStream)
        .play("local-video-container");

      const userStore = useUserStore();
      const me = participants.value.get(userStore.user?.id ?? "");
      if (me) {
        me.videoOn = true;
        me.videoStreamID = state.localVideoStreamID;
        participants.value = new Map(participants.value);
      }

      // Cái này sẽ gửi 1 cái dispatchEvent và bên vue sẽ nhận được cái event này
      window.dispatchEvent(
        new CustomEvent("zego:stream-ready", {
          detail: { containerId: `local-video-container` },
        }),
      );
    } catch (e) {
      console.warn("Không mở được cam:", e);
      videoOn.value = false;
      return null;
    }
  };

  const publishAudioStream = async () => {
    if (!state.zg) return;

    try {
      state.localAudioStream = await state.zg.createZegoStream({
        camera: { audio: true, video: false },
      });
      state.localAudioStreamID = "audio_" + Date.now();

      state.zg.startPublishingStream(
        state.localAudioStreamID,
        state.localAudioStream,
      );

      // Set mic tắt đi lúc vừa vào phòng
      state.zg.muteMicrophone(!micOn.value);

      // Cái cập nhập trạng thái mic thì làm luôn ở store rồi. Tại có hàm mute nữa nên làm ngoài store cho tiện
    } catch (e) {
      console.warn("Không mở được mic:", e);
      audioOn.value = false;
      return null;
    }
  };

  const publishScreenStream = async () => {
    if (!state.zg) return;

    try {
      state.localScreenStream = await state.zg.createZegoStream({
        videoBitrate: 1500,
        screen: {
          audio: false,
          video: {
            quality: 4,
            frameRate: 15,
            width: screen.width,
            height: screen.height,
          },
        },
      });

      state.localScreenStreamID = "screen_" + Date.now();
      await state.zg.startPublishingStream(
        state.localScreenStreamID,
        state.localScreenStream,
      );

      // Có cái setTimeout này là để cách thời gian 1 chút để chắc chắn là video đã sẵn sàng
      await new Promise((r) => setTimeout(r, 100));

      utils.createHiddenContainer("screen-sharing-container");

      state.zg
        .createLocalStreamView(state.localScreenStream)
        .play("screen-sharing-container");

      const userStore = useUserStore();
      const me = participants.value.get(userStore.user?.id!);
      if (me) {
        me.screenOn = true;
        me.screenStreamID = state.localScreenStreamID;
        participants.value = new Map(participants.value);
      }

      // Cái này sẽ gửi 1 cái dispatchEvent và bên vue sẽ nhận được cái event này
      window.dispatchEvent(
        new CustomEvent("zego:stream-ready", {
          detail: { containerId: `screen-sharing-container` },
        }),
      );
    } catch (e) {
      console.warn("Không mở được screen:", e);
      screenOn.value = false;
      throw e;
    }
  };

  const stopVideoStream = () => {
    if (!state.zg || !state.localVideoStream) return;

    // HỦYYYYYY
    state.zg.stopPublishingStream(state.localVideoStreamID);
    state.zg.destroyStream(state.localVideoStream);

    state.localVideoStream = null;
    state.localVideoStreamID = "";

    // Xóa cái element mà mình gắn cái stream vào
    utils.clearHiddenContainer("local-video-container");

    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id ?? "");
    if (me) {
      me.videoOn = false; // Chỉnh lại trạng thái
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

    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id ?? "");
    if (me) {
      me.micOn = false; // Chỉnh lại trạng thái
      participants.value = new Map(participants.value);
    }
  };

  const stopScreenStream = () => {
    if (!state.zg || !state.localScreenStream) return;

    state.zg.stopPublishingStream(state.localScreenStreamID);
    state.zg.destroyStream(state.localScreenStream);

    state.localScreenStream = null;
    state.localScreenStreamID = "";

    utils.clearHiddenContainer("screen-sharing-container");

    const userStore = useUserStore();
    const me = participants.value.get(userStore.user?.id ?? "");
    if (me) {
      me.screenOn = false; // Chỉnh lại trạng thái
      me.screenStreamID = undefined;
      participants.value = new Map(participants.value);
    }
  };

  const changeInputDevice = async (deviceId: string) => {
    if (!state.zg || !state.localAudioStream) return;

    try {
      await state.zg.useAudioDevice(state.localAudioStream, deviceId);
      console.log("Zego đã chuyển sang mic mới:", deviceId);
    } catch (error) {
      console.error("Lỗi khi Zego đổi mic đầu vào:", error);
    }
  };

  const changeOutputDevice = async (deviceId: string) => {
    // Đổi trực tiếp trên từng audio/video element mà Zego đã nhét vào #audio-players
    const audioPlayers = document.getElementById("audio-players");
    if (!audioPlayers) return;

    const mediaElements = audioPlayers.querySelectorAll("audio, video");

    await Promise.allSettled(
      Array.from(mediaElements).map(async (el) => {
        const mediaEl = el as HTMLMediaElement & {
          setSinkId?: (id: string) => Promise<void>;
        };
        if (typeof mediaEl.setSinkId === "function") {
          try {
            await mediaEl.setSinkId(deviceId);
          } catch (err) {
            console.error("Lỗi setSinkId trên element:", el, err);
          }
        }
      }),
    );
  };

  return {
    publishVideoStream,
    publishAudioStream,
    publishScreenStream,
    stopVideoStream,
    stopAudioStream,
    stopScreenStream,
    changeInputDevice,
    changeOutputDevice,
  };
}
