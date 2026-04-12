<script setup lang="ts">
import { ref, watch, computed } from "vue";
import type { CalendarEvent } from "@/types/CalendarEvent";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import CalendarWarningDialog from "./CalendarWarningDialog.vue";

dayjs.locale("vi");

const props = defineProps<{
  show: boolean;
  isEditing: boolean;
  initialData: {
    title: string;
    description: string;
    eventDate: string;
    startTime: string;
    endTime: string;
    recurrenceType?: string;
    recurrenceEndDate?: string;
    allowEditAll: boolean;
    attendees?: string[];
    attachments?: { name: string; size: number; file?: File }[];
  };
  checkConflicts: (date: string, start: string, end: string, excludeId?: string) => Promise<CalendarEvent[]>;
  editingEventId?: string;
}>();

const emit = defineEmits<{
  (e: "update:show", value: boolean): void;
  (e: "save", data: typeof props.initialData): void;
}>();

const formData = ref({ ...props.initialData });
const conflictEvents = ref<CalendarEvent[]>([]);
const isCheckingConflict = ref(false);

let conflictDebounce: ReturnType<typeof setTimeout> | null = null;
const showWarning = ref(false);
const warningMessage = ref("");

// Cấu hình định dạng giờ 24h/12h
const timeFormat = ref<'24h' | '12h'>('24h');

// Dữ liệu options giờ/phút
const hours24 = ref<string[]>([]);
for (let i = 0; i <= 23; i++) {
  let val = i.toString();
  if (val.length < 2) val = '0' + val;
  hours24.value.push(val);
}

const hours12 = ref<string[]>([]);
for (let i = 1; i <= 12; i++) {
  let val = i.toString();
  if (val.length < 2) val = '0' + val;
  hours12.value.push(val);
}

const minutes = ref<string[]>([]);
for (let i = 0; i <= 59; i++) {
  let val = i.toString();
  if (val.length < 2) val = '0' + val;
  minutes.value.push(val);
}

// Biến nội bộ để chọn trên giao diện
const startHour = ref("09");
const startMinute = ref("00");
const startAmPm = ref("AM");

const endHour = ref("10");
const endMinute = ref("00");
const endAmPm = ref("AM");

// Parse chuỗi "HH:mm" thành dữ liệu hiển thị
const parseTimeString = (timeStr: string | undefined, isStart: boolean) => {
  if (!timeStr) return;
  const parts = timeStr.split(':');
  if (parts.length < 2) return;
  
  const hStr = parts[0];
  const mStr = parts[1];
  
  // Kiểm tra an toàn truy cập mảng
  if (!hStr || !mStr) return; 
  
  let h = parseInt(hStr, 10);
  
  let ampm = 'AM';
  let h12 = h;
  if (h >= 12) {
    ampm = 'PM';
    if (h > 12) h12 = h - 12;
  }
  if (h === 0) h12 = 12;
  
  let h24Str = h.toString();
  if (h24Str.length < 2) h24Str = '0' + h24Str;
  
  let h12Str = h12.toString();
  if (h12Str.length < 2) h12Str = '0' + h12Str;

  if (isStart) {
    startMinute.value = mStr;
    startAmPm.value = ampm;
    if (timeFormat.value === '24h') {
      startHour.value = h24Str;
    } else {
      startHour.value = h12Str;
    }
  } else {
    endMinute.value = mStr;
    endAmPm.value = ampm;
    if (timeFormat.value === '24h') {
      endHour.value = h24Str;
    } else {
      endHour.value = h12Str;
    }
  }
};

// Gộp giá trị chọn thành định dạng "HH:mm"
const updateTime = (field: 'startTime' | 'endTime', hour: string, minute: string, ampm: string) => {
  if (timeFormat.value === '24h') {
    formData.value[field] = `${hour}:${minute}`;
  } else {
    let h = parseInt(hour, 10);
    if (ampm === 'PM' && h < 12) h += 12;
    if (ampm === 'AM' && h === 12) h = 0;
    
    let hStr = h.toString();
    if (hStr.length < 2) hStr = '0' + hStr;
    formData.value[field] = `${hStr}:${minute}`;
  }
};

