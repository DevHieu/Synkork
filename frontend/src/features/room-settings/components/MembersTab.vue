<script setup lang="ts">
import { computed, ref, watch } from "vue";
import {
  Users,
  Crown,
  Shield,
  UserMinus,
  ChevronDown,
  MoreVertical,
  MicOff,
  VolumeX,
  MessageSquareOff,
} from "lucide-vue-next";
import { useMemberService } from "@/features/members/services/roomMemberService";
import type { ChatDisableTime, Member } from "@/features/members/types/Member";
import { useRoomMemberStore } from "@/features/members/stores/roomMemberStore";
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
} from "@/components/ui/alert-dialog";
import DropdownMenuCheckboxItem from "@/components/ui/dropdown-menu/DropdownMenuCheckboxItem.vue";
import DropdownMenuSeparator from "@/components/ui/dropdown-menu/DropdownMenuSeparator.vue";
import DropdownMenuSub from "@/components/ui/dropdown-menu/DropdownMenuSub.vue";
import DropdownMenuSubContent from "@/components/ui/dropdown-menu/DropdownMenuSubContent.vue";
import DropdownMenuSubTrigger from "@/components/ui/dropdown-menu/DropdownMenuSubTrigger.vue";
import InviteMemberDialog from "@/components/dialog/InviteMemberDialog.vue";
import { useMemberComposable } from "@/features/members/composables/memberComposable";

type AudioToggleField = "muted" | "deafen";

const props = defineProps<{ roomId: string }>();

const showInviteDialog = ref(false);

const memberService = useMemberService();
const memberComposable = useMemberComposable();
const roomMemberStore = useRoomMemberStore();
const { members, canManage, isOwner, sortedMembers } =
  storeToRefs(roomMemberStore);

const filterRole = ref("ALL");
const memberToKick = ref<Member | null>(null);

const showOwnerTransferDialog = ref(false)
const ownerTransferTarget = ref<Member | null>(null);
const ownerTransferLoading = ref(false);

const chatDisableOptions: { value: ChatDisableTime; label: string }[] = [
  { value: "NOT_DISABLE", label: "Bỏ chặn chat" },
  { value: "MINUTE", label: "1 phút" },
  { value: "FIVE_MINUTES", label: "5 phút" },
  { value: "FIFTEEN_MINUTES", label: "15 phút" },
  { value: "HOUR", label: "1 giờ" },
  { value: "DAY", label: "1 ngày" },
  { value: "WEEK", label: "1 tuần" },
];

const filteredMembers = computed(() => {
  if (filterRole.value === "ALL") return sortedMembers.value;
  return sortedMembers.value.filter((m) => m.role === filterRole.value);
});

const getRoleLabel = (role: string) => {
  if (role === "OWNER") return "Chủ phòng";
  if (role === "ADMIN") return "Quản trị";
  return "Thành viên";
};

const isChatDisabled = (member: Member) => {
  if (!member.chatDisableUntil) return false;
  return new Date(member.chatDisableUntil).getTime() > Date.now();
};

const canModerateMember = (member: Member) => {
  if (!canManage.value || member.role === "OWNER") return false;
  if (isOwner.value) return true;
  return member.role === "MEMBER";
};

const handleKick = (member: Member | null) => {
  if (!member) return;
  memberService.kickMember(member.memberId, props.roomId)
    .then(() => {
      members.value = members.value.filter((m) => m.memberId !== member.memberId);
      memberToKick.value = null;
    })
    .catch((err) => {
      console.error("Kick member error:", err);
    });
};

const handleChangeRole = async (member: Member, newRole: "OWNER" | "ADMIN" | "MEMBER") => {
  if (newRole === "OWNER" && ownerTransferTarget.value == null) {
    ownerTransferTarget.value = member;
    showOwnerTransferDialog.value = true
    return;
  }

  try {
    await memberService.changeMemberAuthority(
      { memberId: member.memberId, newRole },
      props.roomId,
    );
  } catch (err) {
    console.error("Change role error:", err);
  }
};

