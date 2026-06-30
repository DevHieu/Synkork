const fs = require('fs');
const path = require('path');
const readline = require('readline/promises');
const crypto = require('crypto');

const seedPath = path.join(__dirname, 'seed.sql');

const userRefs = [
  '11111111-1111-1111-1111-111111111111',
  '22222222-2222-2222-2222-222222222222',
  '33333333-3333-3333-3333-333333333333',
  '44444444-4444-4444-4444-444444444444',
  '55555555-5555-5555-5555-555555555555',
  '66666666-6666-6666-6666-666666666666',
  '77777777-7777-7777-7777-777777777777',
  '88888888-8888-8888-8888-888888888888'
];

const planByUser = {
  '11111111-1111-1111-1111-111111111111': 'FREE',
  '22222222-2222-2222-2222-222222222222': 'TEAM',
  '33333333-3333-3333-3333-333333333333': 'BUSINESS',
  '44444444-4444-4444-4444-444444444444': 'FREE',
  '55555555-5555-5555-5555-555555555555': 'TEAM',
  '66666666-6666-6666-6666-666666666666': 'BUSINESS',
  '77777777-7777-7777-7777-777777777777': 'FREE',
  '88888888-8888-8888-8888-888888888888': 'TEAM'
};

const statuses = [
  { value: 'PAID', weight: 58 },
  { value: 'PENDING', weight: 24 },
  { value: 'FAILED', weight: 18 }
];

const methods = [
  { value: 'MOMO', weight: 62 },
  { value: 'VNPAY', weight: 38 }
];

const currencies = [
  { value: 'VND', weight: 95 },
  { value: 'USD', weight: 5 }
];

const plans = ['TEAM', 'BUSINESS'];
const txPrefixes = {
  MOMO: ['MOMO', 'MOMO-QR', 'MOMO-APP'],
  VNPAY: ['VNPAY', 'VNPAY-QR', 'VNPAY-WEB']
};
const amountsByPlan = {
  TEAM: [199000, 229000, 249000, 299000],
  BUSINESS: [399000, 449000, 499000, 599000]
};

// Global statistics tracker
const stats = {
  totalRows: 0,
  totalAmount: 0,
  totalPaidAmount: 0,
  status: { PAID: 0, PENDING: 0, FAILED: 0 },
  plan: { TEAM: 0, BUSINESS: 0 },
  currency: { VND: 0, USD: 0 }
};

let rand;

function mulberry32(a) {
  return function () {
    let t = a += 0x6D2B79F5;
    t = Math.imul(t ^ t >>> 15, t | 1);
    t ^= t + Math.imul(t ^ t >>> 7, t | 61);
    return ((t ^ t >>> 14) >>> 0) / 4294967296;
  };
}

function pickWeighted(items) {
  const total = items.reduce((sum, item) => sum + item.weight, 0);
  let cursor = rand() * total;
  for (const item of items) {
    cursor -= item.weight;
    if (cursor <= 0) return item.value;
  }
  return items[items.length - 1].value;
}

function pick(list) {
  return list[Math.floor(rand() * list.length)];
}

function deterministicUuid(seedVal, index) {
  const hash = crypto.createHash('md5').update(`${seedVal}-${index}`).digest('hex');
  const chars = hash.split('');
  chars[12] = '4'; // Version 4
  chars[16] = (parseInt(chars[16], 16) & 0x3 | 0x8).toString(16); // Variant 1
  return [
    chars.slice(0, 8).join(''),
    chars.slice(8, 12).join(''),
    chars.slice(12, 16).join(''),
    chars.slice(16, 20).join(''),
    chars.slice(20, 32).join('')
  ].join('-');
}

function sqlDateExpr(hoursAgo) {
  if (hoursAgo === 0) return 'NOW()';
  if (hoursAgo % 24 === 0) return `DATE_SUB(NOW(), INTERVAL ${hoursAgo / 24} DAY)`;
  return `DATE_SUB(NOW(), INTERVAL ${hoursAgo} HOUR)`;
}

