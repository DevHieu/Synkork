<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue"
import { Sun, Moon, Monitor, Check, RotateCcw } from "lucide-vue-next"
import { toast } from "vue-sonner"
import { useUserStore } from "@/features/users/stores/userStore";
import { useThemeStore } from "@/stores/themeStore" 
import { storeToRefs } from "pinia"

type ThemeId = string

interface ThemeOption {
  id: ThemeId
  label: string
  group: "normal" | "pastel" | "ombre"
  primaryPreview: string
  secondaryPreview: string
}

const userStore = useUserStore()
const { user } = storeToRefs(userStore)

const themeStore = useThemeStore() // Khởi tạo store theme

const selectedTheme = ref<ThemeId>("default")
const saveSuccess = ref(false)

const themes: ThemeOption[] = [
  { id: "default", label: "Cam & Ngọc (Mặc định)", group: "normal", primaryPreview: "#E3662A", secondaryPreview: "#2A9D8F" },
  { id: "violet-storm", label: "Violet Storm", group: "normal", primaryPreview: "#7C3AED", secondaryPreview: "#9333EA" },
  { id: "ocean-blue", label: "Ocean Blue", group: "normal", primaryPreview: "#0EA5E9", secondaryPreview: "#0284C7" },
  { id: "forest-emerald", label: "Forest Emerald", group: "normal", primaryPreview: "#10B981", secondaryPreview: "#059669" },
  { id: "ruby-red", label: "Ruby Red", group: "normal", primaryPreview: "#E63030", secondaryPreview: "#BE185D" },
  { id: "golden-amber", label: "Golden Amber", group: "normal", primaryPreview: "#F59E0B", secondaryPreview: "#D97706" },
  { id: "midnight-slate", label: "Midnight Slate", group: "normal", primaryPreview: "#64748B", secondaryPreview: "#475569" },
  { id: "pastel-rose", label: "Pastel Rose", group: "pastel", primaryPreview: "#FDA4AF", secondaryPreview: "#F9A8D4" },
  { id: "pastel-lavender", label: "Pastel Lavender", group: "pastel", primaryPreview: "#C4B5FD", secondaryPreview: "#E9D5FF" },
  { id: "pastel-mint", label: "Pastel Mint", group: "pastel", primaryPreview: "#6EE7B7", secondaryPreview: "#A7F3D0" },
  { id: "pastel-sky", label: "Pastel Sky", group: "pastel", primaryPreview: "#7DD3FC", secondaryPreview: "#BAE6FD" },
  { id: "pastel-peach", label: "Pastel Peach", group: "pastel", primaryPreview: "#FDBA74", secondaryPreview: "#FED7AA" },
  { id: "pastel-lilac", label: "Pastel Lilac", group: "pastel", primaryPreview: "#E879F9", secondaryPreview: "#D946EF" },
  { id: "ombre-sunset", label: "Sunset", group: "ombre", primaryPreview: "#F97316", secondaryPreview: "#A855F7" },
  { id: "ombre-aurora", label: "Aurora", group: "ombre", primaryPreview: "#14B8A6", secondaryPreview: "#8B5CF6" },
  { id: "ombre-rose-gold", label: "Rose Gold", group: "ombre", primaryPreview: "#F43F5E", secondaryPreview: "#EAB308" },
  { id: "ombre-ocean", label: "Ocean Mist", group: "ombre", primaryPreview: "#06B6D4", secondaryPreview: "#6366F1" },
  { id: "ombre-forest", label: "Forest Dawn", group: "ombre", primaryPreview: "#22C55E", secondaryPreview: "#EAB308" },
]

const normalThemes = computed(() => themes.filter(t => t.group === "normal"))
const pastelThemes = computed(() => themes.filter(t => t.group === "pastel"))
const ombreThemes = computed(() => themes.filter(t => t.group === "ombre"))
const DEFAULT_THEME = themes[0] as ThemeOption
const selectedOption = computed<ThemeOption>(() => themes.find(t => t.id === selectedTheme.value) ?? DEFAULT_THEME)

const canUseTheme = (group: "normal" | "pastel" | "ombre") => {
  const plan = user.value?.currentPlan ?? "FREE"
  if (group === "normal") return true
  if (group === "pastel") return plan === "TEAM" || plan === "BUSINESS"
  if (group === "ombre") return plan === "BUSINESS"
  return false
}

const handleSelectTheme = (opt: ThemeOption) => {
  if (!canUseTheme(opt.group)) {
    toast.error(
      opt.group === "pastel"
        ? "Theme Pastel yêu cầu gói TEAM trở lên."
        : "Theme Ombre yêu cầu gói BUSINESS."
    )
    return
  }
  selectedTheme.value = opt.id
}

const applyToDom = (themeId: ThemeId) => {
  const html = document.documentElement

  // Lấy trạng thái isDark trực tiếp từ getter của Pinia store
  html.classList.toggle("dark", themeStore.isDark)

  if (themeId === "default") {
    html.removeAttribute("data-theme")
  } else {
    html.setAttribute("data-theme", themeId)
  }
}

