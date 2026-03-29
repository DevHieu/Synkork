<script setup lang="ts">
import { ref } from "vue";
import { Users, Crown, Shield, UserMinus } from "lucide-vue-next";

const members = ref([
  {
    id: "1",
    name: "Nguyễn Văn A",
    email: "a@example.com",
    role: "OWNER",
    avatar: "NV",
  },
  {
    id: "2",
    name: "Trần Thị B",
    email: "b@example.com",
    role: "ADMIN",
    avatar: "TT",
  },
  {
    id: "3",
    name: "Lê Văn C",
    email: "c@example.com",
    role: "MEMBER",
    avatar: "LV",
  },
  {
    id: "4",
    name: "Phạm Thị D",
    email: "d@example.com",
    role: "MEMBER",
    avatar: "PT",
  },
]);

const roleLabel: Record<string, string> = {
  OWNER: "Chủ phòng",
  ADMIN: "Quản trị",
  MEMBER: "Thành viên",
};

const handleKickMember = (id: string) => {
  members.value = members.value.filter((m) => m.id !== id);
};
</script>

<template>
  <div>
    <div class="flex items-center justify-between">
      <p class="text-sm text-muted-foreground">
        {{ members.length }} thành viên
      </p>
      <button
        class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-input text-sm font-medium text-foreground hover:bg-muted transition"
      >
        <Users class="h-3.5 w-3.5" />
        Mời thêm
      </button>
    </div>

    <div class="flex flex-col gap-1">
      <div
        v-for="member in members"
        :key="member.id"
        class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-muted/60 group transition"
      >
        <!-- Avatar -->
        <div
          class="w-8 h-8 rounded-full bg-primary/20 text-primary text-xs font-bold flex items-center justify-center shrink-0"
        >
          {{ member.avatar }}
        </div>

        <!-- Info -->
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-foreground truncate">
            {{ member.name }}
          </p>
          <p class="text-xs text-muted-foreground truncate">
            {{ member.email }}
          </p>
        </div>

        <!-- Role badge -->
        <span
          :class="[
            'text-xs px-2 py-0.5 rounded-full font-medium shrink-0',
            member.role === 'OWNER'
              ? 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400'
              : member.role === 'ADMIN'
                ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
                : 'bg-muted text-muted-foreground',
          ]"
        >
          <span class="flex items-center gap-1">
            <Crown v-if="member.role === 'OWNER'" class="h-2.5 w-2.5" />
            <Shield v-else-if="member.role === 'ADMIN'" class="h-2.5 w-2.5" />
            {{ roleLabel[member.role] }}
          </span>
        </span>

        <!-- Kick button (ẩn với owner) -->
        <button
          v-if="member.role !== 'OWNER'"
          @click="handleKickMember(member.id)"
          class="opacity-0 group-hover:opacity-100 transition p-1 rounded hover:bg-destructive/10 hover:text-destructive text-muted-foreground"
          title="Xóa khỏi phòng"
        >
          <UserMinus class="h-4 w-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