function buildRow(i, seedVal) {
  const invoiceId = deterministicUuid(seedVal, i);
  const subscriptionId = deterministicUuid(seedVal, i * 1000); // Generate unique subscription_id
  const userId = pick(userRefs);
  const plan = planByUser[userId] && planByUser[userId] !== 'FREE' ? planByUser[userId] : pick(plans);
  const status = pickWeighted(statuses);
  const paymentMethod = pickWeighted(methods);
  const currency = pickWeighted(currencies);
  const amountVal = pick(amountsByPlan[plan]);
  const amount = amountVal.toFixed(2);
  const txPrefix = pick(txPrefixes[paymentMethod]);

  // Generate a realistic payment provider suffix
  const txSuffixLen = paymentMethod === 'MOMO' ? 10 : 8;
  let txSuffix = '';
  for (let j = 0; j < txSuffixLen; j++) {
    txSuffix += Math.floor(rand() * 10);
  }
  const transactionId = `${txPrefix}-${txSuffix}`;

  const createdHoursAgo = 6 + Math.floor(rand() * 24 * 365);
  let paidHoursAgo = null;
  if (status === 'PAID') {
    const maxDelay = Math.min(48, createdHoursAgo - 1);
    const delay = maxDelay > 1 ? 1 + Math.floor(rand() * maxDelay) : 1;
    paidHoursAgo = createdHoursAgo - delay;
  }

  const createdAt = i % 13 === 0 ? 'NOW()' : sqlDateExpr(createdHoursAgo);
  const paidAt = paidHoursAgo == null ? 'NULL' : sqlDateExpr(paidHoursAgo);

  // Update statistics
  stats.totalRows++;
  stats.totalAmount += amountVal;
  if (status === 'PAID') {
    stats.totalPaidAmount += amountVal;
  }
  stats.status[status] = (stats.status[status] || 0) + 1;
  stats.plan[plan] = (stats.plan[plan] || 0) + 1;
  stats.currency[currency] = (stats.currency[currency] || 0) + 1;

  return [
    `    UUID_TO_BIN('${invoiceId}')`,
    `    UUID_TO_BIN('${userId}')`,
    `    ${amount}`,
    `    '${status}'`,
    `    '${paymentMethod}'`,
    `    '${transactionId}'`,
    `    ${paidAt}`,
    `    ${createdAt}`,
    `    NOW()`
  ].join(',\n');
}

function parseArgs() {
  const args = process.argv.slice(2);
  let rows = Number.parseInt(process.env.SEED_ROWS || '1000', 10);
  let mode = (process.env.SEED_MODE || 'append').toLowerCase();
  let seed = Number.parseInt(process.env.SEED_SEED || '20260615', 10);

  if (args.length > 0) {
    for (let i = 0; i < args.length; i++) {
      const arg = args[i];
      if (arg === '--rows' || arg === '-r') {
        const val = Number.parseInt(args[++i], 10);
        if (!Number.isNaN(val)) rows = val;
      } else if (arg === '--mode' || arg === '-m') {
        mode = args[++i].toLowerCase();
      } else if (arg === '--seed' || arg === '-s') {
        const val = Number.parseInt(args[++i], 10);
        if (!Number.isNaN(val)) seed = val;
      } else if (!arg.startsWith('-')) {
        // Positional arg: <rows> [<mode>] [<seed>]
        const val = Number.parseInt(arg, 10);
        if (!Number.isNaN(val)) {
          rows = val;
          if (args[i + 1] && !args[i + 1].startsWith('-')) {
            mode = args[++i].toLowerCase();
            if (args[i + 1] && !args[i + 1].startsWith('-')) {
              const sVal = Number.parseInt(args[++i], 10);
              if (!Number.isNaN(sVal)) seed = sVal;
            }
          }
        }
      }
    }
  }

  return { rows, mode, seed };
}

async function promptUser() {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });

  try {
    const rowsInput = await rl.question('Enter number of rows to generate [default 1000]: ');
    const modeInput = await rl.question('Enter mode (append/regen) [default append]: ');
    const seedInput = await rl.question('Enter seed value [default 20260615]: ');

    const rows = rowsInput.trim() ? Number.parseInt(rowsInput.trim(), 10) : 1000;
    const mode = modeInput.trim() ? modeInput.trim().toLowerCase() : 'append';
    const seed = seedInput.trim() ? Number.parseInt(seedInput.trim(), 10) : 20260615;

    return {
      rows: Number.isNaN(rows) ? 1000 : rows,
      mode: ['append', 'regen'].includes(mode) ? mode : 'append',
      seed: Number.isNaN(seed) ? 20260615 : seed
    };
  } finally {
    rl.close();
  }
}

