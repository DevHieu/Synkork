<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      @click.self="$emit('close')"
    >
      <div class="bg-background rounded-2xl shadow-2xl border w-[340px] overflow-hidden">

        <!-- Header -->
        <div class="px-5 pt-5 pb-3 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <BellPlus :size="16" class="text-primary" />
            <span class="text-sm font-semibold">Đặt nhắc nhở</span>
          </div>

          <button
            @click="$emit('close')"
            class="p-1 rounded-lg hover:bg-muted transition-colors"
          >
            <X :size="14" class="text-muted-foreground" />
          </button>
        </div>

        <!-- Quick -->
        <div class="px-5 pb-3 flex flex-wrap gap-1.5">
          <button
            v-for="q in quickOptions"
            :key="q.label"
            class="text-xs px-3 py-1 rounded-full border hover:bg-primary hover:text-primary-foreground hover:border-primary transition-all"
            @click="selectQuick(q.minutes)"
          >
            {{ q.label }}
          </button>
        </div>

        <div class="mx-5 border-t mb-3" />

        <!-- Calendar -->
        <div class="px-5">

          <!-- Header -->
          <div class="flex items-center justify-between mb-3">
            <button
              @click="prevMonth"
              class="p-1.5 rounded-lg hover:bg-muted transition-colors"
            >
              <ChevronLeft :size="15" />
            </button>

            <span class="text-sm font-medium">
              {{ monthNames[viewMonth] }} {{ viewYear }}
            </span>

            <button
              @click="nextMonth"
              class="p-1.5 rounded-lg hover:bg-muted transition-colors"
            >
              <ChevronRight :size="15" />
            </button>
          </div>

          <!-- Day names -->
          <div class="grid grid-cols-7 mb-1">
            <div
              v-for="d in dayNames"
              :key="d"
              class="text-center text-[11px] text-muted-foreground font-medium py-1"
            >
              {{ d }}
            </div>
          </div>

          <!-- Days -->
          <div class="grid grid-cols-7 gap-y-0.5 mb-3">

            <div
              v-for="n in firstDayOfMonth"
              :key="'empty-' + n"
            />

            <button
              v-for="day in daysInMonth"
              :key="day"
              :disabled="isPast(day)"
              :class="[
                'text-xs h-8 w-full rounded-lg transition-all flex items-center justify-center',
                isSelectedDay(day)
                  ? 'bg-primary text-primary-foreground font-medium'
                  : isToday(day)
                    ? 'border border-primary text-primary font-medium hover:bg-primary/10'
                    : isPast(day)
                      ? 'text-muted-foreground/40 cursor-not-allowed'
                      : 'hover:bg-muted text-foreground'
              ]"
              @click="selectDay(day)"
            >
              {{ day }}
            </button>

          </div>
        </div>

        <!-- Time -->
        <div class="mx-5 border-t pt-3 pb-3">

          <div class="flex items-center gap-3">

            <div class="flex items-center gap-1">
              <Clock :size="13" class="text-muted-foreground" />
              <span class="text-xs text-muted-foreground">
                Giờ nhắc nhở
              </span>
            </div>

            <div class="flex items-center gap-1.5 ml-auto">

              <!-- Hour -->
              <div class="relative">

                <div class="flex items-center gap-1 bg-muted rounded-lg px-2 py-1.5 border">
                  <input
                    v-model="hourInput"
                    type="number"
                    min="0"
                    max="23"
                    class="w-8 text-sm font-semibold bg-transparent text-center focus:outline-none"
                    @blur="validateHour"
                    @keydown.up.prevent="changeHour(1)"
                    @keydown.down.prevent="changeHour(-1)"
                  />

                  <button
                    @click="toggleHourDropdown"
                    class="text-muted-foreground hover:text-foreground"
                  >
                    <ChevronDown :size="13" />
                  </button>
                </div>

                <div
                  v-if="hourDropdownOpen"
                  class="absolute z-50 bottom-full mb-1 left-0 bg-background border rounded-lg shadow-lg w-16 max-h-40 overflow-y-auto"
                >
                  <button
                    v-for="h in 24"
                    :key="h"
                    :class="[
                      'w-full text-center text-xs px-2 py-1.5 hover:bg-muted',
                      selectedHour === h - 1 ? 'bg-primary/10 text-primary font-medium' : ''
                    ]"
                    @click="pickHour(h - 1)"
                  >
                    {{ String(h - 1).padStart(2, '0') }}
                  </button>
                </div>

              </div>

              <span class="text-sm font-semibold">:</span>

              <!-- Minute -->
              <div class="relative">

                <div class="flex items-center gap-1 bg-muted rounded-lg px-2 py-1.5 border">
                  <input
                    v-model="minuteInput"
                    type="number"
                    min="0"
                    max="59"
                    class="w-8 text-sm font-semibold bg-transparent text-center focus:outline-none"
                    @blur="validateMinute"
                    @keydown.up.prevent="changeMinute(1)"
                    @keydown.down.prevent="changeMinute(-1)"
                  />

                  <button
                    @click="toggleMinuteDropdown"
                    class="text-muted-foreground hover:text-foreground"
                  >
                    <ChevronDown :size="13" />
                  </button>
                </div>

                <div
                  v-if="minuteDropdownOpen"
                  class="absolute z-50 bottom-full mb-1 left-0 bg-background border rounded-lg shadow-lg w-16 max-h-40 overflow-y-auto"
                >
                  <button
                    v-for="m in minuteOptions"
                    :key="m"
                    :class="[
                      'w-full text-center text-xs px-2 py-1.5 hover:bg-muted',
                      selectedMinute === m ? 'bg-primary/10 text-primary font-medium' : ''
                    ]"
                    @click="pickMinute(m)"
                  >
                    {{ String(m).padStart(2, '0') }}
                  </button>
                </div>

              </div>

            </div>
          </div>

        </div>

        <!-- Summary -->
        <div
          v-if="selectedDay"
          class="mx-5 mb-3 px-3 py-2 rounded-lg border"
          :class="
            isSelectedDateTimePast
              ? 'bg-red-500/10 border-red-500/20'
              : 'bg-primary/10 border-primary/20'
          "
        >
          <p
            class="text-xs font-medium flex items-center gap-1.5"
            :class="isSelectedDateTimePast ? 'text-red-500' : 'text-primary'"
          >
            <Bell :size="12" />
            Nhắc lúc
            {{ String(selectedHour).padStart(2,'0') }}:{{ String(selectedMinute).padStart(2,'0') }}
            — {{ selectedDay }}/{{ selectedMonth + 1 }}/{{ selectedYear }}
          </p>

          <p
            v-if="isSelectedDateTimePast"
            class="text-[11px] text-red-500 mt-1"
          >
            Không thể đặt nhắc nhở trong quá khứ
          </p>
        </div>

        <!-- Existing (no day selected yet, but reminder exists) -->
        <div
          v-else-if="note?.reminderAt && !note?.reminderSent"
          class="mx-5 mb-3 px-3 py-2 rounded-lg bg-muted"
        >
          <p class="text-xs text-muted-foreground flex items-center gap-1.5">
            <Bell :size="12" />
            Đang nhắc: {{ formatExisting(note.reminderAt) }}
          </p>
        </div>

        <!-- Footer -->
        <div class="px-5 pb-5 flex justify-between items-center">

          <button
            v-if="note?.reminderAt && !note?.reminderSent"
            class="text-xs text-destructive hover:underline"
            @click="$emit('confirm', null)"
          >
            Xóa nhắc nhở
          </button>

          <div class="flex gap-2 ml-auto">

            <button
              class="text-xs px-3 py-1.5 rounded-lg border hover:bg-muted"
              @click="$emit('close')"
            >
              Hủy
            </button>

            <button
              :disabled="!selectedDay || isSelectedDateTimePast"
              class="text-xs px-3 py-1.5 rounded-lg bg-primary text-primary-foreground disabled:opacity-40"
              @click="handleConfirm"
            >
              Lưu
            </button>

          </div>
        </div>

      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'

