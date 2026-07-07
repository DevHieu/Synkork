import { useRoomMemberStore } from "@/stores/roomMemberStore";
import { storeToRefs } from "pinia";
import { computed, onMounted, ref } from "vue";
import { parseValidDate, formatVNDateTime } from "@/utils/date";

export function chatComposable() {
  const roomMemberStore = useRoomMemberStore();
  const { chatDisabledTime } = storeToRefs(roomMemberStore);

  const now = ref(Date.now());
  let timer: ReturnType<typeof setInterval> | undefined;

  onMounted(() => {
    timer = setInterval(() => {
      now.value = Date.now();
    }, 1000); // tick mỗi giây, tùy nhu cầu chỉnh lại
  });

  const chatDisabledUntilDate = computed(() =>
    parseValidDate(chatDisabledTime.value)
  );

  const isChatDisabled = computed(
    () =>
      !!chatDisabledUntilDate.value &&
      chatDisabledUntilDate.value.getTime() > now.value
  );

  const chatDisabledLabel = computed(() =>
    chatDisabledUntilDate.value
      ? formatVNDateTime(chatDisabledUntilDate.value)
      : ""
  );

  return { chatDisabledUntilDate, isChatDisabled, chatDisabledLabel };
}