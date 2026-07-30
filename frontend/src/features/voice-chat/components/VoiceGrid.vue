<script setup lang="ts">
import { computed } from "vue";
import VoiceItem from "./VoiceItem.vue";
import type { VoiceItemType } from "@/types/VoiceSpaceParticipant";
import type { User } from "@/types/User";

const props = defineProps<{
  list: VoiceItemType[];
  user: User | null;
}>();

const emit = defineEmits<{
  focus: [tileId: string];
  "register-ref": [tileId: string, el: HTMLElement];
}>();

const gridCols = computed(() => {
  const n = props.list.length;
  return n <= 1 ? 1 : n <= 2 ? 2 : 3;
});
</script>

<template>
  <!-- Single tile -->
  <div
    v-if="list.length === 1"
    class="flex-1 min-h-0 flex items-center justify-center"
  >
    <template v-if="list[0]">
      <VoiceItem
        :ref="(el) => el && emit('register-ref', list[0]!.id, (el as any).$el)"
        :item="list[0]"
        :user="user"
        class="w-full"
        style="
          max-height: 100%;
          aspect-ratio: 16/9;
          max-width: min(100%, calc(100vh * 16 / 9 - 200px));
        "
        @focus="emit('focus', $event)"
      />
    </template>
  </div>

  <!-- Grid -->
  <div v-else class="flex-1 min-h-0 flex items-center justify-center">
    <div
      class="grid gap-3 w-full h-full"
      :style="{
        gridTemplateColumns: `repeat(${gridCols}, minmax(0, 1fr))`,
        alignContent: 'center',
      }"
    >
      <VoiceItem
        v-for="item in list"
        :key="item.id"
        :ref="(el) => el && emit('register-ref', item.id, (el as any).$el)"
        :item="item"
        :user="user"
        @focus="emit('focus', $event)"
      />
    </div>
  </div>
</template>
