<template>
  <div class="overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-body">
        <!-- Header -->
        <div class="card-header">
          <span class="card-icon">
            <svg viewBox="0 0 16 16" width="18" height="18" fill="none">
              <rect x="1" y="1" width="14" height="14" rx="2" stroke="currentColor" stroke-width="1.2"/>
              <path d="M4 5h8M4 8h6M4 11h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
            </svg>
          </span>
          <div class="card-title-wrapper">
            <div
              class="card-title"
              contenteditable="true"
              spellcheck="false"
              @blur="handleTitleBlur"
              v-text="card.title"
            />
            <span class="card-meta">
              trong danh sách <strong>{{ card.list }}</strong> · bảng <strong>{{ card.board }}</strong>
            </span>
          </div>
          <button class="close-btn" @click="$emit('close')">✕</button>
        </div>

        <!-- Two column layout -->
        <div class="cols">
          <!-- Main -->
          <div class="main">

            <!-- Members -->
            <div class="section-title">
              <svg viewBox="0 0 14 14" width="14" height="14" fill="none">
                <circle cx="5" cy="4" r="2.5" stroke="currentColor" stroke-width="1.2"/>
                <circle cx="9" cy="4" r="2.5" stroke="currentColor" stroke-width="1.2"/>
                <path d="M1 11c0-2 1.8-3 4-3h4c2.2 0 4 1 4 3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Thành viên
            </div>
            <div class="members-row">
              <div
                v-for="m in card.members"
                :key="m.initials"
                class="avatar"
                :class="m.color"
                :title="m.name"
              >{{ m.initials }}</div>
              <div class="avatar av-add" title="Thêm thành viên" @click="showAddMember = true">+</div>
            </div>

            <!-- Labels -->
            <div class="section-title">
              <svg viewBox="0 0 14 14" width="14" height="14" fill="none">
                <rect x="1" y="3" width="4" height="3" rx="1" stroke="currentColor" stroke-width="1.2"/>
                <rect x="1" y="8" width="4" height="3" rx="1" stroke="currentColor" stroke-width="1.2"/>
                <path d="M7 4.5h6M7 9.5h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Nhãn
            </div>
            <div class="labels-row">
              <span
                v-for="lb in card.labels"
                :key="lb.text"
                class="label"
                :class="lb.color"
              >{{ lb.text }}</span>
              <span class="label lb-add" @click="showAddLabel = true">+ Thêm</span>
            </div>

            <!-- Due date -->
            <div class="section-title">
              <svg viewBox="0 0 14 14" width="14" height="14" fill="none">
                <rect x="1" y="2" width="12" height="11" rx="1.5" stroke="currentColor" stroke-width="1.2"/>
                <path d="M4 1v2M10 1v2M1 6h12" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Hạn hoàn thành
            </div>
            <div style="margin-bottom: 1.25rem">
              <span class="due-badge">
                <svg viewBox="0 0 14 14" width="12" height="12" fill="none">
                  <rect x="1" y="2" width="12" height="11" rx="1.5" stroke="#27500A" stroke-width="1.2"/>
                  <path d="M4 1v2M10 1v2M1 6h12" stroke="#27500A" stroke-width="1.2" stroke-linecap="round"/>
                </svg>
                {{ card.dueDate }}
              </span>
            </div>

            <!-- Description -->
            <div class="section-title">
              <svg viewBox="0 0 14 14" width="14" height="14" fill="none">
                <path d="M1 3h12M1 7h8M1 11h5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Mô tả
            </div>
            <div
              class="desc-box"
              contenteditable="true"
              spellcheck="false"
              @blur="handleDescBlur"
            >{{ card.description }}</div>

            <!-- Checklist -->
            <div class="checklist-title">
              <svg viewBox="0 0 16 16" width="15" height="15" fill="none">
                <path d="M2 4h12M2 8h12M2 12h8" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Checklist
              <span class="pct">{{ progressPct }}%</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPct + '%', background: progressColor }" />
            </div>

            <div>
              <div
                v-for="(item, i) in card.checklist"
                :key="i"
                class="check-item"
                @click="toggleItem(i)"
              >
                <div class="checkbox" :class="{ checked: item.done }">
                  <svg v-if="item.done" width="9" height="7" viewBox="0 0 9 7" fill="none">
                    <path d="M1 3.5l2.5 2.5L8 1" stroke="white" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <span class="check-label" :class="{ done: item.done }">{{ item.text }}</span>
              </div>
            </div>
            <button class="add-item-btn" @click="addChecklistItem">+ Thêm mục</button>

            <!-- Activity -->
            <div class="activity-section">
              <div class="section-title" style="margin-bottom: 12px">
                <svg viewBox="0 0 14 14" width="14" height="14" fill="none">
                  <path d="M2 11V5a2 2 0 012-2h6a2 2 0 012 2v4a2 2 0 01-2 2H5l-3 2z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/>
                </svg>
                Hoạt động
              </div>

              <div v-for="(act, i) in card.activities" :key="i" class="act-row">
                <div class="act-avatar" :class="act.avatarColor">{{ act.initials }}</div>
                <div class="act-content">
                  <div class="act-name">
                    {{ act.name }}
                    <span class="act-verb">{{ act.verb }}</span>
                    <span v-if="act.badge" class="act-badge" :class="act.badgeColor">{{ act.badge }}</span>
                  </div>
                  <div v-if="act.comment" class="act-comment">{{ act.comment }}</div>
                  <div class="act-time">{{ act.time }}</div>
                </div>
              </div>

              <div class="comment-input-row">
                <div class="act-avatar av3" style="flex-shrink: 0">LH</div>
                <input
                  v-model="newComment"
                  class="comment-input"
                  placeholder="Viết bình luận..."
                  @keydown.enter="submitComment"
                />
              </div>
            </div>
          </div>

          <!-- Sidebar -->
          <div class="sidebar">
            <div class="sidebar-section-label">Thêm vào card</div>
            <button class="sidebar-btn" @click="showAddMember = true">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <circle cx="5" cy="4" r="2.5" stroke="currentColor" stroke-width="1.2"/>
                <path d="M1 11c0-2 1.8-3 4-3h3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                <path d="M10 8v4M8 10h4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
              </svg>
              Thành viên
            </button>
            <button class="sidebar-btn" @click="showAddLabel = true">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <rect x="1" y="3" width="4" height="3" rx="0.8" stroke="currentColor" stroke-width="1.2"/>
                <path d="M7 4.5h6M7 9.5h4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
                <rect x="1" y="8" width="4" height="3" rx="0.8" stroke="currentColor" stroke-width="1.2"/>
              </svg>
              Nhãn
            </button>
            <button class="sidebar-btn" @click="addChecklistItem">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <rect x="1" y="1" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.2"/>
                <path d="M4 7l2 2 4-4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Checklist
            </button>
            <button class="sidebar-btn">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <rect x="1" y="2" width="12" height="11" rx="1.5" stroke="currentColor" stroke-width="1.2"/>
                <path d="M4 1v2M10 1v2M1 6h12" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Ngày hết hạn
            </button>
            <button class="sidebar-btn">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <path d="M7 1v8M4 6l3 3 3-3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 11h10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Đính kèm
            </button>

            <div class="sidebar-section-label">Thao tác</div>
            <button class="sidebar-btn">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <path d="M2 7l3 3 7-7" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Di chuyển
            </button>
            <button class="sidebar-btn">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <rect x="3" y="1" width="8" height="10" rx="1" stroke="currentColor" stroke-width="1.2"/>
                <path d="M5 5h4M5 7h3M5 9h2" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
              Sao chép
            </button>
            <button class="sidebar-btn">
              <svg viewBox="0 0 14 14" width="13" height="13" fill="none">
                <circle cx="4" cy="7" r="1" fill="currentColor"/>
                <circle cx="7" cy="7" r="1" fill="currentColor"/>
                <circle cx="10" cy="7" r="1" fill="currentColor"/>
              </svg>
              Chia sẻ
            </button>

            <button class="danger-btn" @click="confirmDelete">
              <svg viewBox="0 0 14 14" width="12" height="12" fill="none">
                <path d="M2 4h10M5 4V2h4v2M5.5 7v3M8.5 7v3M3 4l1 8h6l1-8" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              Xoá card
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const emit = defineEmits(['close', 'delete', 'update'])

