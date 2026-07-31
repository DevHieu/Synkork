import type { Participant } from "@/features/voice-chat/types/VoiceTypes";
import type { ZegoState } from "@/features/voice-chat/types/ZegoTypes";
import globalAudio from "@/utils/appAudioManager";
import { useLocalStorage } from "@vueuse/core";
import type { Ref } from "vue";

export function zegoUtils(
  state: ZegoState,
  participants: Ref<Map<string, Participant>>,
) {
  const playNotificationSound = (type: "join" | "leave") => {
    globalAudio.playSystemSound(`/assets/sounds/${type}Sound.wav`);
  };

  // Hàm này để tạo 1 cái element để zego có thể gắn stream vào
  // Nhưng khi tạo sẽ cho ẩn để nó không hiện ngay. Khi nào cần hiện thì mới chỉnh css lại để hiện
  // Trường hợp lúc xài (share màn hình, ...)
  const createHiddenContainer = (id: string): HTMLElement => {
    let el = document.getElementById(id);
    if (!el) {
      el = document.createElement("div");
      el.id = id;
      el.style.cssText =
        "position:fixed;width:0;height:0;overflow:hidden;pointer-events:none;opacity:0";
      document.body.appendChild(el);
    }
    return el;
  };

  // Xóa srcObject và video element trong hidden container khi stream stop
  const clearHiddenContainer = (id: string) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.querySelectorAll("video").forEach((v) => {
      v.srcObject = null;
      v.remove();
    });
  };

  // Trả về user thôi. Tách hàm ra cho code gọn
  const findUserByStreamID = (
    streamID: string,
    type: "video" | "screen",
  ): string | null => {
    for (const [, p] of participants.value) {
      if (type === "video" && p.videoStreamID === streamID) return p.userID;
      if (type === "screen" && p.screenStreamID === streamID) return p.userID;
    }
    return null;
  };

  const kickMember = (roomId: string, userId: string) => {
    if (!state.zg) return;

    const data = JSON.stringify({ type: "KICK_MEMBER" });

    state.zg.sendCustomCommand(roomId, data, [userId]);
  };

  const audioSettings = useLocalStorage("app-audio-settings", {
    inputVolume: 70,
    inputMuted: false,
  });

  const getCaptureVolume = () =>
    audioSettings.value.inputMuted ? 0 : audioSettings.value.inputVolume;

  const setInputCaptureVolume = async (volume = getCaptureVolume()) => {
    if (!state.zg || !state.localAudioStream) return;

    try {
      await state.zg.setCaptureVolume(state.localAudioStream, volume);
    } catch (error) {
      console.error("Lỗi khi set capture volume:", error);
    }
  };

  return {
    playNotificationSound,
    createHiddenContainer,
    clearHiddenContainer,
    findUserByStreamID,
    kickMember,
    setInputCaptureVolume,
  };
}
