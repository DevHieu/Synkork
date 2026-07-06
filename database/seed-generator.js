const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const seedPath = path.join(__dirname, 'seed.sql');
const schema = process.env.SEED_SCHEMA || 'synkork';
const rows = Number.parseInt(process.env.SEED_ROWS || process.argv[2] || '50', 10);
const minRows = Number.isFinite(rows) && rows > 0 ? Math.max(rows, 50) : 50;
const seedToday = process.env.SEED_TODAY || '2026-07-02';

const passwordHash = '$2a$10$W7nRkYxPZ8xq3x8kB0mC8e7c2Yb3h9nYvVgJ6z8u6T4y9s8Qxw9Ka';
const start = new Date(Date.UTC(2026, 0, 1, 2, 0, 0));

const enums = {
  provider: ['LOCAL', 'GOOGLE', 'GITHUB'],
  role: ['USER', 'USER', 'USER', 'MANAGER', 'ADMIN'],
  userStatus: ['ACTIVE', 'ACTIVE', 'ACTIVE', 'INACTIVE', 'BANNED'],
  plan: ['FREE', 'TEAM', 'BUSINESS'],
  roomType: ['GROUP', 'GROUP', 'DM', 'PERSONAL'],
  roomStatus: ['OPEN', 'OPEN', 'LOCKED', 'PENDING_REMOVAL'],
  roomMemberRole: ['OWNER', 'ADMIN', 'MEMBER', 'MEMBER', 'MEMBER'],
  spaceType: ['CHAT', 'VOICE', 'CALENDAR', 'NOTE', 'TASK'],
  spaceStatus: ['OPEN', 'OPEN', 'PENDING_REMOVAL'],
  messageType: ['TEXT', 'TEXT', 'IMAGE', 'FILE'],
  friendRequestStatus: ['PENDING', 'ACCEPTED', 'REJECTED'],
  notificationType: ['FRIEND', 'CALENDAR', 'TASK', 'NOTE', 'CHAT', 'SUBSCRIPTION'],
  notificationRefType: ['FRIEND_REQUEST', 'FRIEND_ACCEPT', 'FRIEND_REJECT', 'EVENT_REMINDER', 'CARD_ASSIGNED', 'CARD_DUE_SOON', 'CARD_OVER_DUE', 'NOTE_REMINDER'],
  invoiceStatus: ['PENDING', 'PAID', 'FAILED'],
  paymentMethod: ['MOMO', 'VNPAY'],
  reportReason: ['SPAM', 'INAPPROPRIATE', 'HARASSMENT', 'HATE_SPEECH', 'OTHER'],
  reportStatus: ['PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED'],
  reportType: ['USER', 'ROOM'],
  severityByReason: {
    SPAM: 'LOW',
    INAPPROPRIATE: 'MEDIUM',
    HARASSMENT: 'HIGH',
    HATE_SPEECH: 'CRITICAL',
    OTHER: 'LOW'
  },
  auditAction: ['CREATE_USER', 'CREATE_INVOICE', 'UPDATE_INVOICE', 'DELETE_INVOICE', 'CANCEL_SUBSCRIPTION', 'UPDATE_SUBSCRIPTION', 'BAN_USER', 'UNBAN_USER', 'WARN_USER', 'UPDATE_USER', 'DELETE_USER', 'CREATE_WORKSPACE', 'UPDATE_WORKSPACE', 'DELETE_WORKSPACE', 'LOCK_WORKSPACE', 'UNLOCK_WORKSPACE', 'WARN_WORKSPACE', 'CREATE_MANAGER', 'UPDATE_MANAGER', 'LOCK_MANAGER', 'RESOLVE_REPORT', 'DISMISS_REPORT', 'REPORT_CREATED', 'REPORT_REVIEWED', 'REPORT_DELETED'],
  auditEntityType: ['USER', 'WORKSPACE', 'REPORT', 'SUBSCRIPTION', 'MANAGER'],
  passwordResetStatus: ['PENDING', 'APPROVED', 'REJECTED', 'NOT_VERIFIED'],
  verifyType: ['REGISTER', 'FORGOT_PASSWORD', 'CHANGE_EMAIL'],
  attachmentType: ['IMAGE', 'FILE'],
  recurrence: ['NONE', 'DAILY', 'WEEKLY', 'MONTHLY']
};

