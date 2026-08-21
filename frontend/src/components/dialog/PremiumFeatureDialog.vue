<script setup lang="ts">
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Crown, Sparkles, Rocket } from "lucide-vue-next";

const { open, featureName, businessOnly = false } = defineProps<{
  open: boolean;
  featureName?: string;
  businessOnly?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:open", val: boolean): void;
  (e: "dismiss"): void;
}>();

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
        <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-3 bg-amber-500/15">
          <Crown class="w-5 h-5 text-amber-500" />
        </div>
        <DialogTitle class="text-base text-left">
          Tính năng cao cấp
        </DialogTitle>
        <DialogDescription class="text-left text-xs leading-relaxed mt-1">
          <template v-if="featureName">
            <span class="font-medium text-foreground/80">{{ featureName }}</span> chỉ
          </template>
          <template v-else>Tính năng này chỉ</template>
          khả dụng cho gói <span class="font-semibold text-foreground/90">
            {{ businessOnly ? "BUSINESS" : "TEAM" }}
          </span>
          {{ !businessOnly ? "trở lên" : "" }}. Nâng cấp để sử dụng.
        </DialogDescription>
      </DialogHeader>

      <div class="h-px bg-border/60 mx-5" />

      <div class="p-5 pt-4 flex flex-col gap-2.5">
        <!-- Plan cards -->
        <div class="grid gap-2" :class="businessOnly ? 'grid-cols-1' : 'grid-cols-2'">
          <!-- TEAM -->
          <div v-if="!businessOnly"
            class="flex flex-col gap-1.5 rounded-xl px-3 py-3 border border-border/60 bg-muted/40 cursor-pointer hover:bg-muted/80 transition-all"
            @click="handleViewPlans">
            <div class="w-7 h-7 rounded-lg flex items-center justify-center bg-white/6">
              <Sparkles class="w-3.5 h-3.5 text-foreground/40" />
            </div>
            <span class="text-xs font-semibold text-foreground">Gói Team</span>
            <span class="text-[11px] text-muted-foreground leading-tight">Tối đa 10 phòng</span>
            <span class="text-xs font-semibold text-foreground/60 mt-auto">69.000₫/tháng</span>
          </div>

          <!-- BUSINESS (featured) -->
          <div
            class="flex flex-col gap-1.5 rounded-xl px-3 py-3 border border-amber-500/40 bg-amber-500/8 cursor-pointer hover:bg-amber-500/12 transition-all relative"
            @click="handleViewPlans">
            <span
              class="absolute top-2 right-2 text-[9px] font-semibold px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-500 leading-none">
              Phổ biến
            </span>
            <div class="w-7 h-7 rounded-lg flex items-center justify-center bg-amber-500/20">
              <Rocket class="w-3.5 h-3.5 text-amber-500" />
            </div>
            <span class="text-xs font-semibold text-foreground">Gói Business</span>
            <span class="text-[11px] text-muted-foreground leading-tight">Không giới hạn</span>
            <span class="text-xs font-semibold text-amber-500 mt-auto">129.000₫/tháng</span>
          </div>
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