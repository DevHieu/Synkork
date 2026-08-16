<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue"
import { LogOut, Palette, User, Volume2, X } from "lucide-vue-next"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { useAuthService } from "@/features/auth/services/authService"
import AccountSettingsTab from "./components/AccountSettingsTab.vue"
import AudioSettingsTab from "./components/AudioSettingsTab.vue"
import ThemeSettingsTab from "./components/ThemeSettingsTab.vue"

type SettingsTab = "account" | "theme" | "audio"

const emit = defineEmits<{ close: [] }>()

const { logout } = useAuthService()
const activeTab = ref<SettingsTab>("account")

const tabs = [
  {
    key: "account" as SettingsTab,
    label: "Thông tin người dùng",
    icon: User,
  },
  {
    key: "theme" as SettingsTab,
    label: "Giao diện & màu sắc",
    icon: Palette,
  },
  {
    key: "audio" as SettingsTab,
    label: "Âm thanh & giọng nói",
    icon: Volume2,
  },
]

const activeTabLabel = () =>
  tabs.find((tab) => tab.key === activeTab.value)?.label ?? "Cài đặt"

const handleClose = () => emit("close")

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === "Escape") handleClose()
}

onMounted(() => document.addEventListener("keydown", handleKeydown))
onUnmounted(() => document.removeEventListener("keydown", handleKeydown))
</script>

<template>
  <Teleport to="body">
    <Transition name="sf">
      <div
        class="fixed inset-0 z-20 flex items-center justify-center bg-black/75"
        @click.self="handleClose"
      >
        <div
          class="flex w-[min(960px,96vw)] h-[min(660px,95vh)] overflow-hidden rounded-lg shadow-2xl"
        >
          <aside
            class="w-[238px] min-w-[238px] flex flex-col overflow-y-auto overflow-x-hidden bg-muted"
          >
            <h2 class="px-5 pt-6 pb-4 text-base font-bold text-foreground">
              Cài đặt ứng dụng
            </h2>

            <nav class="px-2 flex flex-col gap-1.5">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                type="button"
                class="flex items-center w-full px-3 py-2 rounded-md text-sm font-medium transition-all"
                :class="
                  activeTab === tab.key
                    ? 'bg-primary text-primary-foreground'
                    : 'text-foreground/80 hover:bg-accent hover:text-foreground'
                "
                @click="activeTab = tab.key"
              >
                <component
                  :is="tab.icon"
                  class="size-[14px] mr-2 opacity-75 shrink-0"
                />
                {{ tab.label }}
              </button>
            </nav>

            <Separator class="mt-auto mx-4 w-auto opacity-40" />
            <div class="px-2 py-4">
              <button
                type="button"
                class="flex items-center w-full px-3 py-2 rounded-md text-sm font-medium text-destructive hover:bg-destructive/10 transition-colors"
                @click="logout"
              >
                <LogOut class="size-3.5 mr-2" />
                Đăng xuất
              </button>
            </div>
          </aside>

          <section class="flex-1 flex flex-col bg-card overflow-hidden">
            <header
              class="flex items-center justify-between px-7 pt-5 pb-3 shrink-0"
            >
              <h2 class="text-base font-bold text-foreground">
                {{ activeTabLabel() }}
              </h2>
              <Button
                variant="ghost"
                size="sm"
                class="gap-1.5 text-muted-foreground text-[11px]"
                @click="handleClose"
              >
                <X class="size-[18px]" />
                <span class="font-bold tracking-wide">ESC</span>
              </Button>
            </header>

            <div
              class="flex-1 overflow-y-auto px-8 pb-12 scrollbar-thin scrollbar-thumb-border"
            >
              <KeepAlive>
                <AccountSettingsTab v-if="activeTab === 'account'" />
                <ThemeSettingsTab v-else-if="activeTab === 'theme'" />
                <AudioSettingsTab v-else />
              </KeepAlive>
            </div>
          </section>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.scrollbar-thin {
  scrollbar-width: thin;
  scrollbar-color: var(--border) transparent;
}

.scrollbar-thin::-webkit-scrollbar {
  width: 5px;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 999px;
}

.sf-enter-active,
.sf-leave-active {
  transition: opacity 0.18s ease;
}

.sf-enter-from,
.sf-leave-to {
  opacity: 0;
}
</style>
