<template>
  <Transition name="toast">
    <div v-if="visible"
      class="fixed bottom-4 right-4 z-20 w-80 rounded-2xl border bg-background shadow-2xl overflow-hidden">

      <div class="p-4 flex gap-3">

        <!-- Icon -->
        <div class="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center text-xl">
          🔔
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0">

          <div class="flex items-start justify-between gap-2">
            <div>
              <h3 class="text-sm font-semibold line-clamp-1">
                {{ note.title }}
              </h3>

              <p class="text-xs text-muted-foreground mt-1 line-clamp-2">
                {{ note.note || 'Không có nội dung' }}
              </p>
            </div>

            <button class="text-muted-foreground hover:text-foreground" @click="$emit('close')">
              ✕
            </button>
          </div>

          <!-- Footer -->
          <div class="flex items-center justify-between mt-4">

            <div class="flex items-center gap-2">
              <img v-if="note.avatarUrl" :src="note.avatarUrl" class="w-5 h-5 rounded-full" />

              <span class="text-[11px] text-muted-foreground">
                {{ note.displayName || 'Reminder Bot' }}
              </span>
            </div>

            <div class="flex gap-2">

              <button class="text-xs px-2 py-1 rounded-lg border hover:bg-muted" @click="$emit('snooze', note)">
                Snooze
              </button>

              <button class="text-xs px-2 py-1 rounded-lg bg-primary text-primary-foreground"
                @click="$emit('open', note)">
                Mở
              </button>

            </div>
          </div>

        </div>
      </div>

    </div>
  </Transition>
</template>

<script setup lang="ts">
defineProps<{
  note: any
  visible: boolean
}>()

defineEmits(['close', 'open', 'snooze'])
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all .25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(.95);
}
</style>