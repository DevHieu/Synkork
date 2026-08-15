import { computed, onMounted, ref, watch } from "vue";
import { friendSocket } from "../services/friendSocket";
import { useFriendStore } from "../stores/friendStore";

type FriendTab = "all" | "pending" | "add";

export const useFriendPage = () => {
  const store = useFriendStore();
  const activeTab = ref<FriendTab>("all");

  const totalPending = computed(
    () => store.pendingRequests.length + store.sentRequests.length,
  );

  const subscribeToFriendEvents = () => {
    friendSocket.subscribeFriendRequest(async () => {
      await Promise.all([
        store.fetchPendingRequests(),
        store.fetchSentRequests(),
      ]);
    });

    friendSocket.subscribeFriendAccept(async () => {
      await Promise.all([
        store.fetchFriends(),
        store.fetchPendingRequests(),
        store.fetchSentRequests(),
      ]);
    });

    friendSocket.subscribeFriendReject(async () => {
      await store.fetchSentRequests();
    });

    friendSocket.subscribeFriendCancel(async () => {
      await store.fetchPendingRequests();
    });

    friendSocket.subscribeFriendRemove(async () => {
      await store.fetchFriends();
    });
  };

  const handleSwitchTab = async (tab: "pending") => {
    activeTab.value = tab;
    await Promise.all([
      store.fetchPendingRequests(),
      store.fetchSentRequests(),
    ]);
  };

  onMounted(async () => {
    await Promise.all([
      store.fetchFriends(),
      store.fetchPendingRequests(),
      store.fetchSentRequests(),
    ]);

    subscribeToFriendEvents();
  });

  watch(totalPending, (total) => {
    if (total === 0 && activeTab.value === "pending") {
      activeTab.value = "all";
    }
  });

  return {
    activeTab,
    totalPending,
    handleSwitchTab,
  };
};
