<script setup lang="ts">
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { FileWarning, Zap, Building2 } from "lucide-vue-next";

const props = defineProps<{
  open: boolean;
  fileName: string;
  fileSize: number; // bytes
  currentPlan?: "free" | "personal" | "enterprise";
}>();

const emit = defineEmits<{
  (e: "update:open", val: boolean): void;
  (e: "upgrade", plan: "personal" | "enterprise"): void;
  (e: "dismiss"): void;
}>();

const formatSize = (bytes: number) => {
  if (bytes >= 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  return `${(bytes / 1024).toFixed(0)} KB`;
};

const currentLimit = () => {
  switch (props.currentPlan) {
    case "personal": return "50 MB";
    case "enterprise": return "100 MB";
    default: return "10 MB";
  }
};

const plans = [
  {
    id: "personal" as const,
    name: "Cá nhân",
    limit: "50 MB / file",
    price: "49.000₫ / tháng",
    icon: Zap,
    featured: false,
  },
  {
    id: "enterprise" as const,
    name: "Doanh nghiệp",
    limit: "100 MB / file",
    price: "199.000₫ / tháng",
    icon: Building2,
    featured: true,
  },
];

const handleDismiss = () => {
  emit("update:open", false);
  emit("dismiss");
};

const handleUpgrade = (plan: "personal" | "enterprise") => {
  emit("upgrade", plan);
  emit("update:open", false);
};
</script>

<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent
      class="max-w-sm gap-0 p-0 overflow-hidden border-white/10 bg-card"
    >
      <!-- Header -->
      <DialogHeader class="p-5 pb-4">
        <div
          class="w-10 h-10 rounded-xl flex items-center justify-center mb-3 bg-primary/15"
        >
          <FileWarning class="w-5 h-5 text-primary" />
        </div>
        <DialogTitle class="text-base text-left">
          File vượt quá giới hạn
        </DialogTitle>
        <DialogDescription class="text-left text-xs leading-relaxed mt-1">
          <span class="font-medium text-foreground/75">{{ fileName }}</span>
          ({{ formatSize(fileSize) }}) vượt mức
          <span class="font-medium text-foreground/75">{{ currentLimit() }}</span>
          cho phép ở gói hiện tại. Nâng cấp để tải file lớn hơn.
        </DialogDescription>
      </DialogHeader>

      <!-- Divider -->
      <div class="h-px bg-border/60 mx-5" />

      <!-- Plan cards -->
      <div class="p-5 pt-4 flex flex-col gap-2.5">
        <div
          v-for="plan in plans"
          :key="plan.id"
          class="flex items-center justify-between rounded-xl px-3.5 py-3 border cursor-pointer transition-all"
          :class="
            plan.featured
              ? 'border-primary/40 bg-primary/8 hover:bg-primary/12'
              : 'border-border/60 bg-muted/40 hover:bg-muted/80'
          "
          @click="handleUpgrade(plan.id)"
        >
          <div class="flex items-center gap-2.5">
            <div
              class="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
              :class="plan.featured ? 'bg-primary/20' : 'bg-white/6'"
            >
              <component
                :is="plan.icon"
                class="w-3.5 h-3.5"
                :class="plan.featured ? 'text-primary' : 'text-foreground/40'"
              />
            </div>
            <div class="flex flex-col gap-0.5">
              <div class="flex items-center gap-2">
                <span class="text-xs font-semibold text-foreground">
                  {{ plan.name }}
                </span>
                <span
                  v-if="plan.featured"
                  class="text-[10px] font-medium px-1.5 py-0.5 rounded-full bg-primary/20 text-primary"
                >
                  Phổ biến
                </span>
              </div>
              <span class="text-[11px] text-muted-foreground">
                Tối đa {{ plan.limit }}
              </span>
            </div>
          </div>
          <span class="text-xs font-semibold text-foreground/60 shrink-0 ml-2">
            {{ plan.price }}
          </span>
        </div>

        <!-- Actions -->
        <Button class="w-full mt-1 h-9 text-xs font-semibold" @click="handleUpgrade('enterprise')">
          Xem chi tiết các gói
        </Button>
        <Button
          variant="ghost"
          class="w-full h-8 text-xs text-muted-foreground hover:text-foreground"
          @click="handleDismiss"
        >
          Để sau
        </Button>
      </div>
    </DialogContent>
  </Dialog>
</template>
