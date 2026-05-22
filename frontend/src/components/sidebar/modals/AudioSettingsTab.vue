<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from "vue"
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore"
import { Volume2, VolumeX, Mic, Headphones, Bell } from "lucide-vue-next"

import { Slider } from "@/components/ui/slider"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Separator } from "@/components/ui/separator"
import { Label } from "@/components/ui/label"
import globalAudio from "@/utils/appAudioManager"
import { useLocalStorage } from "@vueuse/core"

const savedInputDevice = useLocalStorage("audio-input-device", "default")
const savedOutputDevice = useLocalStorage("audio-output-device", "default")

// Set mặc định thì như thế này, load từ local storage lên thì khác
const audio = useLocalStorage("app-audio-settings", {
  inputVolume: 70,
  inputMuted: false,

  outputVolume: 100,
  outputMuted: false,

  callVolume: 90,
  callMuted: false,

  systemVolume: 75,
  systemMuted: false,
})

// Tự động đồng bộ âm thanh xuống globalAudio khi có bất kỳ thay đổi nào (Kéo slider / Bấm mute)
watch(
  () => audio.value,
  (newSettings) => {
    globalAudio.syncAudioSettings(newSettings)
  },
  { deep: true }
)

const getVolumeIcon = (muted: boolean, vol: number) => {
  return muted || vol === 0 ? VolumeX : Volume2
}

interface DeviceOption {
  value: string
  label: string
}
const inputDevices = ref<DeviceOption[]>([{ value: "default", label: "Mặc định (Microphone)" }])
const outputDevices = ref<DeviceOption[]>([{ value: "default", label: "Mặc định (Loa/Tai nghe)" }])

const updateDeviceList = async () => {
  try {
    await navigator.mediaDevices.getUserMedia({ audio: true }).catch(() => {
      console.warn("User từ chối cấp quyền mic")
    })

    const devices = await navigator.mediaDevices.enumerateDevices()

    const mics = devices
      .filter(d => d.kind === "audioinput")
      .map(d => ({ value: d.deviceId, label: d.label || `Microphone (${d.deviceId.slice(0, 5)}...)` }))
    if (mics.length > 0) {
      inputDevices.value = mics
      if (!mics.find(d => d.value === savedInputDevice.value)) {
        savedInputDevice.value = mics[0].value
      }
    }

    const speakers = devices
      .filter(d => d.kind === "audiooutput")
      .map(d => ({ value: d.deviceId, label: d.label || `Speaker (${d.deviceId.slice(0, 5)}...)` }))
    if (speakers.length > 0) {
      outputDevices.value = speakers
      if (!speakers.find(d => d.value === savedOutputDevice.value)) {
        savedOutputDevice.value = speakers[0].value
      }
    }

  } catch (error) {
    console.error("Lỗi khi lấy danh sách thiết bị âm thanh:", error)
  }
}

onMounted(() => {
  updateDeviceList()
  navigator.mediaDevices?.addEventListener("devicechange", updateDeviceList)
})

onUnmounted(() => {
  globalAudio.stopMicTest()
  globalAudio.stopSpeakerTest()
  navigator.mediaDevices?.removeEventListener("devicechange", updateDeviceList)
})

const isMicTesting = ref(false)
const micLevel = ref(0)
const toggleMicTest = async () => {
  if (isMicTesting.value) {
    globalAudio.stopMicTest()
    isMicTesting.value = false
    micLevel.value = 0
  } else {
    isMicTesting.value = true
    await globalAudio.startMicTest(savedInputDevice.value, (level) => {
      micLevel.value = Math.round((level / 255) * 100)
    })
  }
}

const isSpeakerTesting = ref(false)
const toggleSpeakerTest = async () => {
  if (isSpeakerTesting.value) {
    globalAudio.stopSpeakerTest()
    isSpeakerTesting.value = false
  } else {
    isSpeakerTesting.value = true
    await globalAudio.testOutput(savedOutputDevice.value, "/assets/sounds/outputTest.mp3", () => {
      isSpeakerTesting.value = false
    })
  }
}

const handleOutputDeviceChange = async (deviceId: string) => {
  isSpeakerTesting.value = false
  await globalAudio.stopSpeakerTest();
  await globalAudio.changeGlobalOutput(deviceId)
}

const handleInputDeviceChange = (deviceId: string) => {
  isMicTesting.value = false
  micLevel.value = 0
  globalAudio.stopMicTest();
  useVoiceSpaceStore().changeInputDevice(deviceId)
}
</script>