const handleConfirmOwnerTransfer = async () => {
  const target = ownerTransferTarget.value;
  if (!target) return;

  ownerTransferLoading.value = true;

  try {
    await handleChangeRole(target, "OWNER");
    showOwnerTransferDialog.value = false;
    ownerTransferTarget.value = null;
  } catch (err) {
    console.error("Transfer owner error:", err);
  } finally {
    ownerTransferLoading.value = false;
  }
};

const handleOwnerTransferDialogOpenChange = (open: boolean) => {
  if (!open && !ownerTransferLoading.value) {
    ownerTransferTarget.value = null;
  }
};

const handleToggleAudioState = async (member: Member, field: AudioToggleField) => {
  const newValue = !member[field];

  try {
    await memberService.muteAudio(props.roomId, member.memberId, {
      muted: field === "muted" ? newValue : null,
      deafen: field === "deafen" ? newValue : null,
    });

    memberComposable.updateMember({ ...member, [field]: newValue });
  } catch (err) {
    console.error(`Toggle ${field} error:`, err);
  }
};

const handleChangeChatDisable = async (
  member: Member,
  time: ChatDisableTime,
) => {
  try {
    const updatedMember = await memberService.muteChatMember(
      props.roomId,
      member.memberId,
      time,
    );

    memberComposable.updateMember(updatedMember);
  } catch (err) {
    console.error("Change chat disable error:", err);
  }
};


watch(filteredMembers, (newMember) => {
  console.log(newMember);

})
</script>

