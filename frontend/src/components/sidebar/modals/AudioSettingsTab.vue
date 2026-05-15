<script setup lang="ts">
import { ref, reactive } from "vue"
import { Volume2, VolumeX, Mic, Headphones, Bell, Music } from "lucide-vue-next"

const audio = reactive({
  masterVolume: 80,
  masterMuted: false,
  inputVolume: 70,
  inputMuted: false,
  outputVolume: 85,
  notificationVolume: 60,
  notificationMuted: false,

  inputDevice: "default",
  outputDevice: "default",

  echoCancellation: true,
  noiseSuppression: true,
  autoGainControl: true,

  notifySoundNewMessage: true,
  notifySoundRequest: true,
  notifySoundJoin: true,
  notifySoundLeave: false,
})

const inputDevices = [
  { value: "default", label: "Mặc định (Microphone tích hợp)" },
  { value: "mic1", label: "Headset Microphone" },
]

const outputDevices = [
  { value: "default", label: "Mặc định (Loa tích hợp)" },
  { value: "speaker1", label: "Headphone (ASUS ROG)" },
  { value: "speaker2", label: "External Monitor" },
]

const isMicTesting = ref(false)
const micLevel = ref(0)
let micTestInterval: any = null

const toggleMicTest = () => {
  isMicTesting.value = !isMicTesting.value
  if (isMicTesting.value) {
    micTestInterval = setInterval(() => {
      micLevel.value = Math.floor(Math.random() * 70 + 10)
    }, 150)
  } else {
    clearInterval(micTestInterval)
    micLevel.value = 0
  }
}

const getVolumeIcon = (muted: boolean, vol: number) => {
  return muted || vol === 0 ? VolumeX : Volume2
}
</script>

<template>
  <div class="audio-root">

    <!-- MASTER VOLUME -->
    <section class="audio-section">
      <div class="section-header">
        <Headphones class="section-icon" />
        <span>Âm lượng tổng</span>
      </div>
      <div class="vol-row">
        <button class="mute-btn" :class="{ muted: audio.masterMuted }" @click="audio.masterMuted = !audio.masterMuted" :title="audio.masterMuted ? 'Bỏ tắt tiếng' : 'Tắt tiếng'">
          <component :is="getVolumeIcon(audio.masterMuted, audio.masterVolume)" class="vol-icon" />
        </button>
        <input type="range" min="0" max="100" step="1" v-model.number="audio.masterVolume" class="vol-slider" :disabled="audio.masterMuted" />
        <span class="vol-label">{{ audio.masterMuted ? '0' : audio.masterVolume }}%</span>
      </div>
    </section>

    <div class="divider" />

    <!-- INPUT / OUTPUT -->
    <section class="audio-section">
      <div class="section-header">
        <Mic class="section-icon" />
        <span>Thiết bị đầu vào / đầu ra</span>
      </div>

      <div class="device-grid">
        <!-- Mic -->
        <div class="device-card">
          <div class="device-label">
            <Mic class="device-icon" />
            Microphone
          </div>
          <select v-model="audio.inputDevice" class="device-select">
            <option v-for="d in inputDevices" :key="d.value" :value="d.value">{{ d.label }}</option>
          </select>
          <div class="vol-row mt-2">
            <button class="mute-btn small" :class="{ muted: audio.inputMuted }" @click="audio.inputMuted = !audio.inputMuted">
              <component :is="getVolumeIcon(audio.inputMuted, audio.inputVolume)" class="vol-icon-sm" />
            </button>
            <input type="range" min="0" max="100" step="1" v-model.number="audio.inputVolume" class="vol-slider" :disabled="audio.inputMuted" />
            <span class="vol-label">{{ audio.inputMuted ? '0' : audio.inputVolume }}%</span>
          </div>

          <!-- Mic test -->
          <div class="mic-test-row">
            <button class="mic-test-btn" :class="{ active: isMicTesting }" @click="toggleMicTest">
              {{ isMicTesting ? 'Dừng kiểm tra' : 'Kiểm tra mic' }}
            </button>
            <div v-if="isMicTesting" class="mic-bar-wrapper">
              <div class="mic-bar" :style="{ width: micLevel + '%' }" />
            </div>
          </div>
        </div>

        <!-- Speaker -->
        <div class="device-card">
          <div class="device-label">
            <Volume2 class="device-icon" />
            Loa / Tai nghe
          </div>
          <select v-model="audio.outputDevice" class="device-select">
            <option v-for="d in outputDevices" :key="d.value" :value="d.value">{{ d.label }}</option>
          </select>
          <div class="vol-row mt-2">
            <Volume2 class="vol-icon-sm text-muted-foreground" />
            <input type="range" min="0" max="100" step="1" v-model.number="audio.outputVolume" class="vol-slider" />
            <span class="vol-label">{{ audio.outputVolume }}%</span>
          </div>
        </div>
      </div>
    </section>

    <div class="divider" />

    <!-- VOICE PROCESSING -->
    <section class="audio-section">
      <div class="section-header">
        <Mic class="section-icon" />
        <span>Xử lý giọng nói</span>
      </div>
      <div class="toggle-list">
        <div class="toggle-item" v-for="item in [
          { key: 'echoCancellation', label: 'Khử tiếng vang', desc: 'Giảm tiếng vọng trong cuộc gọi' },
          { key: 'noiseSuppression', label: 'Khử tiếng ồn', desc: 'Lọc âm thanh nền không mong muốn' },
          { key: 'autoGainControl', label: 'Tự động điều chỉnh âm lượng', desc: 'Cân bằng mức âm lượng mic tự động' },
        ]" :key="item.key">
          <div class="toggle-text">
            <span class="toggle-label">{{ item.label }}</span>
            <span class="toggle-desc">{{ item.desc }}</span>
          </div>
          <button class="toggle-switch" :class="{ on: (audio as any)[item.key] }" @click="(audio as any)[item.key] = !(audio as any)[item.key]">
            <span class="toggle-thumb" />
          </button>
        </div>
      </div>
    </section>

    <div class="divider" />

    <!-- NOTIFICATION SOUNDS -->
    <section class="audio-section">
      <div class="section-header">
        <Bell class="section-icon" />
        <span>Âm thanh thông báo</span>
      </div>
      <div class="vol-row mb-3">
        <button class="mute-btn small" :class="{ muted: audio.notificationMuted }" @click="audio.notificationMuted = !audio.notificationMuted">
          <component :is="getVolumeIcon(audio.notificationMuted, audio.notificationVolume)" class="vol-icon-sm" />
        </button>
        <input type="range" min="0" max="100" step="1" v-model.number="audio.notificationVolume" class="vol-slider" :disabled="audio.notificationMuted" />
        <span class="vol-label">{{ audio.notificationMuted ? '0' : audio.notificationVolume }}%</span>
      </div>

      <div class="toggle-list">
        <div class="toggle-item" v-for="item in [
          { key: 'notifySoundNewMessage', label: 'Tin nhắn mới' },
          { key: 'notifySoundRequest', label: 'Lời mời kết bạn' },
          { key: 'notifySoundJoin', label: 'Thành viên vào phòng' },
          { key: 'notifySoundLeave', label: 'Thành viên rời phòng' },
        ]" :key="item.key">
          <div class="toggle-text">
            <span class="toggle-label">{{ item.label }}</span>
          </div>
          <button class="toggle-switch small" :class="{ on: (audio as any)[item.key] }" @click="(audio as any)[item.key] = !(audio as any)[item.key]">
            <span class="toggle-thumb" />
          </button>
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
.audio-root { padding: 0.25rem 0 2rem; }