async function main() {
  let { rows, mode, seed } = parseArgs();

  // Prompt if no CLI args are provided and standard input is interactive
  if (process.argv.length <= 2 && process.stdin.isTTY) {
    const promptConfig = await promptUser();
    rows = promptConfig.rows;
    mode = promptConfig.mode;
    seed = promptConfig.seed;
  }

  // Initialize PRNG
  rand = mulberry32(seed);

  // Reset stats
  stats.totalRows = 0;
  stats.totalAmount = 0;
  stats.totalPaidAmount = 0;
  stats.status = { PAID: 0, PENDING: 0, FAILED: 0 };
  stats.plan = { TEAM: 0, BUSINESS: 0 };
  stats.currency = { VND: 0, USD: 0 };

  const userInserts = userRefs.map((uid, index) => {
    const idx = index + 1;
    const plan = planByUser[uid] || 'FREE';
    return `  (
    UUID_TO_BIN('${uid}'),
    'user${idx}',
    'User ${idx}',
    'user${idx}@example.com',
    'LOCAL',
    'USER',
    'ACTIVE',
    '${plan}',
    0,
    NOW(),
    NOW()
  )`;
  }).join(',\n');

  const usersSql = [
    'INSERT INTO users (',
    '  id, username, display_name, email, provider, role, status, current_plan, warning, created_at, updated_at',
    ') VALUES',
    userInserts,
    'ON DUPLICATE KEY UPDATE',
    '  username = VALUES(username),',
    '  display_name = VALUES(display_name),',
    '  email = VALUES(email),',
    '  current_plan = VALUES(current_plan),',
    '  updated_at = NOW();'
  ].join('\n');

  const prefix = [
    '-- AUTO-GENERATED BULK SEED FOR SUBSCRIPTION TESTING',
    `-- Generated by database/seed-generator.js at seed=${seed}`,
    '',
    usersSql,
    '',
    'INSERT INTO invoices (',
    '  id, user_id, amount, status, payment_method, transaction_id, paid_at, created_at, updated_at',
    ') VALUES'
  ].join('\n');

  const rowsSql = Array.from({ length: rows }, (_, i) => `  (\n${buildRow(i, seed)}\n  )`).join(',\n');
  const sql = `${prefix}\n${rowsSql}\nON DUPLICATE KEY UPDATE\n  user_id = VALUES(user_id),\n  amount = VALUES(amount),\n  status = VALUES(status),\n  payment_method = VALUES(payment_method),\n  transaction_id = VALUES(transaction_id),\n  paid_at = VALUES(paid_at),\n  updated_at = NOW();\n`;

  if (mode === 'regen') {
    const existing = fs.readFileSync(seedPath, 'utf8');
    const marker = '-- AUTO-GENERATED BULK SEED FOR SUBSCRIPTION TESTING';
    const cut = existing.indexOf(marker);
    const base = cut >= 0 ? existing.slice(0, cut).trimEnd() + '\n' : existing;
    fs.writeFileSync(seedPath, `${base}\n${sql}`, 'utf8');
    console.log(`Regenerated ${rows} rows into ${seedPath}`);
  } else {
    fs.appendFileSync(seedPath, `\n${sql}`, 'utf8');
    console.log(`Appended ${rows} generated invoice rows to ${seedPath}`);
  }

  // Display summary stats
  console.log('\n=== Seed Generation Summary ===');
  console.log(`Total Rows Generated: ${stats.totalRows}`);
  console.log(`Total Revenue (PAID): ${stats.totalPaidAmount.toLocaleString('vi-VN')} VND`);
  console.log(`Total Value (All):    ${stats.totalAmount.toLocaleString('vi-VN')} VND`);
  console.log('\nBreakdown by Status:');
  Object.entries(stats.status).forEach(([key, val]) => {
    const pct = ((val / stats.totalRows) * 100).toFixed(1);
    console.log(`- ${key}: ${val} rows (${pct}%)`);
  });
  console.log('\nBreakdown by Plan:');
  Object.entries(stats.plan).forEach(([key, val]) => {
    console.log(`- ${key}: ${val} rows`);
  });
  console.log('\nBreakdown by Currency:');
  Object.entries(stats.currency).forEach(([key, val]) => {
    const pct = ((val / stats.totalRows) * 100).toFixed(1);
    console.log(`- ${key}: ${val} rows (${pct}%)`);
  });
  console.log('===============================\n');
}

main().catch(err => {
  console.error('Error running seed generator:', err);
  process.exit(1);
});