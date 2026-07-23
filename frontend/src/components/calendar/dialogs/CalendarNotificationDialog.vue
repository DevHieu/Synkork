<script setup lang="ts">
import { computed, ref } from "vue";
import { AlertTriangle, Info, Trash2, XCircle, CheckCircle } from "lucide-vue-next";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

export type NotificationType = "info" | "warning" | "error" | "confirm" | "delete" | "success";

const props = withDefaults(
  defineProps<{
    show: boolean;
    type?: NotificationType;
    title: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
    isLoading?: boolean;
    requireInput?: string;
  }>(),
  {
    type: "info",
    confirmText: "Đồng ý",
    cancelText: "Hủy",
    isLoading: false,
  }
);

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "confirm"): void;
  (e: "cancel"): void;
}>();

// Chặn đóng dialog khi đang loading để tránh mất trạng thái xác nhận.
const inputValue = ref("");

const handleOpenChange = (open: boolean) => {
  if (!open && props.isLoading) return; // Không cho đóng khi đang load
  emit("update:show", open);
  if (!open) {
    emit("cancel");
    inputValue.value = "";
  }
};

const handleConfirm = (e: Event) => {
  e.preventDefault();
  emit("confirm");
  if (props.type !== "confirm" && props.type !== "delete") {
    emit("update:show", false);
    inputValue.value = "";
  }
};

const iconComponent = computed(() => {
  switch (props.type) {
    case "warning":
      return AlertTriangle;
    case "error":
      return XCircle;
    case "delete":
      return Trash2;
    case "success":
      return CheckCircle;
    default:
      return Info;
  }
});

const themeClasses = computed(() => {
  switch (props.type) {
    case "error":
    case "delete":
      return {
        icon: "text-destructive",
        border: "border-destructive/20",
        bg: "bg-destructive/5",
        btn: "bg-destructive text-white hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:focus-visible:ring-destructive/40 dark:bg-destructive/60",
      };
    case "warning":
      return {
        icon: "text-amber-500",
        border: "border-amber-500/20",
        bg: "bg-amber-500/5",
        btn: "bg-amber-500 text-amber-950 hover:bg-amber-500/90",
      };
    case "success":
      return {
        icon: "text-green-500",
        border: "border-green-500/20",
        bg: "bg-green-500/5",
        btn: "bg-green-500 text-white hover:bg-green-600",
      };
    default:
      return {
        icon: "text-primary",
        border: "border-primary/20",
        bg: "bg-primary/5",
        btn: "bg-primary text-primary-foreground hover:bg-primary/90",
      };
  }
});
</script>

<template>
  <Dialog :open="show" @update:open="handleOpenChange">
    <DialogContent
      class="max-w-[calc(100%-2rem)] sm:max-w-[425px] overflow-hidden p-0 border-border/80"
      :show-close-button="false"
      @pointer-down-outside="(e) => e.preventDefault()"
      @escape-key-down="(e) => e.preventDefault()"
    >
      <div class="p-6 pb-4">
        <DialogHeader class="flex flex-col gap-2">
          <div class="flex items-center gap-3 min-w-0 w-full">
            <div
              class="flex size-9 shrink-0 items-center justify-center rounded-md border bg-background/50"
              :class="[themeClasses.border, themeClasses.icon]"
            >
              <component :is="iconComponent" :size="18" />
            </div>
            <DialogTitle class="font-sans font-bold text-base text-foreground leading-none break-words min-w-0">
              {{ title }}
            </DialogTitle>
          </div>
        </DialogHeader>

        <DialogDescription
          class="mt-3 font-sans text-sm leading-relaxed text-muted-foreground break-words"
          as="div"
        >
          <div v-html="message" class="break-words"></div>

          <div v-if="requireInput" class="mt-4">
            <p class="text-xs font-medium mb-1.5 text-foreground">Vui lòng gõ <span class="font-bold select-all">"{{ requireInput }}"</span> để xác nhận:</p>
            <input 
              v-model="inputValue" 
              type="text" 
              class="w-full px-3 py-2 text-sm rounded-md border border-input bg-background shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring transition-colors"
              :placeholder="requireInput"
            />
          </div>
        </DialogDescription>
      </div>

      <DialogFooter class="border-t border-border/60 bg-muted/20 p-4">
        <Button
          v-if="type === 'confirm' || type === 'delete'"
          :disabled="isLoading"
          variant="outline"
          size="sm"
          @click="handleOpenChange(false)"
          class="mt-0 sm:mt-0 w-full sm:w-auto text-xs font-semibold"
        >
          {{ cancelText }}
        </Button>
        
        <Button
          :disabled="isLoading || (requireInput ? inputValue !== requireInput : false)"
          @click="handleConfirm"
          size="sm"
          class="w-full sm:w-auto text-xs font-semibold"
          :class="themeClasses.btn"
        >
          <span v-if="isLoading" class="w-3 h-3 rounded-full border border-current border-t-transparent animate-spin mr-1.5"></span>
          {{ confirmText }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<style scoped>
</style>
