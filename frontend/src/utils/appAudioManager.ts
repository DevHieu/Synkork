class AppAudioManager {
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

      // Delay node để tránh echo feedback
      const delay = this.audioCtx.createDelay(0.5);
      delay.delayTime.value = 0.1;

      // Gain riêng cho mic test để tắt được
      this.micTestGain = this.audioCtx.createGain();
      this.micTestGain.gain.value = 1;

      // source → analyser (đọc level) → delay → gain → destination (nghe tiếng)
      source.connect(this.micAnalyser);
      source.connect(delay);
      delay.connect(this.micTestGain);
      this.micTestGain.connect(this.audioCtx.destination);

      const dataArray = new Uint8Array(this.micAnalyser.frequencyBinCount);
      const tick = () => {
        this.micAnalyser!.getByteFrequencyData(dataArray);
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
    el.src = soundUrl;
    el.loop = true; // Loop để không tự kết thúc, dừng bằng stopSpeakerTest
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
