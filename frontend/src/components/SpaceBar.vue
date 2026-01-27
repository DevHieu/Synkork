<template>
  <aside class="w-64 h-screen border-r flex flex-col bg-white">
    <!-- Header -->
    <div class="p-4 border-b flex items-center justify-between">
      <h2 class="text-lg font-bold">Spaces</h2>
      <button
        class="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-200"
        @click="showCreateModal = true"
        title="Create space"
      >
        +
      </button>
    </div>

    <!-- Space list -->
    <ul class="flex-1 overflow-y-auto p-2">
      <li
        v-for="space in spaceList"
        :key="space.id"
        @click="selectSpace(space)"
        :class="[
          'px-3 py-2 rounded cursor-pointer mb-1',
          activeSpaceId === space.id
            ? 'bg-blue-100 text-blue-700'
            : 'hover:bg-gray-100',
        ]"
      >
        <span class="text-sm font-medium"> # {{ space.name }} </span>
      </li>
    </ul>

    <!-- Create Space Modal -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/40 flex items-center justify-center"
    >
      <div class="bg-white w-80 rounded-lg p-4">
        <h3 class="text-lg font-semibold mb-3">Create Space</h3>

        <div class="mb-3">
          <label class="text-sm text-gray-600">Name</label>
          <input
            v-model="newSpace.name"
            class="w-full border rounded px-3 py-2 mt-1"
            placeholder="e.g. general"
          />
        </div>

        <div class="mb-4">
          <label class="text-sm text-gray-600">Type</label>
          <select
            v-model="newSpace.type"
            class="w-full border rounded px-3 py-2 mt-1"
          >
            <option value="CHAT">Chat</option>
            <option value="CALL">Call</option>
            <option value="CALENDAR">Calendar</option>
            <option value="TASK">Task</option>
            <option value="NOTE">Note</option>
          </select>
        </div>

        <div class="flex justify-end gap-2">
          <button
            class="px-3 py-1 text-sm hover:underline"
            @click="showCreateModal = false"
          >
            Cancel
          </button>
          <button
            class="px-3 py-1 text-sm bg-blue-600 text-white rounded"
            @click="createSpace"
          >
            Create
          </button>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
const emit = defineEmits(["select-space"]);
import { ref, onMounted } from "vue";
import axios from "axios";

const roomId = "33c15bf4-a0d8-4351-acb6-1500ecbef34d";

const spaceList = ref<Space[]>([]);

const activeSpaceId = ref<string | null>(null);
const showCreateModal = ref(false);

interface Space {
  id: string;
  name: string;
  type: string;
}

const newSpace = ref({
  name: "",
  type: "CHAT",
});

onMounted(() => {
  const fetchSpaces = async () => {
    try {
      const response = await axios.get(`/rooms/${roomId}/spaces`);
      spaceList.value = response.data;
      console.log("Fetched spaces:", spaceList.value);
    } catch (error) {
      console.error("Error fetching spaces:", error);
    }
  };

  fetchSpaces();
});

function selectSpace(space: any) {
  activeSpaceId.value = space.id;
  emit("select-space", space);
}

const createSpace = async () => {
  if (!newSpace.value.name.trim()) return;

  spaceList.value.push({
    id: Date.now().toString(),
    name: newSpace.value.name,
    type: newSpace.value.type,
  });

  const response = await axios.post(`/rooms/${roomId}/spaces`, {
    name: newSpace.value.name,
    type: newSpace.value.type,
  });

  console.log("Created space:", response.data);

  newSpace.value.name = "";
  newSpace.value.type = "CHAT";
  showCreateModal.value = false;
};
</script>

<style scoped></style>
