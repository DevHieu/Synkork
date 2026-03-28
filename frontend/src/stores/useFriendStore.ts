import { defineStore } from "pinia"
import { ref, computed } from "vue"
import { friendService } from "@/services/friendService"
import type { Friend, FriendRequest } from "@/types/friend"
import { getUserIdFromToken } from "@/utils/auth"

export const useFriendStore = defineStore("friend", () => {
  // State
  const friends = ref<Friend[]>([])
  const pendingRequests = ref<FriendRequest[]>([])
  const loading = ref(false)

  // Getters (Optional - giúp truy cập nhanh thông tin)
  const friendCount = computed(() => friends.value.length)
  const hasPending = computed(() => pendingRequests.value.length > 0)

  // Actions
  const fetchFriends = async () => {
    try {
      loading.value = true
      const userId = getUserIdFromToken()

      if (!userId) {
        console.warn("⚠️ Không tìm thấy userId từ token")
        friends.value = []
        return
      }

      friends.value = await friendService.getFriends(userId)
    } catch (e: any) {
      console.error("❌ Fetch friends failed:", e.response?.data || e.message)
      friends.value = []
    } finally {
      loading.value = false
    }
  }

  const sendRequest = async (username: string) => {
    if (!username?.trim()) return
    try {
      loading.value = true // Bật loading khi gửi request
      await friendService.sendRequestByUsername(username)
      return true
    } catch (e: any) {
      console.error("Send friend request failed", e)
      throw e?.response?.data || e.message || "Gửi lời mời thất bại"
    } finally {
      loading.value = false
    }
  }

  // Đổi requestId từ number -> string để khớp với Service
  const acceptRequest = async (requestId: string) => {
    try {
      loading.value = true
      await friendService.acceptRequest(requestId)
      // Cập nhật lại cả 2 danh sách sau khi chấp nhận
      await Promise.all([fetchFriends(), fetchPendingRequests()])
      return true
    } catch (e) {
      console.error("Accept request failed", e)
      throw e
    } finally {
      loading.value = false
    }
  }

  const rejectRequest = async (requestId: string) => {
    try {
      loading.value = true
      await friendService.rejectRequest(requestId)
      await fetchPendingRequests()
      return true
    } catch (e) {
      console.error("Reject request failed", e)
      throw e
    } finally {
      loading.value = false
    }
  }

  const removeFriend = async (friendId: string) => {
    try {
      loading.value = true
      const userId = getUserIdFromToken()
      if (!userId) throw new Error("Không tìm thấy userId")

      await friendService.removeFriend(userId, friendId)
      await fetchFriends()
      return true
    } catch (e) {
      console.error("Remove friend failed", e)
      throw e
    } finally {
      loading.value = false
    }
  }

  const fetchPendingRequests = async () => {
    try {
      pendingRequests.value = await friendService.getPendingRequests()
    } catch (e) {
      console.error("Fetch pending requests failed", e)
      pendingRequests.value = []
    }
  }

  return {
    // State & Getters
    friends,
    pendingRequests,
    loading,
    friendCount,
    hasPending,

    // Actions
    fetchFriends,
    sendRequest,
    acceptRequest,
    rejectRequest,
    removeFriend,
    fetchPendingRequests
  }
})