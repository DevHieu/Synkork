<script setup lang="ts">
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import { ref, watch } from "vue";

const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

const WindowLayout = ref<HTMLElement | null>(null);

watch(
  currentSpace, // Updated to ensure correct reference
  (newSpace) => {
    if (newSpace) {
      switch (newSpace.type) {
        case "CHAT":
          WindowLayout.value = import("./windows/ChatWindowLayout.vue").default;
          break;
        case "VOICE":
          // WindowLayout.value = (
          //   await import("./windows/VoiceWindowLayout.vue")
          // ).default;
          WindowLayout.value = null;
          break;
        // Add cases for other space types as needed
        default:
          WindowLayout.value = null;
      }
    }
  },
  { immediate: true }
);
</script>

<template>
  <div>
    <WindowLayout />
  </div>
</template>

<style scoped></style>