import {
  BellPlus,
  Bell,
  X,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  Clock
} from 'lucide-vue-next'

import type { Note } from '@/features/note/types/NoteType'

const props = defineProps<{
  open: boolean
  note: Note | null
}>()

const emit = defineEmits(['close', 'confirm'])

const now = new Date()

const viewMonth = ref(now.getMonth())
const viewYear = ref(now.getFullYear())

const selectedDay = ref<number | null>(null)
const selectedMonth = ref(now.getMonth())
const selectedYear = ref(now.getFullYear())

const selectedHour = ref(now.getHours())
const selectedMinute = ref(now.getMinutes())

const hourInput = ref(String(selectedHour.value).padStart(2, '0'))
const minuteInput = ref(String(selectedMinute.value).padStart(2, '0'))

const hourDropdownOpen = ref(false)
const minuteDropdownOpen = ref(false)

const minuteOptions = Array.from({ length: 60 }, (_, i) => i)

const isSelectedDateTimePast = computed(() => {
  if (!selectedDay.value) return true

  const selected = new Date(
    selectedYear.value,
    selectedMonth.value,
    selectedDay.value,
    selectedHour.value,
    selectedMinute.value,
    0
  )

  return selected.getTime() < Date.now()
})

const monthNames = [
  'Tháng 1','Tháng 2','Tháng 3','Tháng 4',
  'Tháng 5','Tháng 6','Tháng 7','Tháng 8',
  'Tháng 9','Tháng 10','Tháng 11','Tháng 12'
]

