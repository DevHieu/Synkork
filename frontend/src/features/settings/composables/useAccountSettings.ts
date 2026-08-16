import type { Ref, ShallowRef } from "vue"
import { useUserService } from "@/features/users/services/userService"
import { useUserStore } from "@/features/users/stores/userStore"

export type EditableProfileField = "displayName" | "username"

interface AccountSettingsState {
  avatarInput: Readonly<ShallowRef<HTMLInputElement | null>>
  avatarLoading: Ref<boolean>
  avatarError: Ref<string>
  editingField: Ref<EditableProfileField | null>
  editValues: {
    displayName: string
    username: string
  }
  editError: Ref<string>
  editSuccess: Ref<string>
  editLoading: Ref<boolean>
  passwordForm: {
    current: string
    next: string
    confirm: string
  }
  passwordError: Ref<string>
  passwordSuccess: Ref<string>
  passwordLoading: Ref<boolean>
  createPasswordForm: {
    next: string
    confirm: string
  }
  createPasswordError: Ref<string>
  createPasswordLoading: Ref<boolean>
}

export const useAccountSettings = (state: AccountSettingsState) => {
  const userService = useUserService()
  const userStore = useUserStore()

  const startEdit = (field: EditableProfileField) => {
    state.editingField.value = field
    state.editError.value = ""
    state.editSuccess.value = ""
    state.editValues[field] = userStore.user?.[field] ?? ""
  }

  const cancelEdit = () => {
    state.editingField.value = null
    state.editError.value = ""
  }

  const saveEdit = async (field: EditableProfileField) => {
    state.editLoading.value = true
    state.editError.value = ""

    try {
      await userService.updateProfile({ [field]: state.editValues[field] })
      await userStore.getUserInfo()
      state.editSuccess.value = "Đã lưu thành công"
      state.editingField.value = null
      setTimeout(() => (state.editSuccess.value = ""), 2500)
    } catch (error: any) {
      state.editError.value =
        error?.response?.data || error?.message || "Lỗi khi lưu"
    } finally {
      state.editLoading.value = false
    }
  }

  const submitPassword = async () => {
    state.passwordError.value = ""

    if (state.passwordForm.next !== state.passwordForm.confirm) {
      state.passwordError.value = "Mật khẩu mới không khớp"
      return
    }
    if (state.passwordForm.next.length < 6) {
      state.passwordError.value = "Ít nhất 6 ký tự"
      return
    }

    state.passwordLoading.value = true
    try {
      await userService.changePassword({
        currentPassword: state.passwordForm.current,
        newPassword: state.passwordForm.next,
      })
      state.passwordSuccess.value = "Đổi mật khẩu thành công!"
      state.passwordForm.current = ""
      state.passwordForm.next = ""
      state.passwordForm.confirm = ""
      setTimeout(() => (state.passwordSuccess.value = ""), 3000)
    } catch (error: any) {
      state.passwordError.value =
        error?.response?.data || error?.message || "Thất bại"
    } finally {
      state.passwordLoading.value = false
    }
  }

  const submitCreatePassword = async () => {
    state.createPasswordError.value = ""

    if (
      state.createPasswordForm.next !== state.createPasswordForm.confirm
    ) {
      state.createPasswordError.value = "Mật khẩu xác nhận không khớp"
      return
    }
    if (state.createPasswordForm.next.length < 6) {
      state.createPasswordError.value = "Ít nhất 6 ký tự"
      return
    }

    state.createPasswordLoading.value = true
    try {
      await userService.createPassword({
        newPassword: state.createPasswordForm.next,
      })
      state.createPasswordForm.next = ""
      state.createPasswordForm.confirm = ""
      await userStore.getUserInfo()
      state.passwordSuccess.value =
        "Tạo mật khẩu thành công! Bạn có thể đổi mật khẩu bên dưới."
      setTimeout(() => (state.passwordSuccess.value = ""), 4000)
    } catch (error: any) {
      state.createPasswordError.value =
        error?.response?.data || error?.message || "Thất bại"
    } finally {
      state.createPasswordLoading.value = false
    }
  }

  const chooseAvatar = () => {
    if (!state.avatarLoading.value) state.avatarInput.value?.click()
  }

  const uploadAvatar = async (event: Event) => {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ""
    if (!file) return

    state.avatarError.value = ""
    if (!file.type.startsWith("image/")) {
      state.avatarError.value = "Vui lòng chọn một tệp ảnh"
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      state.avatarError.value = "Ảnh phải nhỏ hơn 5 MB"
      return
    }

    state.avatarLoading.value = true
    try {
      userStore.user = await userService.uploadAvatar(file)
    } catch (error: any) {
      state.avatarError.value =
        error?.response?.data || error?.message || "Không thể tải ảnh lên"
    } finally {
      state.avatarLoading.value = false
    }
  }

  return {
    startEdit,
    cancelEdit,
    saveEdit,
    submitPassword,
    submitCreatePassword,
    chooseAvatar,
    uploadAvatar,
  }
}
