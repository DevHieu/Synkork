<script setup lang="ts">
import { computed, ref, watch, onUnmounted, nextTick } from "vue";
import { useMessageStore } from "@/stores/messageStore";
import MessageItem from "./MessageItem.vue";
import dayjs from "dayjs";
import type { Message } from "@/types/Message";
import WelcomeSpace from "./WelcomeSpace.vue";

const props = defineProps<{
  messages: Message[];
  beforeHasMore: boolean;
  afterHasMore: boolean;
  spaceId: string;
  spaceName: string;
  isDm: boolean;
  friendName?: string;
}>();

const messageStore = useMessageStore();

const isLoading = ref(false);
const container = ref<HTMLElement | null>(null);

const beforeSentinel = ref<HTMLElement | null>(null);
const afterSentinel = ref<HTMLElement | null>(null);

let beforeObserver: IntersectionObserver | null = null;
let afterObserver: IntersectionObserver | null = null;

const setRef = (el: any) => {
  container.value = el as HTMLElement | null;
  messageStore.setScrollContainer(el as HTMLElement | null); // ← thêm
  if (el) nextTick(() => setupObserver());
};

const handleScroll = () => {
  if (!container.value) return;

  const { scrollTop, scrollHeight, clientHeight } = container.value;
  const isAtBottom = scrollHeight - scrollTop - clientHeight < 50;

  // Lướt xuống dưới rồi thì reset cái jump mode để khi bên dưới cóc tin mới thì tự scroll xuống
  if (isAtBottom) {
    messageStore.setScrollTop(false);
  } else {
    messageStore.setScrollTop(true);
  }
};

const setupObserver = () => {
  if (!beforeSentinel.value || !afterSentinel.value || !container.value) return;

  beforeObserver?.disconnect();
  afterObserver?.disconnect();

  beforeObserver = new IntersectionObserver(
    ([entry]) => {
      if (entry?.isIntersecting && props.beforeHasMore && !isLoading.value) {
        isLoading.value = true;
        messageStore.loadMore(props.spaceId, "later");
      }
    },
    { root: container.value, rootMargin: "200px" },
  );

  afterObserver = new IntersectionObserver(
    ([entry]) => {
      if (entry?.isIntersecting && props.afterHasMore && !isLoading.value) {
        isLoading.value = true;
        messageStore.loadMore(props.spaceId, "newer");
      }
    },
    { root: container.value, rootMargin: "200px" },
  );

  beforeObserver.observe(beforeSentinel.value);
  afterObserver.observe(afterSentinel.value);
};

watch(
  () => props.messages.length,
  () => {
    isLoading.value = false;
  },
);

watch([beforeSentinel, afterSentinel, container], () => {
  setupObserver();
});

onUnmounted(() => {
  beforeObserver?.disconnect();
  afterObserver?.disconnect();
});

const processedMessages = computed(() => {
  return props.messages.map((msg, index) => {
    const prevMsg = props.messages[index + 1];
    const isDifferentDay =
      !prevMsg || !dayjs(msg.createdAt).isSame(dayjs(prevMsg.createdAt), "day");
    const isGrouped =
      prevMsg &&
      !isDifferentDay &&
      prevMsg.sender.username === msg.sender.username &&
      dayjs(msg.createdAt).diff(dayjs(prevMsg.createdAt), "minute") < 5;
    return { ...msg, isGrouped: !!isGrouped, isDifferentDay: !!isDifferentDay };
  });
});
</script>

<template>
  <div class="relative flex-1 overflow-hidden">
    <!-- Loading spinner -->
    <div v-if="isLoading" class="absolute top-2 left-1/2 -translate-x-1/2 z-10">
      <div
        class="w-5 h-5 border-2 border-muted border-t-foreground rounded-full animate-spin"
      />
    </div>

    <!-- Nút scroll to bottom -->
    <Transition name="fade">
      <button
        v-if="messageStore.isScrollTop"
        @click="messageStore.scrollToBottom(props.spaceId)"
        class="absolute bottom-4 right-4 z-10 flex items-center justify-center w-9 h-9 rounded-full bg-background border border-border shadow-md hover:bg-muted transition-colors"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M12 5v14M5 12l7 7 7-7" />
        </svg>
      </button>
    </Transition>

    <div
      :ref="setRef"
      @scroll="handleScroll"
      class="flex h-full flex-col overflow-y-auto px-4 py-3"
    >
      <div v-if="!beforeHasMore">
        <WelcomeSpace
          :spaceName="props.spaceName"
          :isDm="props.isDm"
          :friendName="props.friendName"
        />
      </div>

      <div ref="beforeSentinel" class="h-px" />

      <div class="mt-auto">
        <template v-for="msg in [...processedMessages].reverse()" :key="msg.id">
          <MessageItem
            :message="msg"
            :isGrouped="msg.isGrouped"
            :isDifferentDay="msg.isDifferentDay"
          />
        </template>
      </div>

      <div ref="afterSentinel" class="h-px" />
    </div>
  </div>
</template>
