<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useRoute, useRouter } from "vue-router"
import axiosClient from "@/lib/axiosClient"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Loader2 } from "lucide-vue-next"

const route = useRoute()
const router = useRouter()

const inviteCode = computed(() => route.params.code as string)

interface InviteInfo {
  id?: string
  roomId?: string
  name?: string
  roomName?: string
  avatarUrl?: string | null
  roomAvatar?: string | null
  memberCount?: number
  roomMembers?: number
  onlineCount?: number
  inviterName?: string
  inviterAvatar?: string | null
}

const getRoomId     = (i: InviteInfo) => i.id        ?? i.roomId   ?? ""
const getRoomName   = (i: InviteInfo) => i.name      ?? i.roomName ?? "Phòng không tên"
const getRoomAvatar = (i: InviteInfo) => i.avatarUrl ?? i.roomAvatar ?? null
const getMemberCount = (i: InviteInfo) => i.memberCount ?? i.roomMembers ?? 0

const inviteInfo  = ref<InviteInfo | null>(null)
const currentUser = ref<any>(null)
const state       = ref<"loading" | "ready" | "joining" | "error" | "invalid">("loading")
const errorMsg    = ref("")

const getInitials = (name?: string) =>
  (name ?? "??")
    .split(/[\s_]+/)
    .map(w => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2)

onMounted(async () => {
  try {
    const infoRes = await axiosClient.get(`/api/rooms/invites/${inviteCode.value}`)
    inviteInfo.value = infoRes.data
    try {
      const meRes = await axiosClient.get("/api/users/me")
      currentUser.value = meRes.data
    } catch { /* chưa đăng nhập */ }
    state.value = "ready"
  } catch (e: any) {
    state.value = e?.response?.status === 404 ? "invalid" : "error"
    errorMsg.value = e?.response?.data?.message || "Không thể tải thông tin lời mời"
  }
})

// Fetch space đầu tiên của room, ưu tiên CHAT
const navigateToRoom = async (roomId: string) => {
  if (!roomId) {
    await router.push("/me")
    return
  }

  try {
    const spacesRes = await axiosClient.get(`/api/rooms/${roomId}/spaces`)
    const spaces: any[] = spacesRes.data ?? []
    const first = spaces.find(s => s.type === "CHAT") ?? spaces[0]
    if (first) {
      const type = (first.type as string).toLowerCase()  // "chat" | "voice" | ...
      await router.push(`/rooms/${type}/${roomId}/${first.id}`)
    } else {
      // Không có space nào — vào room level (fallback)
      await router.push("/me")
    }
  } catch {
    // Nếu API lỗi thì vào room level, RoomLayout tự xử lý
    await router.push("/me")
  }
}

const handleAccept = async () => {
  if (!currentUser.value) {
    // Lưu lại invite URL để sau khi login quay về
    router.push(`/auth?redirect=/invite/${inviteCode.value}`)
    return
  }
  state.value = "joining"
  try {
    const res = await axiosClient.post(`/api/rooms/invites/${inviteCode.value}/join`)
    await navigateToRoom(res.data.id)
  } catch (e: any) {
    const errorData = e?.response?.data
    const msg = typeof errorData === "string" ? errorData : errorData?.message || ""
    if (e?.response?.status === 409 || msg.toLowerCase().includes("already")) {
      // Đã là thành viên → vào luôn
      await navigateToRoom(getRoomId(inviteInfo.value!))
    } else {
      errorMsg.value = msg || "Tham gia thất bại, vui lòng thử lại"
      state.value = "ready"
    }
  }
}
</script>

