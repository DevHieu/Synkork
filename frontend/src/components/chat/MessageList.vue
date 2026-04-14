<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from "vue";
import MessageItem from "./MessageItem.vue";
import dayjs from "dayjs";
import type { Message } from "@/types/Message";
import WelcomeSpace from "./WelcomeSpace.vue";

const props = defineProps<{
  messages: Message[];
  beforeHasMore: boolean;
  afterHasMore: boolean;
  spaceName: string;
  containerRef: (el: HTMLElement | null) => void;
}>();

const emits = defineEmits<{
  (e: "loadBeforeMore"): void;
  (e: "loadAfterMore"): void;
}>();

const isLoading = ref(false);
const container = ref<HTMLElement | null>(null);

// Sentinel là biến gắn trong template để observer có thể theo dõi
const beforeSentinel = ref<HTMLElement | null>(null);
const afterSentinel = ref<HTMLElement | null>(null);

// Observer là cái bắt cái sentinel. Sentinel mà chuẩn bị hiện thì cái callback trong observer sẽ chạy
let beforeObserver: IntersectionObserver | null = null;
let afterObserver: IntersectionObserver | null = null;

const setRef = (el: HTMLElement | null) => {
  container.value = el;
  props.containerRef(el);
};

const setupObserver = () => {
  if (!beforeSentinel.value || !afterSentinel.value || !container.value) return;

  beforeObserver?.disconnect(); // Disconnect cho chắc
  afterObserver?.disconnect();

  beforeObserver = new IntersectionObserver(
    ([entry]) => {
      if (entry?.isIntersecting && props.beforeHasMore && !isLoading.value) {
        isLoading.value = true;
        emits("loadBeforeMore");
      }
    },
    { root: container.value, rootMargin: "200px" },
  );

  afterObserver = new IntersectionObserver(
    ([entry]) => {
      if (entry?.isIntersecting && props.afterHasMore && !isLoading.value) {
        isLoading.value = true;
        emits("loadAfterMore");
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
    const prevMsg = props.messages[index - 1];
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
    <div v-if="isLoading" class="absolute top-2 left-1/2 -translate-x-1/2 z-10">
      <div
        class="w-5 h-5 border-2 border-muted border-t-foreground rounded-full animate-spin"
      />
    </div>

    <div
      :ref="setRef"
      class="h-full overflow-y-auto px-4 py-3"
      style="overflow-anchor: auto"
    >
      <div class="min-h-full flex flex-col justify-end space-y-4">
        <div ref="beforeSentinel" class="h-0" />

        <div v-if="!beforeHasMore">
          <WelcomeSpace :spaceName="props.spaceName" />
        </div>

        <template v-for="msg in processedMessages" :key="msg.id">
          <MessageItem
            :message="msg"
            :isGrouped="msg.isGrouped"
            :isDifferentDay="msg.isDifferentDay"
          />
        </template>

        <div ref="afterSentinel" class="h-0" />
      </div>
    </div>
  </div>
</template>
