class AppAudioManager {
  private audioCtx: AudioContext;
  private masterGain: GainNode;
  private audioElements = new Map<string, HTMLAudioElement>();

  constructor() {
    const AudioContextClass =
      window.AudioContext || (window as any).webkitAudioContext;
    this.audioCtx = new AudioContextClass();
    this.masterGain = this.audioCtx.createGain();
    this.masterGain.connect(this.audioCtx.destination);
  }

  async init(): Promise<void> {
    const savedDeviceId = localStorage.getItem("selectedOutputDevice");
    if (!savedDeviceId || savedDeviceId === "default") return;

    if ("setSinkId" in this.audioCtx) {
      try {
        await (this.audioCtx as any).setSinkId(savedDeviceId);
      } catch (err) {
        console.warn("[AudioManager] init setSinkId lỗi:", err);
      }
    }
  }

  connectRemoteStream(streamId: string, mediaStream: MediaStream): void {
    // Cleanup cái cũ nếu có
    this.disconnectRemoteStream(streamId);

    const el = document.createElement("audio");
    el.autoplay = true;
    el.srcObject = mediaStream;
    el.style.display = "none";
    document.body.appendChild(el);
    this.audioElements.set(streamId, el);

    // Áp output device hiện tại
    const currentDeviceId = localStorage.getItem("selectedOutputDevice");
    if (currentDeviceId && typeof (el as any).setSinkId === "function") {
      (el as any).setSinkId(currentDeviceId).catch(console.warn);
    }
  }

  disconnectRemoteStream(streamId: string): void {
    const el = this.audioElements.get(streamId);
    if (!el) return;
    el.srcObject = null;
    el.remove();
    this.audioElements.delete(streamId);
  }

  async changeGlobalOutput(deviceId: string): Promise<void> {
    localStorage.setItem("selectedOutputDevice", deviceId);

    // Đổi AudioContext (cho notification sounds)
    if ("setSinkId" in this.audioCtx) {
      try {
        await (this.audioCtx as any).setSinkId(deviceId);
      } catch (err) {
        console.warn("[AudioManager] AudioContext setSinkId lỗi:", err);
      }
    }

    // Đổi tất cả audio element đang manage (voice call)
    for (const el of this.audioElements.values()) {
      if (typeof (el as any).setSinkId === "function") {
        try {
          await (el as any).setSinkId(deviceId);
        } catch (err) {
          console.warn("[AudioManager] element setSinkId lỗi:", err);
        }
      }
    }
  }

  playSystemSound(soundUrl: string): void {
    fetch(soundUrl)
      .then((res) => res.arrayBuffer())
      .then((data) => this.audioCtx.decodeAudioData(data))
      .then((buffer) => {
        const source = this.audioCtx.createBufferSource();
        source.buffer = buffer;
        source.connect(this.masterGain);
        source.start();
      })
      .catch((err) =>
        console.error("[AudioManager] playSystemSound lỗi:", err),
      );
  }

  setMasterVolume(volume: number): void {
    const normalized = Math.max(0, Math.min(100, volume)) / 100;
    this.masterGain.gain.setValueAtTime(normalized, this.audioCtx.currentTime);
  }
}

const globalAudio = new AppAudioManager();
export default globalAudio;
