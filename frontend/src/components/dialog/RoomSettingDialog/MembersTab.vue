<script setup lang="ts">
import { computed, ref } from "vue";
import { Users, Crown, Shield, UserMinus, ChevronDown } from "lucide-vue-next";
import {
  changeMemberAuthority,
  kickMember,
} from "@/services/roomMemberService";
import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { storeToRefs } from "pinia";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import DropdownMenu from "@/components/ui/dropdown-menu/DropdownMenu.vue";
import DropdownMenuTrigger from "@/components/ui/dropdown-menu/DropdownMenuTrigger.vue";
import DropdownMenuContent from "@/components/ui/dropdown-menu/DropdownMenuContent.vue";
import DropdownMenuItem from "@/components/ui/dropdown-menu/DropdownMenuItem.vue";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";

const props = defineProps<{ roomId: string }>();

const roomMemberStore = useRoomMemberStore();
const { members, canManage, isOwner, sortedMembers } =
  storeToRefs(roomMemberStore);

const filterRole = ref("ALL");

const filteredMembers = computed(() => {
  if (filterRole.value === "ALL") return sortedMembers.value;
  return sortedMembers.value.filter((m) => m.role === filterRole.value);
});

const getRoleLabel = (role: string) => {
  if (role === "OWNER") return "Chủ phòng";
  if (role === "ADMIN") return "Quản trị";
  return "Thành viên";
};

const getInitials = (name: string) => {
  if (!name) return "?";
  return name
    .split(" ")
    .map((w) => w[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
};

const handleKick = (username: string) => {
  const member = members.value.find((m) => m.username === username);
  if (!member) return;
  kickMember(member.memberId, props.roomId).catch((err) => {
    console.error("Kick member error:", err);
  });
};

const handleChangeRole = async (memberId: string, newRole: string) => {
  try {
    await changeMemberAuthority(
      { memberId, newRole: newRole as "OWNER" | "ADMIN" | "MEMBER" },
      props.roomId,
    );
  } catch (err) {
    console.error("Change role error:", err);
  }
};
</script>

<template>
  <div class="flex flex-col gap-3">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <p class="text-sm text-muted-foreground">
        {{ members.length }} thành viên
      </p>
      <Button variant="outline" size="sm" class="gap-1.5">
        <Users class="h-3.5 w-3.5" />
        Mời thêm
      </Button>
    </div>

    <!-- Filter -->
    <div class="flex gap-1 p-1 bg-muted rounded-lg w-fit">
      <button
        v-for="f in [
          { key: 'ALL', label: 'Tất cả' },
          { key: 'OWNER', label: 'Chủ phòng' },
          { key: 'ADMIN', label: 'Quản trị' },
          { key: 'MEMBER', label: 'Thành viên' },
        ]"
        :key="f.key"
        @click="filterRole = f.key"
        :class="
          filterRole === f.key
            ? 'bg-background shadow-sm text-foreground'
            : 'text-muted-foreground hover:text-foreground'
        "
        class="px-3 py-1 rounded-md text-xs font-medium transition"
      >
        {{ f.label }}
      </button>
    </div>

    <!-- List -->
    <div class="flex flex-col gap-1">
      <div
        v-for="member in filteredMembers"
        :key="member.username"
        class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-muted/60 group transition"
      >
        <!-- Avatar -->
        <Avatar class="h-8 w-8 shrink-0">
          <AvatarImage :src="member.avatarUrl" />
          <AvatarFallback class="text-xs font-bold">
            {{ getInitials(member.displayName || member.username) }}
          </AvatarFallback>
        </Avatar>

        <!-- Info -->
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium truncate">
            {{ member.displayName || member.username }}
          </p>
          <p class="text-xs text-muted-foreground truncate">
            @{{ member.username }}
          </p>
        </div>

        <!-- Actions -->
        <div
          class="opacity-0 group-hover:opacity-100 transition flex items-center gap-1"
        >
          <AlertDialog>
            <AlertDialogTrigger as-child>
              <Button
                v-if="canManage"
                variant="ghost"
                size="icon"
                class="h-7 w-7 hover:bg-destructive/10 hover:text-destructive text-muted-foreground"
                title="Xóa khỏi phòng"
              >
                <UserMinus class="h-4 w-4" />
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Xóa thành viên khỏi phòng?</AlertDialogTitle>
                <AlertDialogDescription>
                  <span class="font-medium text-foreground"
                    >@{{ member.username }}</span
                  >
                  sẽ bị xóa khỏi phòng và mất toàn bộ quyền truy cập. Hành động
                  này không thể hoàn tác.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Hủy</AlertDialogCancel>
                <AlertDialogAction
                  class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  @click="handleKick(member.username)"
                >
                  Xóa khỏi phòng
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>

          <DropdownMenu v-if="isOwner && member.role !== 'OWNER'">
            <DropdownMenuTrigger as-child>
              <Button
                variant="ghost"
                size="sm"
                class="h-7 gap-1 text-xs text-muted-foreground"
              >
                <Shield class="h-3.5 w-3.5" />
                Đổi quyền
                <ChevronDown class="h-3 w-3" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" class="w-36">
              <DropdownMenuItem
                @click="handleChangeRole(member.memberId, 'OWNER')"
                :class="member.role === 'OWNER' ? 'text-primary' : ''"
              >
                <Crown class="h-3.5 w-3.5 mr-2" /> Chủ phòng
              </DropdownMenuItem>
              <DropdownMenuItem
                @click="handleChangeRole(member.memberId, 'ADMIN')"
                :class="member.role === 'ADMIN' ? 'text-primary' : ''"
              >
                <Shield class="h-3.5 w-3.5 mr-2" /> Quản trị
              </DropdownMenuItem>
              <DropdownMenuItem
                @click="handleChangeRole(member.memberId, 'MEMBER')"
                :class="member.role === 'MEMBER' ? 'text-primary' : ''"
              >
                <Users class="h-3.5 w-3.5 mr-2" /> Thành viên
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <!-- Badge -->
        <Badge
          variant="outline"
          class="shrink-0 gap-1 text-xs font-medium"
          :class="
            member.role === 'OWNER'
              ? 'border-amber-300 bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-700'
              : member.role === 'ADMIN'
                ? 'border-blue-300 bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-700'
                : 'border-transparent'
          "
        >
          <Crown v-if="member.role === 'OWNER'" class="h-2.5 w-2.5" />
          <Shield v-else-if="member.role === 'ADMIN'" class="h-2.5 w-2.5" />
          {{ getRoleLabel(member.role) }}
        </Badge>
      </div>

      <Separator v-if="filteredMembers.length > 0" class="my-1" />

      <p
        v-if="filteredMembers.length === 0"
        class="text-sm text-muted-foreground text-center py-6"
      >
        Không có thành viên nào
      </p>
    </div>
  </div>
</template>
