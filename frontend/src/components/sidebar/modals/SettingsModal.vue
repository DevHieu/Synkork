<script setup lang="ts">
import {
  LogOut, Sparkles, X, User, Pencil,
  Eye, EyeOff, Check, AlertCircle, Volume2, Palette
} from "lucide-vue-next"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { Badge } from "@/components/ui/badge"
import { logout } from "@/services/authService"
import { userService } from "@/services/userService"
import { useUserStore } from "@/stores/userStore"
import { ref, reactive, computed, onMounted, onUnmounted } from "vue"
import AudioSettingsTab from "@/components/sidebar/modals/AudioSettingsTab.vue"
import ThemeSettingsTab from "@/components/sidebar/modals/ThemeSettingsTab.vue"

const emit = defineEmits<{ close: [] }>()

const userStore = useUserStore()
const currentUser = computed(() => userStore.user)
const activeTab = ref("account")
const avatarInput = ref<HTMLInputElement | null>(null)
const avatarLoading = ref(false)
const avatarError = ref("")

const settingsTabs = [
  {
    group: "Cài đặt người dùng",
    items: [{ id: "account", label: "Tài Khoản Của Tôi", icon: "User" }],
  },
  {
    group: "Giao Diện & Âm Thanh",
    items: [
      { id: "theme", label: "Giao Diện & Màu Sắc", icon: "Palette" },
      { id: "audio", label: "Âm Thanh & Giọng Nói", icon: "Volume2" },
    ],
  },
  {
    group: "Cài Đặt Thanh Toán",
    items: [{ id: "billing", label: "Nitro / Pro", icon: "Sparkles" }],
  },
]

const navIcons: Record<string, any> = { User, Palette, Volume2, Sparkles }

// ── OAuth check ────────────────────────────────────────────
const isOAuth = computed(() => {
  const p = currentUser.value?.provider
  return p && p !== "LOCAL"
})
const showChangePasswordForm = computed(() => !isOAuth.value || currentUser.value?.hasPassword)

const providerLabel = computed(() => {
  const map: Record<string, string> = { GOOGLE: "Google", FACEBOOK: "Facebook", GITHUB: "GitHub" }
  return map[currentUser.value?.provider] ?? currentUser.value?.provider ?? ""
})

// ── Edit states ────────────────────────────────────────────
const editingField = ref<string | null>(null)
const editValues = reactive({ displayName: "", username: "" })
const editError = ref("")
const editSuccess = ref("")
const editLoading = ref(false)

function startEdit(field: string) {
  editingField.value = field
  editError.value = ""
  editSuccess.value = ""
  if (field === "displayName") editValues.displayName = currentUser.value?.displayName ?? ""
  if (field === "username") editValues.username = currentUser.value?.username ?? ""
}

function cancelEdit() {
  editingField.value = null
  editError.value = ""
}

async function saveEdit(field: string) {
  editLoading.value = true
  editError.value = ""
  try {
    if (field === "displayName") await userService.updateProfile({ displayName: editValues.displayName })
    else if (field === "username") await userService.updateProfile({ username: editValues.username })
    await userStore.getUserInfo()
    editSuccess.value = "Đã lưu thành công"
    editingField.value = null
    setTimeout(() => (editSuccess.value = ""), 2500)
  } catch (e: any) {
    editError.value = e?.response?.data || e?.message || "Lỗi khi lưu"
  } finally {
    editLoading.value = false
  }
}

// ── Change password (LOCAL) ────────────────────────────────
const pwForm = reactive({ current: "", next: "", confirm: "" })
const showPw = reactive({ current: false, next: false, confirm: false })
const pwError = ref("")
const pwSuccess = ref("")
const pwLoading = ref(false)

async function submitPw() {
  pwError.value = ""
  if (pwForm.next !== pwForm.confirm) { pwError.value = "Mật khẩu mới không khớp"; return }
  if (pwForm.next.length < 6) { pwError.value = "Ít nhất 6 ký tự"; return }
  pwLoading.value = true
  try {
    await userService.changePassword({ currentPassword: pwForm.current, newPassword: pwForm.next })
    pwSuccess.value = "Đổi mật khẩu thành công!"
    pwForm.current = pwForm.next = pwForm.confirm = ""
    setTimeout(() => (pwSuccess.value = ""), 3000)
  } catch (e: any) {
    pwError.value = e?.response?.data || e?.message || "Thất bại"
  } finally {
    pwLoading.value = false
  }
}

