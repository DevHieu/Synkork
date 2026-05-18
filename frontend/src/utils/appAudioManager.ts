class AppAudioManager {
  // Định nghĩa kiểu dữ liệu cho thuộc tính trong class
  private audioCtx: AudioContext;
  private masterGain: GainNode;

  constructor() {
    // Dự phòng trường hợp trình duyệt cũ, ép kiểu window về any để lấy webkitAudioContext
    const AudioContextClass =
      window.AudioContext || (window as any).webkitAudioContext;

    // 1. Tạo "bàn mixer" trung tâm cho toàn app
    this.audioCtx = new AudioContextClass();

    // Tạo một GainNode (bộ điều khiển âm lượng tổng của app)
    this.masterGain = this.audioCtx.createGain();
    this.masterGain.connect(this.audioCtx.destination);
  }

  /**
   * Hàm đổi thiết bị đầu ra cho TOÀN BỘ âm thanh đi qua mixer này
   * @param deviceId ID của thiết bị đầu ra (loa/tai nghe) lấy từ enumerateDevices()
   */
  async changeGlobalOutput(deviceId: string): Promise<void> {
    // Kiểm tra tính năng setSinkId có tồn tại trong audioCtx không
    if ("setSinkId" in this.audioCtx) {
      try {
        // Ép kiểu qua 'any' để đánh lừa bộ kiểm tra của TypeScript vì setSinkId là API mới
        await (this.audioCtx as any).setSinkId(deviceId);
        console.log(
          `[AudioManager] Đã đổi thiết bị đầu ra toàn app sang: ${deviceId}`,
        );
      } catch (err) {
        console.error("[AudioManager] Lỗi khi đổi thiết bị đầu ra:", err);
      }
    } else {
      console.warn(
        "[AudioManager] Trình duyệt không hỗ trợ setSinkId trên AudioContext",
      );
    }
  }

  /**
   * Hàm dùng để phát tiếng thông báo hệ thống (Notification, Click...)
   * @param soundUrl Đường dẫn file âm thanh (ví dụ: '/sounds/ting.mp3')
   */
  playSystemSound(soundUrl: string): void {
    fetch(soundUrl)
      .then((res: Response) => res.arrayBuffer())
      .then((data: ArrayBuffer) => this.audioCtx.decodeAudioData(data))
      .then((buffer: AudioBuffer) => {
        const source: AudioBufferSourceNode =
          this.audioCtx.createBufferSource();
        source.buffer = buffer;

        // Cắm dây âm thanh này vào bộ điều khiển tổng
        source.connect(this.masterGain);
        source.start();
      })
      .catch((err: Error) => {
        console.error(
          `[AudioManager] Lỗi không thể phát âm thanh từ url: ${soundUrl}`,
          err,
        );
      });
  }

  /**
   * Hàm dùng để cắm luồng WebRTC cuộc gọi vào mixer tổng
   * @param mediaStream Luồng stream nhận từ WebRTC (hoặc từ Zego)
   */
  connectRemoteStream(mediaStream: MediaStream): void {
    // Biến cái stream nhận từ WebRTC thành một Node trong Web Audio
    const source: MediaStreamAudioSourceNode =
      this.audioCtx.createMediaStreamSource(mediaStream);

    // Cắm vào bộ điều khiển tổng
    source.connect(this.masterGain);
  }

  /**
   * Tính năng tặng thêm: Thay đổi âm lượng tổng (Master Volume)
   * @param volume Giá trị từ 0 đến 100
   */
  setMasterVolume(volume: number): void {
    // Giới hạn giá trị trong khoảng 0 -> 100
    const normalizedVolume = Math.max(0, Math.min(100, volume)) / 100;
    // Thay đổi âm lượng mượt mà bằng API của Web Audio
    this.masterGain.gain.setValueAtTime(
      normalizedVolume,
      this.audioCtx.currentTime,
    );
  }
}

// Khởi tạo một instance duy nhất (Singleton) để dùng xuyên suốt dự án
const globalAudio = new AppAudioManager();
export default globalAudio;
