<script setup lang="ts">
import { computed, reactive, ref, useTemplateRef } from "vue"
import {
  AlertCircle,
  Check,
  Eye,
  EyeOff,
  Loader2,
  Pencil,
  User,
} from "lucide-vue-next"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { useUserStore } from "@/features/users/stores/userStore"
import {
  useAccountSettings,
  type EditableProfileField,
} from "../composables/useAccountSettings"

const userStore = useUserStore()
const currentUser = computed(() => userStore.user)

const avatarInput = useTemplateRef<HTMLInputElement>("avatarInput")
const avatarLoading = ref(false)
const avatarError = ref("")

const editingField = ref<EditableProfileField | null>(null)
const editValues = reactive({ displayName: "", username: "" })
const editError = ref("")
const editSuccess = ref("")
const editLoading = ref(false)

const passwordForm = reactive({ current: "", next: "", confirm: "" })
const showPassword = reactive({ current: false, next: false, confirm: false })
const passwordError = ref("")
const passwordSuccess = ref("")
const passwordLoading = ref(false)

const createPasswordForm = reactive({ next: "", confirm: "" })
const showCreatePassword = reactive({ next: false, confirm: false })
const createPasswordError = ref("")
const createPasswordLoading = ref(false)

const showEmail = ref(false)
const maskedEmail = computed(() =>
  (currentUser.value?.email ?? "").replace(/(.{2})[^@]+(@.+)/, "$1***$2"),
)
const displayName = computed(
  () =>
    currentUser.value?.displayName || currentUser.value?.username || "—",
)
const isOAuth = computed(() => {
  const provider = currentUser.value?.provider
  return provider && provider !== "LOCAL"
})
const showChangePasswordForm = computed(
  () => !isOAuth.value || currentUser.value?.hasPassword,
)

const {
  startEdit,
  cancelEdit,
  saveEdit,
  submitPassword,
  submitCreatePassword,
  chooseAvatar,
  uploadAvatar,
} = useAccountSettings({
  avatarInput,
  avatarLoading,
  avatarError,
  editingField,
  editValues,
  editError,
  editSuccess,
  editLoading,
  passwordForm,
  passwordError,
  passwordSuccess,
  passwordLoading,
  createPasswordForm,
  createPasswordError,
  createPasswordLoading,
})
</script>

