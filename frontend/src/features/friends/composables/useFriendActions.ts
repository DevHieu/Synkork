import { userSocket } from "@/features/users/services/userSocket";
import { useUserStore } from "@/features/users/stores/userStore";
import { socketService } from "@/services/socketService";
import { friendService } from "../services/friendService";
import { useFriendStore } from "../stores/friendStore";

let onlineStatusSubscription: ReturnType<
  typeof userSocket.subscribeFriendOnlineStatus
> | null = null;

export const useFriendActions = () => {
  const friendStore = useFriendStore();
  const userStore = useUserStore();

  const getCurrentUserId = async (): Promise<string | null> => {
    if (!userStore.user) await userStore.getUserInfo();
    return userStore.user?.id ?? null;
  };

  const subscribeOnlineStatus = async () => {
    if (onlineStatusSubscription) return;

    await socketService.connect();
    onlineStatusSubscription = userSocket.subscribeFriendOnlineStatus(
      ({ userId, isOnline }) => {
        const friend = friendStore.friends.find((item) => item.id === userId);
        if (friend) friend.isOnline = isOnline;
      },
    );
  };

  const fetchFriends = async () => {
    try {
      friendStore.loading = true;
      const userId = await getCurrentUserId();

      if (!userId) {
        friendStore.friends = [];
        return;
      }

      friendStore.friends = await friendService.getFriends(userId);
      void subscribeOnlineStatus();
    } catch (error: any) {
      console.error(
        "Fetch friends failed:",
        error?.response?.data || error?.message,
      );
      friendStore.friends = [];
    } finally {
      friendStore.loading = false;
    }
  };

  const fetchPendingRequests = async () => {
    try {
      friendStore.pendingRequests = await friendService.getPendingRequests();
    } catch (error) {
      console.error("Fetch pending requests failed", error);
      friendStore.pendingRequests = [];
    }
  };

  const fetchSentRequests = async () => {
    try {
      friendStore.sentRequests = await friendService.getSentRequests();
    } catch (error) {
      console.error("Fetch sent requests failed", error);
      friendStore.sentRequests = [];
    }
  };

  const sendRequest = async (username: string) => {
    if (!username.trim()) return;

    try {
      friendStore.loading = true;
      await friendService.sendRequestByUsername(username);
      await fetchSentRequests();
      return true;
    } catch (error: any) {
      console.error("Send friend request failed", error);
      throw (
        error?.response?.data || error?.message || "Gửi lời mời thất bại"
      );
    } finally {
      friendStore.loading = false;
    }
  };

  const cancelRequest = async (requestId: string) => {
    try {
      friendStore.loading = true;
      await friendService.cancelRequest(requestId);
      await fetchSentRequests();
      return true;
    } catch (error) {
      console.error("Cancel request failed", error);
      throw error;
    } finally {
      friendStore.loading = false;
    }
  };

  const acceptRequest = async (requestId: string) => {
    try {
      friendStore.loading = true;
      await friendService.acceptRequest(requestId);
      await Promise.all([fetchFriends(), fetchPendingRequests()]);
      return true;
    } catch (error) {
      console.error("Accept request failed", error);
      throw error;
    } finally {
      friendStore.loading = false;
    }
  };

  const rejectRequest = async (requestId: string) => {
    try {
      friendStore.loading = true;
      await friendService.rejectRequest(requestId);
      await fetchPendingRequests();
      return true;
    } catch (error) {
      console.error("Reject request failed", error);
      throw error;
    } finally {
      friendStore.loading = false;
    }
  };

  const removeFriend = async (friendId: string) => {
    try {
      friendStore.loading = true;
      const userId = await getCurrentUserId();
      if (!userId) throw new Error("Không tìm thấy userId");

      await friendService.removeFriend(userId, friendId);
      await fetchFriends();
      return true;
    } catch (error) {
      console.error("Remove friend failed", error);
      throw error;
    } finally {
      friendStore.loading = false;
    }
  };

  return {
    fetchFriends,
    fetchPendingRequests,
    fetchSentRequests,
    sendRequest,
    cancelRequest,
    acceptRequest,
    rejectRequest,
    removeFriend,
  };
};