<template>
  <div class="p-6 space-y-6 text-foreground">
    <!-- INPUT / OUTPUT -->
    <section class="space-y-4">
      <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
        <Mic class="w-3.5 h-3.5" />
        <span>Thiết bị đầu vào / đầu ra</span>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <!-- Microphone Card -->
        <Card>
          <CardContent class="p-4 space-y-3">
            <div class="flex items-center gap-2">
              <Mic class="w-3.5 h-3.5 text-primary" />
              <Label class="text-sm font-semibold">Microphone đầu vào</Label>
            </div>

            <Select v-model="savedInputDevice" @update:model-value="handleInputDeviceChange">
              <SelectTrigger class="w-full h-8 text-xs">
                <SelectValue placeholder="Chọn Microphone" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem v-for="d in inputDevices" :key="d.value" :value="d.value" class="text-xs">
                  {{ d.label }}
                </SelectItem>
              </SelectContent>
            </Select>

            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" class="w-7 h-7 shrink-0"
                @click="audio.inputMuted = !audio.inputMuted">
                <component :is="getVolumeIcon(audio.inputMuted, audio.inputVolume)" class="w-3.5 h-3.5" />
              </Button>
              <Slider :min="0" :max="100" :step="1" :model-value="[audio.inputMuted ? 0 : audio.inputVolume]"
                :disabled="audio.inputMuted" class="flex-1" @update:model-value="(val) => audio.inputVolume = val[0]" />
              <span class="text-xs font-medium w-8 text-right">
                {{ audio.inputMuted ? '0' : audio.inputVolume }}%
              </span>
            </div>

            <!-- Mic test -->
            <div class="space-y-2 pt-1">
              <Button variant="secondary" size="sm" class="w-full text-xs h-7"
                :class="{ 'bg-destructive text-destructive-foreground hover:bg-destructive/90': isMicTesting }"
                @click="toggleMicTest">
                {{ isMicTesting ? 'Dừng kiểm tra' : 'Kiểm tra mic' }}
              </Button>
              <div class="h-1.5 w-full bg-muted rounded-full overflow-hidden">
                <div class="h-full bg-primary transition-all duration-100 ease-out"
                  :style="{ width: micLevel + '%' }" />
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- Speaker Card -->
        <Card>
          <CardContent class="p-4 space-y-3">
            <div class="flex items-center gap-2">
              <Volume2 class="w-3.5 h-3.5 text-primary" />
              <Label class="text-sm font-semibold">Loa / Tai nghe đầu ra</Label>
            </div>

            <Select v-model="savedOutputDevice" @update:model-value="handleOutputDeviceChange">
              <SelectTrigger class="w-full h-8 text-xs">
                <SelectValue placeholder="Chọn thiết bị đầu ra" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem v-for="d in outputDevices" :key="d.value" :value="d.value" class="text-xs">
                  {{ d.label }}
                </SelectItem>
              </SelectContent>
            </Select>
            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" class="w-7 h-7 shrink-0"
                @click="audio.outputMuted = !audio.outputMuted">
                <component :is="getVolumeIcon(audio.outputMuted, audio.outputVolume)" class="w-3.5 h-3.5" />
              </Button>
              <Slider :min="0" :max="100" :step="1" :model-value="[audio.outputMuted ? 0 : audio.outputVolume]"
                :disabled="audio.outputMuted" class="flex-1"
                @update:model-value="(val) => audio.outputVolume = val[0]" />
              <span class="text-xs font-medium w-8 text-right">
                {{ audio.outputMuted ? '0' : audio.outputVolume }}%
              </span>
            </div>

            <!-- Speaker test -->
            <div class="space-y-2 pt-1">
              <Button variant="secondary" size="sm" class="w-full text-xs h-7"
                :class="{ 'bg-destructive text-destructive-foreground hover:bg-destructive/90': isSpeakerTesting }"
                @click="toggleSpeakerTest">
                {{ isSpeakerTesting ? 'Dừng kiểm tra' : 'Kiểm tra loa' }}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </section>

    <Separator />

    <!-- CALL & SYSTEM VOLUME -->
    <section class="space-y-4">
      <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
        <Bell class="w-3.5 h-3.5" />
        <span>Âm lượng chi tiết</span>
      </div>

      <div class="grid grid-cols-1 gap-4">
        <!-- Call Volume -->
        <Card>
          <CardContent class="p-4 space-y-3">
            <div class="flex items-center gap-2">
              <Headphones class="w-3.5 h-3.5 text-primary" />
              <Label class="text-sm font-semibold">
                Âm thanh cuộc gọi
              </Label>
            </div>

            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" class="w-7 h-7 shrink-0"
                @click="audio.callMuted = !audio.callMuted">
                <component :is="getVolumeIcon(audio.callMuted, audio.callVolume)" class="w-3.5 h-3.5" />
              </Button>

              <Slider :min="0" :max="100" :step="1" :model-value="[audio.callMuted ? 0 : audio.callVolume]"
                :disabled="audio.callMuted" class="flex-1" @update:model-value="(val) => audio.callVolume = val[0]" />

              <span class="text-xs font-medium w-8 text-right">
                {{ audio.callMuted ? '0' : audio.callVolume }}%
              </span>
            </div>
          </CardContent>
        </Card>

        <!-- System Volume -->
        <Card>
          <CardContent class="p-4 space-y-3">
            <div class="flex items-center gap-2">
              <Bell class="w-3.5 h-3.5 text-primary" />
              <Label class="text-sm font-semibold">
                Âm thanh hệ thống
              </Label>
            </div>

            <div class="flex items-center gap-3">
              <Button variant="outline" size="icon" class="w-7 h-7 shrink-0"
                @click="audio.systemMuted = !audio.systemMuted">
                <component :is="getVolumeIcon(audio.systemMuted, audio.systemVolume)" class="w-3.5 h-3.5" />
              </Button>

              <Slider :min="0" :max="100" :step="1" :model-value="[audio.systemMuted ? 0 : audio.systemVolume]"
                :disabled="audio.systemMuted" class="flex-1"
                @update:model-value="(val) => audio.systemVolume = val[0]" />

              <span class="text-xs font-medium w-8 text-right">
                {{ audio.systemMuted ? '0' : audio.systemVolume }}%
              </span>
            </div>
          </CardContent>
        </Card>
      </div>
    </section>
  </div>
</template>