const dayNames = ['CN','T2','T3','T4','T5','T6','T7']

const quickOptions = [
  { label: 'Sau 30 phút', minutes: 30 },
  { label: 'Sau 1 giờ', minutes: 60 },
  { label: 'Sau 3 giờ', minutes: 180 },
  { label: 'Ngày mai', minutes: 1440 }
]

const daysInMonth = computed(() =>
  new Date(viewYear.value, viewMonth.value + 1, 0).getDate()
)

const firstDayOfMonth = computed(() =>
  new Date(viewYear.value, viewMonth.value, 1).getDay()
)

function isPast(day: number) {
  const d = new Date(
    viewYear.value,
    viewMonth.value,
    day,
    selectedHour.value,
    selectedMinute.value,
    0
  )
  return d.getTime() < Date.now()
}

function isToday(day: number) {
  return (
    day === now.getDate() &&
    viewMonth.value === now.getMonth() &&
    viewYear.value === now.getFullYear()
  )
}

function isSelectedDay(day: number) {
  return (
    day === selectedDay.value &&
    viewMonth.value === selectedMonth.value &&
    viewYear.value === selectedYear.value
  )
}

function selectDay(day: number) {

selectedDay.value = day
selectedMonth.value = viewMonth.value
selectedYear.value = viewYear.value

// force reactive update
selectedHour.value = Number(selectedHour.value)
selectedMinute.value = Number(selectedMinute.value)

if (isSelectedDateTimePast.value) {
  const next = new Date(Date.now() + 60000)

  selectedHour.value = next.getHours()
  selectedMinute.value = next.getMinutes()

  hourInput.value = String(selectedHour.value).padStart(2, '0')
  minuteInput.value = String(selectedMinute.value).padStart(2, '0')
}
}

function prevMonth() {
  if (viewMonth.value === 0) {
    viewMonth.value = 11
    viewYear.value--
  } else {
    viewMonth.value--
  }
}

function nextMonth() {
  if (viewMonth.value === 11) {
    viewMonth.value = 0
    viewYear.value++
  } else {
    viewMonth.value++
  }
}