<template>
  <!-- Background -->
  <div class="invite-bg">
    <div class="orb orb-1" />
    <div class="orb orb-2" />
    <div class="orb orb-3" />
    <div class="stars" />

    <!-- Loading full-screen -->
    <Transition name="fade">
      <div v-if="state === 'loading'" class="absolute inset-0 z-20 flex items-center justify-center">
        <Loader2 class="size-10 text-violet-400 animate-spin" />
      </div>
    </Transition>

    <!-- Card -->
    <Transition name="card-pop" appear>
      <div
        v-if="state !== 'loading'"
        class="invite-card relative z-10 w-[420px] max-w-[calc(100vw-32px)] rounded-2xl border border-white/10
               bg-[rgba(30,25,50,0.82)] backdrop-blur-2xl text-center px-11 py-10
               shadow-[0_0_0_1px_rgba(139,92,246,0.15),0_8px_48px_rgba(0,0,0,0.6)]"
      >

        <!-- INVALID -->
        <template v-if="state === 'invalid'">
          <p class="text-5xl mb-4">💔</p>
          <h2 class="text-xl font-extrabold text-white">Link mời không hợp lệ</h2>
          <p class="mt-2 text-sm text-white/50">Link này đã hết hạn hoặc không tồn tại.</p>
          <Button class="w-full mt-6" @click="router.push('/')">Về trang chủ</Button>
        </template>

        <!-- ERROR -->
        <template v-else-if="state === 'error'">
          <p class="text-5xl mb-4">⚠️</p>
          <h2 class="text-xl font-extrabold text-white">Đã có lỗi xảy ra</h2>
          <p class="mt-2 text-sm text-white/50">{{ errorMsg }}</p>
          <Button class="w-full mt-6" @click="router.push('/')">Về trang chủ</Button>
        </template>

        <!-- READY / JOINING -->
        <template v-else-if="inviteInfo">

          <!-- Inviter avatar -->
          <div class="flex justify-center mb-1">
            <Avatar class="size-[72px] border-2 border-white/15 shadow-[0_4px_20px_rgba(124,58,237,0.45)]">
              <AvatarImage v-if="inviteInfo.inviterAvatar" :src="inviteInfo.inviterAvatar" />
              <AvatarFallback class="bg-gradient-to-br from-violet-600 to-indigo-500 text-white text-lg font-bold">
                {{ getInitials(inviteInfo.inviterName) }}
              </AvatarFallback>
            </Avatar>
          </div>

          <p class="mt-2.5 text-sm text-white/60 leading-relaxed">
            <span class="font-semibold text-white/85">{{ inviteInfo.inviterName }}</span>
            đã mời bạn tham gia
          </p>

          <!-- Room name + avatar -->
          <div class="flex items-center justify-center gap-3 mt-4">
            <Avatar class="size-11 rounded-xl shadow-[0_2px_12px_rgba(79,70,229,0.4)]">
              <AvatarImage v-if="getRoomAvatar(inviteInfo)" :src="getRoomAvatar(inviteInfo)!" />
              <AvatarFallback class="rounded-xl bg-gradient-to-br from-indigo-500 to-violet-600 text-white font-extrabold text-base">
                {{ getInitials(getRoomName(inviteInfo)) }}
              </AvatarFallback>
            </Avatar>
            <h1 class="text-[1.45rem] font-extrabold text-white tracking-tight leading-tight">
              {{ getRoomName(inviteInfo) }}
            </h1>
          </div>

          <!-- Stats badges -->
          <div class="flex items-center justify-center gap-3 mt-3.5">
            <Badge variant="outline" class="gap-1.5 border-white/10 text-white/55 bg-white/5 text-xs">
              <span class="size-2 rounded-full bg-emerald-400 shadow-[0_0_6px_#23d16066]" />
              {{ inviteInfo.onlineCount ?? 0 }} Trực tuyến
            </Badge>
            <Badge variant="outline" class="gap-1.5 border-white/10 text-white/55 bg-white/5 text-xs">
              <span class="size-2 rounded-full bg-white/35" />
              {{ getMemberCount(inviteInfo) }} Thành viên
            </Badge>
          </div>

          <!-- Error message -->
          <Transition name="fade">
            <p
              v-if="errorMsg && state === 'ready'"
              class="mt-3 rounded-lg border border-red-500/25 bg-red-500/10 px-3 py-2 text-xs text-red-400"
            >
              {{ errorMsg }}
            </p>
          </Transition>

          <!-- CTA button -->
          <Button
            class="w-full mt-7 h-11 text-base font-bold
                   bg-gradient-to-r from-violet-700 to-indigo-600
                   hover:from-violet-600 hover:to-indigo-500
                   shadow-[0_4px_20px_rgba(91,33,182,0.5)]
                   hover:shadow-[0_6px_28px_rgba(91,33,182,0.65)]
                   transition-all duration-200 hover:-translate-y-px"
            :disabled="state === 'joining'"
            @click="handleAccept"
          >
            <Loader2 v-if="state === 'joining'" class="size-4 animate-spin" />
            <span v-else-if="!currentUser">Đăng nhập để tham gia</span>
            <span v-else>Chấp nhận lời mời</span>
          </Button>

          <!-- User hint -->
          <p class="mt-3 text-xs text-white/38 leading-relaxed">
            <template v-if="currentUser">
              Đang đăng nhập với tư cách
              <span class="text-white/60 font-semibold">{{ currentUser.displayName || currentUser.username }}</span>
            </template>
            <template v-else>Bạn sẽ được chuyển tới trang đăng nhập.</template>
          </p>

        </template>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.invite-bg {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at 20% 50%, #1a1060 0%, #0d0620 40%, #050210 100%);
  position: relative;
  overflow: hidden;
}

/* Orbs */
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
  animation: float 8s ease-in-out infinite;
}
.orb-1 { width: 420px; height: 420px; background: radial-gradient(circle, #5b21b6, transparent 70%); top: -10%; left: -8%; }
.orb-2 { width: 300px; height: 300px; background: radial-gradient(circle, #1d4ed8, transparent 70%); bottom: -5%; right: 5%; animation-delay: -3s; }
.orb-3 { width: 250px; height: 250px; background: radial-gradient(circle, #7e22ce, transparent 70%); top: 60%; left: 60%; animation-delay: -5s; }

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50%       { transform: translateY(-20px) scale(1.04); }
}

/* Stars */
.stars {
  position: absolute; inset: 0; pointer-events: none;
  background-image:
    radial-gradient(1px 1px at 10% 20%, rgba(255,255,255,0.55) 0%, transparent 100%),
    radial-gradient(1px 1px at 30% 70%, rgba(255,255,255,0.40) 0%, transparent 100%),
    radial-gradient(1.5px 1.5px at 55% 15%, rgba(255,255,255,0.60) 0%, transparent 100%),
    radial-gradient(1px 1px at 70% 45%, rgba(255,255,255,0.35) 0%, transparent 100%),
    radial-gradient(1px 1px at 85% 80%, rgba(255,255,255,0.45) 0%, transparent 100%),
    radial-gradient(1px 1px at 45% 85%, rgba(255,255,255,0.30) 0%, transparent 100%),
    radial-gradient(1.5px 1.5px at 20% 55%, rgba(255,255,255,0.50) 0%, transparent 100%),
    radial-gradient(1px 1px at 90% 30%, rgba(255,255,255,0.40) 0%, transparent 100%);
}

/* Transitions */
.card-pop-enter-active { animation: card-pop-in 0.45s cubic-bezier(0.34, 1.56, 0.64, 1) both; }
@keyframes card-pop-in {
  from { opacity: 0; transform: scale(0.88) translateY(16px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