function uuid(label, index) {
  const hash = crypto.createHash('md5').update(`synkork-seed:${label}:${index}`).digest('hex').split('');
  hash[12] = '4';
  hash[16] = (parseInt(hash[16], 16) & 0x3 | 0x8).toString(16);
  return [
    hash.slice(0, 8).join(''),
    hash.slice(8, 12).join(''),
    hash.slice(12, 16).join(''),
    hash.slice(16, 20).join(''),
    hash.slice(20, 32).join('')
  ].join('-');
}

function hexId(label, index) {
  return `0x${uuid(label, index).replace(/-/g, '').toUpperCase()}`;
}

function strId(label, index) {
  return uuid(label, index);
}

function q(value) {
  if (value === null || value === undefined) return 'NULL';
  return `'${String(value).replace(/'/g, "''")}'`;
}

function bool(value) {
  return value ? '1' : '0';
}

function pick(list, index) {
  return list[index % list.length];
}

function dt(index, hourOffset = 0) {
  const date = new Date(start.getTime() + (index * 29 + hourOffset) * 60 * 60 * 1000);
  return date.toISOString().slice(0, 19).replace('T', ' ');
}

function recentDt(index, total, monthsBack = 4, offsetMinutes = 0) {
  const end = new Date(`${seedToday}T23:00:00Z`);
  const spanHours = monthsBack * 30 * 24;
  const stepHours = total <= 1 ? 0 : Math.floor(spanHours / (total - 1));
  const date = new Date(end.getTime() - index * stepHours * 60 * 60 * 1000 + offsetMinutes * 60 * 1000);
  return date.toISOString().slice(0, 19).replace('T', ' ');
}

function createdAt(index, total) {
  return q(recentDt(index, total, 4, -60));
}

function updatedAt(index, total) {
  return q(recentDt(index, total));
}

function dateOnly(index) {
  const date = new Date(Date.UTC(2026, 6, 1 + (index % 90), 0, 0, 0));
  return date.toISOString().slice(0, 10);
}

function timeOnly(index, baseHour = 8) {
  const hour = baseHour + (index % 8);
  return `${String(hour).padStart(2, '0')}:00:00`;
}

function values(rows) {
  return rows.map(row => `  (${row.join(', ')})`).join(',\n');
}