// ── Create password (OAuth) ────────────────────────────────
const createPwForm = reactive({ next: "", confirm: "" })
const showCreatePw = reactive({ next: false, confirm: false })
const createPwError = ref("")
const createPwSuccess = ref("")
const createPwLoading = ref(false)

async function submitCreatePw() {
  createPwError.value = ""
  if (createPwForm.next !== createPwForm.confirm) { createPwError.value = "Mật khẩu xác nhận không khớp"; return }
  if (createPwForm.next.length < 6) { createPwError.value = "Ít nhất 6 ký tự"; return }
  createPwLoading.value = true
  try {
    await userService.createPassword({ newPassword: createPwForm.next })
    createPwForm.next = createPwForm.confirm = ""
    await userStore.getUserInfo()
    pwSuccess.value = "Tạo mật khẩu thành công! Bạn có thể đổi mật khẩu bên dưới."
    setTimeout(() => (pwSuccess.value = ""), 4000)
  } catch (e: any) {
    createPwError.value = e?.response?.data || e?.message || "Thất bại"
  } finally {
    createPwLoading.value = false
  }
}

// ── Email mask ─────────────────────────────────────────────
const showEmail = ref(false)
const maskedEmail = computed(() =>
  (currentUser.value?.email ?? "").replace(/(.{2})[^@]+(@.+)/, "$1***$2")
)
const displayName = computed(() =>
  currentUser.value?.displayName || currentUser.value?.username || "—"
)

// ── ESC ───────────────────────────────────────────────────
function chooseAvatar() {
  if (!avatarLoading.value) avatarInput.value?.click()
}

async function uploadAvatar(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ""
  if (!file) return

  avatarError.value = ""
  if (!file.type.startsWith("image/")) {
    avatarError.value = "Vui lòng chọn một tệp ảnh"
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    avatarError.value = "Ảnh phải nhỏ hơn 5 MB"
    return
  }

  avatarLoading.value = true
  try {
    userStore.user = await userService.uploadAvatar(file)
  } catch (e: any) {
    avatarError.value = e?.response?.data || e?.message || "Không thể tải ảnh lên"
  } finally {
    avatarLoading.value = false
  }
}

function onKeydown(e: KeyboardEvent) { if (e.key === "Escape") emit("close") }
onMounted(() => document.addEventListener("keydown", onKeydown))
onUnmounted(() => document.removeEventListener("keydown", onKeydown))
</script>