// Lắng nghe thay đổi từ local themeId HOẶC mode trong store
watch([selectedTheme, () => themeStore.mode], () => {
  applyAndSave()
})

const applyAndSave = () => {
  localStorage.setItem("synkork_theme_id", selectedTheme.value)
  // Không cần lưu mode vào localStorage ở đây nữa vì setMode trong store đã làm
  applyToDom(selectedTheme.value)
  saveSuccess.value = true
  setTimeout(() => (saveSuccess.value = false), 2000)
}

const resetDefaults = () => {
  selectedTheme.value = "default"
  themeStore.setMode("dark")
}

onMounted(() => {
  selectedTheme.value = localStorage.getItem("synkork_theme_id") ?? "default"
  applyToDom(selectedTheme.value)
})

const swatchStyle = (opt: ThemeOption) =>
  opt.group === "ombre"
    ? { background: `linear-gradient(135deg, ${opt.primaryPreview} 0%, ${opt.secondaryPreview} 100%)` }
    : { background: opt.primaryPreview }
</script>

<template>
  <div class="theme-root">

    <!-- CHẾ ĐỘ HIỂN THỊ -->
    <section class="ts-section">
      <div class="flex justify-between">
        <div class="ts-section-header">
          <Monitor class="ts-section-icon" />
          <span>Chế độ hiển thị</span>
        </div>
        <Transition name="fade">
          <span v-if="saveSuccess" class="save-ok">✓ Đã áp dụng!</span>
        </Transition>
      </div>
      <div class="mode-grid">
        <button v-for="m in ([
          { value: 'light', label: 'Sáng', icon: Sun },
          { value: 'dark', label: 'Tối', icon: Moon },
          { value: 'system', label: 'Hệ thống', icon: Monitor },
        ] as const)" :key="m.value" class="mode-card" :class="{ selected: themeStore.mode === m.value }"
          @click="themeStore.setMode(m.value)">
          <component :is="m.icon" class="mode-icon" />
          <span class="mode-label">{{ m.label }}</span>
          <div v-if="themeStore.mode === m.value" class="mode-check">
            <Check class="check-icon" />
          </div>
        </button>
      </div>
    </section>

    <div class="ts-divider" />

    <!-- NORMAL -->
    <section class="ts-section">
      <div class="ts-section-header">
        <span class="group-badge normal">Thông thường</span>
      </div>
      <div class="swatch-grid">
        <button v-for="opt in normalThemes" :key="opt.id" class="swatch-btn"
          :class="{ selected: selectedTheme === opt.id }" :title="opt.label" :style="swatchStyle(opt)"
          @click="handleSelectTheme(opt)">
          <Check v-if="selectedTheme === opt.id" class="swatch-check" />
        </button>
      </div>
      <p class="selected-label">
        <span class="dot" :style="{ background: selectedOption.primaryPreview }" />
        <template v-if="selectedOption.group !== 'ombre'">{{ selectedOption.label }}</template>
        <template v-else>
          <span class="ombre-tag">Ombre —</span>&nbsp;{{ selectedOption.label }}
        </template>
      </p>
    </section>

    <div class="ts-divider" />

    <!-- PASTEL -->
    <section class="ts-section">
      <div class="ts-section-header">
        <span class="group-badge pastel">Pastel</span>
        <span v-if="!canUseTheme('pastel')" class="text-xs text-muted-foreground ml-1">
          🔒 Yêu cầu gói TEAM
        </span>
      </div>
      <div class="swatch-grid">
        <button v-for="opt in pastelThemes" :key="opt.id" class="swatch-btn pastel-swatch" :class="{
          selected: selectedTheme === opt.id,
          'opacity-40 cursor-not-allowed': !canUseTheme('pastel')
        }" :title="opt.label" :style="swatchStyle(opt)" @click="handleSelectTheme(opt)">
          <Check v-if="selectedTheme === opt.id" class="swatch-check" />
        </button>
      </div>
    </section>

    <div class="ts-divider" />

    <!-- OMBRE -->
    <section class="ts-section">
      <div class="ts-section-header">
        <span class="group-badge ombre">Ombre</span>
        <span v-if="!canUseTheme('ombre')" class="text-xs text-muted-foreground ml-1">
          🔒 Yêu cầu gói BUSINESS
        </span>
      </div>
      <div class="swatch-grid">
        <button v-for="opt in ombreThemes" :key="opt.id" class="swatch-btn ombre-swatch" :class="{
          selected: selectedTheme === opt.id,
          'opacity-40 cursor-not-allowed': !canUseTheme('ombre')
        }" :title="opt.label" :style="swatchStyle(opt)" @click="handleSelectTheme(opt)">
          <Check v-if="selectedTheme === opt.id" class="swatch-check" />
        </button>
      </div>
    </section>

    <div class="ts-divider" />

    <!-- XEM TRƯỚC -->
    <section class="ts-section">
      <p class="ts-label-small">Xem trước</p>
      <div class="preview-card">
        <div class="preview-header">
          <div class="preview-avatar" :style="{ background: selectedOption.primaryPreview }">S</div>
          <div>
            <div class="preview-name">Synkork</div>
            <div class="preview-status">Đang hoạt động</div>
          </div>
        </div>
        <div class="preview-msg-row">
          <div class="preview-bubble self" :style="{ background: selectedOption.primaryPreview }">
            Xin chào! 👋
          </div>
        </div>
        <div class="preview-msg-row other">
          <div class="preview-bubble">Chào mừng bạn đến với Synkork!</div>
        </div>
        <div class="preview-btn-row">
          <button class="preview-btn" :style="selectedOption.group === 'ombre'
            ? { background: `linear-gradient(135deg, ${selectedOption.primaryPreview}, ${selectedOption.secondaryPreview})` }
            : { background: selectedOption.primaryPreview }">
            Gửi tin nhắn
          </button>
        </div>
      </div>
    </section>

    <!-- ACTIONS -->
    <div class="action-row">
      <button class="reset-btn" @click="resetDefaults">
        <RotateCcw class="reset-icon" /> Khôi phục mặc định
      </button>
    </div>

  </div>
