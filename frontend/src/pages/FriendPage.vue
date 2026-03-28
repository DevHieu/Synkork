<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { useFriendStore } from "@/stores/useFriendStore"

const store = useFriendStore()

// State
const activeTab = ref<"all" | "add">("all")
const searchAll = ref("")      
const searchAdd = ref("")      
const isSending = ref(false)   

// Load danh sách bạn bè khi vào trang
onMounted(() => {
  store.fetchFriends()
})

// Computed cho tab Tất cả
const filteredFriends = computed(() => {
  if (!searchAll.value.trim()) return store.friends
  
  const term = searchAll.value.toLowerCase().trim()
  return store.friends.filter(f => 
    f.name?.toLowerCase().includes(term)
  )
})

// Gửi lời mời kết bạn
const sendFriendRequest = async () => {
  const username = searchAdd.value.trim()
  if (!username) return

  isSending.value = true
  try {
    await store.sendRequest(username)
    searchAdd.value = ""                    
  } catch (error: any) {
    console.error("Gửi lời mời thất bại:", error)
  } finally {
    isSending.value = false
  }
}
</script>

<template>
  <div class="h-full flex flex-col background text-foreground">

    <!-- TOP NAV -->
    <div class="h-12 border-b border-border flex items-center px-4 gap-4 flex-shrink-0">
      <div class="flex items-center gap-2">
        <span class="text-xl">👥</span>
        <span class="font-semibold">Bạn bè</span>
      </div>

      <div class="flex gap-1 bg-muted rounded-md p-0.5">
        <button
          @click="activeTab = 'all'"
          :class="activeTab === 'all' ? 'bg-card text-foreground' : 'text-muted-foreground hover:text-foreground'"
          class="px-5 py-1.5 text-sm font-medium rounded transition"
        >
          Tất cả
        </button>
        <button
          @click="activeTab = 'add'"
          :class="activeTab === 'add' ? 'bg-primary text-primary-foreground' : 'text-muted-foreground hover:text-foreground'"
          class="px-5 py-1.5 text-sm font-medium rounded transition"
        >
          Thêm Bạn
        </button>
      </div>
    </div>

    <!-- TAB TẤT CẢ -->
    <div v-if="activeTab === 'all'" class="flex-1 flex flex-col">
      <div class="p-4">
        <div class="relative">
          <input
            v-model="searchAll"
            placeholder="Tìm kiếm bạn bè..."
            class="w-full bg-muted border border-border focus:border-primary rounded-md px-4 py-2.5 pl-11 text-sm"
          />
          <div class="absolute left-4 top-3 text-muted-foreground">🔍</div>
        </div>
      </div>

      <div class="px-6 text-xs uppercase text-muted-foreground mb-2">
        TẤT CẢ BẠN BÈ — {{ filteredFriends.length }}
      </div>

      <div class="flex-1 overflow-y-auto px-2">
        <!-- Loading -->
        <div v-if="store.loading" class="text-center py-10 text-muted-foreground">
          Đang tải danh sách bạn bè...
        </div>

        <!-- Có bạn bè -->
        <div v-else-if="filteredFriends.length > 0">
          <div
            v-for="f in filteredFriends"
            :key="f.id"
            class="group flex items-center gap-3 px-4 py-3 mx-2 rounded hover:bg-card transition"
          >
            <div class="relative w-10 h-10 flex-shrink-0">
              <div class="w-10 h-10 rounded-full bg-muted flex items-center justify-center text-sm font-bold overflow-hidden border border-border">
                <img v-if="f.avatarUrl" :src="f.avatarUrl" class="w-full h-full object-cover" alt="avatar" />
                <span v-else>{{ f.name?.slice(0, 2).toUpperCase() }}</span>
              </div>
              <div class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 rounded-full border-2 border-card bg-muted-foreground"></div>
            </div>

            <div class="flex-1 min-w-0">
              <div class="font-medium truncate">{{ f.name }}</div>
              <div class="text-xs text-muted-foreground">Ngoại tuyến</div>
            </div>

            <div class="opacity-0 group-hover:opacity-100 flex gap-1">
              <button class="w-8 h-8 hover:bg-muted rounded flex items-center justify-center">💬</button>
              <button class="w-8 h-8 hover:bg-muted rounded flex items-center justify-center">⋮</button>
            </div>
          </div>
        </div>

        <!-- Không có bạn bè -->
        <div v-else class="text-center py-20 text-muted-foreground">
          Chưa có bạn bè nào.<br>
          Hãy chuyển sang tab <span class="text-primary">"Thêm Bạn"</span> để bắt đầu kết nối!
        </div>
      </div>
    </div>

    <!-- TAB THÊM BẠN -->
    <div v-else class="flex-1 flex flex-col items-center justify-center p-8">
      <div class="max-w-md w-full text-center">
        <h1 class="text-3xl font-semibold mb-3">Thêm Bạn</h1>
        <p class="text-muted-foreground mb-8">
          Nhập tên người dùng Discord của họ để gửi lời mời kết bạn.
        </p>

        <div class="flex gap-3">
          <input
            v-model="searchAdd"
            placeholder="Nhập tên người dùng Discord"
            class="flex-1 bg-muted border border-border focus:border-primary rounded-md px-5 py-3 text-sm"
            @keyup.enter="sendFriendRequest"
          />
          <button 
            @click="sendFriendRequest"
            :disabled="isSending || !searchAdd.trim()"
            class="bg-primary hover:bg-primary/90 disabled:bg-primary/70 disabled:cursor-not-allowed 
                   text-primary-foreground px-6 py-3 rounded-md font-medium transition flex items-center gap-2"
          >
            <span v-if="isSending">Đang gửi...</span>
            <span v-else>Gửi Yêu Cầu</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>