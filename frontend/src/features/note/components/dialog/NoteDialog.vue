<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  X, Pin, Bell, Palette, BellPlus
} from 'lucide-vue-next'
import type { Note, NoteRequest } from '@/features/note/types/NoteType'
import type { SuggestedNoteDraft } from '@/types/CalendarSuggestion'
import { useNoteStore } from '@/features/note/stores/noteStore'
import DateTimePicker from '@/components/DateTimePicker.vue'

const COLORS = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#3b82f6', '#8b5cf6', '#ec4899']

const QUICK_REMINDERS = [
  { label: '30 phút', minutes: 30 },
  { label: '1 giờ',   minutes: 60 },
  { label: '3 giờ',   minutes: 180 },
  { label: 'Tối nay', minutes: -1 },   // special case – xem setQuickReminder
  { label: 'Ngày mai', minutes: 1440 },
]

const props = defineProps<{ spaceId: string; open: boolean; note?: Note | null; draft?: SuggestedNoteDraft | null }>()
const emit = defineEmits<{ close: [] }>()

const store = useNoteStore()

const isEdit      = ref(false)
const submitting  = ref(false)
const showReminder = ref(false)
const customDatetime = ref('')   // dạng "YYYY-MM-DDTHH:mm" — định dạng DateTimePicker cần

const form = ref<NoteRequest>({
  title: '',
  note: '',
  color: '',
  pinned: false,
  reminderAt: null,
  version: undefined,
})

// ── helpers ──────────────────────────────────────────────

