import { computed, onMounted, onUnmounted, reactive, ref } from "vue"
import type { ShallowRef } from "vue"
import { useAuthService } from "@/features/auth/services/authService"
import { useUserService } from "@/features/users/services/userService"
import { useUserStore } from "@/features/users/stores/userStore"

export const useAccountSettings = (
  closeSettings: () => void,
  avatarInput: Readonly<ShallowRef<HTMLInputElement | null>>,
) => {
  const authService = useAuthService()
  const userService = useUserService()
  const userStore = useUserStore()

  const currentUser = computed(() => userStore.user)
  const avatarLoading = ref(false)
  const avatarError = ref("")

  const isOAuth = computed(() => {
    const provider = currentUser.value?.provider
    return provider && provider !== "LOCAL"
  })
  const showChangePasswordForm = computed(
    () => !isOAuth.value || currentUser.value?.hasPassword,
  )

  const editingField = ref<string | null>(null)
  const editValues = reactive({ displayName: "", username: "" })
  const editError = ref("")
  const editSuccess = ref("")
  const editLoading = ref(false)

  const startEdit = (field: string) => {
    editingField.value = field
    editError.value = ""
    editSuccess.value = ""

    if (field === "displayName") {
      editValues.displayName = currentUser.value?.displayName ?? ""
    }
    if (field === "username") {
      editValues.username = currentUser.value?.username ?? ""
    }
  }

  const cancelEdit = () => {
    editingField.value = null
    editError.value = ""
  }

  const saveEdit = async (field: string) => {
    editLoading.value = true
    editError.value = ""

    try {
      if (field === "displayName") {
        await userService.updateProfile({ displayName: editValues.displayName })
      } else if (field === "username") {
        await userService.updateProfile({ username: editValues.username })
      }

      await userStore.getUserInfo()
      editSuccess.value = "Đã lưu thành công"
      editingField.value = null
      setTimeout(() => (editSuccess.value = ""), 2500)
    } catch (error: any) {
      editError.value =
        error?.response?.data || error?.message || "Lỗi khi lưu"
    } finally {
      editLoading.value = false
    }
  }

  const pwForm = reactive({ current: "", next: "", confirm: "" })
  const showPw = reactive({ current: false, next: false, confirm: false })
  const pwError = ref("")
  const pwSuccess = ref("")
  const pwLoading = ref(false)

  const submitPw = async () => {
    pwError.value = ""
    if (pwForm.next !== pwForm.confirm) {
      pwError.value = "Mật khẩu mới không khớp"
      return
    }
    if (pwForm.next.length < 6) {
      pwError.value = "Ít nhất 6 ký tự"
      return
    }

    pwLoading.value = true
    try {
      await userService.changePassword({
        currentPassword: pwForm.current,
        newPassword: pwForm.next,
      })
      pwSuccess.value = "Đổi mật khẩu thành công!"
      pwForm.current = pwForm.next = pwForm.confirm = ""
      setTimeout(() => (pwSuccess.value = ""), 3000)
    } catch (error: any) {
      pwError.value = error?.response?.data || error?.message || "Thất bại"
    } finally {
      pwLoading.value = false
    }
  }

  const createPwForm = reactive({ next: "", confirm: "" })
  const showCreatePw = reactive({ next: false, confirm: false })
  const createPwError = ref("")
  const createPwSuccess = ref("")
  const createPwLoading = ref(false)

  const submitCreatePw = async () => {
    createPwError.value = ""
    if (createPwForm.next !== createPwForm.confirm) {
      createPwError.value = "Mật khẩu xác nhận không khớp"
      return
    }
    if (createPwForm.next.length < 6) {
      createPwError.value = "Ít nhất 6 ký tự"
      return
    }

    createPwLoading.value = true
    try {
      await userService.createPassword({ newPassword: createPwForm.next })
      createPwForm.next = createPwForm.confirm = ""
      await userStore.getUserInfo()
      pwSuccess.value =
        "Tạo mật khẩu thành công! Bạn có thể đổi mật khẩu bên dưới."
      setTimeout(() => (pwSuccess.value = ""), 4000)
    } catch (error: any) {
      createPwError.value =
        error?.response?.data || error?.message || "Thất bại"
    } finally {
      createPwLoading.value = false
    }
  }

  const showEmail = ref(false)
  const maskedEmail = computed(() =>
    (currentUser.value?.email ?? "").replace(/(.{2})[^@]+(@.+)/, "$1***$2"),
  )
  const displayName = computed(
    () =>
      currentUser.value?.displayName || currentUser.value?.username || "—",
  )

  const chooseAvatar = () => {
    if (!avatarLoading.value) avatarInput.value?.click()
  }

  const uploadAvatar = async (event: Event) => {
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
    } catch (error: any) {
      avatarError.value =
        error?.response?.data || error?.message || "Không thể tải ảnh lên"
    } finally {
      avatarLoading.value = false
    }
  }

  const onKeydown = (event: KeyboardEvent) => {
    if (event.key === "Escape") closeSettings()
  }

  const logout = () => authService.logout()

  onMounted(() => document.addEventListener("keydown", onKeydown))
  onUnmounted(() => document.removeEventListener("keydown", onKeydown))

  return {
    currentUser,
    avatarLoading,
    avatarError,
    isOAuth,
    showChangePasswordForm,
    editingField,
    editValues,
    editError,
    editSuccess,
    editLoading,
    startEdit,
    cancelEdit,
    saveEdit,
    pwForm,
    showPw,
    pwError,
    pwSuccess,
    pwLoading,
    submitPw,
    createPwForm,
    showCreatePw,
    createPwError,
    createPwSuccess,
    createPwLoading,
    submitCreatePw,
    showEmail,
    maskedEmail,
    displayName,
    chooseAvatar,
    uploadAvatar,
    logout,
  }
}