<template>
  <input
    ref="avatarInput"
    type="file"
    accept="image/*"
    class="hidden"
    @change="uploadAvatar"
  />

  <div class="rounded-xl overflow-hidden bg-muted mb-5">
    <div class="h-[90px] bg-gradient-to-br from-primary to-secondary" />
    <div class="flex items-end px-4 -mt-10 mb-3">
      <button
        type="button"
        class="relative cursor-pointer group/avatar disabled:cursor-wait"
        :disabled="avatarLoading"
        aria-label="Thay ảnh đại diện"
        @click="chooseAvatar"
      >
        <Avatar class="w-20 h-20 border-4 border-card">
          <AvatarImage
            v-if="currentUser?.avatarUrl"
            :src="currentUser.avatarUrl"
          />
          <AvatarFallback
            class="bg-primary text-primary-foreground text-3xl font-bold"
          >
            {{ displayName.charAt(0).toUpperCase() }}
          </AvatarFallback>
        </Avatar>

        <div
          class="absolute inset-0 rounded-full flex items-center justify-center bg-black/50 transition-opacity"
          :class="
            avatarLoading
              ? 'opacity-100'
              : 'opacity-0 group-hover/avatar:opacity-100'
          "
        >
          <Loader2 v-if="avatarLoading" class="size-5 text-white animate-spin" />
          <Pencil v-else class="size-5 text-white" />
        </div>
      </button>
    </div>
    <p class="px-4 pb-3 text-sm font-bold text-foreground">
      {{ displayName }}
    </p>
    <p v-if="avatarError" class="px-4 pb-3 text-xs text-destructive">
      {{ avatarError }}
    </p>
  </div>

  <Transition name="toast">
    <div
      v-if="editSuccess"
      class="flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium mb-4 bg-primary/15 text-primary"
    >
      <Check class="size-4" />
      {{ editSuccess }}
    </div>
  </Transition>

  <div class="rounded-lg overflow-hidden bg-muted">
    <div class="flex items-start justify-between gap-4 px-4 py-3">
      <div class="flex-1 min-w-0">
        <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">
          Tên hiển thị
        </Label>
        <template v-if="editingField === 'displayName'">
          <Input
            v-model="editValues.displayName"
            class="mt-1 h-8 text-sm"
            autofocus
            @keyup.enter="saveEdit('displayName')"
            @keyup.escape="cancelEdit"
          />
          <p v-if="editError" class="text-[11px] text-destructive mt-1">
            {{ editError }}
          </p>
          <div class="flex gap-2 mt-2">
            <Button
              size="sm"
              class="h-7 text-xs"
              :disabled="editLoading"
              @click="saveEdit('displayName')"
            >
              {{ editLoading ? "..." : "Lưu" }}
            </Button>
            <Button
              size="sm"
              variant="outline"
              class="h-7 text-xs"
              @click="cancelEdit"
            >
              Huỷ
            </Button>
          </div>
        </template>
        <p v-else class="text-sm text-foreground mt-0.5">{{ displayName }}</p>
      </div>
      <Button
        v-if="editingField !== 'displayName'"
        variant="outline"
        size="sm"
        class="mt-5 text-xs shrink-0"
        @click="startEdit('displayName')"
      >
        Chỉnh sửa
      </Button>
    </div>

    <Separator class="mx-4 w-auto opacity-40" />

    <div class="flex items-start justify-between gap-4 px-4 py-3">
      <div class="flex-1 min-w-0">
        <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">
          Tên đăng nhập
        </Label>
        <template v-if="editingField === 'username'">
          <Input
            v-model="editValues.username"
            class="mt-1 h-8 text-sm"
            autofocus
            @keyup.enter="saveEdit('username')"
            @keyup.escape="cancelEdit"
          />
          <p v-if="editError" class="text-[11px] text-destructive mt-1">
            {{ editError }}
          </p>
          <div class="flex gap-2 mt-2">
            <Button
              size="sm"
              class="h-7 text-xs"
              :disabled="editLoading"
              @click="saveEdit('username')"
            >
              {{ editLoading ? "..." : "Lưu" }}
            </Button>
            <Button
              size="sm"
              variant="outline"
              class="h-7 text-xs"
              @click="cancelEdit"
            >
              Huỷ
            </Button>
          </div>
        </template>
        <p v-else class="text-sm text-foreground mt-0.5">
          {{ currentUser?.username }}
        </p>
      </div>
      <Button
        v-if="editingField !== 'username'"
        variant="outline"
        size="sm"
        class="mt-5 text-xs shrink-0"
        @click="startEdit('username')"
      >
        Chỉnh sửa
      </Button>
    </div>

    <Separator class="mx-4 w-auto opacity-40" />

    <div class="px-4 py-3">
      <Label class="text-[10px] uppercase tracking-wider text-muted-foreground">
        Email
      </Label>
      <div class="flex items-center gap-2 mt-0.5">
        <p class="text-sm text-foreground">
          {{ showEmail ? currentUser?.email : maskedEmail }}
        </p>
        <button
          type="button"
          class="text-[11px] font-semibold text-primary hover:opacity-75 transition-opacity"
          @click="showEmail = !showEmail"
        >
          {{ showEmail ? "Ẩn" : "Hiển thị" }}
        </button>
      </div>
    </div>
  </div>

  <div class="mt-7">
    <p
      class="text-[10px] uppercase tracking-widest font-bold text-muted-foreground mb-3"
    >
      Mật Khẩu & Xác Thực
    </p>

    <template v-if="isOAuth && !showChangePasswordForm">
      <div class="rounded-lg bg-muted p-4 space-y-4">
        <div
          class="flex items-start gap-3 p-3 rounded-lg border border-primary/20 bg-primary/8"
        >
          <div
            class="shrink-0 w-9 h-9 rounded-full bg-background flex items-center justify-center"
          >
            <svg
              v-if="currentUser?.provider === 'GOOGLE'"
              viewBox="0 0 24 24"
              class="size-5"
              fill="none"
            >
              <path
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                fill="#4285F4"
              />
              <path
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                fill="#34A853"
              />
              <path
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"
                fill="#FBBC05"
              />
              <path
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                fill="#EA4335"
              />
            </svg>
            <User v-else class="size-5 text-muted-foreground" />
          </div>
          <div>
            <p class="text-sm font-bold text-foreground">Đăng nhập qua Google</p>
            <p class="text-xs text-muted-foreground mt-0.5 leading-relaxed">
              Tài khoản chưa có mật khẩu. Tạo mật khẩu để có thêm cách đăng nhập.
            </p>
          </div>
        </div>

        <div class="space-y-3">
          <div class="space-y-1.5">
            <Label
              class="text-[10px] uppercase tracking-wider text-muted-foreground"
            >
              Mật khẩu mới
            </Label>
            <div class="relative">
              <Input
                v-model="createPasswordForm.next"
                :type="showCreatePassword.next ? 'text' : 'password'"
                class="pr-10 h-9 text-sm"
                placeholder="Tối thiểu 6 ký tự"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                @click="showCreatePassword.next = !showCreatePassword.next"
              >
                <Eye v-if="!showCreatePassword.next" class="size-4" />
                <EyeOff v-else class="size-4" />
              </button>
            </div>
          </div>
          <div class="space-y-1.5">
            <Label
              class="text-[10px] uppercase tracking-wider text-muted-foreground"
            >
              Xác nhận mật khẩu
            </Label>
            <div class="relative">
              <Input
                v-model="createPasswordForm.confirm"
                :type="showCreatePassword.confirm ? 'text' : 'password'"
                class="pr-10 h-9 text-sm"
                placeholder="Nhập lại mật khẩu"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                @click="
                  showCreatePassword.confirm = !showCreatePassword.confirm
                "
              >
                <Eye v-if="!showCreatePassword.confirm" class="size-4" />
                <EyeOff v-else class="size-4" />
              </button>
            </div>
          </div>
        </div>

        <Transition name="toast">
          <div
            v-if="createPasswordError"
            class="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium bg-destructive/10 text-destructive"
          >
            <AlertCircle class="size-4 shrink-0" />
            {{ createPasswordError }}
          </div>
        </Transition>

        <Button
          class="text-sm"
          :disabled="
            createPasswordLoading ||
            !createPasswordForm.next ||
            !createPasswordForm.confirm
          "
          @click="submitCreatePassword"
        >
          {{ createPasswordLoading ? "Đang tạo..." : "Tạo mật khẩu" }}
        </Button>
      </div>
    </template>

    <template v-else>
      <div class="rounded-lg bg-muted p-4 space-y-3">
        <div class="space-y-1.5">
          <Label
            class="text-[10px] uppercase tracking-wider text-muted-foreground"
          >
            Mật khẩu hiện tại
          </Label>
          <div class="relative">
            <Input
              v-model="passwordForm.current"
              :type="showPassword.current ? 'text' : 'password'"
              class="pr-10 h-9 text-sm"
              placeholder="••••••••"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              @click="showPassword.current = !showPassword.current"
            >
              <Eye v-if="!showPassword.current" class="size-4" />
              <EyeOff v-else class="size-4" />
            </button>
          </div>
        </div>
        <div class="space-y-1.5">
          <Label
            class="text-[10px] uppercase tracking-wider text-muted-foreground"
          >
            Mật khẩu mới
          </Label>
          <div class="relative">
            <Input
              v-model="passwordForm.next"
              :type="showPassword.next ? 'text' : 'password'"
              class="pr-10 h-9 text-sm"
              placeholder="••••••••"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              @click="showPassword.next = !showPassword.next"
            >
              <Eye v-if="!showPassword.next" class="size-4" />
              <EyeOff v-else class="size-4" />
            </button>
          </div>
        </div>
        <div class="space-y-1.5">
          <Label
            class="text-[10px] uppercase tracking-wider text-muted-foreground"
          >
            Xác nhận mật khẩu mới
          </Label>
          <div class="relative">
            <Input
              v-model="passwordForm.confirm"
              :type="showPassword.confirm ? 'text' : 'password'"
              class="pr-10 h-9 text-sm"
              placeholder="••••••••"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              @click="showPassword.confirm = !showPassword.confirm"
            >
              <Eye v-if="!showPassword.confirm" class="size-4" />
              <EyeOff v-else class="size-4" />
            </button>
          </div>
        </div>

        <Transition name="toast">
          <div
            v-if="passwordError"
            class="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium bg-destructive/10 text-destructive"
          >
            <AlertCircle class="size-4 shrink-0" />
            {{ passwordError }}
          </div>
        </Transition>
        <Transition name="toast">
          <div
            v-if="passwordSuccess"
            class="flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium bg-primary/15 text-primary"
          >
            <Check class="size-4 shrink-0" />
            {{ passwordSuccess }}
          </div>
        </Transition>

        <Button
          class="text-sm"
          :disabled="passwordLoading || !passwordForm.current || !passwordForm.next"
          @click="submitPassword"
        >
          {{ passwordLoading ? "Đang lưu..." : "Đổi mật khẩu" }}
        </Button>
      </div>
    </template>
  </div>
</template>

<style scoped>
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