<template>
  <div class="flex flex-col gap-3">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <p class="text-sm text-muted-foreground">
        {{ members.length }} thành viên
      </p>
      <Button variant="outline" size="sm" class="gap-1.5" @click="showInviteDialog = true">
        <Users class="h-3.5 w-3.5" />
        Mời thêm
      </Button>
    </div>

    <!-- Filter -->
    <div class="flex gap-1 p-1 bg-muted rounded-lg w-fit">
      <button v-for="f in [
        { key: 'ALL', label: 'Tất cả' },
        { key: 'OWNER', label: 'Chủ phòng' },
        { key: 'ADMIN', label: 'Quản trị' },
        { key: 'MEMBER', label: 'Thành viên' },
      ]" :key="f.key" @click="filterRole = f.key" :class="filterRole === f.key
        ? 'bg-background shadow-sm text-foreground'
        : 'text-muted-foreground hover:text-foreground'
        " class="px-3 py-1 rounded-md text-xs font-medium transition">
        {{ f.label }}
      </button>
    </div>

    <!-- List -->
    <div class="flex flex-col gap-1">
      <div v-for="member in filteredMembers" :key="member.username"
        class="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-muted/60 group transition">
        <!-- Avatar -->
        <Avatar class="h-8 w-8 shrink-0">
          <AvatarImage v-if="member.avatarUrl" :src="member.avatarUrl ?? undefined" />
          <AvatarFallback class="text-xs font-bold"> </AvatarFallback>
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
        <div class="opacity-0 group-hover:opacity-100 transition flex items-center gap-1">
          <DropdownMenu v-if="canManage && member.role !== 'OWNER'">
            <DropdownMenuTrigger as-child>
              <Button variant="ghost" size="sm" class="h-7 gap-1 text-xs text-muted-foreground">
                <Shield class="h-3.5 w-3.5" />
                Đổi quyền
                <ChevronDown class="h-3 w-3" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" class="w-36">
              <DropdownMenuItem @click="handleChangeRole(member, 'OWNER')">
                <Crown class="h-3.5 w-3.5 mr-2" /> Chủ phòng
              </DropdownMenuItem>
              <DropdownMenuItem @click="handleChangeRole(member, 'ADMIN')"
                :class="member.role === 'ADMIN' ? 'text-primary' : ''">
                <Shield class="h-3.5 w-3.5 mr-2" /> Quản trị
              </DropdownMenuItem>
              <DropdownMenuItem @click="handleChangeRole(member, 'MEMBER')">
                <Users class="h-3.5 w-3.5 mr-2" /> Thành viên
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <!-- Badge -->
        <Badge variant="outline" class="shrink-0 gap-1 text-xs font-medium" :class="member.role === 'OWNER'
          ? 'border-amber-300 bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400 dark:border-amber-700'
          : member.role === 'ADMIN'
            ? 'border-blue-300 bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400 dark:border-blue-700'
            : 'border-transparent'
          ">
          <Crown v-if="member.role === 'OWNER'" class="h-2.5 w-2.5" />
          <Shield v-else-if="member.role === 'ADMIN'" class="h-2.5 w-2.5" />
          {{ getRoleLabel(member.role) }}
        </Badge>

        <DropdownMenu v-if="canModerateMember(member)">
          <DropdownMenuTrigger as-child>
            <Button variant="ghost" size="icon" class="h-7 w-7 text-muted-foreground" title="Thao tác">
              <MoreVertical class="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" class="w-48">
            <DropdownMenuCheckboxItem :checked="member.muted" @update:checked="() => { }"
              @select.prevent="handleToggleAudioState(member, 'muted')">
              <MicOff class="h-3.5 w-3.5" />
              Mute mic
            </DropdownMenuCheckboxItem>
            <DropdownMenuCheckboxItem :checked="member.deafen" @update:checked="() => { }"
              @select.prevent="handleToggleAudioState(member, 'deafen')">
              <VolumeX class="h-3.5 w-3.5" />
              Mute loa
            </DropdownMenuCheckboxItem>

            <DropdownMenuSub>
              <DropdownMenuSubTrigger>
                <MessageSquareOff class="mr-2 h-3.5 w-3.5" />
                Chặn chat
              </DropdownMenuSubTrigger>
              <DropdownMenuSubContent class="w-40">
                <DropdownMenuItem v-for="option in chatDisableOptions" :key="option.value"
                  @click="handleChangeChatDisable(member, option.value)"
                  :class="option.value === 'NOT_DISABLE' && !isChatDisabled(member) ? 'text-primary' : ''">
                  {{ option.label }}
                </DropdownMenuItem>
              </DropdownMenuSubContent>
            </DropdownMenuSub>

            <DropdownMenuSeparator />
            <DropdownMenuItem class="text-destructive focus:text-destructive" @select.prevent="memberToKick = member">
              <UserMinus class="mr-2 h-3.5 w-3.5" />
              Xóa khỏi phòng
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <Separator v-if="filteredMembers.length > 0" class="my-1" />

      <p v-if="filteredMembers.length === 0" class="text-sm text-muted-foreground text-center py-6">
        Không có thành viên nào
      </p>
    </div>
  </div>

  <AlertDialog :open="!!memberToKick" @update:open="(open) => !open && (memberToKick != null)">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>Xóa thành viên khỏi phòng?</AlertDialogTitle>
        <AlertDialogDescription>
          <span class="font-medium text-foreground">@{{ memberToKick?.username }}</span>
          sẽ bị xóa khỏi phòng và mất toàn bộ quyền truy cập. Hành động
          này không thể hoàn tác.
        </AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <AlertDialogCancel>Hủy</AlertDialogCancel>
        <AlertDialogAction class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
          @click="handleKick(memberToKick)">
          Xóa khỏi phòng
        </AlertDialogAction>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>

  <AlertDialog :open="showOwnerTransferDialog" @update:open="handleOwnerTransferDialogOpenChange">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle>Chuyển quyền chủ phòng?</AlertDialogTitle>
        <AlertDialogDescription>
          <span class="font-medium text-foreground">@{{ ownerTransferTarget?.username }}</span>
          sẽ trở thành chủ phòng mới. Tài khoản chủ phòng hiện tại sẽ được đổi xuống quyền
          <span class="font-medium text-foreground">Quản trị</span>.
        </AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter>
        <AlertDialogCancel :disabled="ownerTransferLoading">
          Hủy
        </AlertDialogCancel>
        <Button :disabled="ownerTransferLoading" class="bg-primary text-white hover:bg-primary/70"
          @click="handleConfirmOwnerTransfer">
          {{ ownerTransferLoading ? "Đang chuyển..." : "Chuyển chủ phòng" }}
        </Button>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>

  <InviteMemberDialog v-model:open="showInviteDialog" />
</template>
