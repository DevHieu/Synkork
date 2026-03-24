<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { useSpaceStore } from "@/stores/spaceStore";
import { storeToRefs } from "pinia";
import dayjs from "dayjs";
import {
  getEventsByDateRange,
  createEvent,
  updateEvent,
  deleteEvent,
  checkConflicts,
} from "@/services/calendarService";
import {
  connectWebSocket,
  addUserToSocketRoom,
} from "@/services/websocket/chatSocket";
import {
  subscribeCalendarSpace,
  unsubscribeCalendarSpace,
} from "@/services/websocket/calendarSocket";

// ===== Interfaces =====
interface CalendarEvent {
  id: string;
  spaceId: string;
  title: string;
  description: string;
  eventDate: string;
  startTime: string;
  endTime: string;
  allowEditAll: boolean;
  createdById: string;
  createdByUsername: string;
  createdByDisplayName: string;
  createdAt: string;
  updatedAt: string;
}

// ===== Route & Store =====
const spaceStore = useSpaceStore();
const { currentSpace } = storeToRefs(spaceStore);

// ===== State =====
const viewMode = ref<"week" | "month" | "year">("month");
const currentDate = ref(dayjs());
const selectedDate = ref(dayjs());
const events = ref<CalendarEvent[]>([]);
const loading = ref(false);

// Dialog state
const showDialog = ref(false);
const isEditing = ref(false);
const editingEvent = ref<CalendarEvent | null>(null);
const formData = ref({
  title: "",
  description: "",
  eventDate: "",
  startTime: "09:00",
  endTime: "10:00",
  allowEditAll: false,
});

const currentUserId = sessionStorage.getItem("userId") || "";

// ===== Computed =====
const headerTitle = computed(() => {
  if (viewMode.value === "week") {
    const start = currentDate.value.startOf("week");
    const end = currentDate.value.endOf("week");
    return `${start.format("DD/MM")} - ${end.format("DD/MM/YYYY")}`;
  } else if (viewMode.value === "year") {
    return currentDate.value.format("YYYY");
  }
  return currentDate.value.format("MMMM YYYY");
});

// Lấy danh sách ngày cho view tháng
const monthDays = computed(() => {
  const startOfMonth = currentDate.value.startOf("month");
  const endOfMonth = currentDate.value.endOf("month");
  const startDay = startOfMonth.day(); // 0=Sun
  const days: dayjs.Dayjs[] = [];

  // Ngày padding đầu tháng
  for (let i = startDay - 1; i >= 0; i--) {
    days.push(startOfMonth.subtract(i + 1, "day"));
  }
  // Ngày trong tháng
  for (let d = startOfMonth; d.isBefore(endOfMonth) || d.isSame(endOfMonth, "day"); d = d.add(1, "day")) {
    days.push(d);
  }
  // Padding cuối cho đủ 42 ô (6 hàng x 7 cột)
  while (days.length < 42) {
    days.push(endOfMonth.add(days.length - endOfMonth.date() - startDay + 1, "day"));
  }
  return days;
});

// Lấy danh sách ngày cho view tuần
const weekDays = computed(() => {
  const start = currentDate.value.startOf("week");
  const days: dayjs.Dayjs[] = [];
  for (let i = 0; i < 7; i++) {
    days.push(start.add(i, "day"));
  }
  return days;
});

// 12 tháng cho view năm
const yearMonths = computed(() => {
  const months: { month: number; name: string; days: dayjs.Dayjs[] }[] = [];
  for (let m = 0; m < 12; m++) {
    const monthStart = currentDate.value.month(m).startOf("month");
    const monthEnd = monthStart.endOf("month");
    const startDay = monthStart.day();
    const days: dayjs.Dayjs[] = [];

    for (let i = startDay - 1; i >= 0; i--) {
      days.push(monthStart.subtract(i + 1, "day"));
    }
    for (let d = monthStart; d.isBefore(monthEnd) || d.isSame(monthEnd, "day"); d = d.add(1, "day")) {
      days.push(d);
    }
    while (days.length < 42) {
      days.push(monthEnd.add(days.length - monthEnd.date() - startDay + 1, "day"));
    }

    months.push({
      month: m,
      name: monthStart.format("MMMM"),
      days,
    });
  }
  return months;
});

