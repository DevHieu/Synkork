<script setup lang="ts">
import { computed, ref } from "vue";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import {
  Copy,
  Download,
  Check,
  FileText,
  Sparkles,
  Loader2,
  CheckSquare,
  ListTodo,
  ChevronDown,
} from "lucide-vue-next";
import { toast } from "vue-sonner";

interface Props {
  open: boolean;
  isLoading?: boolean;
  transcript?: string;
  summaryJson?: string;
}

const props = withDefaults(defineProps<Props>(), {
  isLoading: false,
  transcript: "",
  summaryJson: "{}",
});

const emit = defineEmits<{
  (e: "update:open", value: boolean): void;
}>();

const isCopied = ref(false);
const transcriptOpen = ref(false);

// Phân tách chuỗi JSON tóm tắt từ AI
const parsedSummary = computed(() => {
  if (!props.summaryJson || props.summaryJson.trim() === "") {
    return {
      summary: "Không có tóm tắt cuộc họp.",
      keyPoints: [] as string[],
      actionItems: [] as string[],
    };
  }
  try {
    const data = JSON.parse(props.summaryJson);
    return {
      summary: data.summary || "Không có tóm tắt cuộc họp.",
      keyPoints: Array.isArray(data.keyPoints) ? data.keyPoints : [],
      actionItems: Array.isArray(data.actionItems) ? data.actionItems : [],
    };
  } catch (e) {
    console.error("Lỗi parse JSON tóm tắt cuộc họp:", e);
    return {
      summary: props.summaryJson || "Không thể phân tách nội dung tóm tắt.",
      keyPoints: [] as string[],
      actionItems: [] as string[],
    };
  }
});

// Định dạng toàn bộ nội dung để tải xuống hoặc sao chép
const formattedContent = computed(() => {
  const dateStr = new Date().toLocaleString("vi-VN");
  const summaryPart = parsedSummary.value;

  let text = `==================================================\n`;
  text += `BÁO CÁO TÓM TẮT CUỘC HỌP AI (SYNKORK)\n`;
  text += `Thời gian xuất: ${dateStr}\n`;
  text += `==================================================\n\n`;

  text += `1. TÓM TẮT CHÍNH:\n`;
  text += `${summaryPart.summary}\n\n`;

  text += `2. CÁC Ý CHÍNH QUAN TRỌNG:\n`;
  if (summaryPart.keyPoints.length > 0) {
    (summaryPart.keyPoints as string[]).forEach((point) => {
      text += ` - ${point}\n`;
    });
  } else {
    text += ` - (Không có thông tin)\n`;
  }
  text += `\n`;

  text += `3. VIỆC CẦN LÀM (ACTION ITEMS):\n`;
  if (summaryPart.actionItems.length > 0) {
    (summaryPart.actionItems as string[]).forEach((item) => {
      text += ` - [ ] ${item}\n`;
    });
  } else {
    text += ` - (Không có việc cần làm)\n`;
  }
  text += `\n`;

  text += `==================================================\n`;
  text += `4. BẢN GHI HỘI THOẠI (TRANSCRIPT):\n`;
  text += `==================================================\n`;
  text += props.transcript || "Không có dữ liệu hội thoại.";
  text += `\n`;

  return text;
});

const handleClose = () => {
  emit("update:open", false);
};

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(formattedContent.value);
    isCopied.value = true;
    toast.success("Đã sao chép nội dung tóm tắt vào bộ nhớ tạm!");
    setTimeout(() => {
      isCopied.value = false;
    }, 2000);
  } catch (err) {
    toast.error("Không thể sao chép nội dung.");
  }
};

