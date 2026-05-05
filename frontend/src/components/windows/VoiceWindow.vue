<script setup lang="ts">
import { useUserStore } from "@/stores/userStore";
import { useVoiceSpaceStore } from "@/stores/voiceSpaceStore";
import type { VoiceItemType } from "@/types/VoiceSpaceParticipant";
import { storeToRefs } from "pinia";
import { computed, onMounted, onUnmounted, ref, nextTick, watch } from "vue";
import { useRoute } from "vue-router";

import VoiceFocusItem from "../voice/VoiceFocusItem.vue";
import VoiceStripFocus from "../voice/VoiceStripFocus.vue";
import VoiceGrid from "../voice/VoiceGrid.vue";
import ControlBar from "../voice/ControlBar.vue";
import VoiceHeader from "../voice/VoiceHeader.vue";

const route = useRoute();
const spaceId = ref(route.params.spaceId as string);

const voiceSpaceStore = useVoiceSpaceStore();
const { participantList } = storeToRefs(voiceSpaceStore);

const { user } = storeToRefs(useUserStore());

// Cái element
const focusedId = ref<string | null>(null);
const itemRefs = ref<Record<string, HTMLElement | null>>({});

const voiceList = computed((): VoiceItemType[] => {
  //Lọc ra máy cái stream là screen
  const screens = participantList.value
    .filter((p) => p.screenOn)
    .map((p) => ({
      id: `screen-${p.userID}`,
      type: "screen" as const,
      userID: p.userID,
      isLocal: p.isLocal,
      userName: p.userName,
      videoOn: false,
      micOn: true,
      audioOn: true,
      muted: p.muted,
      deafen: p.deafen,
    }));

  const participants = participantList.value.map((p) => ({
    id: `participant-${p.userID}`,
    type: "participant" as const,
    userID: p.userID,
    isLocal: p.isLocal,
    userName: p.userName,
    videoOn: p.videoOn,
    micOn: p.micOn,
    audioOn: p.audioOn,
    muted: p.muted,
    deafen: p.deafen,
  }));

  // Xếp screen lên trước để ưu tiên screen sẽ ưu tiên hiện trước
  return [...screens, ...participants];
});

// Khi focusId thay đổi thì 2 cái này sẽ tự thay đổi theo (công dụng của computed)
// Lấy cái item đã focus
const focusItem = computed(() =>
  focusedId.value
    ? voiceList.value.find((t) => t.id === focusedId.value)
    : null,
);
// Dach sách mấy item còn lại
const otherItemWhenFocused = computed(() =>
  focusedId.value
    ? voiceList.value.filter((t) => t.id !== focusedId.value)
    : [],
);

// SYNC VIDEO
const syncVideoToItem = (containerId: string, itemElement: HTMLElement) => {
  // containerId là cái id của cái hidden element
  // itemElement cái container mình sẽ copy cái video từ cái hidden vào trong đây -> Thấy ảnh !!!

  if (!itemElement) return;

  // Đoạn này là để lấy cái video của zego gắn trong hiddem element (trong composable zego)
  const sourceVideo = document
    .getElementById(containerId)
    ?.querySelector("video");

  // Nếu mà không có (tắt cam/share) thì nó sẽ xóa cái component mà mình tạo để nó không hiện cái ô mà ko có hình khi mình tắt (tạo bên dưới á)
  if (!sourceVideo?.srcObject) {
    const stale = itemElement.querySelector<HTMLVideoElement>("video.mirrored");
    if (stale) {
      stale.srcObject = null;
      stale.remove();
    }
    return;
  }

  // này là để lấy ra cái element đang chứa cái video
  let target = itemElement.querySelector<HTMLVideoElement>("video.mirrored");
  if (!target) {
    // Chưa có thì tạo
    target = Object.assign(document.createElement("video"), {
      className:
        "mirrored absolute inset-0 w-full h-full object-cover pointer-events-none",
      autoplay: true,
      muted: true,
      playsInline: true,
    });
    itemElement.appendChild(target);
  }

  // srcObject khác → cập nhật stream mới
  if (target.srcObject !== sourceVideo.srcObject) {
    target.srcObject = sourceVideo.srcObject;
    target.play().catch(() => {});
  }
};