// Event cho ngày được chọn
const selectedDateEvents = computed(() => {
  return events.value
    .filter((e) => e.eventDate === selectedDate.value.format("YYYY-MM-DD"))
    .sort((a, b) => a.startTime.localeCompare(b.startTime));
});

// Kiểm tra ngày có event không
const hasEvent = (date: dayjs.Dayjs) => {
  return events.value.some((e) => e.eventDate === date.format("YYYY-MM-DD"));
};

// Lấy event cho 1 ngày (dùng cho week view)
const getEventsForDate = (date: dayjs.Dayjs) => {
  return events.value
    .filter((e) => e.eventDate === date.format("YYYY-MM-DD"))
    .sort((a, b) => a.startTime.localeCompare(b.startTime));
};

const isToday = (date: dayjs.Dayjs) => date.isSame(dayjs(), "day");
const isSelected = (date: dayjs.Dayjs) => date.isSame(selectedDate.value, "day");
const isCurrentMonth = (date: dayjs.Dayjs) => date.month() === currentDate.value.month();

// ===== Fetch Events =====
const fetchEvents = async () => {
  if (!currentSpace.value?.id) return;
  loading.value = true;

  let start: string, end: string;

  if (viewMode.value === "week") {
    start = currentDate.value.startOf("week").format("YYYY-MM-DD");
    end = currentDate.value.endOf("week").format("YYYY-MM-DD");
  } else if (viewMode.value === "year") {
    start = currentDate.value.startOf("year").format("YYYY-MM-DD");
    end = currentDate.value.endOf("year").format("YYYY-MM-DD");
  } else {
    // Lấy rộng hơn 1 chút cho padding
    start = currentDate.value.startOf("month").subtract(7, "day").format("YYYY-MM-DD");
    end = currentDate.value.endOf("month").add(7, "day").format("YYYY-MM-DD");
  }

  try {
    const res = await getEventsByDateRange(currentSpace.value.id, start, end);
    events.value = res.data;
  } catch (err) {
    console.error("Error fetching events:", err);
  } finally {
    loading.value = false;
  }
};

// ===== Navigation =====
const goNext = () => {
  if (viewMode.value === "week") currentDate.value = currentDate.value.add(1, "week");
  else if (viewMode.value === "month") currentDate.value = currentDate.value.add(1, "month");
  else currentDate.value = currentDate.value.add(1, "year");
};

const goPrev = () => {
  if (viewMode.value === "week") currentDate.value = currentDate.value.subtract(1, "week");
  else if (viewMode.value === "month") currentDate.value = currentDate.value.subtract(1, "month");
  else currentDate.value = currentDate.value.subtract(1, "year");
};

const goToday = () => {
  currentDate.value = dayjs();
  selectedDate.value = dayjs();
};

const selectDate = (date: dayjs.Dayjs) => {
  selectedDate.value = date;
};

const clickYearMonth = (monthIndex: number) => {
  currentDate.value = currentDate.value.month(monthIndex);
  viewMode.value = "month";
};

// Text hiển thị cho nút "Hôm nay"
const relativeTimeText = computed(() => {
  const now = dayjs();
  if (viewMode.value === "week") {
    const diff = currentDate.value.startOf("week").diff(now.startOf("week"), "week");
    if (diff === 0) return "Tuần này";
    if (diff === -1) return "Tuần trước";
    if (diff === 1) return "Tuần sau";
    if (diff < -1) return `${-diff} tuần trước`;
    return `${diff} tuần sau`;
  } else if (viewMode.value === "month") {
    const diff = currentDate.value.startOf("month").diff(now.startOf("month"), "month");
    if (diff === 0) return "Tháng này";
    if (diff === -1) return "Tháng trước";
    if (diff === 1) return "Tháng sau";
    if (diff < -1) return `${-diff} tháng trước`;
    return `${diff} tháng sau`;
  } else {
    const diff = currentDate.value.year() - now.year();
    if (diff === 0) return "Năm nay";
    if (diff === -1) return "Năm ngoái";
    if (diff === 1) return "Năm sau";
    return `Năm ${currentDate.value.year()}`;
  }
});