const handleDownload = () => {
  try {
    const blob = new Blob([formattedContent.value], {
      type: "text/plain;charset=utf-8",
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;

    // Tên file theo định dạng ngày giờ hiện tại
    const date = new Date();
    const dateFormatted = `${date.getFullYear()}${(date.getMonth() + 1).toString().padStart(2, "0")}${date.getDate().toString().padStart(2, "0")}_${date.getHours().toString().padStart(2, "0")}${date.getMinutes().toString().padStart(2, "0")}`;

    link.download = `Synkork_Summary_${dateFormatted}.txt`;
    link.click();
    URL.revokeObjectURL(url);
    toast.success("Đã tải tóm tắt cuộc họp dạng file .txt!");
  } catch (err) {
    toast.error("Không thể tải file xuống.");
  }
};
</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent :show-close-button="false"
      class="summary-modal flex flex-col p-0 overflow-hidden max-h-[85vh] sm:max-w-[640px] w-[95vw]">

      <!-- HEADER — sticky top -->
      <DialogHeader class="modal-header">
        <div class="header-icon">
          <Sparkles class="h-4 w-4" />
        </div>
        <div>
          <DialogTitle class="modal-title">Tóm tắt cuộc họp</DialogTitle>
          <DialogDescription class="modal-subtitle">
            Chuyển đổi và phân tích tự động bằng AI
          </DialogDescription>
        </div>
      </DialogHeader>

      <!-- BODY — scrolls with the overlay -->
      <div class="modal-body min-h-0">

        <!-- Loading state -->
        <div v-if="isLoading" class="loading-state">
          <Loader2 class="h-6 w-6 loading-spinner" />
          <p class="loading-text">Đang xử lý bản ghi âm...</p>
        </div>

        <!-- Content -->
        <div v-else class="content-stack">

          <!-- ① Tóm tắt AI -->
          <section class="content-section">
            <h3 class="section-label">
              <Sparkles class="h-3.5 w-3.5" />
              Tóm tắt
            </h3>
            <p class="summary-text">{{ parsedSummary.summary }}</p>
          </section>

          <!-- ② Điểm chính -->
          <section v-if="parsedSummary.keyPoints.length > 0" class="content-section">
            <h3 class="section-label">
              <CheckSquare class="h-3.5 w-3.5" />
              Điểm chính
            </h3>
            <ul class="item-list">
              <li v-for="(point, index) in parsedSummary.keyPoints" :key="index" class="item-row">
                <span class="item-dot" aria-hidden="true" />
                <span>{{ point }}</span>
              </li>
            </ul>
          </section>

          <!-- ③ Việc cần làm -->
          <section v-if="parsedSummary.actionItems.length > 0" class="content-section">
            <h3 class="section-label">
              <ListTodo class="h-3.5 w-3.5" />
              Việc cần làm
            </h3>
            <ul class="item-list">
              <li v-for="(item, index) in parsedSummary.actionItems" :key="index" class="item-row">
                <span class="action-checkbox" aria-hidden="true" />
                <span>{{ item }}</span>
              </li>
            </ul>
          </section>

          <!-- Bản ghi hội thoại (accordion) -->
          <section class="content-section">
            <button class="transcript-toggle" :aria-expanded="transcriptOpen" @click="transcriptOpen = !transcriptOpen">
              <span class="section-label no-margin">
                <FileText class="h-3.5 w-3.5" />
                Bản ghi cuộc trò chuyện
              </span>
              <ChevronDown class="h-3.5 w-3.5 toggle-chevron" :class="{ 'chevron-open': transcriptOpen }" />
            </button>
            <div v-if="transcriptOpen" class="transcript-body">
              {{ transcript || "Không tìm thấy dữ liệu hội thoại." }}
            </div>
          </section>

        </div>
      </div>

      <!-- FOOTER — sticky bottom -->
      <DialogFooter class="modal-footer">
        <Button variant="outline" size="sm" class="btn-close" @click="handleClose">
          Đóng
        </Button>
        <div class="footer-actions">
          <Button variant="outline" size="sm" class="btn-copy" :disabled="isLoading || !transcript" @click="handleCopy">
            <component :is="isCopied ? Check : Copy" class="h-3.5 w-3.5" />
            {{ isCopied ? "Đã sao chép" : "Sao chép" }}
          </Button>
          <Button size="sm" class="btn-download" :disabled="isLoading || !transcript" @click="handleDownload">
            <Download class="h-3.5 w-3.5" />
            Tải xuống
          </Button>
        </div>
      </DialogFooter>

    </DialogContent>
  </Dialog>
</template>

<style scoped>
/* ── Modal shell ──────────────────────────────────────────── */
.summary-modal {
  /* Layout is now handled by Tailwind classes to ensure grid and padding overrides */
  background-color: var(--background);
  border-color: var(--border);
  color: var(--foreground);
}

/* ── Header ───────────────────────────────────────────────── */
.modal-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: var(--radius);
  background-color: color-mix(in oklch, var(--primary) 12%, transparent);
  color: var(--primary);
  flex-shrink: 0;
}

.modal-title {
  font-size: 0.9375rem;
  font-weight: 600;
  line-height: 1.4;
  color: var(--foreground);
}

.modal-subtitle {
  font-size: 0.75rem;
  color: var(--muted-foreground);
  margin-top: 0.125rem;
}

/* ── Body ─────────────────────────────────────────────────── */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.25rem 1.5rem;
  scrollbar-width: thin;
  scrollbar-color: var(--border) transparent;
}

