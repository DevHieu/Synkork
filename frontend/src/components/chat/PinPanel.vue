<script setup lang="ts">
import { ref, computed } from "vue";
import { storeToRefs } from "pinia";
import { useMessageStore } from "@/stores/messageStore";
import { Pin, Search } from "lucide-vue-next";

const props = defineProps<{
  spaceId: string;
}>();

const messageStore = useMessageStore();
const { pinnedMessages, pinnedHasMore, pinLoading } = storeToRefs(messageStore);

const searchQuery = ref("");

const filtered = computed(() => {
  if (!searchQuery.value.trim()) return pinnedMessages.value;
  const q = searchQuery.value.toLowerCase();
  return pinnedMessages.value.filter((m) =>
    m.content.toLowerCase().includes(q),
  );
});

const handleUnpin = (messageId: string) => {
  messageStore.changePinStatus(props.spaceId, messageId);
};

const loadMore = async () => {
  await messageStore.fetchPinnedList(props.spaceId, messageStore.pinnedCursor);
};
</script>

<template>
  <aside
    class="hidden lg:flex flex-col w-full h-full shrink-0 overflow-hidden"
    style="
      background: color-mix(in oklch, var(--sidebar) 40%, transparent);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
    "
  >
    <!-- Header -->
    <div class="px-3 pt-4 pb-2 flex items-center justify-between">
      <div
        class="flex items-center gap-1.5 text-[11px] font-medium tracking-widest uppercase"
        style="color: var(--sidebar-foreground); opacity: 0.6"
      >
        <Pin class="w-3 h-3" />
        Tin nhắn ghim
      </div>
    </div>

    <!-- Search -->
    <div class="px-3 pb-2">
      <div
        class="flex items-center gap-1.5 rounded-md px-2.5 h-7 bg-background/60 border"
        style="border-color: var(--border)"
      >
        <Search class="w-3.5 h-3.5 text-foreground" />
        <input
          v-model="searchQuery"
          placeholder="Tìm trong ghim..."
          class="flex-1 bg-transparent border-none outline-none text-[12px] text-foreground placeholder:text-muted-foreground"
        />
      </div>
    </div>

    <!-- List -->
    <div ref="scrollContainer" class="flex-1 overflow-y-auto pb-4">
      <!-- Loading ban đầu -->
      <div
        v-if="pinLoading && !pinnedMessages.length"
        class="px-2 py-1 space-y-2"
      >
        <div
          v-for="i in 4"
          :key="i"
          class="px-3 py-2.5 rounded-md space-y-1.5"
          :style="{ opacity: 1 - i * 0.2 }"
        >
          <div class="flex items-center gap-1.5">
            <div class="w-4 h-4 rounded-full bg-muted animate-pulse" />
            <div class="h-2.5 w-20 bg-muted animate-pulse rounded" />
          </div>
          <div class="h-2 bg-muted animate-pulse rounded w-full" />
          <div class="h-2 bg-muted animate-pulse rounded w-3/4" />
        </div>
      </div>

      <!-- Empty -->
      <div
        v-else-if="!pinnedMessages.length"
        class="flex flex-col items-center justify-center gap-2 px-4 py-12 text-center"
      >
        <Pin class="w-7 h-7 text-muted-foreground/40" />
        <p class="text-[12px] text-muted-foreground">Chưa có tin nhắn ghim</p>
      </div>

      <!-- No result -->
      <div
        v-else-if="!filtered.length"
        class="px-4 py-8 text-center text-[12px] text-muted-foreground"
      >
        Không tìm thấy kết quả
      </div>

      <!-- Items -->
      <div v-else class="flex flex-col px-2 gap-1">
        <div
          v-for="msg in filtered"
          :key="msg.id"
          class="group relative rounded-lg px-3 py-3 cursor-pointer transition-all duration-200 border border-transparent hover:border-border hover:bg-accent/70"
          @click="messageStore.jumpToMessage(props.spaceId, msg.id)"
        >
          <!-- Header -->
          <div class="flex items-center gap-2 mb-1.5">
            <img
              v-if="msg.sender?.avatarUrl"
              :src="msg.sender.avatarUrl"
              class="w-5 h-5 rounded-full object-cover shrink-0"
            />
            <div
              v-else
              class="w-5 h-5 rounded-full bg-muted flex items-center justify-center text-[9px] font-semibold text-muted-foreground"
            >
              {{ msg.sender?.displayName?.charAt(0).toUpperCase() ?? "?" }}
            </div>

            <span class="text-[12px] font-semibold text-foreground truncate">
              {{ msg.sender?.displayName ?? msg.sender?.username }}
            </span>

            <span class="text-[10px] text-muted-foreground ml-auto shrink-0">
              {{
                new Date(msg.createdAt).toLocaleTimeString("vi-VN", {
                  hour: "2-digit",
                  minute: "2-digit",
                })
              }}
            </span>
          </div>

          <!-- Content -->
          <p
            class="text-[13px] text-muted-foreground leading-relaxed line-clamp-3 group-hover:text-foreground transition-colors"
          >
            {{ msg.content }}
          </p>

          <!-- Unpin -->
          <button
            class="absolute bottom-2 right-2 opacity-0 group-hover:opacity-100 transition text-[10px] text-muted-foreground hover:text-destructive px-1.5 py-0.5 rounded-md border border-transparent hover:border-destructive/40 hover:bg-destructive/5"
            :disabled="pinLoading"
            @click.stop="handleUnpin(msg.id)"
          >
            Bỏ ghim
          </button>
        </div>
        <!-- Load more -->
        <button
          v-if="pinnedHasMore"
          class="w-full mt-1 py-1.5 text-[11px] text-muted-foreground hover:text-foreground hover:bg-accent/60 rounded-md transition-colors flex items-center justify-center gap-1.5"
          :disabled="pinLoading"
          @click="loadMore"
        >
          <div
            v-if="pinLoading"
            class="w-3 h-3 border-2 border-muted border-t-foreground rounded-full animate-spin"
          />
          <span>{{ pinLoading ? "Đang tải..." : "Xem thêm" }}</span>
        </button>
      </div>
    </div>
  </aside>
</template>