// ===== Dialog =====
const openCreateDialog = () => {
  isEditing.value = false;
  editingEvent.value = null;
  formData.value = {
    title: "",
    description: "",
    eventDate: selectedDate.value.format("YYYY-MM-DD"),
    startTime: "09:00",
    endTime: "10:00",
    allowEditAll: false,
  };
  showDialog.value = true;
};

const openEditDialog = (event: CalendarEvent) => {
  isEditing.value = true;
  editingEvent.value = event;
  formData.value = {
    title: event.title,
    description: event.description || "",
    eventDate: event.eventDate,
    startTime: event.startTime.substring(0, 5),
    endTime: event.endTime.substring(0, 5),
    allowEditAll: event.allowEditAll,
  };
  showDialog.value = true;
};

const canEdit = (event: CalendarEvent) => {
  return event.createdById === currentUserId || event.allowEditAll;
};

const canDelete = (event: CalendarEvent) => {
  return event.createdById === currentUserId;
};

const handleSubmit = async () => {
  if (!formData.value.title.trim()) return;

  try {
    if (isEditing.value && editingEvent.value) {
      await updateEvent(editingEvent.value.id, {
        ...formData.value,
        createdById: currentUserId,
        spaceId: currentSpace.value.id,
      });
    } else {
      await createEvent({
        ...formData.value,
        spaceId: currentSpace.value.id,
        createdById: currentUserId,
      });
    }
    showDialog.value = false;
    await fetchEvents();
  } catch (err) {
    console.error("Error saving event:", err);
    alert("Có lỗi xảy ra khi lưu sự kiện!");
  }
};

const showDeleteEventDialog = ref(false);
const eventToDelete = ref<CalendarEvent | null>(null);
const isDeletingEvent = ref(false);

const handleDelete = (event: CalendarEvent) => {
  eventToDelete.value = event;
  showDeleteEventDialog.value = true;
};

const executeDelete = async () => {
  if (!eventToDelete.value) return;
  isDeletingEvent.value = true;
  try {
    await deleteEvent(eventToDelete.value.id, currentUserId);
    showDeleteEventDialog.value = false;
    eventToDelete.value = null;
    await fetchEvents();
  } catch (err) {
    console.error("Error deleting event:", err);
    alert("Có lỗi xảy ra khi xóa sự kiện!");
  } finally {
    isDeletingEvent.value = false;
  }
};

// ===== Watchers =====
watch(currentSpace, () => fetchEvents(), { immediate: true });
watch([currentDate, viewMode], () => fetchEvents());

const dayNames = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

// ===== WebSocket Real-time =====
const isSocketReady = ref(false);

onMounted(() => {
  connectWebSocket(() => {
    addUserToSocketRoom(currentUserId);
    isSocketReady.value = true;
  });
});

onUnmounted(() => {
  unsubscribeCalendarSpace();
});

// Subscribe/unsubscribe khi space thay đổi
watch(
  [currentSpace, isSocketReady],
  ([space, ready]) => {
    if (!space?.id || !ready) return;
    subscribeCalendarSpace(space.id, (payload) => {
      const { action, event } = payload;
      if (action === "CREATED") {
        // Chỉ thêm nếu chưa tồn tại (tránh trùng từ chính mình)
        if (!events.value.find((e) => e.id === event.id)) {
          events.value.push(event);
        }
      } else if (action === "UPDATED") {
        const idx = events.value.findIndex((e) => e.id === event.id);
        if (idx !== -1) events.value[idx] = event;
        else events.value.push(event);
      } else if (action === "DELETED") {
        events.value = events.value.filter((e) => e.id !== event.id);
      }
    });
  },
  { immediate: true }
);

// ===== Conflict Detection =====
const conflictEvents = ref<CalendarEvent[]>([]);
const isCheckingConflict = ref(false);

let conflictDebounce: ReturnType<typeof setTimeout> | null = null;

