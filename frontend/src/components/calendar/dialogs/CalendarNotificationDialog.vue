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
    confirmText: "ĐỒNG Ý",
    cancelText: "HỦY",
    isLoading: false,
  }
);

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "confirm"): void;
  (e: "cancel"): void;
}>();

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
        border: "border-destructive",
        bg: "bg-destructive/10",
        btn: "bg-destructive text-destructive-foreground border-destructive hover:bg-background hover:text-destructive",
        shadow: "box-shadow: 4px 4px 0px 0px var(--color-destructive);",
      };
    case "warning":
      return {
        icon: "text-accent",
        border: "border-accent",
        bg: "bg-accent/10",
        btn: "bg-accent text-accent-foreground border-accent hover:bg-background hover:text-accent",
        shadow: "box-shadow: 4px 4px 0px 0px var(--color-accent);",
      };
    default:
      return {
        icon: "text-primary",
        border: "border-primary",
        bg: "bg-primary/10",
        btn: "bg-primary text-primary-foreground border-primary hover:bg-background hover:text-primary",
        shadow: "box-shadow: 4px 4px 0px 0px var(--color-primary);",
      };
  }
});
</script>

<template>
  <AlertDialog :open="show" @update:open="handleOpenChange">
    <AlertDialogContent
      class="max-w-sm overflow-hidden rounded-[1.5rem] border-2 bg-background p-0 text-foreground shadow-[0_32px_100px_-48px_rgba(0,0,0,0.75)] sm:max-w-[425px]"
      :class="themeClasses.border"
    >
      <div class="p-6">
        <AlertDialogHeader class="flex flex-row items-center gap-4 space-y-0 pb-4">
          <div
            class="flex size-12 items-center justify-center rounded-full border-2"
            :class="[themeClasses.border, themeClasses.bg, themeClasses.icon]"
          >
            <component :is="iconComponent" :size="24" stroke-width="2.5" />
          </div>
          <div class="flex flex-col gap-1">
            <AlertDialogTitle class="font-mono font-bold text-lg uppercase tracking-widest text-foreground">
              {{ title }}
            </AlertDialogTitle>
          </div>
        </AlertDialogHeader>

        <AlertDialogDescription class="font-mono text-sm uppercase text-muted-foreground leading-relaxed">
          <span v-html="message"></span>
        </AlertDialogDescription>
      </div>

      <AlertDialogFooter class="flex items-center justify-end gap-3 border-t-2 bg-background p-4" :class="themeClasses.border">
        <button
          v-if="type === 'confirm' || type === 'delete'"
          :disabled="isLoading"
          @click="handleOpenChange(false)"
          class="flex h-10 items-center justify-center rounded-full border-2 border-border px-4 font-mono text-xs font-bold uppercase tracking-wider text-muted-foreground transition-colors hover:border-primary hover:bg-primary hover:text-primary-foreground"
        >
          {{ cancelText }}
        </button>
        
        <button
          @click="handleConfirm"
          :disabled="isLoading"
          class="flex h-10 items-center justify-center gap-2 rounded-full border-2 px-5 font-mono text-xs font-bold uppercase tracking-wider transition-colors"
          :class="themeClasses.btn"
        >
          <span v-if="isLoading" class="w-3 h-3 rounded-full border-2 border-current border-t-transparent animate-spin"></span>
          {{ confirmText }}
        </button>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>
