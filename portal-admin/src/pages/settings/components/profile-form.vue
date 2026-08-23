<script setup lang="ts">
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { useChangePasswordMutation, useUpdateProfileMutation } from '@/services/api/admin-auth.api'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// --- Update Profile Form ---
const { mutate: updateProfile, isPending: isUpdatingProfile } = useUpdateProfileMutation()

const username = ref('')
const displayName = ref('')

watch(
  () => authStore.user,
  (user) => {
    username.value = user?.username ?? ''
    displayName.value = user?.displayName ?? ''
  },
  { immediate: true },
)

function onUpdateProfile() {
  updateProfile(
    { username: username.value, displayName: displayName.value },
    {
      onSuccess: async () => {
        await authStore.getUserInfo()
        toast.success('Cập nhật thông tin tài khoản thành công!')
      },
      onError: (error: any) => {
        const message = error?.response?.data?.message ?? 'Đã có lỗi xảy ra, vui lòng thử lại.'
        toast.error(message)
      },
    },
  )
}

function resetProfileForm() {
  username.value = authStore.user?.username ?? ''
  displayName.value = authStore.user?.displayName ?? ''
}

// --- Change Password Form ---
const { mutate: changePassword, isPending: isChangingPassword } = useChangePasswordMutation()

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

function onChangePassword() {
  if (newPassword.value !== confirmPassword.value) {
    toast.error('Mật khẩu xác nhận không khớp.')
    return
  }
  if (newPassword.value.length < 6) {
    toast.error('Mật khẩu mới phải chứa ít nhất 6 ký tự.')
    return
  }

  changePassword(
    { currentPassword: currentPassword.value, newPassword: newPassword.value },
    {
      onSuccess: () => {
        toast.success('Đổi mật khẩu thành công!')
        currentPassword.value = ''
        newPassword.value = ''
        confirmPassword.value = ''
      },
      onError: (error: any) => {
        const message = error?.response?.data?.message ?? 'Đổi mật khẩu thất bại. Vui lòng kiểm tra lại.'
        toast.error(message)
      },
    },
  )
}
</script>

<template>
  <div class="space-y-10 grid grid-cols-2 gap-4">
    <!-- Section 1: Update Profile -->
    <div>
      <div>
        <h3 class="text-lg font-medium">
          Thông tin tài khoản
        </h3>
        <p class="text-sm text-muted-foreground">
          Cập nhật thông tin hiển thị cá nhân của bạn trong hệ thống quản trị.
        </p>
      </div>

      <Separator orientation="horizontal" class="my-3" />

      <form class="space-y-6 max-w-xl" @submit.prevent="onUpdateProfile">
        <div class="space-y-2">
          <label class="text-sm font-medium leading-none">Username</label>
          <Input v-model="username" type="text" placeholder="username" />
          <p class="text-sm text-muted-foreground">
            Tên đăng nhập hệ thống của bạn.
          </p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium leading-none">Tên hiển thị</label>
          <Input v-model="displayName" type="text" placeholder="Nguyễn Văn A" />
          <p class="text-sm text-muted-foreground">
            Tên hiển thị công khai trên giao diện.
          </p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium leading-none">Email</label>
          <Input type="email" :model-value="authStore.user?.email ?? ''" disabled
            class="bg-muted text-muted-foreground cursor-not-allowed" />
          <p class="text-sm text-muted-foreground">
            Email không thể thay đổi.
          </p>
        </div>

        <div class="flex justify-start gap-2">
          <Button type="submit" :disabled="isUpdatingProfile">
            {{ isUpdatingProfile ? 'Đang cập nhật...' : 'Cập nhật thông tin' }}
          </Button>
          <Button type="button" variant="outline" :disabled="isUpdatingProfile" @click="resetProfileForm">
            Đặt lại
          </Button>
        </div>
      </form>
    </div>

    <!-- Section 2: Change Password -->
    <div>
      <div>
        <h3 class="text-lg font-medium">
          Đổi mật khẩu
        </h3>
        <p class="text-sm text-muted-foreground">
          Đảm bảo sử dụng mật khẩu mạnh để bảo mật tài khoản của bạn.
        </p>
      </div>

      <Separator orientation="horizontal" class="my-3" />

      <form class="space-y-6 max-w-xl" @submit.prevent="onChangePassword">
        <div class="space-y-2">
          <label class="text-sm font-medium leading-none">Mật khẩu hiện tại</label>
          <Input v-model="currentPassword" type="password" placeholder="••••••••" />
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium leading-none">Mật khẩu mới</label>
          <Input v-model="newPassword" type="password" placeholder="••••••••" />
          <p class="text-sm text-muted-foreground">
            Mật khẩu mới phải chứa ít nhất 6 ký tự.
          </p>
        </div>

        <div class="space-y-2">
          <label class="text-sm font-medium leading-none">Xác nhận mật khẩu mới</label>
          <Input v-model="confirmPassword" type="password" placeholder="••••••••" />
        </div>

        <div class="flex justify-start gap-2">
          <Button type="submit" :disabled="isChangingPassword">
            {{ isChangingPassword ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
          </Button>
        </div>
      </form>
    </div>
  </div>
</template>