.audio-section { margin-bottom: 0.25rem; }

.section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--muted-foreground);
  margin-bottom: 0.75rem;
}

.section-icon { width: 13px; height: 13px; }

.divider {
  height: 1px;
  background: var(--border);
  opacity: 0.4;
  margin: 1.25rem 0;
}

/* Volume Row */
.vol-row {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.mt-2 { margin-top: 0.6rem; }
.mb-3 { margin-bottom: 0.85rem; }

.mute-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent);
  color: var(--foreground);
  transition: background 0.12s, color 0.12s;
  flex-shrink: 0;
  border: none;
  cursor: pointer;
}

.mute-btn.muted { background: var(--destructive); color: white; }
.mute-btn.small { width: 26px; height: 26px; border-radius: 5px; }

.vol-icon { width: 15px; height: 15px; }
.vol-icon-sm { width: 13px; height: 13px; }

.vol-slider {
  flex: 1;
  height: 4px;
  border-radius: 999px;
  accent-color: var(--primary);
  cursor: pointer;
}

.vol-slider:disabled { opacity: 0.4; cursor: not-allowed; }

.vol-label {
  font-size: 0.78rem;
  color: var(--muted-foreground);
  min-width: 32px;
  text-align: right;
}

/* Device */
.device-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.85rem;
}

.device-card {
  background: var(--muted);
  border-radius: 8px;
  padding: 0.85rem;
}

.device-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--foreground);
  margin-bottom: 0.5rem;
}

.device-icon { width: 12px; height: 12px; color: var(--muted-foreground); }

.device-select {
  width: 100%;
  background: var(--background);
  border: 1px solid var(--border);
  border-radius: 5px;
  padding: 0.3rem 0.5rem;
  font-size: 0.75rem;
  color: var(--foreground);
  outline: none;
  transition: border-color 0.15s;
  cursor: pointer;
}

.device-select:focus { border-color: var(--primary); }

/* Mic test */
.mic-test-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.6rem;
}

.mic-test-btn {
  font-size: 0.72rem;
  padding: 0.25rem 0.65rem;
  border-radius: 4px;
  border: 1px solid var(--border);
  background: var(--accent);
  color: var(--foreground);
  cursor: pointer;
  transition: all 0.12s;
  white-space: nowrap;
  flex-shrink: 0;
}

.mic-test-btn.active { background: var(--primary); color: white; border-color: var(--primary); }

.mic-bar-wrapper {
  flex: 1;
  height: 6px;
  background: var(--border);
  border-radius: 999px;
  overflow: hidden;
}

.mic-bar {
  height: 100%;
  background: var(--primary);
  border-radius: 999px;
  transition: width 0.1s ease;
}

/* Toggle List */
.toggle-list { display: flex; flex-direction: column; gap: 0; }

.toggle-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.6rem 0;
  border-bottom: 1px solid var(--border);
  opacity: 0.9;
}

.toggle-item:last-child { border-bottom: none; }

.toggle-text {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.toggle-label { font-size: 0.84rem; color: var(--foreground); }
.toggle-desc { font-size: 0.72rem; color: var(--muted-foreground); }

/* Toggle Switch */
.toggle-switch {
  width: 38px;
  height: 22px;
  border-radius: 999px;
  background: var(--border);
  position: relative;
  cursor: pointer;
  border: none;
  transition: background 0.2s;
  flex-shrink: 0;
}

.toggle-switch.on { background: var(--primary); }

.toggle-switch.small { width: 32px; height: 18px; }

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: white;
  transition: transform 0.2s;
  display: block;
}

.toggle-switch.on .toggle-thumb { transform: translateX(16px); }
.toggle-switch.small .toggle-thumb { width: 14px; height: 14px; }
.toggle-switch.small.on .toggle-thumb { transform: translateX(14px); }
</style>