function changeHour(delta: number) {
  selectedHour.value = (selectedHour.value + delta + 24) % 24
  hourInput.value = String(selectedHour.value).padStart(2, '0')
}

function validateHour() {
  let v = parseInt(hourInput.value)
  if (isNaN(v) || v < 0) v = 0
  if (v > 23) v = 23
  selectedHour.value = v
  hourInput.value = String(v).padStart(2, '0')
}

function toggleHourDropdown() {
  hourDropdownOpen.value = !hourDropdownOpen.value
  minuteDropdownOpen.value = false
}

function pickHour(h: number) {
  selectedHour.value = h
  hourInput.value = String(h).padStart(2, '0')
  hourDropdownOpen.value = false
}

function changeMinute(delta: number) {
  selectedMinute.value = (selectedMinute.value + delta + 60) % 60
  minuteInput.value = String(selectedMinute.value).padStart(2, '0')
}

function validateMinute() {
  let v = parseInt(minuteInput.value)
  if (isNaN(v) || v < 0) v = 0
  if (v > 59) v = 59
  selectedMinute.value = v
  minuteInput.value = String(v).padStart(2, '0')
}

function toggleMinuteDropdown() {
  minuteDropdownOpen.value = !minuteDropdownOpen.value
  hourDropdownOpen.value = false
}

function pickMinute(m: number) {
  selectedMinute.value = m
  minuteInput.value = String(m).padStart(2, '0')
  minuteDropdownOpen.value = false
}

function selectQuick(minutes: number) {
  const d = new Date(Date.now() + minutes * 60000)
  viewMonth.value = d.getMonth()
  viewYear.value = d.getFullYear()
  selectedDay.value = d.getDate()
  selectedMonth.value = d.getMonth()
  selectedYear.value = d.getFullYear()
  selectedHour.value = d.getHours()
  selectedMinute.value = d.getMinutes()
  hourInput.value = String(d.getHours()).padStart(2, '0')
  minuteInput.value = String(d.getMinutes()).padStart(2, '0')
}

function formatExisting(isoStr: string) {
  const d = new Date(isoStr)
  return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')} — ${d.getDate()}/${d.getMonth()+1}/${d.getFullYear()}`
}

function handleConfirm() {
  if (!selectedDay.value) return

  const d = new Date(
    selectedYear.value,
    selectedMonth.value,
    selectedDay.value,
    selectedHour.value,
    selectedMinute.value,
    0
  )

  if (d.getTime() < Date.now()) {
    alert('Không thể đặt nhắc nhở trong quá khứ')
    return
  }

  emit('confirm', d.toISOString())
}

watch(
  () => props.open,
  (val) => {
    if (!val) {
      hourDropdownOpen.value = false
      minuteDropdownOpen.value = false
      return
    }

    const n = new Date()

    // Ưu tiên reminderAt nếu còn trong tương lai
    const r = props.note?.reminderAt ? new Date(props.note.reminderAt) : null

    if (r && r.getTime() > Date.now()) {
      viewMonth.value = r.getMonth()
      viewYear.value = r.getFullYear()
      selectedDay.value = r.getDate()
      selectedMonth.value = r.getMonth()
      selectedYear.value = r.getFullYear()
      selectedHour.value = r.getHours()
      selectedMinute.value = r.getMinutes()
    } else {
      // Không có reminder hợp lệ → reset về hiện tại, không pre-select ngày
      viewMonth.value = n.getMonth()
      viewYear.value = n.getFullYear()
      selectedDay.value = null
      selectedMonth.value = n.getMonth()
      selectedYear.value = n.getFullYear()
      selectedHour.value = n.getHours()
      selectedMinute.value = n.getMinutes()
    }

    hourInput.value = String(selectedHour.value).padStart(2, '0')
    minuteInput.value = String(selectedMinute.value).padStart(2, '0')
  }
)
</script>

<style scoped>
input[type='number']::-webkit-inner-spin-button,
input[type='number']::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type='number'] {
  -moz-appearance: textfield;
}
</style>