</template>

<style scoped>
.theme-root {
  padding: 0.25rem 0 2rem;
}

.ts-section {
  margin-bottom: 0.25rem;
}

.ts-section-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.85rem;
}

.ts-section-icon {
  width: 13px;
  height: 13px;
}

.ts-label-small {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--muted-foreground);
  margin-bottom: 0.75rem;
}

.group-badge {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  padding: 0.18rem 0.6rem;
  border-radius: 999px;
}

.group-badge.normal {
  background: color-mix(in oklch, var(--primary) 18%, transparent);
  color: var(--primary);
}

.group-badge.pastel {
  background: oklch(0.88 0.07 320 / 0.35);
  color: oklch(0.55 0.12 320);
}

.group-badge.ombre {
  background: oklch(0.85 0.08 50 / 0.30);
  color: oklch(0.50 0.15 35);
}

.ts-divider {
  height: 1px;
  background: var(--border);
  opacity: 0.4;
  margin: 1.25rem 0;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.6rem;
}

.mode-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
  padding: 1rem 0.5rem;
  border-radius: 8px;
  border: 1.5px solid var(--border);
  background: var(--muted);
  cursor: pointer;
  transition: all 0.15s;
}

.mode-card:hover {
  border-color: var(--primary);
  background: var(--accent);
}

.mode-card.selected {
  border-color: var(--primary);
  background: color-mix(in oklch, var(--primary) 12%, var(--muted));
}

.mode-icon {
  width: 20px;
  height: 20px;
  color: var(--foreground);
}

.mode-label {
  font-size: 0.8rem;
  color: var(--foreground);
}

.mode-check {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  width: 11px;
  height: 11px;
  color: white;
}

.swatch-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
}

.swatch-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2.5px solid transparent;
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.12s, border-color 0.15s, box-shadow 0.15s;
  flex-shrink: 0;
}

.swatch-btn:hover {
  transform: scale(1.18);
}

.swatch-btn.selected {
  border-color: var(--foreground);
  box-shadow: 0 0 0 2px var(--background), 0 0 0 4px var(--foreground);
}

.pastel-swatch {
  width: 34px;
  height: 34px;
}

.ombre-swatch {
  width: 52px;
  height: 26px;
  border-radius: 999px;
}

.swatch-check {
  width: 14px;
  height: 14px;
  color: white;
  filter: drop-shadow(0 1px 1px rgba(0, 0, 0, .5));
}

.selected-label {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  margin-top: 0.65rem;
  font-size: 0.8rem;
  color: var(--muted-foreground);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.ombre-tag {
  font-weight: 700;
  color: var(--primary);
}

.preview-card {
  background: var(--muted);
  border-radius: 10px;
  padding: 1rem;
  border: 1px solid var(--border);
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.75rem;
  background: var(--accent);
  border-radius: 6px;
  margin-bottom: 0.85rem;
}

.preview-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.preview-name {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--foreground);
}

.preview-status {
  font-size: 0.7rem;
  color: var(--muted-foreground);
}

.preview-msg-row {
  display: flex;
  margin-bottom: 0.45rem;
}

.preview-msg-row.other {
  justify-content: flex-start;
}

.preview-bubble {
  max-width: 70%;
  padding: 0.4rem 0.7rem;
  font-size: 0.8rem;
  color: var(--foreground);
  background: var(--accent);
  border-radius: 8px;
}

.preview-bubble.self {
  color: white;
  margin-left: auto;
}

.preview-btn-row {
  margin-top: 0.75rem;
}

.preview-btn {
  padding: 0.4rem 1rem;
  font-size: 0.78rem;
  color: white;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  cursor: default;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.42rem 0.85rem;
  border-radius: 6px;
  font-size: 0.8rem;
  border: 1px solid var(--border);
  background: var(--accent);
  color: var(--muted-foreground);
  cursor: pointer;
  transition: all 0.12s;
}

.reset-btn:hover {
  color: var(--foreground);
  border-color: var(--foreground);
}

.reset-icon {
  width: 12px;
  height: 12px;
}

.save-ok {
  font-size: 0.8rem;
  color: var(--primary);
  font-weight: 600;
  margin-left: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
