import { computed, ref } from "vue";
import { defineStore } from "pinia";
import type { Friend, FriendRequest } from "../types/Friends";

export const useFriendStore = defineStore("friend", () => {
  const friends = ref<Friend[]>([]);
  const pendingRequests = ref<FriendRequest[]>([]);
  const sentRequests = ref<FriendRequest[]>([]);
  const loading = ref(false);

  const friendCount = computed(() => friends.value.length);
  const hasPending = computed(
    () => pendingRequests.value.length > 0 || sentRequests.value.length > 0,
  );

  const getFriendshipStatus = (username: string) => {
    const friend =
      friends.value.find((item) => item.username === username) ?? null;
    const sentRequest =
      sentRequests.value.find((item) => item.receiverUsername === username) ??
      null;
    const receivedRequest =
      pendingRequests.value.find((item) => item.senderUsername === username) ??
      null;

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
    getFriendshipStatus,
  };
});