// Convert Date → "YYYY-MM-DDTHH:mm" (giờ local) để truyền vào DateTimePicker
function toLocalDateTimeString(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function setQuickReminder(minutes: number) {
  let d: Date
  if (minutes === -1) {
    // "Tối nay" = hôm nay lúc 20:00 (nếu đã qua 20h thì chuyển sang mai)
    d = new Date()
    d.setHours(20, 0, 0, 0)
    if (d <= new Date()) d.setDate(d.getDate() + 1)
  } else {
    d = new Date(Date.now() + minutes * 60 * 1000)
  }
  form.value.reminderAt = d.toISOString()
  customDatetime.value  = toLocalDateTimeString(d)
}

function isQuickSelected(minutes: number): boolean {
  if (!form.value.reminderAt) return false
  const actual = new Date(form.value.reminderAt).getTime()
  if (minutes === -1) {
    const tonight = new Date()
    tonight.setHours(20, 0, 0, 0)
    if (tonight <= new Date()) tonight.setDate(tonight.getDate() + 1)
    return Math.abs(actual - tonight.getTime()) < 60_000
  }
  const expected = Date.now() + minutes * 60 * 1000
  return Math.abs(actual - expected) < 5_000
}

// Nhận giá trị từ DateTimePicker (dạng "YYYY-MM-DDTHH:mm"), convert sang ISO để lưu vào form
function onCustomDatetimeChange(value: string) {
  customDatetime.value = value
  if (value) {
    form.value.reminderAt = new Date(value).toISOString()
  }
}

function clearReminder() {
  form.value.reminderAt = null
  customDatetime.value  = ''
}

function formatReminder(iso: string) {
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

// ── sync form from props ──────────────────────────────────

function syncFormFromProps() {
  if (!props.open) return

  showReminder.value = false

  if (props.note) {
    isEdit.value = true
    const reminder = props.note.reminderAt ? new Date(props.note.reminderAt) : null
    form.value = {
      title:      props.note.title,
      note:       props.note.note || '',
      color:      props.note.color || '',
      pinned:     props.note.pinned,
      reminderAt: reminder ? reminder.toISOString() : null,
      version:    props.note.version,
    }
    customDatetime.value = reminder ? toLocalDateTimeString(reminder) : ''
    if (reminder) showReminder.value = true
    return
  }

  if (props.draft) {
    isEdit.value = false
    form.value = {
      title:      props.draft.title,
      note:       props.draft.note || '',
      color:      props.draft.color || '',
      pinned:     props.draft.pinned || false,
      reminderAt: null,
    }
    customDatetime.value = ''
    return
  }

  isEdit.value = false
  form.value   = { title: '', note: '', color: '', pinned: false, reminderAt: null }
  customDatetime.value = ''
}

watch(() => [props.open, props.note, props.draft], syncFormFromProps, { immediate: true })

// ── actions ───────────────────────────────────────────────

function handleClose() { emit('close') }

async function handleSubmit() {
  if (!form.value.title.trim()) return
  submitting.value = true
  try {
    const data: NoteRequest = {
      title:      form.value.title.trim(),
      note:       form.value.note?.trim() ?? '',
      color:      form.value.color || '',
      pinned:     form.value.pinned ?? false,
      reminderAt: form.value.reminderAt ?? null,
      version:    form.value.version,
    }

    if (props.note?.id) {
      const success = await store.updateNote(props.spaceId, props.note.id, data)
      if (success) {
        emit('close')
      }
    } else {
      await store.createNote(props.spaceId, data)
      emit('close')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="handleClose" />
        <div class="relative z-10 w-full max-w-lg mx-4 bg-background rounded-xl shadow-2xl border overflow-hidden">
          <div v-if="form.color" class="h-1.5 w-full" :style="{ backgroundColor: form.color }" />
          <div class="p-6">
            <!-- Header -->
            <div class="flex items-center justify-between mb-4">
              <h2 class="text-lg font-semibold">{{ isEdit ? 'Chỉnh sửa ghi chú' : 'Ghi chú mới' }}</h2>
              <button @click="handleClose" class="p-1 rounded hover:bg-muted transition-colors">
                <X :size="18" />
              </button>
            </div>

            <div class="space-y-3">
              <!-- Title -->
              <input v-model="form.title" placeholder="Tiêu đề..."
                class="w-full bg-transparent border-0 border-b border-border pb-2 text-base font-medium placeholder:text-muted-foreground focus:outline-none focus:border-primary transition-colors" />

              <!-- Content -->
              <textarea v-model="form.note" placeholder="Nội dung ghi chú..." rows="5"
                class="w-full bg-transparent border rounded-lg p-3 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring resize-none transition-colors" />

              <!-- Toolbar -->
              <div class="flex items-center border rounded-lg px-2 py-1.5 bg-muted/30 gap-0.5">
                <!-- Palette -->
                <button type="button"
                  class="p-1.5 rounded hover:bg-muted transition-colors text-muted-foreground hover:text-foreground"
                  title="Màu sắc">
                  <Palette :size="16" />
                </button>
                <!-- Reminder toggle -->
                <button type="button"
                  @click="showReminder = !showReminder"
                  :class="[
                    'p-1.5 rounded transition-colors',
                    showReminder || form.reminderAt
                      ? 'bg-teal-100 text-teal-600 dark:bg-teal-900/40 dark:text-teal-400'
                      : 'hover:bg-muted text-muted-foreground hover:text-foreground'
                  ]"
                  title="Nhắc nhở">
                  <BellPlus :size="16" />
                </button>
              </div>

              <!-- ── REMINDER PANEL ── -->
              <Transition name="reminder">
                <div v-if="showReminder" class="border rounded-lg p-3 bg-muted/20 space-y-2.5">
                  <p class="text-xs font-medium text-muted-foreground flex items-center gap-1.5">
                    <Bell :size="12" />
                    Đặt nhắc nhở
                  </p>

                  <!-- Quick options -->
                  <div class="flex flex-wrap gap-1.5">
                    <button
                      v-for="opt in QUICK_REMINDERS"
                      :key="opt.label"
                      type="button"
                      @click="setQuickReminder(opt.minutes)"
                      :class="[
                        'px-2.5 py-1 text-xs rounded-md border transition-colors',
                        isQuickSelected(opt.minutes)
                          ? 'bg-teal-600 text-white border-teal-600'
                          : 'bg-background hover:bg-muted border-border text-foreground'
                      ]"
                    >
                      {{ opt.label }}
                    </button>
                  </div>

                  <!-- Custom datetime -->
                  <div class="flex items-center gap-2">
                    <DateTimePicker
                      :value="customDatetime"
                      :on-change="onCustomDatetimeChange"
                      placeholder="Chọn ngày giờ..."
                      class="flex-1 text-xs"
                    />
                    <button
                      v-if="form.reminderAt"
                      type="button"
                      @click="clearReminder"
                      class="p-1.5 rounded-md border hover:bg-destructive/10 text-destructive transition-colors"
                      title="Xóa nhắc nhở"
                    >
                      <X :size="14" />
                    </button>
                  </div>

                  <!-- Preview -->
                  <p v-if="form.reminderAt" class="text-xs text-teal-600 dark:text-teal-400 flex items-center gap-1">
                    <Bell :size="11" />
                    Sẽ nhắc lúc {{ formatReminder(form.reminderAt) }}
                  </p>
                </div>
              </Transition>

              <!-- Color picker -->
              <div class="flex items-center gap-2">
                <span class="text-xs text-muted-foreground">Màu:</span>
                <div class="flex gap-1.5">
                  <button v-for="color in COLORS" :key="color"
                    class="w-5 h-5 rounded-full border-2 transition-transform hover:scale-110"
                    :style="{ backgroundColor: color, borderColor: form.color === color ? color : 'transparent' }"
                    @click="form.color = form.color === color ? '' : color" />
                  <button
                    class="w-5 h-5 rounded-full border-2 border-dashed border-border hover:border-muted-foreground transition-colors flex items-center justify-center"
                    @click="form.color = ''">
                    <X :size="10" class="text-muted-foreground" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center justify-between mt-5">
              <label class="flex items-center gap-2 text-sm cursor-pointer select-none">
                <input type="checkbox" v-model="form.pinned" class="rounded" />
                <Pin :size="14" /> Ghim
              </label>
              <div class="flex gap-2">
                <button @click="handleClose"
                  class="px-4 py-2 text-sm rounded-lg border hover:bg-muted transition-colors">
                  Hủy
                </button>
                <button @click="handleSubmit" :disabled="!form.title.trim() || submitting"
                  class="px-4 py-2 text-sm rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                  {{ submitting ? 'Đang lưu...' : isEdit ? 'Cập nhật' : 'Tạo mới' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}
.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

.reminder-enter-active,
.reminder-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease, max-height 0.2s ease;
  max-height: 200px;
  overflow: hidden;
}
.reminder-enter-from,
.reminder-leave-to {
  opacity: 0;
  transform: translateY(-4px);
  max-height: 0;
}
</style>