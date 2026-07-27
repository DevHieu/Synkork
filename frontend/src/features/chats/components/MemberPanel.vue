<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Separator } from "@/components/ui/separator";
import MemberSection from "@/components/member/MemberSection.vue";
import MemberRow from "@/components/member/MemberRow.vue";

const roomMemberStore = useRoomMemberStore();
const { owners, admins, regularMembers, loading } =
  storeToRefs(roomMemberStore);
</script>

<template>
  <aside class="hidden lg:flex flex-col w-full h-full shrink-0 overflow-hidden" style="
      background: color-mix(in oklch, var(--sidebar) 40%, transparent);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
    ">
    <div class="px-3 pt-4 pb-2 text-[11px] font-medium tracking-widest uppercase"
      style="color: var(--sidebar-foreground); opacity: 0.6">
      Thành viên
    </div>

    <ScrollArea class="flex-1 pb-4">
      <!-- Loading skeleton -->
      <template v-if="loading">
        <div class="px-2 py-1 space-y-1">
          <div class="px-3 py-2">
            <div class="h-2.5 w-16 bg-muted animate-pulse rounded" />
          </div>
          <div v-for="i in 5" :key="i" class="flex items-center gap-2.5 px-3 py-1.5" :style="{ opacity: 1 - i * 0.15 }">
            <div class="h-8 w-8 rounded-full bg-muted animate-pulse shrink-0" />
            <div class="flex-1 space-y-1.5">
              <div class="h-2.5 bg-muted animate-pulse rounded" :style="{ width: `${65 - i * 5}%` }" />
              <div class="h-2 bg-muted animate-pulse rounded w-1/3" />
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <template v-if="owners.length">
          <MemberSection label="OWNER" :count="owners.length">
            <MemberRow v-for="m in owners" :key="m.username" :member="m" badge="OWNER" />
          </MemberSection>
          <Separator class="mx-3 my-2 w-auto" />
        </template>

        <template v-if="admins.length">
          <MemberSection label="Quản trị viên" :count="admins.length">
            <MemberRow v-for="m in admins" :key="m.username" :member="m" badge="ADMIN" />
          </MemberSection>
          <Separator class="mx-3 my-2 w-auto" />
        </template>

        <template v-if="regularMembers.length">
          <MemberSection label="Thành viên" :count="regularMembers.length">
            <MemberRow v-for="m in regularMembers" :key="m.username" :member="m" />
          </MemberSection>
        </template>
      </template>
    </ScrollArea>
  </aside>
</template>
