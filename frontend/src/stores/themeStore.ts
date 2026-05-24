import { defineStore } from "pinia";

const STORAGE_KEY_MODE = "synkork_theme_mode";

export const useThemeStore = defineStore("theme", {
  state: () => ({
    mode: "dark" as "dark" | "light" | "system",
  }),

  getters: {
    isDark: (state): boolean => {
      if (state.mode === "system") {
        return window.matchMedia("(prefers-color-scheme: dark)").matches;
      }
      return state.mode === "dark";
    },
  },

  actions: {
    init() {
      const saved = localStorage.getItem(STORAGE_KEY_MODE) as
        | typeof this.mode
        | null;
      if (saved) this.mode = saved;
    },

    setMode(mode: "dark" | "light" | "system") {
      this.mode = mode;
    },
  },
});

// const themeStore = useThemeStore()
// themeStore.init() // gọi trong App.vue onMounted

// themeStore.isDark  // true/false, đã resolve system rồi
// themeStore.mode
