import type { Participant } from "@/types/VoiceSpaceParticipant";
import globalAudio from "@/utils/appAudioManager";
import type { Ref } from "vue";

export function zegoUtils(participants: Ref<Map<string, Participant>>) {
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

  return {
    playNotificationSound,
    createHiddenContainer,
    clearHiddenContainer,
    findUserByStreamID,
  };
}
