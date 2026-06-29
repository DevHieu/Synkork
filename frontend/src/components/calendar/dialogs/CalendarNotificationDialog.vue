<script setup lang="ts">
import { computed } from "vue";
import { AlertTriangle, Info, Trash2, XCircle } from "lucide-vue-next";
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

export type NotificationType = "info" | "warning" | "error" | "confirm" | "delete";

const props = withDefaults(
  defineProps<{
    show: boolean;
    type?: NotificationType;
    title: string;
    message: string;
    confirmText?: string;
    cancelText?: string;
    isLoading?: boolean;
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
const handleOpenChange = (open: boolean) => {
  if (!open && props.isLoading) return; // Không cho đóng khi đang load
  emit("update:show", open);
  if (!open) {
    emit("cancel");
  }
};

const handleConfirm = (e: Event) => {
  e.preventDefault();
  emit("confirm");
  if (props.type !== "confirm" && props.type !== "delete") {
    emit("update:show", false);
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
        btn: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
      };
    case "warning":
      return {
        icon: "text-amber-500",
        border: "border-amber-500/20",
        bg: "bg-amber-500/5",
        btn: "bg-amber-500 text-amber-950 hover:bg-amber-500/90",
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
  <AlertDialog :open="show" @update:open="handleOpenChange">
    <AlertDialogContent
      class="max-w-sm overflow-hidden rounded-lg border border-border/80 bg-background p-0 text-foreground shadow-lg sm:max-w-[425px]"
    >
      <div class="p-6 pb-4">
        <AlertDialogHeader class="flex flex-col gap-2">
          <div class="flex items-center gap-3">
            <div
              class="flex size-9 shrink-0 items-center justify-center rounded-md border bg-background/50"
              :class="[themeClasses.border, themeClasses.icon]"
            >
              <component :is="iconComponent" :size="18" />
            </div>
            <AlertDialogTitle class="font-sans font-bold text-base text-foreground leading-none">
              {{ title }}
            </AlertDialogTitle>
          </div>
        </AlertDialogHeader>

        <AlertDialogDescription
          class="mt-3 font-sans text-sm leading-relaxed text-muted-foreground"
        >
          <span v-html="message"></span>
        </AlertDialogDescription>
      </div>

      <AlertDialogFooter class="flex items-center justify-end gap-2 border-t border-border/60 bg-muted/20 p-4">
        <button
          v-if="type === 'confirm' || type === 'delete'"
          :disabled="isLoading"
          @click="handleOpenChange(false)"
          class="flex h-8 items-center justify-center rounded-md border border-border bg-background px-3 font-sans text-xs font-semibold text-muted-foreground transition-colors hover:bg-accent"
        >
          {{ cancelText }}
        </button>
        
        <button
          @click="handleConfirm"
          :disabled="isLoading"
          class="flex h-8 items-center justify-center gap-1.5 rounded-md px-3.5 font-sans text-xs font-semibold transition-colors shadow-sm"
          :class="themeClasses.btn"
        >
          <span v-if="isLoading" class="w-3 h-3 rounded-full border border-current border-t-transparent animate-spin"></span>
          {{ confirmText }}
        </button>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>

<style scoped>
</style>
