<script setup lang="ts">
import { ref, computed } from "vue";
import { useFriendStore } from "../stores/friendStore";
import { useRouter } from "vue-router";
import type { Friend } from "../types/Friends";
import { Search } from "lucide-vue-next";

const store = useFriendStore();
const router = useRouter();

const searchAll = ref("");
const openMenuId = ref<string | null>(null);
const confirmRemoveId = ref<string | null>(null);
const removingId = ref<string | null>(null);

const filteredFriends = computed(() => {
  if (!searchAll.value.trim()) return store.friends;
  const term = searchAll.value.toLowerCase().trim();
  return store.friends.filter((f) => f.name?.toLowerCase().includes(term));
});

const toggleMenu = (id: string, e: MouseEvent) => {
  e.stopPropagation();
  openMenuId.value = openMenuId.value === id ? null : id;
  confirmRemoveId.value = null;
};

const closeMenu = () => {
  openMenuId.value = null;
  confirmRemoveId.value = null;
};

const openChat = (f: Friend) => {
  closeMenu();
  router.push(`/me/${f.id}`);
};

const askConfirmRemove = (id: string, e: MouseEvent) => {
  e.stopPropagation();
  confirmRemoveId.value = id;
  openMenuId.value = null;
};

const handleRemove = async (friendId: string) => {
  removingId.value = friendId;
  confirmRemoveId.value = null;
  try {
    await store.removeFriend(friendId);
  } finally {
    removingId.value = null;
  }
};
</script>

<template>
  <div class="flex-1 flex flex-col" @click="closeMenu">
    <div class="p-4">
      <div class="relative">
        <input v-model="searchAll" placeholder="Tìm kiếm bạn bè..."
          class="w-full bg-muted border border-border focus:border-primary rounded-md px-4 py-2.5 pl-11 text-sm" />
        <div class="absolute left-4 top-3 text-muted-foreground">
          <Search class="w-4 h-4" />
        </div>
      </div>
    </div>

    <div class="px-6 text-xs uppercase text-muted-foreground mb-2">
      TẤT CẢ BẠN BÈ — {{ filteredFriends.length }}
    </div>

    <div class="flex-1 overflow-y-auto px-2">
      <div v-if="store.loading" class="text-center py-10 text-muted-foreground">
        Đang tải danh sách bạn bè...
      </div>

      <div v-else-if="filteredFriends.length > 0">
        <div v-for="f in filteredFriends" :key="f.id"
          class="group relative flex items-center gap-3 px-4 py-3 mx-2 rounded hover:bg-card transition cursor-pointer"
          @click="openChat(f)">
          <!-- Avatar -->
          <div class="relative w-10 h-10 flex-shrink-0">
            <div
              class="w-10 h-10 rounded-full bg-muted flex items-center justify-center text-sm font-bold overflow-hidden border border-border">
              <img v-if="f.avatarUrl" :src="f.avatarUrl" class="w-full h-full object-cover" alt="avatar" />
              <span v-else>{{ f.name?.slice(0, 2).toUpperCase() }}</span>
            </div>
            <div class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full border-2 border-card"
              :class="f.isOnline ? 'bg-green-500' : 'bg-gray-400'" />
          </div>

          <!-- Info -->
          <div class="flex-1 min-w-0">
            <div class="font-medium truncate">{{ f.name }}</div>
            <div class="text-xs text-muted-foreground">
              {{ f.isOnline ? "Đang hoạt động" : "Ngoại tuyến" }}
            </div>
          </div>

          <!-- Nút nhắn tin + dấu ... dọc (hiện khi hover) -->
          <div class="opacity-0 group-hover:opacity-100 flex gap-1 items-center" @click.stop>
            <!-- Nút ⋮ dọc -->
            <button class="w-8 h-8 hover:bg-muted rounded flex items-center justify-center text-muted-foreground"
              title="Thêm" @click="toggleMenu(f.id, $event)">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
                <circle cx="12" cy="5" r="1.5" />
                <circle cx="12" cy="12" r="1.5" />
                <circle cx="12" cy="19" r="1.5" />
              </svg>
            </button>
          </div>

          <!-- Dropdown menu -->
          <div v-if="openMenuId === f.id"
            class="absolute right-2 top-12 z-50 min-w-[140px] rounded-md border border-border bg-popover shadow-lg py-1"
            @click.stop>
            <button
              class="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-muted text-red-400 hover:text-red-500 transition"
              @click="askConfirmRemove(f.id, $event)">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6l-1 14H6L5 6" />
                <path d="M10 11v6M14 11v6" />
                <path d="M9 6V4h6v2" />
              </svg>
              Xóa bạn
            </button>
            <button class="w-full flex items-center gap-2 px-3 py-2 text-sm hover:bg-muted text-foreground transition"
              @click.stop="closeMenu">
              <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
              </svg>
              Chặn
            </button>
          </div>

          <!-- Xác nhận xóa -->
          <div v-if="confirmRemoveId === f.id"
            class="absolute right-2 top-12 z-50 min-w-[200px] rounded-md border border-border bg-popover shadow-lg p-3"
            @click.stop>
            <p class="text-sm mb-3">
              Xóa <span class="font-semibold">{{ f.name }}</span> khỏi danh sách
              bạn bè?
            </p>
            <div class="flex gap-2 justify-end">
              <button class="text-xs px-3 py-1.5 rounded bg-muted hover:bg-muted/80 text-muted-foreground transition"
                @click="confirmRemoveId = null">
                Huỷ
              </button>
              <button
                class="text-xs px-3 py-1.5 rounded bg-red-500 hover:bg-red-600 text-white font-medium transition disabled:opacity-60"
                :disabled="removingId === f.id" @click="handleRemove(f.id)">
                {{ removingId === f.id ? "Đang xóa..." : "Xóa" }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-20 text-muted-foreground">
        Chưa có bạn bè nào.<br />
        Hãy chuyển sang tab <span class="text-primary">"Thêm Bạn"</span> để bắt
        đầu kết nối!
      </div>
    </div>
  </div>
</template>