/* ── Loading ──────────────────────────────────────────────── */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem 0;
}

.loading-spinner {
  color: var(--primary);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 0.8125rem;
  color: var(--muted-foreground);
}

/* ── Content sections ─────────────────────────────────────── */
.content-stack {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.content-section {
  padding: 0.875rem 0;
  border-bottom: 1px solid var(--border);
}

.content-section:last-child {
  border-bottom: none;
}

/* Left-border signature element */
.content-section:not(:has(.transcript-toggle)) {
  padding-left: 0.875rem;
  border-left: 2px solid color-mix(in oklch, var(--primary) 30%, transparent);
  margin-left: 0;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.6875rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--muted-foreground);
  margin-bottom: 0.625rem;
}

.section-label.no-margin {
  margin-bottom: 0;
}

/* ── Summary text ─────────────────────────────────────────── */
.summary-text {
  font-size: 0.875rem;
  line-height: 1.65;
  color: var(--foreground);
}

/* ── List items ───────────────────────────────────────────── */
.item-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  list-style: none;
  padding: 0;
  margin: 0;
}

.item-row {
  display: flex;
  align-items: flex-start;
  gap: 0.625rem;
  font-size: 0.8125rem;
  line-height: 1.6;
  color: var(--foreground);
}

.item-dot {
  display: inline-block;
  width: 0.375rem;
  height: 0.375rem;
  border-radius: 9999px;
  background-color: var(--primary);
  margin-top: 0.45rem;
  flex-shrink: 0;
  opacity: 0.7;
}

.action-checkbox {
  display: inline-block;
  width: 0.875rem;
  height: 0.875rem;
  border-radius: 3px;
  border: 1.5px solid color-mix(in oklch, var(--primary) 50%, transparent);
  margin-top: 0.25rem;
  flex-shrink: 0;
}

/* ── Transcript accordion ─────────────────────────────────── */
.transcript-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  color: var(--foreground);
}

.transcript-toggle:hover .section-label {
  color: var(--foreground);
}

.toggle-chevron {
  color: var(--muted-foreground);
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.chevron-open {
  transform: rotate(180deg);
}

.transcript-body {
  margin-top: 0.75rem;
  padding: 0.875rem;
  background-color: var(--muted);
  border-radius: var(--radius);
  font-size: 0.8125rem;
  line-height: 1.7;
  color: var(--muted-foreground);
  white-space: pre-line;
}

/* ── Footer ───────────────────────────────────────────────── */
.modal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

.footer-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-close {
  color: var(--muted-foreground);
  font-size: 0.8125rem;
}

.btn-copy {
  font-size: 0.8125rem;
  display: flex;
  align-items: center;
  gap: 0.375rem;
}

.btn-download {
  font-size: 0.8125rem;
  display: flex;
  align-items: center;
  gap: 0.375rem;
  background-color: var(--primary);
  color: var(--primary-foreground);
}

.btn-download:hover:not(:disabled) {
  opacity: 0.88;
}
</style>
