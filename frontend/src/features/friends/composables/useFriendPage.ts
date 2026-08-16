import { computed, onMounted, ref, watch } from "vue";
import { friendSocket } from "../services/friendSocket";
import { useFriendStore } from "../stores/friendStore";
import { useFriendActions } from "./useFriendActions";

type FriendTab = "all" | "pending" | "add";

export const useFriendPage = () => {
  const store = useFriendStore();
  const {
    fetchFriends,
    fetchPendingRequests,
    fetchSentRequests,
  } = useFriendActions();
  const activeTab = ref<FriendTab>("all");

  const totalPending = computed(
    () => store.pendingRequests.length + store.sentRequests.length,
  );

  const subscribeToFriendEvents = () => {
    friendSocket.subscribeFriendRequest(async () => {
      await Promise.all([fetchPendingRequests(), fetchSentRequests()]);
    });

    friendSocket.subscribeFriendAccept(async () => {
      await Promise.all([
        fetchFriends(),
        fetchPendingRequests(),
        fetchSentRequests(),
      ]);
    });

    friendSocket.subscribeFriendReject(async () => {
      await fetchSentRequests();
    });

    friendSocket.subscribeFriendCancel(async () => {
      await fetchPendingRequests();
    });

    friendSocket.subscribeFriendRemove(async () => {
      await fetchFriends();
    });
  };

  const handleSwitchTab = async (tab: "pending") => {
    activeTab.value = tab;
    await Promise.all([fetchPendingRequests(), fetchSentRequests()]);
  };

  onMounted(async () => {
    await Promise.all([fetchFriends(), fetchPendingRequests(), fetchSentRequests()]);

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