function insert(table, columns, rows) {
  return [
    `-- ${table}: ${rows.length} rows`,
    `INSERT IGNORE INTO \`${schema}\`.\`${table}\` (${columns.map(column => `\`${column}\``).join(', ')}) VALUES`,
    `${values(rows)};`,
    ''
  ].join('\n');
}

const users = Array.from({ length: Math.max(minRows, 80) }, (_, i) => ({
  id: hexId('user', i),
  rawId: strId('user', i),
  email: `seed.user${String(i + 1).padStart(3, '0')}@synkork.test`,
  username: `seed_user_${String(i + 1).padStart(3, '0')}`,
  name: `Seed User ${String(i + 1).padStart(3, '0')}`,
  role: pick(enums.role, i),
  plan: pick(enums.plan, i),
  status: pick(enums.userStatus, i)
}));

const rooms = Array.from({ length: minRows }, (_, i) => ({
  id: hexId('room', i),
  rawId: strId('room', i),
  owner: users[i % users.length],
  name: `Seed Room ${String(i + 1).padStart(3, '0')}`,
  type: pick(enums.roomType, i),
  status: pick(enums.roomStatus, i)
}));

const roomMembers = [];
for (let roomIndex = 0; roomIndex < rooms.length; roomIndex += 1) {
  for (let memberIndex = 0; memberIndex < 5; memberIndex += 1) {
    const userIndex = (roomIndex * 5 + memberIndex) % users.length;
    roomMembers.push({
      id: hexId('room-member', roomIndex * 5 + memberIndex),
      rawId: strId('room-member', roomIndex * 5 + memberIndex),
      room: rooms[roomIndex],
      user: users[userIndex],
      role: memberIndex === 0 ? 'OWNER' : pick(enums.roomMemberRole, memberIndex + roomIndex)
    });
  }
}

const spaces = [];
for (let roomIndex = 0; roomIndex < rooms.length; roomIndex += 1) {
  for (let typeIndex = 0; typeIndex < enums.spaceType.length; typeIndex += 1) {
    const index = roomIndex * enums.spaceType.length + typeIndex;
    const type = enums.spaceType[typeIndex];
    spaces.push({
      id: hexId('space', index),
      rawId: strId('space', index),
      room: rooms[roomIndex],
      type,
      name: `${type.charAt(0)}${type.slice(1).toLowerCase()} Space ${String(roomIndex + 1).padStart(3, '0')}`
    });
  }
}

const chatSpaces = spaces.filter(space => space.type === 'CHAT');
const taskSpaces = spaces.filter(space => space.type === 'TASK');
const noteSpaces = spaces.filter(space => space.type === 'NOTE');
const calendarSpaces = spaces.filter(space => space.type === 'CALENDAR');

const columns = [];
const columnNames = ['Backlog', 'Todo', 'Doing', 'Review', 'Done'];
taskSpaces.forEach((space, spaceIndex) => {
  columnNames.forEach((name, columnIndex) => {
    const index = spaceIndex * columnNames.length + columnIndex;
    columns.push({
      id: hexId('column', index),
      rawId: strId('column', index),
      space,
      name,
      position: columnIndex
    });
  });
});

const cards = Array.from({ length: Math.max(minRows, 120) }, (_, i) => {
  const column = columns[i % columns.length];
  const roomMember = roomMembers.find(member => member.room.rawId === column.space.room.rawId) || roomMembers[i % roomMembers.length];
  return {
    id: hexId('card', i),
    rawId: strId('card', i),
    column,
    createdBy: roomMember,
    position: i % 20
  };
});

const calendarEvents = Array.from({ length: minRows }, (_, i) => ({
  id: hexId('calendar-event', i),
  rawId: strId('calendar-event', i),
  space: calendarSpaces[i % calendarSpaces.length],
  createdBy: users[i % users.length]
}));

const reports = Array.from({ length: minRows }, (_, i) => {
  const reason = pick(enums.reportReason, i);
  const reportType = pick(enums.reportType, i);
  return {
    id: hexId('report', i),
    rawId: strId('report', i),
    targetUser: reportType === 'USER' ? users[(i + 7) % users.length] : null,
    targetRoom: reportType === 'ROOM' ? rooms[(i + 3) % rooms.length] : null,
    reporter: users[i % users.length],
    reason,
    severity: enums.severityByReason[reason],
    reportType
  };
});

const sections = [];

sections.push(insert('users',
  ['id', 'username', 'display_name', 'email', 'password', 'avatar_url', 'avatar_id', 'provider', 'role', 'status', 'current_plan', 'plan_expires_at', 'personal_note_id', 'personal_calendar_id', 'google_calendar_refresh_token', 'google_calendar_access_token', 'google_calendar_access_token_expires_at', 'warning', 'created_at', 'updated_at'],
  users.map((user, i) => [
    user.id, q(user.username), q(user.name), q(user.email), q(passwordHash), q(`https://cdn.synkork.test/avatar/${i + 1}.png`), q(`seed-avatar-${i + 1}`),
    q(pick(enums.provider, i)), q(user.role), q(user.status), q(user.plan),
    user.plan === 'FREE' ? 'NULL' : q(dt(i, 24 * 45)),
    'NULL', 'NULL', 'NULL', 'NULL', 'NULL', i % 5, createdAt(i, users.length), updatedAt(i, users.length)
  ])));

sections.push(insert('rooms',
  ['id', 'name', 'avatar_url', 'avatar_id', 'description', 'type', 'status', 'invite_code', 'owner_id', 'warning', 'created_at', 'updated_at'],
  rooms.map((room, i) => [
    room.id, q(room.name), 'NULL', 'NULL', q(`Seed workspace room ${i + 1}`), q(room.type), q(room.status),
    q(`SEED${String(i + 1).padStart(4, '0')}`), room.owner.id, i % 4, createdAt(i, rooms.length), updatedAt(i, rooms.length)
  ])));

sections.push(insert('room_members',
  ['id', 'room_id', 'user_id', 'role', 'joined_at', 'muted', 'deafen'],
  roomMembers.map((member, i) => [
    member.id, member.room.id, member.user.id, q(member.role), q(dt(i, 4)), bool(i % 17 === 0), bool(i % 23 === 0)
  ])));