const card = ref({
  title: 'Thiết kế UI cho trang Dashboard',
  list: 'Đang làm',
  board: 'Dự án Website',
  dueDate: '5 Tháng 5, 2025',
  description: 'Thiết kế toàn bộ giao diện trang dashboard bao gồm: biểu đồ thống kê, bảng danh sách task, widget thời tiết và thông báo. Cần đảm bảo responsive trên mọi thiết bị. Tham khảo Figma file đã được share trong nhóm Slack.',
  members: [
    { name: 'Nguyễn Minh', initials: 'NM', color: 'av1' },
    { name: 'Trần Linh',   initials: 'TL', color: 'av2' },
    { name: 'Lê Hoa',      initials: 'LH', color: 'av3' },
  ],
  labels: [
    { text: 'Frontend',    color: 'lb-green' },
    { text: 'Design',      color: 'lb-blue'  },
    { text: 'Ưu tiên cao', color: 'lb-amber' },
  ],
  checklist: [
    { text: 'Phân tích yêu cầu và tạo wireframe', done: true  },
    { text: 'Chọn color palette và typography',   done: true  },
    { text: 'Thiết kế component system',          done: true  },
    { text: 'Thiết kế trang dashboard chính',     done: false },
    { text: 'Review và chỉnh sửa theo feedback',  done: false },
  ],
  activities: [
    {
      name: 'Trần Linh', initials: 'TL', avatarColor: 'av2',
      verb: 'đã thêm nhãn', badge: 'Frontend', badgeColor: 'badge-green',
      comment: null, time: '3 giờ trước',
    },
    {
      name: 'Nguyễn Minh', initials: 'NM', avatarColor: 'av1',
      verb: 'đã bình luận:', badge: null, badgeColor: null,
      comment: 'Mình đã hoàn thành phần wireframe, anh chị xem và cho ý kiến nhé 🎨',
      time: '1 ngày trước',
    },
  ],
})

