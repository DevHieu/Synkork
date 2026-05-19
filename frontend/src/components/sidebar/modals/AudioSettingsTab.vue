<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from "vue"
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore"
import { Volume2, VolumeX, Mic, Headphones, Bell } from "lucide-vue-next"

import { Slider } from "@/components/ui/slider"
import { Switch } from "@/components/ui/switch"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Separator } from "@/components/ui/separator"
import { Label } from "@/components/ui/label"
import globalAudio from "@/utils/appAudioManager"
import { useLocalStorage } from "@vueuse/core"


const savedInputDevice = useLocalStorage("audio-input-device", "default")
const savedOutputDevice = useLocalStorage("audio-output-device", "default")

const audio = reactive({
  masterVolume: 80,
  masterMuted: false,
  inputVolume: 70,
  inputMuted: false,
  outputVolume: 85,
  notificationVolume: 60,
  notificationMuted: false,

  get inputDevice() { return savedInputDevice.value },
  set inputDevice(val) { savedInputDevice.value = val },

  get outputDevice() { return savedOutputDevice.value },
  set outputDevice(val) { savedOutputDevice.value = val },

  notifySoundNewMessage: true,
  notifySoundRequest: true,
  notifySoundJoin: true,
  notifySoundLeave: false,
})

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
      // ← Nếu device đã lưu không còn tồn tại (bị rút ra), fallback về default
      if (!mics.find(d => d.value === savedInputDevice.value)) {
        savedInputDevice.value = mics[0].value
      }
    }

    const speakers = devices
      .filter(d => d.kind === "audiooutput")
      .map(d => ({ value: d.deviceId, label: d.label || `Speaker (${d.deviceId.slice(0, 5)}...)` }))
    if (speakers.length > 0) {
      outputDevices.value = speakers
      // ← Tương tự cho output
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
  navigator.mediaDevices?.removeEventListener("devicechange", updateDeviceList)
  if (micTestInterval) clearInterval(micTestInterval)
})

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

// const onVolumeChange = (values: number[]) => {
//   if (values.length > 0) {
//     globalAudio.setMasterVolume(values[0])
//   }
// }

const handleOutputDeviceChange = async (deviceId: string) => {
  await globalAudio.changeGlobalOutput(deviceId)
}

const handleInputDeviceChange = (deviceId: string) => {
  console.log("Mic vừa đổi thành:", deviceId)

  useVoiceSpaceStore().changeInputDevice(deviceId)
}
</script>

<template>
  <div class="p-6 space-y-6 text-foreground">

    <!-- MASTER VOLUME -->
    <section class="space-y-3">
      <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
        <Headphones class="w-3.5 h-3.5" />
        <span>Âm lượng tổng</span>
      </div>
      <div class="flex items-center gap-4 bg-muted/40 p-4 rounded-xl">
        <Button variant="ghost" size="icon" class="shrink-0 w-8 h-8" @click="audio.masterMuted = !audio.masterMuted">
          <component :is="getVolumeIcon(audio.masterMuted, audio.masterVolume)" class="w-4 h-4" />
        </Button>
        <Slider :min="0" :max="100" :step="1" :model-value="[audio.masterMuted ? 0 : audio.masterVolume]"
          :disabled="audio.masterMuted" class="flex-1" @update:model-value="(val) => audio.masterVolume = val[0]" />
        <span class="w-10 text-right text-sm font-semibold">
          {{ audio.masterMuted ? '0' : audio.masterVolume }}%
        </span>
      </div>
    </section>

    <Separator />

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

            <Select v-model="audio.inputDevice" @update:model-value="handleInputDeviceChange">
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

            <Select v-model="audio.outputDevice" @update:model-value="handleOutputDeviceChange">
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
              <Volume2 class="w-3.5 h-3.5 text-muted-foreground shrink-0" />
              <Slider :min="0" :max="100" :step="1" :model-value="[audio.outputVolume]" class="flex-1"
                @update:model-value="(val) => audio.outputVolume = val[0]" />
              <span class="text-xs font-medium w-8 text-right">{{ audio.outputVolume }}%</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </section>

    <Separator />

    <!-- NOTIFICATION SOUNDS -->
    <section class="space-y-3">
      <div class="flex items-center gap-2 text-xs font-semibold uppercase tracking-widest text-muted-foreground">
        <Bell class="w-3.5 h-3.5" />
        <span>Âm thanh thông báo</span>
      </div>

      <div class="flex items-center gap-4 bg-muted/40 p-4 rounded-xl">
        <Button variant="ghost" size="icon" class="w-8 h-8 shrink-0"
          @click="audio.notificationMuted = !audio.notificationMuted">
          <component :is="getVolumeIcon(audio.notificationMuted, audio.notificationVolume)" class="w-4 h-4" />
        </Button>
        <Slider :min="0" :max="100" :step="1" :model-value="[audio.notificationMuted ? 0 : audio.notificationVolume]"
          :disabled="audio.notificationMuted" class="flex-1"
          @update:model-value="(val) => audio.notificationVolume = val[0]" />
        <span class="w-10 text-right text-xs font-semibold">
          {{ audio.notificationMuted ? '0' : audio.notificationVolume }}%
        </span>
      </div>

      <div class="grid grid-cols-2 gap-2">
        <div v-for="item in [
          { key: 'notifySoundNewMessage', label: 'Tin nhắn mới' },
          { key: 'notifySoundRequest', label: 'Lời mời kết bạn' },
          { key: 'notifySoundJoin', label: 'Thành viên vào phòng' },
          { key: 'notifySoundLeave', label: 'Thành viên rời phòng' },
        ]" :key="item.key" class="flex items-center justify-between p-3 border rounded-xl">
          <Label class="text-xs text-muted-foreground">{{ item.label }}</Label>
          <Switch :checked="(audio as any)[item.key]" @update:checked="(val) => (audio as any)[item.key] = val" />
        </div>
      </div>
    </section>

  </div>
</template>