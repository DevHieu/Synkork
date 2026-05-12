<script setup lang="ts">
import { ref, watch } from "vue";
import { searchMessage } from "@/services/chatService";
import { useMessageStore } from "@/stores/messageStore";
import { ArrowRight, Search, X } from "lucide-vue-next";
import { refDebounced } from "@vueuse/core";
import type { Message } from "@/types/Message";
import dayjs from "dayjs";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import Avatar from "@/components/ui/avatar/Avatar.vue";
import AvatarImage from "@/components/ui/avatar/AvatarImage.vue";
import AvatarFallback from "@/components/ui/avatar/AvatarFallback.vue";
import Badge from "@/components/ui/badge/Badge.vue";

const props = defineProps<{
  spaceId: string;
}>();

const messageStore = useMessageStore();

const searchKeyword = ref("");
const searchResults = ref<Message[]>([]);
const isSearching = ref(false);
const searchActive = ref(false);
const LIMIT = 5;

const searchCursor = ref<string | null>(null);
const searchHasMore = ref(false);

const debouncedInput = refDebounced(searchKeyword, 500);

const openSearch = () => {
  searchActive.value = true;
};

const close = () => {
  searchActive.value = false;
  searchKeyword.value = "";
  clearSearch();
};

watch(debouncedInput, (val) => {
  if (val.trim()) {
    handleSearch(val, true);
  } else {
    clearSearch();
  }
});

const handleSearch = async (val: string, reset = false) => {
  if (reset) {
    searchCursor.value = null;
    searchResults.value = [];
  }

  isSearching.value = true;

  const res = await searchMessage(
    props.spaceId,
    val,
    searchCursor.value,
    LIMIT,
  );
  searchResults.value = reset
    ? res.messages
    : [...searchResults.value, ...res.messages];
  searchHasMore.value = res.beforeHasMore;
  searchCursor.value = res.beforeCursor ?? null;
};

const loadMore = async () => {
  if (!searchHasMore.value || !searchKeyword.value.trim()) return;
  await handleSearch(searchKeyword.value, false);
};

const clearSearch = () => {
  isSearching.value = false;
  searchResults.value = [];
  searchHasMore.value = false;
  searchCursor.value = null;
};

const jumpToMessage = async (messageId: string) => {
  close();
  await messageStore.jumpToMessage(props.spaceId, messageId);
};
</script>

<template>
  <div class="relative">
    <!-- Collapsed -->
    <button
      v-if="!searchActive"
      class="w-8 h-8 rounded-md flex items-center justify-center text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
      @click="openSearch"
    >
      <Search class="w-4 h-4 text-foreground/70" />
    </button>

    <!-- Expanded -->
    <div
      v-else
      class="flex items-center gap-1.5 bg-muted rounded-md px-2.5 h-8"
    >
      <Search class="w-3.5 h-3.5 text-foreground/70 shrink-0" />
      <input
        v-model="searchKeyword"
        placeholder="Tìm trong kênh..."
        class="bg-transparent border-none outline-none text-[13px] w-44 text-foreground placeholder:text-muted-foreground"
        autofocus
      />
      <button
        @click="close"
        class="text-muted-foreground hover:text-foreground transition-colors"
      >
        <X class="w-3.5 h-3.5" />
      </button>
    </div>

    <!-- Results Popover -->
    <div
      v-if="isSearching && searchActive"
      class="absolute top-full right-0 mt-2 w-96 z-50"
    >
      <Card class="shadow-xl border overflow-hidden p-0">
        <CardContent class="p-0 max-h-72 overflow-y-auto">
          <div
            v-if="searchResults.length === 0"
            class="flex flex-col items-center justify-center py-8 gap-2"
          >
            <Search class="w-8 h-8 text-foreground/70" />
            <p class="text-sm text-muted-foreground">Không tìm thấy kết quả</p>
          </div>

          <div v-else>
            <div
              v-for="msg in searchResults"
              :key="msg.id"
              @click="jumpToMessage(msg.id)"
              class="flex items-start gap-3 px-3 py-2.5 hover:bg-accent cursor-pointer border-b last:border-0 transition-colors group"
            >
              <Avatar class="w-7 h-7 shrink-0 mt-0.5">
                <AvatarImage
                  v-if="msg.sender?.avatarUrl"
                  :src="msg.sender.avatarUrl"
                />
                <AvatarFallback
                  class="text-[10px] bg-primary text-primary-foreground"
                >
                </AvatarFallback>
              </Avatar>

              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2 mb-0.5">
                  <span class="text-xs font-semibold text-foreground">{{
                    msg.sender?.displayName
                  }}</span>
                  <span class="text-[10px] text-muted-foreground shrink-0">
                    {{ dayjs(msg.createdAt).format("DD/MM HH:mm") }}
                  </span>
                </div>
                <p
                  class="text-xs text-muted-foreground truncate leading-relaxed"
                >
                  {{ msg.content }}
                </p>
              </div>

              <ArrowRight
                class="w-3.5 h-3.5 text-muted-foreground shrink-0 mt-1 opacity-0 group-hover:opacity-100 transition-opacity"
              />
            </div>
          </div>
          <div
            v-if="searchHasMore"
            class="px-3 py-1.5 border-t bg-muted/30 justify-center"
          >
            <button
              @click="loadMore"
              class="text-xs text-primary hover:underline font-medium"
            >
              Xem thêm kết quả
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
