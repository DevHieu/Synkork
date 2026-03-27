<script setup lang="ts">
import { useRoute } from "vue-router";

const route = useRoute();
const roomId = route.params.roomId;
const spaceId = route.params.spaceId;

</script>

<template>
  <div><h1>{{ roomId }}</h1></div>
  <div><h1>{{ spaceId }}</h1></div>
</template>

<style scoped></style>
