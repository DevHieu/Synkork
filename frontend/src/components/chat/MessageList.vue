<script setup lang="ts">
import { computed, ref, watch, onUnmounted } from "vue";
import MessageItem from "./MessageItem.vue";
import dayjs from "dayjs";
import type { Message } from "@/types/Message";
import WelcomeSpace from "./WelcomeSpace.vue";

const props = defineProps<{
  messages: Message[];
  hasMore: boolean;
  spaceName: string;
  containerRef: (el: HTMLElement | null) => void;
}>();

const emits = defineEmits<{
  (e: "loadMore"): void;
}>();

const isLoading = ref(false);
const container = ref<HTMLElement | null>(null);
const sentinel = ref<HTMLElement | null>(null);
let observer: IntersectionObserver | null = null;

const setRef = (el: HTMLElement | null) => {
  container.value = el;
  props.containerRef(el);
};

const setupObserver = () => {
  if (!sentinel.value || !container.value) return;

  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting && props.hasMore && !isLoading.value) {
        isLoading.value = true;
        emits("loadMore");
      }
    },
    { root: container.value, rootMargin: "200px" },
  );

  observer.observe(sentinel.value);
};

watch(
  () => props.messages.length,
  () => {
    isLoading.value = false;
  },
);

watch(sentinel, (el) => {
  if (el) setupObserver();
});

onUnmounted(() => {
  observer?.disconnect();
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
        <div ref="sentinel" class="h-0" />

        <div v-if="!hasMore">
          <WelcomeSpace :spaceName="props.spaceName" />
        </div>

        <template v-for="msg in processedMessages" :key="msg.id">
          <MessageItem
            :message="msg"
            :isGrouped="msg.isGrouped"
            :isDifferentDay="msg.isDifferentDay"
          />
        </template>
      </div>
    </div>
  </div>
</template>