sections.push(insert('spaces',
  ['id', 'name', 'type', 'room_id', 'status', 'is_restricted', 'created_at', 'updated_at'],
  spaces.map((space, i) => [
    space.id, q(space.name), q(space.type), space.room.id, q(pick(enums.spaceStatus, i)), bool(i % 19 === 0), createdAt(i, spaces.length), updatedAt(i, spaces.length)
  ])));

sections.push(insert('messages',
  ['id', 'space_id', 'sender_id', 'content', 'deleted', 'pinned', 'edited', 'type', 'reply_to_id', 'attachment_url', 'attachment_public_id', 'attachment_resource_type', 'attachment_name', 'created_at', 'updated_at'],
  Array.from({ length: Math.max(minRows, 150) }, (_, i) => {
    const space = chatSpaces[i % chatSpaces.length];
    const sender = roomMembers.find(member => member.room.rawId === space.room.rawId) || roomMembers[i % roomMembers.length];
    const type = pick(enums.messageType, i);
    return [
      hexId('message', i), space.id, sender.id, q(`Seed message ${i + 1} in ${space.name}`),
      bool(false), bool(i % 31 === 0), bool(i % 11 === 0), q(type), i > 0 && i % 10 === 0 ? hexId('message', i - 1) : 'NULL',
      type === 'TEXT' ? 'NULL' : q(`https://cdn.synkork.test/files/message-${i + 1}`),
      type === 'TEXT' ? 'NULL' : q(`seed/messages/${i + 1}`),
      type === 'IMAGE' ? q('image') : type === 'FILE' ? q('raw') : 'NULL',
      type === 'TEXT' ? 'NULL' : q(`message-${i + 1}.${type === 'IMAGE' ? 'png' : 'pdf'}`),
      createdAt(i, Math.max(minRows, 150)), updatedAt(i, Math.max(minRows, 150))
    ];
  })));

sections.push(insert('columns',
  ['id', 'space_id', 'name', 'position', 'archived', 'archived_at'],
  columns.map((column, i) => [
    column.id, column.space.id, q(column.name), column.position, bool(false), i % 37 === 0 ? q(dt(i, 20)) : 'NULL'
  ])));

sections.push(insert('cards',
  ['id', 'column_id', 'title', 'description', 'position', 'created_by', 'created_at', 'updated_at', 'due_date', 'overdue_mail_sent', 'due_soon_mail_sent', 'archived', 'archived_at'],
  cards.map((card, i) => [
    card.id, card.column.id, q(`Seed task card ${i + 1}`), q(`Generated task card ${i + 1} for ${card.column.name}`), card.position, card.createdBy.id,
    createdAt(i, cards.length), updatedAt(i, cards.length), q(dt(i, 24 * (3 + (i % 21)))), bool(i % 18 === 0), bool(i % 15 === 0), bool(i % 41 === 0), i % 41 === 0 ? q(dt(i, 11)) : 'NULL'
  ])));

sections.push(insert('card_assignees',
  ['card_id', 'room_member_id'],
  cards.map((card, i) => [
    card.id, roomMembers[(i * 3) % roomMembers.length].id
  ])));

sections.push(insert('notes',
  ['id', 'space_id', 'title', 'note', 'pinned', 'allow_edit_all', 'created_by', 'color', 'pos_x', 'pos_y', 'width', 'height', 'reminder_at', 'reminder_sent', 'archived', 'created_at', 'updated_at'],
  Array.from({ length: minRows }, (_, i) => {
    const space = noteSpaces[i % noteSpaces.length];
    return [
      hexId('note', i), space.id, q(`Seed note ${i + 1}`), q(`Long note body for seed note ${i + 1}`), bool(i % 9 === 0), bool(i % 4 !== 0),
      users[i % users.length].id, q(pick(['#FDE68A', '#BFDBFE', '#BBF7D0', '#FBCFE8', '#DDD6FE'], i)), (i % 6) * 2, Math.floor(i / 6) % 8, 2 + (i % 3), 2 + (i % 2),
      i % 5 === 0 ? q(dt(i, 24 * 7)) : 'NULL', bool(i % 13 === 0), bool(i % 29 === 0), createdAt(i, minRows), updatedAt(i, minRows)
    ];
  })));

