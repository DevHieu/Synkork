<script setup lang="ts">
import {
  Dialog, DialogContent, DialogHeader,
  DialogTitle, DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Sparkles, Rocket, FileWarning, LayoutGrid, MessageSquare, Mic, StickyNote, CalendarX, ClipboardList } from "lucide-vue-next";
import { PlanLimitUtils, type PlanType, type LimitType } from "@/utils/PlanLimitUtils";
import { computed, type Component } from "vue";

const props = defineProps<{
  open: boolean;
  limitType: LimitType;
  currentPlan?: PlanType;
  fileName?: string;
  fileSize?: number;
}>();

const emit = defineEmits<{
  (e: "update:open", val: boolean): void;
  (e: "dismiss"): void;
}>();

// Config cho từng loại limit
const limitConfig: Record<LimitType, {
  icon: Component;
  title: string;
  getDescription: (plan: PlanType, extra?: any) => string;
  getValue: (plan: PlanType) => string;
}> = {
  file: {
    icon: FileWarning,
    title: "File vượt quá giới hạn",
    getDescription: (plan, extra) =>
      `"${extra?.fileName}" (${PlanLimitUtils.formatFileSize(extra?.fileSize)}) vượt mức ${PlanLimitUtils.fileSizeLabel(plan)} cho phép. Nâng cấp để tải file lớn hơn.`,
    getValue: (plan) => PlanLimitUtils.fileSizeLabel(plan),
  },
  rooms: {
    icon: LayoutGrid,
    title: "Đã đạt giới hạn phòng",
    getDescription: (plan) =>
      `Gói hiện tại cho phép tối đa ${PlanLimitUtils.maxRooms(plan)} phòng. Nâng cấp để tạo thêm.`,
    getValue: (plan) => `${PlanLimitUtils.maxRooms(plan)} phòng`,
  },
  chat: {
    icon: MessageSquare,
    title: "Đã đạt giới hạn kênh chat",
    getDescription: (plan) =>
      `Gói hiện tại cho phép tối đa ${PlanLimitUtils.maxChatSpaces(plan)} kênh chat. Nâng cấp để tạo thêm.`,
    getValue: (plan) => `${PlanLimitUtils.maxChatSpaces(plan)} kênh`,
  },
  voice: {
    icon: Mic,
    title: "Đã đạt giới hạn kênh voice",
    getDescription: (plan) =>
      `Gói hiện tại cho phép tối đa ${PlanLimitUtils.maxVoiceSpaces(plan)} kênh voice. Nâng cấp để tạo thêm.`,
    getValue: (plan) => `${PlanLimitUtils.maxVoiceSpaces(plan)} kênh`,
  },
  note: {
    icon: StickyNote,
    title: "Đã đạt giới hạn bảng note",
    getDescription: (plan) =>
      `Gói hiện tại cho phép tối đa ${PlanLimitUtils.maxCollaborationSpaces(plan)} bảng note. Nâng cấp để tạo thêm.`,
    getValue: (plan) => `${PlanLimitUtils.maxCollaborationSpaces(plan)} bảng`,
  },
  calendar: {
    icon: CalendarX,
    title: "Đã đạt giới hạn lịch",
    getDescription: (plan) =>
      `Gói hiện tại cho phép tối đa ${PlanLimitUtils.maxCollaborationSpaces(plan)} lịch. Nâng cấp để tạo thêm.`,
    getValue: (plan) => `${PlanLimitUtils.maxCollaborationSpaces(plan)} lịch`,
  },
  task: {
    icon: ClipboardList,
    title: "Đã đạt giới hạn task",
    getDescription: (plan) =>
      `Gói hiện tại cho phép tối đa ${PlanLimitUtils.maxCollaborationSpaces(plan)} task. Nâng cấp để tạo thêm.`,
    getValue: (plan) => `${PlanLimitUtils.maxCollaborationSpaces(plan)} task`,
  }
};

const plan = computed(() => props.currentPlan ?? "FREE");
const config = computed(() => limitConfig[props.limitType]);
const description = computed(() =>
  config.value.getDescription(plan.value, {
    fileName: props.fileName,
    fileSize: props.fileSize,
  })
);

const plans = computed(() => [
  {
    id: "TEAM",
    name: "Gói Team",
    value: config.value.getValue("TEAM"),
    price: "69.000₫ / tháng",
    icon: Sparkles,
    featured: false,
  },
  {
    id: "BUSINESS",
    name: "Gói Business",
    value: config.value.getValue("BUSINESS"),
    price: "129.000₫ / tháng",
    icon: Rocket,
    featured: true,
  },
]);

const handleDismiss = () => {
  emit("update:open", false);
  emit("dismiss");
};

const handleViewPlans = () => {
  window.location.href = "/me/subscriptions";
};
</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="max-w-sm gap-0 p-0 overflow-hidden border-white/10 bg-card">
      <DialogHeader class="p-5 pb-4">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-3 bg-primary/15">
          <!-- icon động theo limitType -->
          <component :is="config.icon" class="w-5 h-5 text-primary" />
        </div>
        <DialogTitle class="text-base text-left">
          {{ config.title }}
        </DialogTitle>
        <DialogDescription class="text-left text-xs leading-relaxed mt-1">
          {{ description }}
        </DialogDescription>
      </DialogHeader>

      <div class="h-px bg-border/60 mx-5" />

      <div class="p-5 pt-4 flex flex-col gap-2.5">
        <div v-for="p in plans" :key="p.id"
          class="flex items-center justify-between rounded-xl px-3.5 py-3 border cursor-pointer transition-all" :class="p.featured
            ? 'border-primary/40 bg-primary/8 hover:bg-primary/12'
            : 'border-border/60 bg-muted/40 hover:bg-muted/80'" @click="handleViewPlans">
          <div class="flex items-center gap-2.5">
            <div class="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
              :class="p.featured ? 'bg-primary/20' : 'bg-white/6'">
              <component :is="p.icon" class="w-3.5 h-3.5" :class="p.featured ? 'text-primary' : 'text-foreground/40'" />
            </div>
            <div class="flex flex-col gap-0.5">
              <div class="flex items-center gap-2">
                <span class="text-xs font-semibold text-foreground">{{ p.name }}</span>
                <span v-if="p.featured"
                  class="text-[10px] font-medium px-1.5 py-0.5 rounded-full bg-primary/20 text-primary">
                  Phổ biến
                </span>
              </div>
              <span class="text-[11px] text-muted-foreground">Tối đa {{ p.value }}</span>
            </div>
          </div>
          <span class="text-xs font-semibold text-foreground/60 shrink-0 ml-2">{{ p.price }}</span>
        </div>

        <Button class="w-full mt-1 h-9 text-xs font-semibold" @click="handleViewPlans">
          Xem chi tiết các gói
        </Button>
        <Button variant="ghost" class="w-full h-8 text-xs text-muted-foreground hover:text-foreground"
          @click="handleDismiss">
          Để sau
        </Button>
      </div>
    </DialogContent>
  </Dialog>
</template>