watch(
  () => [formData.value.eventDate, formData.value.startTime, formData.value.endTime],
  ([date, start, end]) => {
    if (!date || !start || !end || !currentSpace.value?.id) {
      conflictEvents.value = [];
      return;
    }
    if (conflictDebounce) clearTimeout(conflictDebounce);
    conflictDebounce = setTimeout(async () => {
      isCheckingConflict.value = true;
      try {
        const excludeId = isEditing.value && editingEvent.value ? editingEvent.value.id : undefined;
        const res = await checkConflicts(
          currentSpace.value.id,
          date as string,
          start as string,
          end as string,
          excludeId
        );
        conflictEvents.value = res.data;
      } catch {
        conflictEvents.value = [];
      } finally {
        isCheckingConflict.value = false;
      }
    }, 400);
  }
);
</script>

<template>
  <div class="flex flex-col h-screen bg-transparent overflow-hidden">
    <!-- Header -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-white/10">
      <div class="flex items-center gap-3">
        <SidebarTrigger class="-ml-1" />
        <span class="font-semibold text-lg flex items-center gap-2">
          <font-awesome-icon icon="calendar-alt" class="text-teal-400" />
          {{ currentSpace?.name }}
        </span>
      </div>

      <div class="flex items-center gap-2">
        <!-- View Mode Buttons -->
        <div class="flex rounded-lg overflow-hidden border border-white/20">
          <button
            @click="viewMode = 'week'"
            :class="[
              'px-3 py-1.5 text-sm font-medium transition-all duration-200',
              viewMode === 'week'
                ? 'bg-teal-600 text-white'
                : 'hover:bg-white/10 text-gray-300',
            ]"
          >
            Tuần
          </button>
          <button
            @click="viewMode = 'month'"
            :class="[
              'px-3 py-1.5 text-sm font-medium transition-all duration-200',
              viewMode === 'month'
                ? 'bg-teal-600 text-white'
                : 'hover:bg-white/10 text-gray-300',
            ]"
          >
            Tháng
          </button>
          <button
            @click="viewMode = 'year'"
            :class="[
              'px-3 py-1.5 text-sm font-medium transition-all duration-200',
              viewMode === 'year'
                ? 'bg-teal-600 text-white'
                : 'hover:bg-white/10 text-gray-300',
            ]"
          >
            Năm
          </button>
        </div>
      </div>
    </div>

    <!-- Navigation Bar -->
    <div class="flex items-center justify-between px-4 py-2 border-b border-white/10">
      <div class="flex items-center gap-2">
        <button
          @click="goPrev"
          class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-white/10 transition-colors text-gray-300"
        >
          <font-awesome-icon icon="chevron-left" />
        </button>
        <button
          @click="goToday"
          class="px-3 py-1.5 text-sm rounded-lg bg-teal-600/20 text-teal-400 hover:bg-teal-600/30 transition-colors font-medium min-w-[90px]"
        >
          {{ relativeTimeText }}
        </button>
        <button
          @click="goNext"
          class="p-2 w-8 h-8 flex items-center justify-center rounded-lg hover:bg-white/10 transition-colors text-gray-300"
        >
          <font-awesome-icon icon="chevron-right" />
        </button>
      </div>
      <span class="text-lg font-semibold text-white">{{ headerTitle }}</span>
      <button
        @click="openCreateDialog"
        class="px-4 py-1.5 bg-teal-600 text-white rounded-lg hover:bg-teal-700 transition-colors text-sm font-medium flex items-center gap-1.5"
      >
        <font-awesome-icon icon="plus" />
        Thêm sự kiện
      </button>
    </div>

    <!-- Main Content -->
    <div class="flex-1 overflow-hidden flex">
      <!-- ====== MONTH VIEW ====== -->
      <template v-if="viewMode === 'month'">
        <!-- Calendar Grid -->
        <div class="flex-1 flex flex-col overflow-hidden p-3">
          <!-- Day Headers -->
          <div class="grid grid-cols-7 gap-1 mb-1">
            <div
              v-for="day in dayNames"
              :key="day"
              class="text-center text-xs font-semibold text-gray-400 py-1"
            >
              {{ day }}
            </div>
          </div>

          <!-- Days Grid -->
          <div class="grid grid-cols-7 gap-1 flex-1">
            <div
              v-for="(date, idx) in monthDays"
              :key="idx"
              @click="selectDate(date)"
              :class="[
                'relative rounded-lg p-1 cursor-pointer transition-all duration-200 flex flex-col items-center',
                'hover:bg-white/10',
                isSelected(date) ? 'bg-teal-600/30 ring-1 ring-teal-500' : '',
                isToday(date) ? 'ring-1 ring-teal-400/50' : '',
                !isCurrentMonth(date) ? 'opacity-30' : '',
              ]"
            >
              <span
                :class="[
                  'text-sm font-medium w-7 h-7 flex items-center justify-center rounded-full',
                  isToday(date)
                    ? 'bg-teal-500 text-white'
                    : 'text-gray-200',
                ]"
              >
                {{ date.date() }}
              </span>
              <!-- Event Indicator Dots -->
              <div v-if="hasEvent(date)" class="flex gap-0.5 mt-0.5">
                <span
                  v-for="n in Math.min(getEventsForDate(date).length, 3)"
                  :key="n"
                  class="w-1.5 h-1.5 rounded-full bg-teal-400"
                ></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Selected Day Event List (Right Panel) -->
        <div class="w-80 border-l border-white/10 flex flex-col overflow-hidden">
          <div class="px-4 py-3 border-b border-white/10">
            <h3 class="font-semibold text-white">
              {{ selectedDate.format("DD/MM/YYYY") }}
            </h3>
            <p class="text-xs text-gray-400">
              {{ selectedDateEvents.length }} sự kiện
            </p>
          </div>
          <div class="flex-1 overflow-y-auto px-3 py-2 space-y-2">
            <div
              v-if="selectedDateEvents.length === 0"
              class="text-center text-gray-500 text-sm mt-8"
            >
              Không có sự kiện nào
            </div>
            <div
              v-for="event in selectedDateEvents"
              :key="event.id"
              class="group bg-white/5 rounded-lg p-3 hover:bg-white/10 transition-colors border border-white/5"
            >
              <div class="flex items-start justify-between">
                <div class="flex-1 min-w-0">
                  <h4 class="font-medium text-white text-sm truncate">
                    {{ event.title }}
                  </h4>
                  <p class="text-xs text-teal-400 mt-1 flex items-center gap-1.5">
                    <font-awesome-icon icon="clock" />
                    {{ event.startTime.substring(0, 5) }} -
                    {{ event.endTime.substring(0, 5) }}
                  </p>
                  <p
                    v-if="event.description"
                    class="text-xs text-gray-400 mt-1 line-clamp-2"
                  >
                    {{ event.description }}
                  </p>
                  <p class="text-xs text-gray-500 mt-1 flex items-center gap-1.5">
                    <font-awesome-icon icon="user" />
                    {{ event.createdByDisplayName }}
                  </p>
                </div>
                <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity ml-2">
                  <button
                    v-if="canEdit(event)"
                    @click.stop="openEditDialog(event)"
                    class="p-1 w-6 h-6 flex items-center justify-center rounded hover:bg-white/20 text-gray-300 text-xs"
                    title="Chỉnh sửa"
                  >
                    <font-awesome-icon icon="edit" />
                  </button>
                  <button
                    v-if="canDelete(event)"
                    @click.stop="handleDelete(event)"
                    class="p-1 w-6 h-6 flex items-center justify-center rounded hover:bg-red-500/20 text-gray-300 text-xs hover:text-red-400 transition-colors"
                    title="Xóa"
                  >
                    <font-awesome-icon icon="trash" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- ====== WEEK VIEW ====== -->
      <template v-if="viewMode === 'week'">
        <div class="flex-1 overflow-y-auto p-3">
          <div class="grid grid-cols-7 gap-2">
            <div
              v-for="(date, idx) in weekDays"
              :key="idx"
              class="flex flex-col"
            >
              <!-- Day Header -->
              <div
                @click="selectDate(date)"
                :class="[
                  'text-center p-2 rounded-t-lg cursor-pointer transition-colors',
                  isToday(date) ? 'bg-teal-600/30' : 'bg-white/5',
                  isSelected(date) ? 'ring-1 ring-teal-500' : '',
                ]"
              >
                <div class="text-xs text-gray-400">{{ dayNames[date.day()] }}</div>
                <div
                  :class="[
                    'text-lg font-bold',
                    isToday(date) ? 'text-teal-400' : 'text-white',
                  ]"
                >
                  {{ date.date() }}
                </div>
              </div>

              <!-- Events List -->
              <div class="flex-1 bg-white/5 rounded-b-lg p-1.5 space-y-1 min-h-[200px]">
                <div
                  v-for="event in getEventsForDate(date)"
                  :key="event.id"
                  @click="openEditDialog(event)"
                  class="bg-teal-600/20 rounded p-1.5 cursor-pointer hover:bg-teal-600/30 transition-colors border-l-2 border-teal-500"
                >
                  <p class="text-xs font-medium text-white truncate">
                    {{ event.title }}
                  </p>
                  <p class="text-xs text-teal-300">
                    {{ event.startTime.substring(0, 5) }}
                  </p>
                </div>
                <div
                  v-if="getEventsForDate(date).length === 0"
                  class="text-center text-gray-600 text-xs mt-4"
                >
                  —
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- ====== YEAR VIEW ====== -->
      <template v-if="viewMode === 'year'">
        <div class="flex-1 overflow-y-auto p-4">
          <div class="grid grid-cols-3 xl:grid-cols-4 gap-4">
            <div
              v-for="m in yearMonths"
              :key="m.month"
              @click="clickYearMonth(m.month)"
              class="bg-white/5 rounded-xl p-3 cursor-pointer hover:bg-white/10 transition-all duration-200 hover:ring-1 hover:ring-teal-500/30"
            >
              <h4
                :class="[
                  'text-sm font-semibold mb-2 text-center',
                  currentDate.month() === m.month
                    ? 'text-teal-400'
                    : 'text-gray-300',
                ]"
              >
                {{ m.name }}
              </h4>
              <div class="grid grid-cols-7 gap-px">
                <div
                  v-for="dn in ['C', 'H', 'B', 'T', 'N', 'S', 'B']"
                  :key="dn"
                  class="text-center text-[8px] text-gray-500 font-medium"
                >
                  {{ dn }}
                </div>
                <div
                  v-for="(day, di) in m.days.slice(0, 42)"
                  :key="di"
                  :class="[
                    'text-center text-[10px] rounded p-px',
                    day.month() === m.month ? 'text-gray-300' : 'text-gray-600',
                    isToday(day) ? 'bg-teal-500 text-white font-bold' : '',
                    hasEvent(day) && day.month() === m.month
                      ? 'text-teal-400 font-semibold'
                      : '',
                  ]"
                >
                  {{ day.date() }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- ===== Event Dialog (Create / Edit) ===== -->
    <Teleport to="body">
      <div
        v-if="showDialog"
        class="fixed inset-0 z-50 flex items-center justify-center"
      >
        <!-- Overlay -->
        <div
          class="absolute inset-0 bg-black/60 backdrop-blur-sm"
          @click="showDialog = false"
        ></div>

        <!-- Dialog Content -->
        <div
          class="relative bg-zinc-900 rounded-2xl shadow-2xl border border-white/10 w-full max-w-md mx-4 p-6"
        >
          <h2 class="text-lg font-semibold text-white mb-4">
            {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
          </h2>

          <form @submit.prevent="handleSubmit" class="space-y-4">
            <!-- Title -->
            <div>
              <label class="block text-sm text-gray-400 mb-1">Tiêu đề *</label>
              <input
                v-model="formData.title"
                type="text"
                required
                placeholder="Nhập tiêu đề sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
              />
            </div>

            <!-- Description -->
            <div>
              <label class="block text-sm text-gray-400 mb-1">Mô tả</label>
              <textarea
                v-model="formData.description"
                rows="3"
                placeholder="Mô tả chi tiết sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 resize-none"
              ></textarea>
            </div>

            <!-- Date -->
            <div>
              <label class="block text-sm text-gray-400 mb-1">Ngày *</label>
              <input
                v-model="formData.eventDate"
                type="date"
                required
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
              />
            </div>

            <!-- Time -->
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-sm text-gray-400 mb-1"
                  >Giờ bắt đầu *</label
                >
                <input
                  v-model="formData.startTime"
                  type="time"
                  required
                  class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
                />
              </div>
              <div>
                <label class="block text-sm text-gray-400 mb-1"
                  >Giờ kết thúc *</label
                >
                <input
                  v-model="formData.endTime"
                  type="time"
                  required
                  class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500"
                />
              </div>
            </div>

            <!-- Conflict Warning -->
            <div
              v-if="conflictEvents.length > 0"
              class="bg-amber-500/10 border border-amber-500/30 rounded-lg p-3"
            >
              <div class="flex items-center gap-2 text-amber-400 text-sm font-medium mb-1">
                <font-awesome-icon icon="exclamation-triangle" />
                Trùng giờ với {{ conflictEvents.length }} sự kiện:
              </div>
              <ul class="text-xs text-amber-300/80 space-y-0.5 ml-5">
                <li v-for="c in conflictEvents" :key="c.id">
                  • {{ c.title }} ({{ c.startTime.substring(0, 5) }} - {{ c.endTime.substring(0, 5) }})
                </li>
              </ul>
            </div>

            <!-- Allow Edit All -->
            <div class="flex items-center gap-3">
              <label class="relative inline-flex items-center cursor-pointer">
                <input
                  v-model="formData.allowEditAll"
                  type="checkbox"
                  class="sr-only peer"
                />
                <div
                  class="w-9 h-5 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-teal-600"
                ></div>
              </label>
              <span class="text-sm text-gray-300"
                >Cho phép mọi người chỉnh sửa</span
              >
            </div>

            <!-- Actions -->
            <div class="flex gap-2 justify-end pt-2">
              <button
                type="button"
                @click="showDialog = false"
                class="px-4 py-2 rounded-lg text-gray-300 hover:bg-white/10 transition-colors text-sm"
              >
                Hủy
              </button>
              <button
                type="submit"
                class="px-4 py-2 bg-teal-600 text-white rounded-lg hover:bg-teal-700 transition-colors text-sm font-medium"
              >
                {{ isEditing ? "Cập nhật" : "Tạo sự kiện" }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- Modal Xóa Sự Kiện -->
    <Teleport to="body">
      <div v-if="showDeleteEventDialog" class="fixed inset-0 z-50 flex items-center justify-center">
        <!-- Overlay -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="showDeleteEventDialog = false"></div>
        
        <!-- Content -->
        <div class="relative bg-zinc-900 rounded-xl border border-red-500/20 w-full max-w-sm mx-4 p-5 shadow-2xl">
          <h3 class="text-lg font-semibold text-white mb-2 flex items-center gap-2">
            <span class="text-red-500"><font-awesome-icon icon="trash" /></span> 
            Xóa Sự Kiện
          </h3>
          
          <p class="text-sm text-gray-400 mb-6">
            Bạn có chắc chắn muốn xóa sự kiện "<span class="text-gray-200 font-medium">{{ eventToDelete?.title }}</span>" không? 
            Hành động này không thể hoàn tác.
          </p>
          
          <div class="flex justify-end gap-2">
            <button 
              type="button" 
              @click="showDeleteEventDialog = false"
              class="px-4 py-2 rounded-lg text-sm text-gray-300 hover:bg-white/10 transition-colors"
              :disabled="isDeletingEvent"
            >
              Hủy
            </button>
            <button 
              @click="executeDelete"
              class="px-4 py-2 rounded-lg text-sm font-medium bg-red-600 text-white hover:bg-red-700 transition-colors disabled:opacity-50 flex items-center gap-2"
              :disabled="isDeletingEvent"
            >
              <span v-if="isDeletingEvent" class="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin"></span>
              Xoá sự kiện
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
/* Smooth scroll for event lists */
.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.1) transparent;
}

.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
}

/* Line clamp */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Date input styling for dark theme */
input[type="date"]::-webkit-calendar-picker-indicator,
input[type="time"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}
</style>
