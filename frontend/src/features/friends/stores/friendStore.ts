import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { friendService } from "../services/friendService";
import type { Friend, FriendRequest } from "../types/Friends";
import { useUserStore } from "@/features/users/stores/userStore";
import { userSocket } from "@/features/users/services/userSocket";
import { socketService } from "@/services/socketService";

export const useFriendStore = defineStore("friend", () => {
  const friends = ref<Friend[]>([]);
  const pendingRequests = ref<FriendRequest[]>([]);
  const sentRequests = ref<FriendRequest[]>([]);
  const loading = ref(false);
  let onlineStatusSubscription: any = null;

  const friendCount = computed(() => friends.value.length);
  const hasPending = computed(
    () => pendingRequests.value.length > 0 || sentRequests.value.length > 0,
  );

  const getCurrentUserId = async (): Promise<string | null> => {
    const userStore = useUserStore();
    if (!userStore.user) {
      await userStore.getUserInfo();
    }
    return (userStore.user as any)?.id ?? null;
  };

  const fetchFriends = async () => {
    try {
      loading.value = true;
      const userId = await getCurrentUserId();
      if (!userId) {
        friends.value = [];
        return;
      }
      friends.value = await friendService.getFriends(userId);

      subscribeOnlineStatus();
    } catch (e: any) {
      console.error(" Fetch friends failed:", e.response?.data || e.message);
      friends.value = [];
    } finally {
      loading.value = false;
    }
  };

  const sendRequest = async (username: string) => {
    if (!username?.trim()) return;
    try {
      loading.value = true;
      await friendService.sendRequestByUsername(username);
      await fetchSentRequests();
      return true;
    } catch (e: any) {
      console.error("Send friend request failed", e);
      throw e?.response?.data || e.message || "Gửi lời mời thất bại";
    } finally {
      loading.value = false;
    }
  };

  const cancelRequest = async (requestId: string) => {
    try {
      loading.value = true;
      await friendService.cancelRequest(requestId);
      await fetchSentRequests();
      return true;
    } catch (e) {
      console.error("Cancel request failed", e);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  const acceptRequest = async (requestId: string) => {
    try {
      loading.value = true;
      await friendService.acceptRequest(requestId);
      await Promise.all([fetchFriends(), fetchPendingRequests()]);
      return true;
    } catch (e) {
      console.error("Accept request failed", e);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  const rejectRequest = async (requestId: string) => {
    try {
      loading.value = true;
      await friendService.rejectRequest(requestId);
      await fetchPendingRequests();
      return true;
    } catch (e) {
      console.error("Reject request failed", e);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  const removeFriend = async (friendId: string) => {
    try {
      loading.value = true;
      const userId = await getCurrentUserId();
      if (!userId) throw new Error("Không tìm thấy userId");
      await friendService.removeFriend(userId, friendId);
      await fetchFriends();
      return true;
    } catch (e) {
      console.error("Remove friend failed", e);
      throw e;
    } finally {
      loading.value = false;
    }
  };

  const fetchPendingRequests = async () => {
    try {
      pendingRequests.value = await friendService.getPendingRequests();
    } catch (e) {
      console.error("Fetch pending requests failed", e);
      pendingRequests.value = [];
    }
  };

  const fetchSentRequests = async () => {
    try {
      sentRequests.value = await friendService.getSentRequests();
    } catch (e) {
      console.error("Fetch sent requests failed", e);
      sentRequests.value = [];
    }
  };

  const subscribeOnlineStatus = async () => {
    // Tránh subscribe nhiều lần
    if (onlineStatusSubscription) return;

    await socketService.connect();

    onlineStatusSubscription = userSocket.subscribeFriendOnlineStatus(
      ({ userId, isOnline }) => {
        const friend = friends.value.find((f) => f.id === userId);

        if (friend) {
          friend.isOnline = isOnline;
        }
      },
    );
  };

  const getFriendshipStatus = (username: string) => {
    const friend = friends.value.find((f) => f.username === username) ?? null;

    const sentRequest =
      sentRequests.value.find((r) => r.receiverUsername === username) ?? null;

    const receivedRequest =
      pendingRequests.value.find((r) => r.senderUsername === username) ?? null;

    return {
      friend,
      sentRequest,
      receivedRequest,
      isFriend: !!friend,
      isPending: !!sentRequest,
      isReceived: !!receivedRequest,
      requestId: sentRequest?.id || receivedRequest?.id || null,
    };
  };

  return {
    friends,
    pendingRequests,
    sentRequests,
    loading,
    friendCount,
    hasPending,
    fetchFriends,
    sendRequest,
    cancelRequest,
    acceptRequest,
    rejectRequest,
    removeFriend,
    fetchPendingRequests,
    fetchSentRequests,
    getFriendshipStatus,
  };
});