const newComment = ref('')

const progressPct = computed(() => {
  const total = card.value.checklist.length
  if (!total) return 0
  const done = card.value.checklist.filter(i => i.done).length
  return Math.round((done / total) * 100)
})

const progressColor = computed(() => progressPct.value === 100 ? '#1D9E75' : '#1D9E75')

function toggleItem(i) {
  card.value.checklist[i].done = !card.value.checklist[i].done
}

function addChecklistItem() {
  const text = prompt('Tên mục mới:')
  if (!text?.trim()) return
  card.value.checklist.push({ text: text.trim(), done: false })
}

function submitComment() {
  const val = newComment.value.trim()
  if (!val) return
  card.value.activities.push({
    name: 'Lê Hoa', initials: 'LH', avatarColor: 'av3',
    verb: 'đã bình luận:', badge: null, badgeColor: null,
    comment: val, time: 'Vừa xong',
  })
  newComment.value = ''
}

function handleTitleBlur(e) {
  card.value.title = e.target.innerText.trim()
}

function handleDescBlur(e) {
  card.value.description = e.target.innerText.trim()
}

function confirmDelete() {
  if (confirm('Xoá card này?')) emit('delete', card.value)
}

const showAddMember = ref(false)
const showAddLabel  = ref(false)
</script>

<style scoped>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 2rem 1rem;
  z-index: 1000;
  overflow-y: auto;
}

.modal {
  background: #f1f2f4;
  border-radius: 12px;
  width: 100%;
  max-width: 700px;
  min-height: 400px;
}

.modal-body {
  padding: 1.25rem 1.5rem 1.5rem;
}

/* Header */
.card-header {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 1.25rem;
}

.card-icon {
  font-size: 18px;
  margin-top: 3px;
  flex-shrink: 0;
  color: #44546f;
}

.card-title-wrapper { flex: 1; }