// Đồng bộ UI với state formData
watch([startHour, startMinute, startAmPm], () => updateTime('startTime', startHour.value, startMinute.value, startAmPm.value));
watch([endHour, endMinute, endAmPm], () => updateTime('endTime', endHour.value, endMinute.value, endAmPm.value));

// Parse lại hiển thị khi đổi mode giờ
watch(timeFormat, () => {
  parseTimeString(formData.value.startTime, true);
  parseTimeString(formData.value.endTime, false);
});
// Quản lý người tham gia và tệp đính kèm
const attendeeInput = ref("");
const attendees = ref<string[]>(props.initialData.attendees || []);

const addAttendee = () => {
  const email = attendeeInput.value.trim();
  // Validate format email
  if (email && /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)) {
    // Kiểm tra trùng lặp email
    let isExist = false;
    for (let i = 0; i < attendees.value.length; i++) {
        if (attendees.value[i] === email) {
            isExist = true;
            break;
        }
    }
    if (!isExist) {
        attendees.value.push(email);
    }
    attendeeInput.value = "";
  }
};

const removeAttendee = (index: number) => {
  attendees.value.splice(index, 1);
};

interface Attachment {
  name: string;
  size: number;
  file?: File;
}
const attachments = ref<Attachment[]>(props.initialData.attachments || []);

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (!target.files) return;
  
  // Thêm file vào danh sách đính kèm
  for (let i = 0; i < target.files.length; i++) {
    const file = target.files[i];
    if (!file) continue; // Bỏ qua nếu null/undefined do Type mảng
    
    attachments.value.push({
      name: file.name,
      size: file.size,
      file: file
    });
  }
  target.value = "";
};

const removeAttachment = (index: number) => {
  attachments.value.splice(index, 1);
};


// Reset dữ liệu khi mở dialog
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      formData.value = { ...props.initialData };
      conflictEvents.value = [];
      
      attendeeInput.value = "";
      attendees.value = props.initialData.attendees ? [...props.initialData.attendees] : [];
      attachments.value = props.initialData.attachments ? [...props.initialData.attachments] : [];

      // Khởi tạo hiển thị giờ
      parseTimeString(formData.value.startTime, true);
      parseTimeString(formData.value.endTime, false);
    }
  }
);

// Tự động kiểm tra trùng lịch khi thay đổi thời gian
watch(
  () => [formData.value.eventDate, formData.value.startTime, formData.value.endTime],
  ([date, start, end]) => {
    if (!props.show || !date || !start || !end) {
      conflictEvents.value = [];
      return;
    }
    if (conflictDebounce) clearTimeout(conflictDebounce);
    conflictDebounce = setTimeout(async () => {
      isCheckingConflict.value = true;
      try {
        conflictEvents.value = await props.checkConflicts(
          date as string,
          start as string,
          end as string,
          props.isEditing ? props.editingEventId : undefined
        );
      } catch {
        conflictEvents.value = [];
      } finally {
        isCheckingConflict.value = false;
      }
    }, 400);
  }
);

const handleSubmit = () => {
  if (!formData.value.title.trim()) return;

  if (!props.isEditing) {
    const eventDateTime = dayjs(`${formData.value.eventDate}T${formData.value.startTime}`);
    if (eventDateTime.isBefore(dayjs())) {
      warningMessage.value = "Bạn không thể tạo sự kiện với thời gian nằm ở trong quá khứ! Vui lòng chọn lại ngày và giờ phù hợp.";
      showWarning.value = true;
      return;
    }
  }

  emit("save", { 
    ...formData.value, 
    attendees: attendees.value, 
    attachments: attachments.value 
  });
};

