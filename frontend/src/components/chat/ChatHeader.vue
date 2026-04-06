<script setup lang="ts">
import { ref } from "vue";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Pin, Search, Users, X } from "lucide-vue-next";

const props = defineProps<{
  spaceName: string;
  memberOpen: boolean;
}>();

const emit = defineEmits<{
  search: [query: string];
  "toggle-members": [];
}>();

const searchActive = ref(false);
const searchQuery = ref("");

const openSearch = () => {
  searchActive.value = true;
};

const closeSearch = () => {
  searchActive.value = false;
  searchQuery.value = "";
  emit("search", "");
};
</script>

<template>
  <div
    class="flex items-center justify-between px-4 py-3 border-b transition-all"
  >
    <!-- Left -->
    <div class="flex items-center gap-2 min-w-0">
      <SidebarTrigger class="-ml-1 shrink-0" />
      <span class="text-muted-foreground text-xl font-light shrink-0">#</span>
      <span class="font-semibold text-[15px] truncate">{{ spaceName }}</span>
    </div>

    <!-- Right -->
    <div class="flex items-center gap-1.5 shrink-0 ml-4">
      <!-- Search: expanded -->
      <template v-if="searchActive">
        <div class="flex items-center gap-1.5 bg-muted rounded-md px-2.5 h-8">
          <Search class="w-3.5 h-3.5 text-muted-foreground shrink-0" />
          <input
            v-model="searchQuery"
            @input="$emit('search', searchQuery)"
            placeholder="Tìm trong kênh..."
            class="bg-transparent border-none outline-none text-[13px] w-44 text-foreground placeholder:text-muted-foreground"
            autofocus
          />
          <button
            @click="closeSearch"
            class="text-muted-foreground hover:text-foreground transition-colors"
          >
            <X class="w-3.5 h-3.5" />
          </button>
        </div>
      </template>

      <!-- Search: collapsed -->
      <template v-else>
        <button
          class="w-8 h-8 rounded-md flex items-center justify-center text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
          title="Tìm kiếm"
          @click="openSearch"
        >
          <Search class="w-4.5 h-4.5" />
        </button>
      </template>

      <!-- Pin -->
      <button
        class="w-8 h-8 rounded-md flex items-center justify-center text-muted-foreground hover:bg-accent hover:text-foreground transition-colors"
        title="Tin nhắn được ghim"
      >
        <Pin class="w-4.5 h-4.5" />
      </button>

      <div class="w-px h-5 bg-border" />

      <!-- Toggle members -->
      <button
        class="w-8 h-8 rounded-md flex items-center justify-center transition-colors"
        :class="
          memberOpen
            ? 'bg-accent text-foreground'
            : 'text-muted-foreground hover:bg-accent hover:text-foreground'
        "
        title="Danh sách thành viên"
        @click="$emit('toggle-members')"
      >
        <Users class="w-4.5 h-4.5" />
      </button>
    </div>
  </div>
</template>