.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #172b4d;
  line-height: 1.4;
  outline: none;
  border-radius: 4px;
  padding: 2px 4px;
  margin: -2px -4px;
  cursor: text;
}
.card-title:hover { background: #e2e4ea; }
.card-title:focus { background: #fff; box-shadow: 0 0 0 2px #0c66e4; }

.card-meta {
  display: block;
  font-size: 12px;
  color: #626f86;
  margin-top: 4px;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #626f86;
  padding: 4px 8px;
  border-radius: 6px;
  flex-shrink: 0;
  line-height: 1;
}
.close-btn:hover { background: #dfe1e6; color: #172b4d; }

/* Layout */
.cols {
  display: grid;
  grid-template-columns: 1fr 160px;
  gap: 1.5rem;
}

/* Section title */
.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #44546f;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

/* Members */
.members-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 1.25rem;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  border: 2px solid #f1f2f4;
  flex-shrink: 0;
}

.av1 { background: #b3d4f5; color: #0c44a0; }
.av2 { background: #9fe1cb; color: #085041; }
.av3 { background: #f4c0d1; color: #72243e; }
.av-add {
  background: #dfe1e6;
  color: #626f86;
  font-size: 18px;
  border: 1.5px dashed #b3bac5;
  font-weight: 400;
}
.av-add:hover { background: #c2c7d0; }

/* Labels */
.labels-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 1.25rem;
}

.label {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
}
.lb-green { background: #baf3db; color: #164b35; }
.lb-blue  { background: #cce0ff; color: #09326c; }
.lb-amber { background: #f8e6a0; color: #533f04; }
.lb-add   { background: #dfe1e6; color: #626f86; font-weight: 500; }
.lb-add:hover { background: #c2c7d0; }

/* Due date */
.due-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  background: #baf3db;
  color: #164b35;
  cursor: pointer;
  margin-bottom: 1.25rem;
}
.due-badge:hover { background: #aae8cb; }

/* Description */
.desc-box {
  background: #fff;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 13px;
  color: #44546f;
  line-height: 1.6;
  margin-bottom: 1.5rem;
  cursor: text;
  border: 0.5px solid transparent;
  outline: none;
  min-height: 64px;
}
.desc-box:hover { border-color: #b3bac5; background: #fafbfc; }
.desc-box:focus { border-color: #0c66e4; background: #fff; box-shadow: 0 0 0 1px #0c66e4; }

/* Checklist */
.checklist-title {
  font-size: 14px;
  font-weight: 600;
  color: #172b4d;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.pct {
  font-size: 12px;
  color: #626f86;
  font-weight: 400;
  margin-left: auto;
}

.progress-bar {
  height: 6px;
  background: #dfe1e6;
  border-radius: 20px;
  margin-bottom: 12px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 20px;
  transition: width 0.3s ease;
}

.check-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 4px 6px;
  border-radius: 6px;
  cursor: pointer;
}
.check-item:hover { background: #e2e4ea; }

.checkbox {
  width: 15px;
  height: 15px;
  border-radius: 3px;
  border: 1.5px solid #8590a2;
  flex-shrink: 0;
  margin-top: 1px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  transition: all 0.15s;
}
.checkbox.checked { background: #1d9e75; border-color: #1d9e75; }

.check-label { font-size: 13px; color: #172b4d; line-height: 1.4; }
.check-label.done { text-decoration: line-through; color: #8590a2; }

.add-item-btn {
  font-size: 12px;
  color: #44546f;
  background: #dfe1e6;
  border: none;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 6px;
  margin-top: 6px;
  margin-left: 25px;
}
.add-item-btn:hover { background: #c2c7d0; color: #172b4d; }

/* Activity */
.activity-section { margin-top: 1.5rem; }

.act-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 14px;
}

.act-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  border: 2px solid #f1f2f4;
}

.act-content { flex: 1; }
.act-name { font-size: 12px; font-weight: 600; color: #172b4d; }
.act-verb { font-weight: 400; color: #626f86; }

.act-badge {
  padding: 1px 7px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}
.badge-green { background: #baf3db; color: #164b35; }

.act-comment {
  background: #fff;
  padding: 8px 10px;
  border-radius: 6px;
  border: 0.5px solid #dfe1e6;
  font-size: 12px;
  color: #44546f;
  line-height: 1.5;
  margin-top: 4px;
}
.act-time { font-size: 11px; color: #8590a2; margin-top: 3px; }

.comment-input-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-top: 1rem;
}
.comment-input {
  flex: 1;
  font-size: 13px;
  padding: 7px 12px;
  border-radius: 6px;
  border: 1px solid #b3bac5;
  background: #fff;
  color: #172b4d;
  outline: none;
  font-family: inherit;
}
.comment-input:focus { border-color: #0c66e4; box-shadow: 0 0 0 1px #0c66e4; }

/* Sidebar */
.sidebar-section-label {
  font-size: 11px;
  color: #8590a2;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin: 12px 0 6px;
}
.sidebar-section-label:first-child { margin-top: 0; }

.sidebar-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 6px 10px;
  border-radius: 6px;
  background: #dfe1e6;
  border: none;
  font-size: 12px;
  font-weight: 500;
  color: #44546f;
  cursor: pointer;
  margin-bottom: 6px;
  text-align: left;
  font-family: inherit;
}
.sidebar-btn:hover { background: #c2c7d0; color: #172b4d; }

.danger-btn {
  background: none;
  border: none;
  color: #ae2e24;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 0;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-family: inherit;
  font-weight: 500;
}
.danger-btn:hover { text-decoration: underline; }

/* Responsive */
@media (max-width: 540px) {
  .cols { grid-template-columns: 1fr; }
  .sidebar { order: -1; display: flex; flex-wrap: wrap; gap: 6px; }
  .sidebar-btn { width: auto; }
  .sidebar-section-label { width: 100%; }
}
</style>