// Tạo văn bản mô tả chế độ lặp lại
const recurrenceSummary = computed(() => {
  const type = formData.value.recurrenceType;
  if (!type || type === "NONE") return "";

  const date = dayjs(formData.value.eventDate);

  let text = "";
  switch (type) {
    case "DAILY":
      text = "Sự kiện sẽ lặp lại vào mỗi ngày.";
      break;
    case "WEEKLY":
      text = `Sự kiện sẽ lặp lại vào mỗi thứ ${date.format("dddd")} hàng tuần.`;
      break;
    case "MONTHLY":
      text = `Sự kiện sẽ lặp lại vào ngày ${date.date()} hàng tháng.`;
      break;
    case "YEARLY":
      text = `Sự kiện sẽ lặp lại vào ngày ${date.format("DD [tháng] MM")} hàng năm.`;
      break;
  }

  if (formData.value.recurrenceEndDate) {
    text += ` Kết thúc vào ngày ${dayjs(formData.value.recurrenceEndDate).format("DD/MM/YYYY")}.`;
  } else {
    text += " Tiếp diễn trong vòng 1 năm tiếp theo.";
  }

  return text;
});
</script>

<template>
  <Teleport to="body">
    <div
      v-if="show"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <!-- Overlay -->
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="emit('update:show', false)"
      ></div>

      <!-- Dialog Content -->
      <div
        class="relative bg-zinc-900 rounded-2xl shadow-2xl border border-white/10 w-full max-w-md mx-4 flex flex-col max-h-[90vh] overflow-hidden"
      >
        <!-- Header -->
        <div class="p-6 pb-4 border-b border-white/5 bg-zinc-900/50 backdrop-blur-md sticky top-0 z-10">
          <h2 class="text-lg font-semibold text-white">
            {{ isEditing ? "Chỉnh sửa sự kiện" : "Thêm sự kiện mới" }}
          </h2>
        </div>

        <form @submit.prevent="handleSubmit" class="flex flex-col flex-1 min-h-0">
          <!-- Scrollable Body -->
          <div class="flex-1 overflow-y-auto p-6 space-y-5 custom-scrollbar">
            <!-- Title -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Tiêu đề *</label>
              <input
                v-model="formData.title"
                type="text"
                required
                placeholder="Nhập tiêu đề sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm"
              />
            </div>

            <!-- Description -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Mô tả</label>
              <textarea
                v-model="formData.description"
                rows="3"
                placeholder="Mô tả chi tiết sự kiện..."
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 resize-none text-sm transition-all"
              ></textarea>
            </div>

            <!-- Định dạng 24h hoặc 12h Toggle -->
            <div>
              <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-3">Định dạng giờ</label>
              <div class="inline-flex bg-black/20 p-1 rounded-xl border border-white/5 gap-1.5">
                <button
                  type="button"
                  @click="timeFormat = '24h'"
                  :class="[
                    'px-4 py-1.5 rounded-lg text-xs font-bold transition-all duration-300',
                    timeFormat === '24h' ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20' : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
                  ]"
                >24h</button>
                <button
                  type="button"
                  @click="timeFormat = '12h'"
                  :class="[
                    'px-4 py-1.5 rounded-lg text-xs font-bold transition-all duration-300',
                    timeFormat === '12h' ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20' : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
                  ]"
                >12h (AM/PM)</button>
              </div>
            </div>

            <!-- Date & Time Grid -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="md:col-span-2">
                <label class="block text-sm text-gray-400 mb-1.5 font-medium">Ngày diễn ra *</label>
                <input
                  v-model="formData.eventDate"
                  type="date"
                  required
                  class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 text-sm transition-all"
                />
              </div>
              
              <!-- Component Giờ bắt đầu -->
              <div>
                <label class="block text-sm text-gray-400 mb-1.5 font-medium">Giờ bắt đầu *</label>
                <div class="flex gap-2">
                  <select v-model="startHour" class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
                    <option class="text-black" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">{{ h }}</option>
                  </select>
                  <span class="text-white font-bold self-center">:</span>
                  <select v-model="startMinute" class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
                    <option class="text-black" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
                  </select>
                  <select v-if="timeFormat === '12h'" v-model="startAmPm" class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full">
                    <option class="text-black" value="AM">AM</option>
                    <option class="text-black" value="PM">PM</option>
                  </select>
                </div>
              </div>

              <!-- Component Giờ kết thúc -->
              <div>
                <label class="block text-sm text-gray-400 mb-1.5 font-medium">Giờ kết thúc *</label>
                <div class="flex gap-2">
                  <select v-model="endHour" class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
                    <option class="text-black" v-for="h in (timeFormat === '24h' ? hours24 : hours12)" :key="h" :value="h">{{ h }}</option>
                  </select>
                  <span class="text-white font-bold self-center">:</span>
                  <select v-model="endMinute" class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full custom-scrollbar">
                    <option class="text-black" v-for="m in minutes" :key="m" :value="m">{{ m }}</option>
                  </select>
                  <select v-if="timeFormat === '12h'" v-model="endAmPm" class="bg-white/5 border border-white/10 rounded-lg px-2 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 text-sm w-full">
                    <option class="text-black" value="AM">AM</option>
                    <option class="text-black" value="PM">PM</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Recurrence Settings -->
            <div class="space-y-4 p-4 bg-zinc-800/40 rounded-2xl border border-white/5 shadow-inner">
              <div>
                <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-3">Chế độ lặp lại</label>
                <div class="grid grid-cols-5 gap-1.5 bg-black/20 p-1 rounded-xl border border-white/5">
                  <button
                    v-for="opt in [
                      { val: 'NONE', label: 'Không', icon: 'pi pi-ban' },
                      { val: 'DAILY', label: 'Ngày', icon: 'pi pi-sync' },
                      { val: 'WEEKLY', label: 'Tuần', icon: 'pi pi-calendar-plus' },
                      { val: 'MONTHLY', label: 'Tháng', icon: 'pi pi-calendar' },
                      { val: 'YEARLY', label: 'Năm', icon: 'pi pi-star' },
                    ]"
                    :key="opt.val"
                    type="button"
                    @click="formData.recurrenceType = opt.val"
                    :class="[
                      'flex flex-col items-center gap-1.5 py-2 px-1 rounded-lg transition-all duration-300',
                      formData.recurrenceType === opt.val
                        ? 'bg-teal-600 text-white shadow-lg shadow-teal-500/20'
                        : 'text-gray-500 hover:text-gray-300 hover:bg-white/5'
                    ]"
                  >
                    <i :class="[opt.icon, 'text-sm']"></i>
                    <span class="text-[9px] font-bold uppercase">{{ opt.label }}</span>
                  </button>
                </div>
              </div>

              <Transition
                enter-active-class="transition duration-300 ease-out"
                enter-from-class="transform -translate-y-2 opacity-0"
                enter-to-class="transform translate-y-0 opacity-100"
                leave-active-class="transition duration-200 ease-in"
                leave-from-class="transform translate-y-0 opacity-100"
                leave-to-class="transform -translate-y-2 opacity-0"
              >
                <div v-if="formData.recurrenceType !== 'NONE'" class="space-y-4 pt-1">
                  <!-- Summary -->
                  <div class="flex items-start gap-2.5 px-3 py-2 bg-teal-500/10 rounded-lg border border-teal-500/20">
                    <i class="pi pi-info-circle text-teal-400 mt-0.5 text-xs"></i>
                    <p class="text-[11px] text-teal-300/90 leading-relaxed font-medium">
                      {{ recurrenceSummary }}
                    </p>
                  </div>

                  <!-- End Date -->
                  <div>
                    <label class="block text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-2">Ngày kết thúc</label>
                    <div class="relative group">
                      <input
                        v-model="formData.recurrenceEndDate"
                        type="date"
                        class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm"
                      />
                      <div v-if="!formData.recurrenceEndDate" class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[10px] text-gray-500 italic">
                        Mặc định: 1 năm
                      </div>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>

            <!-- Conflict Warning -->
            <div
              v-if="conflictEvents.length > 0"
              class="bg-amber-500/10 border border-amber-500/30 rounded-xl p-4 shadow-lg shadow-amber-500/5"
            >
              <div class="flex items-center gap-2.5 text-amber-400 text-sm font-semibold mb-2">
                <i class="pi pi-exclamation-triangle"></i>
                Trùng giờ với {{ conflictEvents.length }} sự kiện:
              </div>
              <ul class="text-xs text-amber-300/70 space-y-1.5 ml-6">
                <li v-for="c in conflictEvents" :key="c.id" class="list-disc leading-relaxed">
                  <span class="font-bold text-amber-400/90">{{ c.title }}</span>
                  <br/>
                  <span class="text-[10px] italic">({{ c.startTime.substring(0, 5) }} - {{ c.endTime.substring(0, 5) }})</span>
                </li>
              </ul>
            </div>

            <!-- Người tham gia (Attendees) -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Người tham gia</label>
              <div class="flex flex-col gap-2">
                <div class="flex gap-2">
                  <input
                    v-model="attendeeInput"
                    @keyup.enter="addAttendee"
                    @keydown.enter.prevent
                    type="text"
                    placeholder="Nhập email và ấn Enter hoặc nút thêm..."
                    class="flex-1 bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-teal-500/50 focus:border-teal-500 transition-all text-sm"
                  />
                  <button type="button" @click="addAttendee" class="bg-white/10 text-white px-3 py-2 rounded-lg hover:bg-white/20 transition-all">
                    <i class="pi pi-plus"></i>
                  </button>
                </div>
                <!-- Danh sách người tham gia -->
                <div v-if="attendees.length > 0" class="flex flex-wrap gap-2 mt-1">
                  <div v-for="(email, idx) in attendees" :key="idx" class="flex items-center gap-1.5 bg-teal-500/20 text-teal-300 px-2 py-1 rounded-md text-xs border border-teal-500/20">
                    <span>{{ email }}</span>
                    <button type="button" @click="removeAttendee(idx)" class="hover:text-white transition-colors">
                      <i class="pi pi-times text-[10px]"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Tệp đính kèm (Attachments) -->
            <div>
              <label class="block text-sm text-gray-400 mb-1.5 font-medium">Tệp đính kèm</label>
              <div class="flex flex-col gap-2">
                <label class="flex justify-center items-center w-full h-20 px-4 transition bg-white/5 border-2 border-white/10 border-dashed rounded-lg appearance-none cursor-pointer hover:border-teal-500/50 hover:bg-white/10 focus:outline-none">
                  <span class="flex items-center space-x-2">
                    <i class="pi pi-upload text-gray-400"></i>
                    <span class="font-medium text-gray-400 text-sm">Nhấn để chọn tệp...</span>
                  </span>
                  <input type="file" multiple class="hidden" @change="handleFileUpload" />
                </label>
                <!-- Danh sách file đính kèm -->
                <div v-if="attachments.length > 0" class="flex flex-col gap-1.5 mt-1">
                  <div v-for="(file, idx) in attachments" :key="idx" class="flex items-center justify-between bg-black/20 p-2 rounded-lg border border-white/5 text-xs">
                    <div class="flex items-center gap-2 truncate">
                      <i class="pi pi-file text-gray-400"></i>
                      <span class="text-gray-300 truncate">{{ file.name }}</span>
                    </div>
                    <button type="button" @click="removeAttachment(idx)" class="text-red-400/80 hover:text-red-400 px-2 shrink-0">
                      <i class="pi pi-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Allow Edit All -->
            <div class="flex items-center gap-3 py-1">
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
              <span class="text-sm text-gray-300 font-medium">Cho phép mọi người chỉnh sửa</span>
            </div>
          </div>

          <!-- Actions Footer -->
          <div class="p-6 pt-4 border-t border-white/5 flex gap-2 justify-end bg-zinc-900/50 backdrop-blur-md sticky bottom-0 z-10 shadow-[0_-10px_20px_-10px_rgba(0,0,0,0.3)]">
            <button
              type="button"
              @click="emit('update:show', false)"
              class="px-5 py-2.5 rounded-xl text-gray-400 hover:text-white hover:bg-white/5 transition-all text-sm font-medium"
            >
              Hủy
            </button>
            <button
              type="submit"
              class="px-6 py-2.5 bg-teal-600 text-white rounded-xl hover:bg-teal-700 hover:shadow-lg hover:shadow-teal-500/20 active:scale-95 transition-all text-sm font-bold"
            >
              {{ isEditing ? "Cập nhật" : "Tạo sự kiện" }}
            </button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- Modal Cảnh báo -->
    <CalendarWarningDialog 
      v-model:show="showWarning" 
      :message="warningMessage" 
    />
  </Teleport>
</template>

<style scoped>
input[type="date"]::-webkit-calendar-picker-indicator,
input[type="time"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
