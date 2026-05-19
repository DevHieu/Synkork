import { useLocalStorage } from "@vueuse/core";

const audio = useLocalStorage("app-audio-settings", {
  inputVolume: 70,
  inputMuted: false,

  outputVolume: 100,
  outputMuted: false,

  callVolume: 90,
  callMuted: false,

  systemVolume: 75,
  systemMuted: false,
});
class AppAudioManager {
  private currentInputVolume: number = 70;
  private audioCtx: AudioContext;
  private masterGain: GainNode;
  private audioElements = new Map<string, HTMLAudioElement>();
  private micTestStream: MediaStream | null = null;
  private micAnalyser: AnalyserNode | null = null;
  private micTestAnimFrame: number | null = null;
  private micTestGain: GainNode | null = null;
  private speakerTestEl: HTMLAudioElement | null = null;

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

    this.syncAudioSettings(audio.value);

    if ("setSinkId" in this.audioCtx) {
      try {
        await (this.audioCtx as any).setSinkId(savedDeviceId);
      } catch (err) {
        console.warn("[AudioManager] init setSinkId lỗi:", err);
      }
    }
  }

  syncAudioSettings(settings: typeof audio.value) {
    // Tỉ lệ của âm lượng tổng (từ 0.0 đến 1.0). Nếu mute tổng thì tỉ lệ bằng 0.
    const masterScale = settings.outputMuted ? 0 : settings.outputVolume / 100;

    // Mic
    const actualInputVol = settings.inputMuted ? 0 : settings.inputVolume;
    this.setMicroVolume(actualInputVol);

    // Call
    const baseCallVol = settings.callMuted ? 0 : settings.callVolume;
    const actualCallVol = Math.round(baseCallVol * masterScale);
    this.setRemoteVolume(actualCallVol);

    // System
    const baseSysVol = settings.systemMuted ? 0 : settings.systemVolume;
    const actualSysVol = Math.round(baseSysVol * masterScale);
    this.setSystemVolume(actualSysVol);
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

  setMicroVolume(volume: number): void {
    this.currentInputVolume = volume;
    const normalized = Math.max(0, Math.min(100, volume)) / 100;

    // Nếu mic đang test thì áp dụng thay đổi ngay lập tức lên node Gain của mic test
    if (this.micTestGain) {
      this.micTestGain.gain.setValueAtTime(
        normalized,
        this.audioCtx.currentTime,
      );
    }
  }

  setSystemVolume(volume: number): void {
    const normalized = Math.max(0, Math.min(100, volume)) / 100;
    this.masterGain.gain.setValueAtTime(normalized, this.audioCtx.currentTime);
  }

  setRemoteVolume(volume: number): void {
    const normalized = Math.max(0, Math.min(100, volume)) / 100;

    // Duyệt qua tất cả các thẻ audio của remote stream để giảm volume trực tiếp
    for (const el of this.audioElements.values()) {
      el.volume = normalized;
    }

    // Đồng thời áp dụng cho cả thiết bị test loa nếu nó đang chạy
    if (this.speakerTestEl) {
      this.speakerTestEl.volume = normalized;
    }
  }

  async startMicTest(
    deviceId: string,
    onLevel: (level: number) => void,
  ): Promise<void> {
    this.stopMicTest();

    try {
      this.micTestStream = await navigator.mediaDevices.getUserMedia({
        audio: { deviceId: { exact: deviceId } },
      });

      const source = this.audioCtx.createMediaStreamSource(this.micTestStream);

      this.micAnalyser = this.audioCtx.createAnalyser();
      this.micAnalyser.fftSize = 256;

      const delay = this.audioCtx.createDelay(0.5);
      delay.delayTime.value = 0.1;

      this.micTestGain = this.audioCtx.createGain();

      const normalized =
        Math.max(0, Math.min(100, this.currentInputVolume)) / 100;
      this.micTestGain.gain.value = normalized;

      source.connect(this.micTestGain);
      this.micTestGain.connect(this.micAnalyser);

      this.micTestGain.connect(delay);
      delay.connect(this.audioCtx.destination);

      const dataArray = new Uint8Array(this.micAnalyser.frequencyBinCount);
      const tick = () => {
        if (!this.micAnalyser) return; // Bảo vệ an toàn đề phòng bị stop bất chợt
        this.micAnalyser.getByteFrequencyData(dataArray);
        const avg = dataArray.reduce((a, b) => a + b, 0) / dataArray.length;
        onLevel(Math.round(avg));
        this.micTestAnimFrame = requestAnimationFrame(tick);
      };
      tick();
    } catch (err) {
      console.error("[AudioManager] startMicTest lỗi:", err);
    }
  }

  stopMicTest(): void {
    if (this.micTestAnimFrame) {
      cancelAnimationFrame(this.micTestAnimFrame);
      this.micTestAnimFrame = null;
    }
    // Fade out để tránh click sound
    if (this.micTestGain) {
      this.micTestGain.gain.setValueAtTime(
        this.micTestGain.gain.value,
        this.audioCtx.currentTime,
      );
      this.micTestGain.gain.linearRampToValueAtTime(
        0,
        this.audioCtx.currentTime + 0.05,
      );
      this.micTestGain = null;
    }
    if (this.micTestStream) {
      this.micTestStream.getTracks().forEach((t) => t.stop());
      this.micTestStream = null;
    }
    this.micAnalyser = null;
  }

  async testOutput(
    deviceId: string,
    soundUrl: string,
    onEnded?: () => void,
  ): Promise<void> {
    // Dừng cái đang chạy nếu có
    this.stopSpeakerTest();

    const el = document.createElement("audio");

    if (this.masterGain) {
      el.volume = this.masterGain.gain.value;
    }

    el.src = soundUrl;
    el.loop = true;
    el.style.display = "none";
    document.body.appendChild(el);
    this.speakerTestEl = el;

    if (typeof (el as any).setSinkId === "function") {
      try {
        await (el as any).setSinkId(deviceId);
      } catch (err) {
        console.warn("[AudioManager] testOutput setSinkId lỗi:", err);
      }
    }

    el.onended = () => {
      el.remove();
      this.speakerTestEl = null;
      onEnded?.();
    };

    await el.play();
  }

  stopSpeakerTest(): void {
    if (!this.speakerTestEl) return;
    this.speakerTestEl.pause();
    this.speakerTestEl.remove();
    this.speakerTestEl = null;
  }
}

const globalAudio = new AppAudioManager();
export default globalAudio;