sections.push(insert('calendar_events',
  ['id', 'space_id', 'title', 'description', 'event_date', 'start_time', 'end_time', 'recurrence_type', 'recurrence_end_date', 'allow_edit_all', 'remind_before_minutes', 'created_by', 'created_at', 'updated_at'],
  calendarEvents.map((event, i) => [
    event.id, event.space.id, q(`Seed event ${i + 1}`), q(`Calendar fixture event ${i + 1}`), q(dateOnly(i)), q(timeOnly(i)), q(timeOnly(i, 9)),
    q(pick(enums.recurrence, i)), i % 4 === 0 ? q(dateOnly(i + 30)) : 'NULL', bool(i % 3 !== 0), pick([5, 10, 15, 30, 60], i), event.createdBy.id, createdAt(i, calendarEvents.length), updatedAt(i, calendarEvents.length)
  ])));

sections.push(insert('event_attendees',
  ['id', 'event_id', 'user_id', 'created_at', 'updated_at'],
  calendarEvents.map((event, i) => [
    hexId('event-attendee', i), event.id, users[(i * 2 + 1) % users.length].id, createdAt(i, calendarEvents.length), updatedAt(i, calendarEvents.length)
  ])));

sections.push(insert('event_attachments',
  ['id', 'event_id', 'uploaded_by', 'file_url', 'file_name', 'file_size_kb', 'type', 'created_at', 'updated_at'],
  calendarEvents.map((event, i) => {
    const type = pick(enums.attachmentType, i);
    return [
      hexId('event-attachment', i), event.id, users[(i + 5) % users.length].id,
      q(`https://cdn.synkork.test/calendar/event-${i + 1}.${type === 'IMAGE' ? 'png' : 'pdf'}`),
      q(`event-${i + 1}.${type === 'IMAGE' ? 'png' : 'pdf'}`), 128 + (i * 7), q(type), createdAt(i, calendarEvents.length), updatedAt(i, calendarEvents.length)
    ];
  })));

sections.push(insert('friend_requests',
  ['id', 'sender_id', 'receiver_id', 'status', 'message', 'created_at', 'updated_at'],
  Array.from({ length: minRows }, (_, i) => [
    hexId('friend-request', i), users[i % users.length].id, users[(i + 17) % users.length].id, q(pick(enums.friendRequestStatus, i)),
    q(`Seed friend request ${i + 1}`), createdAt(i, minRows), updatedAt(i, minRows)
  ])));

sections.push(insert('friends',
  ['id', 'user_id', 'friend_id', 'conversation_id', 'created_at'],
  Array.from({ length: minRows }, (_, i) => [
    hexId('friend', i), users[i % users.length].id, users[(i + 23) % users.length].id, hexId('friend-conversation', i), createdAt(i, minRows)
  ])));

sections.push(insert('notifications',
  ['id', 'user_id', 'actor_id', 'type', 'ref_id', 'ref_type', 'is_read', 'created_at', 'room_id', 'space_id'],
  Array.from({ length: Math.max(minRows, 100) }, (_, i) => {
    const room = rooms[i % rooms.length];
    const space = spaces[i % spaces.length];
    return [
      hexId('notification', i), users[i % users.length].id, users[(i + 3) % users.length].id, q(pick(enums.notificationType, i)),
      i % 2 === 0 ? cards[i % cards.length].id : calendarEvents[i % calendarEvents.length].id, q(pick(enums.notificationRefType, i)), bool(i % 4 === 0), createdAt(i, Math.max(minRows, 100)), room.id, space.id
    ];
  })));

sections.push(insert('invoices',
  ['id', 'user_id', 'amount', 'status', 'payment_method', 'transaction_id', 'paid_at', 'created_at', 'updated_at'],
  Array.from({ length: Math.max(minRows, 100) }, (_, i) => {
    const status = pick(enums.invoiceStatus, i);
    const method = pick(enums.paymentMethod, i);
    const amount = pick([99000, 199000, 249000, 399000, 499000], i).toFixed(2);
    return [
      hexId('invoice', i), users[i % users.length].id, amount, q(status), q(method),
      q(`${method}-SEED-${String(i + 1).padStart(6, '0')}`), status === 'PAID' ? updatedAt(i, Math.max(minRows, 100)) : 'NULL', createdAt(i, Math.max(minRows, 100)), updatedAt(i, Math.max(minRows, 100))
    ];
  })));

