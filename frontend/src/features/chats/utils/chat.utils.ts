import { ref, type Ref } from "vue";

export const MESSAGE_SIZE = 20;
export const PINNED_SIZE = 10;
export const SEARCH_LIMIT = 5;

export const chatJoinedSpaceId: Ref<string | null> = ref(null);
