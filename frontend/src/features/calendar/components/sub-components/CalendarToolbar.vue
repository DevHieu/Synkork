<script setup lang="ts">

import { SidebarTrigger } from "@/components/ui/sidebar";
import {
  ChevronLeft,
  ChevronRight,
  Plus,
  Link as LinkIcon,
  CalendarDays
} from "lucide-vue-next";
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import axiosClient from "@/lib/axiosClient";
import { useUserStore } from "@/features/users/stores/userStore";
import PremiumFeatureDialog from "@/components/dialog/PremiumFeatureDialog.vue";
import CalendarNotificationDialog from "../dialogs/CalendarNotificationDialog.vue";
import { useSpaceStore } from "@/features/spaces/stores/spaceStore.ts";

const props = defineProps<{
  currentSpaceName?: string;
  viewMode: string;
  headerTitle: string;
  relativeTimeText: string;
}>();

const emit = defineEmits<{
  (e: "update:viewMode", mode: string): void;
  (e: "goPrev"): void;
  (e: "goNext"): void;
  (e: "goToday"): void;

  (e: "openCreateDialog"): void;
}>();

const isConnecting = ref(false);
const showPremiumDialog = ref(false);
const showConfirmDialog = ref(false);
const route = useRoute();
const userStore = useUserStore();
const spaceStore = useSpaceStore();

onMounted(() => {
  userStore.getUserInfo();
});

// Nút kết nối Google Calendar chỉ dùng được cho gói BUSINESS:
// 1. User ở gói FREE hoặc TEAM -> Nút hiển thị để click mở dialog Nâng cấp gói Business
// 2. User ở gói BUSINESS -> Chỉ hiển thị khi chưa kết nối, tự động ẩn khi đã kết nối
const isConnectButtonVisible = computed(() => {
  console.log("isConnectButtonVisible", spaceStore.isPersonalSpace);
  if (!spaceStore.isPersonalSpace) return false; // Không hiển thị nút kết nối Google Calendar trong Room Space
  if (userStore.userPlan !== "BUSINESS") return true;
  return !userStore.isGoogleCalendarConnected;
});

const promptConnectGoogleCalendar = () => {
  if (userStore.userPlan !== "BUSINESS") {
    showPremiumDialog.value = true;
    return;
  }
  showConfirmDialog.value = true;
};

const executeConnectGoogleCalendar = async () => {
  try {
    isConnecting.value = true;
    const response = await axiosClient.get('/api/integrations/google-calendar/authorize-url', {
      params: { redirectPath: route.path }
    });
    if (response.data && response.data.authorizeUrl) {
      window.location.href = response.data.authorizeUrl;
    }
  } catch (error) {
    console.error("Failed to get Google Calendar auth URL", error);
  } finally {
    isConnecting.value = false;
    showConfirmDialog.value = false;
  }
};

// Toolbar chỉ phát sự kiện điều hướng, toàn bộ logic nằm ở layout cha.
</script>

<template>
  <div class="flex flex-col gap-3 bg-transparent">
    <!-- Unified Toolbar -->
    <div
      class="flex items-center justify-between px-5 pt-3 pb-2.5 border-b border-border/50 bg-background/60 backdrop-blur-sm">
      <!-- Left: Sidebar Trigger & Space Name -->
      <div class="flex items-center gap-3">
        <SidebarTrigger class="-ml-1 shrink-0 text-muted-foreground hover:text-foreground" />
        <div class="h-4 w-px bg-border/60" />
        <span class="flex items-center gap-2 font-sans text-sm font-semibold text-foreground">
          <div class="w-7 h-7 rounded-lg bg-primary/15 flex items-center justify-center">
            <CalendarDays class="h-4.5 w-4.5 text-primary" />
          </div>
          {{ currentSpaceName }}
        </span>
      </div>

      <!-- Center: Navigation Controls & Month/Year Display -->
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-1.5">
          <button @click="emit('goPrev')"
            class="flex size-8 items-center justify-center rounded-md border border-border bg-background text-muted-foreground transition-all hover:bg-accent hover:text-foreground"
            :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} trước`">
            <ChevronLeft :size="14" />
          </button>
          <button @click="emit('goToday')"
            class="flex h-8 items-center justify-center rounded-md border border-border bg-background px-3 font-sans text-xs font-medium text-foreground transition-all hover:bg-accent">
            {{ relativeTimeText }}
          </button>
          <button @click="emit('goNext')"
            class="flex size-8 items-center justify-center rounded-md border border-border bg-background text-muted-foreground transition-all hover:bg-accent hover:text-foreground"
            :title="`${viewMode === 'week' ? 'Tuần' : viewMode === 'month' ? 'Tháng' : 'Năm'} sau`">
            <ChevronRight :size="14" />
          </button>
        </div>

        <span class="font-sans text-sm font-semibold text-foreground">{{ headerTitle }}</span>
      </div>

      <!-- Right: View Mode Switcher & Add Event Button -->
      <div class="flex items-center gap-3">
        <!-- View Mode Switcher -->
        <div class="flex rounded-md border border-border/80 bg-muted p-1">
          <button @click="emit('update:viewMode', 'week')"
            :class="['rounded-sm px-3 py-1 text-xs font-sans font-medium transition-all', viewMode === 'week' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground']">
            Tuần
          </button>
          <button @click="emit('update:viewMode', 'month')"
            :class="['rounded-sm px-3 py-1 text-xs font-sans font-medium transition-all', viewMode === 'month' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground']">
            Tháng
          </button>
          <button @click="emit('update:viewMode', 'year')"
            :class="['rounded-sm px-3 py-1 text-xs font-sans font-medium transition-all', viewMode === 'year' ? 'bg-background text-foreground shadow-sm' : 'text-muted-foreground hover:text-foreground']">
            Năm
          </button>
        </div>

        <!-- Connect Google Calendar Button -->
        <button v-if="isConnectButtonVisible" @click="promptConnectGoogleCalendar" :disabled="isConnecting"
          class="flex h-8 items-center gap-1.5 rounded-md border border-border bg-background px-3 font-sans text-xs font-semibold text-foreground shadow-sm transition-all hover:bg-accent">
          <LinkIcon :size="12" />
          {{ isConnecting ? 'Đang kết nối...' : 'Kết nối Google Calendar' }}
        </button>

        <!-- Add Event Button -->
        <button @click="emit('openCreateDialog')"
          class="flex h-8 items-center gap-1.5 rounded-md border border-primary bg-primary px-3 font-sans text-xs font-semibold text-primary-foreground shadow-sm transition-all hover:bg-primary/90">
          <Plus :size="12" />
          Thêm sự kiện
        </button>
      </div>
    </div>

    <!-- Confirmation Dialog -->
    <CalendarNotificationDialog v-model:show="showConfirmDialog" type="confirm" title="Xác nhận kết nối"
      message="Bạn có chắc chắn muốn kết nối với Google Calendar để đồng bộ lịch làm việc không?"
      confirm-text="Đồng ý kết nối" cancel-text="Hủy" :is-loading="isConnecting" @confirm="executeConnectGoogleCalendar"
      @cancel="showConfirmDialog = false" />

    <!-- Premium Feature Dialog -->
    <PremiumFeatureDialog v-model:open="showPremiumDialog" feature-name="Đồng bộ Google Calendar"
      :business-only="true" />
  </div>
</template>

<style scoped>
/* Industrial / Utilitarian styling applied mostly via Tailwind classes. */
</style>