sections.push(insert('reports',
  ['id', 'target_user_id', 'target_room_id', 'reason', 'description', 'report_type', 'reporter_id', 'status', 'severity', 'created_at', 'updated_at'],
  reports.map((report, i) => [
    report.id, report.targetUser ? report.targetUser.id : 'NULL', report.targetRoom ? report.targetRoom.id : 'NULL',
    q(report.reason), q(`Seed report ${i + 1}`), q(report.reportType), report.reporter.id, q(pick(enums.reportStatus, i)), q(report.severity), createdAt(i, reports.length), updatedAt(i, reports.length)
  ])));

sections.push(insert('audit_logs',
  ['id', 'actor_id', 'actor_email', 'action', 'entity_type', 'entity_id', 'entity_name', 'workspace_id', 'description', 'metadata', 'created_at'],
  Array.from({ length: Math.max(minRows, 100) }, (_, i) => {
    const actor = users[i % users.length];
    const target = reports[i % reports.length];
    return [
      hexId('audit-log', i), actor.id, q(actor.email), q(pick(enums.auditAction, i)), q(pick(enums.auditEntityType, i)),
      q(target.rawId), q(`Seed audit target ${i + 1}`), rooms[i % rooms.length].id, q(`Seed audit log ${i + 1}`),
      q(JSON.stringify({ seed: true, index: i + 1, source: 'database/seed-generator.js' })), createdAt(i, Math.max(minRows, 100))
    ];
  })));

sections.push(insert('statistics',
  ['id', 'created_at', 'new_users', 'new_rooms', 'new_subscriptions', 'user_onlines', 'total_users', 'total_rooms', 'total_subscriptions'],
  Array.from({ length: minRows }, (_, i) => [
    hexId('statistic', i), updatedAt(i, minRows), 2 + Math.floor((minRows - 1 - i) / 6), 1 + Math.floor((minRows - 1 - i) / 10), Math.floor((minRows - 1 - i) / 8), 10 + Math.floor((minRows - 1 - i) / 2), 80 + (minRows - 1 - i), 50 + (minRows - 1 - i), 20 + (minRows - 1 - i)
  ])));

sections.push(insert('password_reset_requests',
  ['id', 'user_id', 'new_password', 'status', 'created_at', 'updated_at'],
  Array.from({ length: minRows }, (_, i) => [
    hexId('password-reset', i), users[i % users.length].id, q(`${passwordHash}-reset-${i + 1}`), q(pick(enums.passwordResetStatus, i)), createdAt(i, minRows), updatedAt(i, minRows)
  ])));

sections.push(insert('verification',
  ['id', 'created_at', 'expired_at', 'user_id', 'type', 'otp-code'],
  Array.from({ length: minRows }, (_, i) => [
    hexId('verification', i), createdAt(i, minRows), updatedAt(i, minRows), users[i % users.length].id, q(pick(enums.verifyType, i)), q(String(100000 + i).slice(-6))
  ])));

const sql = [
  '-- ===============================================',
  '-- Synkork Comprehensive MySQL Seed',
  `-- Generated by database/seed-generator.js`,
  `-- Minimum rows per entity: ${minRows}`,
  '-- Password for generated users uses existing bcrypt fixture hash.',
  '-- ===============================================',
  'SET FOREIGN_KEY_CHECKS = 0;',
  '',
  ...sections,
  'SET FOREIGN_KEY_CHECKS = 1;',
  '-- ===== END COMPREHENSIVE SEED =====',
  ''
].join('\n');

fs.writeFileSync(seedPath, sql, 'utf8');

console.log(`Generated ${seedPath}`);
console.log(`Minimum rows per entity: ${minRows}`);
console.log(`Users: ${users.length}`);
console.log(`Rooms: ${rooms.length}`);
console.log(`Room members: ${roomMembers.length}`);
console.log(`Spaces: ${spaces.length}`);
console.log(`Columns: ${columns.length}`);
console.log(`Cards: ${cards.length}`);
console.log(`Calendar events: ${calendarEvents.length}`);
console.log(`Reports: ${reports.length}`);