const syncAll = () => {
  for (const tile of voiceList.value) {
    const el = itemRefs.value[tile.id];
    if (!el) continue;
    syncVideoToItem(getVideoContainerId(tile), el);
  }
};

// Hàm này để xử lí khi dưới zego chuẩn bị sẵn sàng thì sẽ gửi 1 event lên -> hàm này chạy
const handleStreamReady = (e: Event) => {
  const { containerId } = (e as CustomEvent).detail;

  // Tìm item tương ứng với containerId này
  const tile = voiceList.value.find(
    (t) => getVideoContainerId(t) === containerId,
  );
  if (!tile) return;

  nextTick(() => {
    const el = itemRefs.value[tile.id];
    if (!el) return;
    syncVideoToItem(containerId, el);
  });
};

const getVideoContainerId = (item: VoiceItemType) => {
  if (item.type === "screen")
    return item.isLocal
      ? "screen-sharing-container"
      : `remote-screen-${item.userID}`;
  return item.isLocal ? "local-video-container" : `remote-video-${item.userID}`;
};

onMounted(async () => {
  window.addEventListener("zego:stream-ready", handleStreamReady);

  if (
    voiceSpaceStore.isInRoom &&
    voiceSpaceStore.currentSpaceId === spaceId.value
  ) {
    await voiceSpaceStore.replayAllStreamsToDOM();
    syncAll();
  } else {
    await voiceSpaceStore.joinRoom(spaceId.value);
  }
});

onUnmounted(() => {
  window.removeEventListener("zego:stream-ready", handleStreamReady);
  focusedId.value = null;
  itemRefs.value = {};
});

// Khi relooad trang lúc đang ở voice space -> Vừa vào khi user được fetch dữ liệu xong sẽ tự động join vào
watch(user, async (newUser) => {
  if (newUser && !voiceSpaceStore.isInRoom)
    await voiceSpaceStore.joinRoom(spaceId.value);
});

// Đổi phòng khác thì set mấy cái này về null
watch(
  () => route.fullPath,
  () => {
    focusedId.value = null;
  },
);

// Khi list có thay đôit hoặc thay đôỉ focus vào đứa nào đó thì sẽ reset lại mấy cái DOM hiện cho đúng
watch([focusedId, voiceList], () => syncAll(), { flush: "post" });

// Khi list thay đổi thì sẽ loop 1 vòng check xem những cái DOM nào khác. Nếu khác thì xóa srcObject trong hidden element đi để sạch sẽ
watch(
  voiceList,
  (newTiles, oldTiles) => {
    if (!oldTiles) return;
    const newIds = new Set(newTiles.map((t) => t.id));
    for (const old of oldTiles) {
      if (newIds.has(old.id)) continue;
      const v =
        itemRefs.value[old.id]?.querySelector<HTMLVideoElement>(
          "video.mirrored",
        );
      if (v) {
        v.srcObject = null;
        v.remove();
      }
      delete itemRefs.value[old.id];
    }
  },
  { flush: "post" },
);
</script>

<template>
  <div
    class="flex flex-col h-full bg-background text-foreground select-none overflow-hidden"
  >
    <VoiceHeader />

    <!-- ── Video Area ── -->
    <div class="flex-1 min-h-0 p-3 overflow-hidden flex flex-col gap-3">
      <!-- FOCUS MODE -->
      <template v-if="focusItem">
        <VoiceFocusItem
          :focusedTile="focusItem"
          :user="user"
          @minimize="focusedId = null"
          @register-ref="(id, el) => (itemRefs[id] = el)"
        />

        <VoiceStripFocus
          :otherPeople="otherItemWhenFocused"
          @focus="focusedId = $event"
          @register-ref="(id, el) => (itemRefs[id] = el)"
        />
      </template>

      <!-- NORMAL MODE -->
      <template v-else>
        <VoiceGrid
          :list="voiceList"
          :user="user"
          @focus="focusedId = $event"
          @register-ref="(id, el) => (itemRefs[id] = el)"
        />
      </template>
    </div>

    <ControlBar />
  </div>
</template>
<style scoped></style>