<template>
  <Teleport to="body">
    <input ref="avatarInput" type="file" accept="image/*" class="hidden" @change="uploadAvatar" />
    <Transition name="sf">
      <div class="fixed inset-0 z-20 flex items-center justify-center bg-black/75" @click.self="emit('close')">
        <div class="flex w-[min(960px,96vw)] h-[min(660px,95vh)] overflow-hidden rounded-lg shadow-2xl">

          <!-- ═══ LEFT SIDEBAR ═══ -->
          <div class="w-[238px] min-w-[238px] flex flex-col overflow-y-auto overflow-x-hidden pt-14 bg-muted">

            <!-- User mini card -->
            <div class="px-4 pb-3">
              <button type="button" class="relative cursor-pointer group/ava w-fit disabled:cursor-wait"
                :disabled="avatarLoading" aria-label="Thay ảnh đại diện" @click="chooseAvatar">
                <Avatar class="w-[68px] h-[68px] border-2 border-muted">
                  <AvatarImage v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" />
                  <AvatarFallback class="bg-primary text-primary-foreground text-2xl font-bold">
                    {{ displayName.charAt(0).toUpperCase() }}
                  </AvatarFallback>
                </Avatar>
                <div
                  class="absolute inset-0 rounded-full flex items-center justify-center bg-black/50 opacity-0 group-hover/ava:opacity-100 transition-opacity">
                  <Pencil class="size-4 text-white" />
                </div>
              </button>
              <p v-if="avatarError" class="mt-1 text-[11px] text-destructive">{{ avatarError }}</p>
              <div class="mt-2">
                <p class="text-sm font-bold text-foreground">{{ displayName }}</p>
                <button
                  class="flex items-center gap-1 text-[11px] text-muted-foreground hover:text-foreground transition-colors mt-0.5">
                  <Pencil class="size-2.5" /> Sửa Hồ Sơ
                </button>
              </div>
            </div>

            <Separator class="my-2 mx-4 w-auto opacity-40" />

            <!-- Nav -->
            <div v-for="group in settingsTabs" :key="group.group" class="px-2 mb-4">
              <p class="px-2 pb-1 text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                {{ group.group }}
              </p>
              <button v-for="item in group.items" :key="item.id"
                class="flex items-center w-full text-left px-3 py-[7px] rounded-md text-sm font-medium transition-all"
                :class="activeTab === item.id
                  ? 'bg-primary text-primary-foreground'
                  : 'text-foreground/80 hover:bg-accent hover:text-foreground'" @click="activeTab = item.id">
                <component :is="navIcons[item.icon]" class="size-[14px] mr-2 opacity-75 shrink-0" />
                {{ item.label }}
              </button>
            </div>

            <Separator class="mt-auto mx-4 w-auto opacity-40" />
            <div class="px-2 py-4">
              <button
                class="flex items-center w-full px-3 py-[7px] rounded-md text-sm font-medium text-destructive hover:bg-destructive/10 transition-colors"
                @click="logout">
                <LogOut class="size-3.5 mr-2" /> Đăng xuất
              </button>
            </div>
          </div>

          <!-- ═══ RIGHT CONTENT ═══ -->
          <div class="flex-1 flex flex-col bg-card overflow-hidden">

            <!-- Topbar -->
            <div class="flex items-center justify-between px-7 pt-5 pb-3 shrink-0">
              <h2 class="text-base font-bold text-foreground">
                <template v-if="activeTab === 'account'">Tài Khoản Của Tôi</template>
                <template v-else>{{settingsTabs.flatMap(g => g.items).find(i => i.id === activeTab)?.label
                }}</template>
              </h2>
              <Button variant="ghost" size="sm" class="gap-1.5 text-muted-foreground text-[11px]"
                @click="emit('close')">
                <X class="size-[18px]" />
                <span class="font-bold tracking-wide">ESC</span>
              </Button>
            </div>

            <!-- Body -->
            <div class="flex-1 overflow-y-auto px-8 pb-12 scrollbar-thin scrollbar-thumb-border">

              <!-- ──── ACCOUNT ──── -->
              <template v-if="activeTab === 'account'">

                <!-- Profile card -->
                <div class="rounded-xl overflow-hidden bg-muted mb-5">
                  <div class="h-[90px] bg-gradient-to-br from-primary to-secondary" />
                  <div class="flex items-end px-4 -mt-10 mb-3">
                    <button type="button" class="relative cursor-pointer group/ava2 disabled:cursor-wait"
                      :disabled="avatarLoading" aria-label="Thay ảnh đại diện" @click="chooseAvatar">
                      <Avatar class="w-20 h-20 border-4 border-card">
                        <AvatarImage v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" />
                        <AvatarFallback class="bg-primary text-primary-foreground text-3xl font-bold">
                          {{ displayName.charAt(0).toUpperCase() }}
                        </AvatarFallback>
                      </Avatar>
                      <div
                        class="absolute inset-0 rounded-full flex items-center justify-center bg-black/50 opacity-0 group-hover/ava2:opacity-100 transition-opacity">
                        <Pencil class="size-5 text-white" />
                      </div>
                    </button>
                  </div>
                  <p class="px-4 pb-3 text-sm font-bold text-foreground">{{ displayName }}</p>
                  <p v-if="avatarError" class="px-4 pb-3 text-xs text-destructive">{{ avatarError }}</p>
                </div>

                <!-- Success toast -->
                <Transition name="toast">
                  <div v-if="editSuccess"
                    class="flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium mb-4 bg-primary/15 text-primary">
                    <Check class="size-4" /> {{ editSuccess }}
                  </div>
                </Transition>

                <!-- Sub tabs -->
                <div class="flex border-b border-border mb-5">
                  <button
                    class="px-4 py-2 text-sm font-medium text-primary border-b-2 border-primary -mb-px transition-colors">Bảo
                    mật</button>
                  <button
                    class="px-4 py-2 text-sm font-medium text-muted-foreground hover:text-foreground -mb-px transition-colors">Trạng
                    thái</button>
                </div>

                <!-- Info fields -->
                <div class="rounded-lg overflow-hidden bg-muted">

                  <!-- Display name -->
                  <div class="flex items-start justify-between gap-4 px-4 py-3">
                    <div class="flex-1 min-w-0">
                      <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Tên hiển thị</Label>
                      <template v-if="editingField === 'displayName'">
                        <Input v-model="editValues.displayName" class="mt-1 h-8 text-sm" autofocus
                          @keyup.enter="saveEdit('displayName')" @keyup.escape="cancelEdit" />
                        <p v-if="editError" class="text-[11px] text-destructive mt-1">{{ editError }}</p>
                        <div class="flex gap-2 mt-2">
                          <Button size="sm" class="h-7 text-xs" :disabled="editLoading"
                            @click="saveEdit('displayName')">
                            {{ editLoading ? '...' : 'Lưu' }}
                          </Button>
                          <Button size="sm" variant="outline" class="h-7 text-xs" @click="cancelEdit">Huỷ</Button>
                        </div>
                      </template>
                      <p v-else class="text-sm text-foreground mt-0.5">{{ displayName }}</p>
                    </div>
                    <Button v-if="editingField !== 'displayName'" variant="outline" size="sm"
                      class="mt-5 text-xs shrink-0" @click="startEdit('displayName')">
                      Chỉnh sửa
                    </Button>
                  </div>

                  <Separator class="mx-4 w-auto opacity-40" />

                  <!-- Username -->
                  <div class="flex items-start justify-between gap-4 px-4 py-3">
                    <div class="flex-1 min-w-0">
                      <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Tên đăng nhập</Label>
                      <template v-if="editingField === 'username'">
                        <Input v-model="editValues.username" class="mt-1 h-8 text-sm" autofocus
                          @keyup.enter="saveEdit('username')" @keyup.escape="cancelEdit" />
                        <p v-if="editError" class="text-[11px] text-destructive mt-1">{{ editError }}</p>
                        <div class="flex gap-2 mt-2">
                          <Button size="sm" class="h-7 text-xs" :disabled="editLoading" @click="saveEdit('username')">
                            {{ editLoading ? '...' : 'Lưu' }}
                          </Button>
                          <Button size="sm" variant="outline" class="h-7 text-xs" @click="cancelEdit">Huỷ</Button>
                        </div>
                      </template>
                      <p v-else class="text-sm text-foreground mt-0.5">{{ currentUser?.username }}</p>
                    </div>
                    <Button v-if="editingField !== 'username'" variant="outline" size="sm" class="mt-5 text-xs shrink-0"
                      @click="startEdit('username')">
                      Chỉnh sửa
                    </Button>
                  </div>

                  <Separator class="mx-4 w-auto opacity-40" />

                  <!-- Email — read-only, toggle show/hide -->
                  <div class="px-4 py-3">
                    <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Email</Label>
                    <div class="flex items-center gap-2 mt-0.5">
                      <p class="text-sm text-foreground">{{ showEmail ? currentUser?.email : maskedEmail }}</p>
                      <button class="text-[11px] font-semibold text-primary hover:opacity-75 transition-opacity" @click="showEmail = !showEmail">
                        {{ showEmail ? 'Ẩn' : 'Hiển thị' }}
                      </button>
                    </div>
                  </div>
                </div>

                <!-- ══ PASSWORD SECTION ══ -->
                <div class="mt-7">
                  <p class="text-[10px] uppercase tracking-widest font-bold text-muted-foreground mb-3">Mật Khẩu & Xác
                    Thực</p>

                  <!-- OAuth: tạo mật khẩu lần đầu -->
                  <template v-if="isOAuth && !showChangePasswordForm">
                    <div class="rounded-lg bg-muted p-4 space-y-4">

                      <!-- Banner -->
                      <div class="flex items-start gap-3 p-3 rounded-lg border border-primary/20 bg-primary/8">
                        <div class="shrink-0 w-9 h-9 rounded-full bg-background flex items-center justify-center">
                          <!-- Google -->
                          <svg v-if="currentUser?.provider === 'GOOGLE'" viewBox="0 0 24 24" class="size-5" fill="none">
                            <path
                              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                              fill="#4285F4" />
                            <path
                              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                              fill="#34A853" />
                            <path
                              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"
                              fill="#FBBC05" />
                            <path
                              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                              fill="#EA4335" />
                          </svg>
                          <!-- GitHub -->
                          <svg v-else-if="currentUser?.provider === 'GITHUB'" viewBox="0 0 24 24" class="size-5"
                            fill="currentColor">
                            <path
                              d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z" />
                          </svg>
                          <!-- Fallback -->
                          <svg v-else viewBox="0 0 24 24" class="size-5 text-muted-foreground" fill="currentColor">
                            <path
                              d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z" />
                          </svg>
                        </div>
                        <div>
                          <p class="text-sm font-bold text-foreground">Đăng nhập qua {{ providerLabel }}</p>
                          <p class="text-xs text-muted-foreground mt-0.5 leading-relaxed">
                            Tài khoản chưa có mật khẩu. Tạo mật khẩu để có thêm cách đăng nhập.
                          </p>
                        </div>
                      </div>

                      <!-- Form tạo mật khẩu -->
                      <div class="space-y-3">
                        <div class="space-y-1.5">
                          <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Mật khẩu mới</Label>
                          <div class="relative">
                            <Input :type="showCreatePw.next ? 'text' : 'password'" v-model="createPwForm.next"
                              class="pr-10 h-9 text-sm" placeholder="Tối thiểu 6 ký tự" />
                            <button
                              class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                              @click="showCreatePw.next = !showCreatePw.next">
                              <Eye v-if="!showCreatePw.next" class="size-4" />
                              <EyeOff v-else class="size-4" />
                            </button>
                          </div>
                        </div>
                        <div class="space-y-1.5">
                          <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Xác nhận mật
                            khẩu</Label>
                          <div class="relative">
                            <Input :type="showCreatePw.confirm ? 'text' : 'password'" v-model="createPwForm.confirm"
                              class="pr-10 h-9 text-sm" placeholder="Nhập lại mật khẩu" />
                            <button
                              class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                              @click="showCreatePw.confirm = !showCreatePw.confirm">
                              <Eye v-if="!showCreatePw.confirm" class="size-4" />
                              <EyeOff v-else class="size-4" />
                            </button>
                          </div>
                        </div>
                      </div>

                      <Transition name="toast">
                        <div v-if="createPwError"
                          class="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium bg-destructive/10 text-destructive">
                          <AlertCircle class="size-4 shrink-0" /> {{ createPwError }}
                        </div>
                      </Transition>
                      <Transition name="toast">
                        <div v-if="createPwSuccess"
                          class="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium bg-primary/15 text-primary">
                          <Check class="size-4 shrink-0" /> {{ createPwSuccess }}
                        </div>
                      </Transition>

                      <Button class="text-sm" :disabled="createPwLoading || !createPwForm.next || !createPwForm.confirm"
                        @click="submitCreatePw">
                        {{ createPwLoading ? 'Đang tạo...' : 'Tạo mật khẩu' }}
                      </Button>
                    </div>
                  </template>

                  <!-- LOCAL hoặc OAuth đã tạo mật khẩu: đổi mật khẩu -->
                  <template v-else>
                    <div class="rounded-lg bg-muted p-4 space-y-3">
                      <div class="space-y-1.5">
                        <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Mật khẩu hiện
                          tại</Label>
                        <div class="relative">
                          <Input :type="showPw.current ? 'text' : 'password'" v-model="pwForm.current"
                            class="pr-10 h-9 text-sm" placeholder="••••••••" />
                          <button
                            class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                            @click="showPw.current = !showPw.current">
                            <Eye v-if="!showPw.current" class="size-4" />
                            <EyeOff v-else class="size-4" />
                          </button>
                        </div>
                      </div>
                      <div class="space-y-1.5">
                        <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Mật khẩu mới</Label>
                        <div class="relative">
                          <Input :type="showPw.next ? 'text' : 'password'" v-model="pwForm.next"
                            class="pr-10 h-9 text-sm" placeholder="••••••••" />
                          <button
                            class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                            @click="showPw.next = !showPw.next">
                            <Eye v-if="!showPw.next" class="size-4" />
                            <EyeOff v-else class="size-4" />
                          </button>
                        </div>
                      </div>
                      <div class="space-y-1.5">
                        <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">Xác nhận mật khẩu
                          mới</Label>
                        <div class="relative">
                          <Input :type="showPw.confirm ? 'text' : 'password'" v-model="pwForm.confirm"
                            class="pr-10 h-9 text-sm" placeholder="••••••••" />
                          <button
                            class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                            @click="showPw.confirm = !showPw.confirm">
                            <Eye v-if="!showPw.confirm" class="size-4" />
                            <EyeOff v-else class="size-4" />
                          </button>
                        </div>
                      </div>

                      <Transition name="toast">
                        <div v-if="pwError"
                          class="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium bg-destructive/10 text-destructive">
                          <AlertCircle class="size-4 shrink-0" /> {{ pwError }}
                        </div>
                      </Transition>
                      <Transition name="toast">
                        <div v-if="pwSuccess"
                          class="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium bg-primary/15 text-primary">
                          <Check class="size-4 shrink-0" /> {{ pwSuccess }}
                        </div>
                      </Transition>

                      <Button class="text-sm" :disabled="pwLoading || !pwForm.current || !pwForm.next"
                        @click="submitPw">
                        {{ pwLoading ? 'Đang lưu...' : 'Đổi mật khẩu' }}
                      </Button>
                    </div>
                  </template>
                </div>

              </template>

              <!-- ──── THEME ──── -->
              <template v-else-if="activeTab === 'theme'">
                <ThemeSettingsTab />
              </template>

              <!-- ──── AUDIO ──── -->
              <template v-else-if="activeTab === 'audio'">
                <AudioSettingsTab />
              </template>

              <!-- ──── BILLING ──── -->
              <template v-else-if="activeTab === 'billing'">
                <p class="text-[10px] uppercase tracking-widest font-bold text-muted-foreground mb-3">Nâng cấp lên Pro
                </p>
                <div class="rounded-lg bg-muted p-10 text-center">
                  <Sparkles class="size-12 mx-auto mb-3 text-primary" />
                  <p class="font-bold text-base text-foreground mb-1">Synkork Pro</p>
                  <p class="text-sm text-muted-foreground mb-5">Mở khóa toàn bộ tính năng cao cấp</p>
                  <Button>Nâng cấp ngay</Button>
                </div>
              </template>

              <!-- ──── PLACEHOLDER ──── -->
              <template v-else>
                <p class="text-[10px] uppercase tracking-widest font-bold text-muted-foreground mb-3">
                  {{settingsTabs.flatMap(g => g.items).find(i => i.id === activeTab)?.label}}
                </p>
                <div class="rounded-lg bg-muted p-5">
                  <p class="text-sm text-muted-foreground">Mục này sẽ sớm được cập nhật.</p>
                </div>
              </template>

            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.scrollbar-thin {
  scrollbar-width: thin;
  scrollbar-color: var(--border) transparent;
}

.scrollbar-thin::-webkit-scrollbar {
  width: 5px;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 999px;
}

.sf-enter-active,
.sf-leave-active {
  transition: opacity 0.18s ease;
}

.sf-enter-from,
.sf-leave-to {
  opacity: 0;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
