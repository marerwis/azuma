// test-api.ts — Azooma API Live Integration Test
// Hits all public endpoints on the running dev server and prints results.
// Run with: npx tsx test-api.ts

const BASE = 'http://localhost:3000';

// ── Helpers ────────────────────────────────────────────────────────────────
const GREEN  = '\x1b[32m';
const RED    = '\x1b[31m';
const YELLOW = '\x1b[33m';
const CYAN   = '\x1b[36m';
const BOLD   = '\x1b[1m';
const RESET  = '\x1b[0m';

let passed = 0;
let failed = 0;

async function test(label: string, fn: () => Promise<void>) {
  process.stdout.write(`  ${CYAN}▶${RESET} ${label} ... `);
  try {
    await fn();
    console.log(`${GREEN}${BOLD}PASS${RESET}`);
    passed++;
  } catch (err: any) {
    console.log(`${RED}${BOLD}FAIL${RESET} — ${err.message}`);
    failed++;
  }
}

async function get(path: string): Promise<any> {
  const res = await fetch(`${BASE}${path}`);
  const body = await res.json();
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${JSON.stringify(body)}`);
  return body;
}

function assert(condition: boolean, message: string) {
  if (!condition) throw new Error(message);
}

function truncate(val: any, maxLen = 80): string {
  const str = JSON.stringify(val);
  return str.length > maxLen ? str.slice(0, maxLen) + '…' : str;
}

// ── Test Suites ────────────────────────────────────────────────────────────

async function testHealth() {
  console.log(`\n${BOLD}${YELLOW}══ Health Check ══════════════════════════════════════${RESET}`);

  await test('GET /health → status ok', async () => {
    const body = await get('/health');
    assert(body.status === 'ok', `Expected status=ok, got: ${body.status}`);
    console.log(`\n      ${CYAN}timestamp:${RESET} ${body.timestamp}`);
  });
}

async function testCategories() {
  console.log(`\n${BOLD}${YELLOW}══ App Categories ════════════════════════════════════${RESET}`);

  let categoryId: string | null = null;

  await test('GET /api/v1/categories → returns array', async () => {
    const body = await get('/api/v1/categories');
    assert(body.success === true, 'success flag missing');
    assert(Array.isArray(body.data), 'data should be array');

    if (body.data.length > 0) {
      categoryId = body.data[0].id;
      console.log(`\n      ${CYAN}Found ${body.data.length} categor${body.data.length === 1 ? 'y' : 'ies'}:${RESET}`);
      body.data.forEach((cat: any, i: number) => {
        console.log(`      ${i + 1}. ${BOLD}${cat.name}${RESET} (stores: ${cat._count?.stores ?? '?'}) [${cat.id}]`);
      });
    } else {
      console.log(`\n      ${YELLOW}⚠ No categories in database yet.${RESET}`);
    }
  });

  if (categoryId) {
    await test(`GET /api/v1/categories/${categoryId.slice(0,8)}… → single category with stores`, async () => {
      const body = await get(`/api/v1/categories/${categoryId}`);
      assert(body.success === true, 'success flag missing');
      assert(body.data.id === categoryId, 'ID mismatch');
      console.log(`\n      ${CYAN}Category:${RESET} ${body.data.name}`);
      console.log(`      ${CYAN}image_url:${RESET} ${body.data.image_url ?? '(none)'}`);
      console.log(`      ${CYAN}Stores inside:${RESET} ${body.data.stores?.length ?? 0}`);
    });
  }
}

async function testStores() {
  console.log(`\n${BOLD}${YELLOW}══ Stores ════════════════════════════════════════════${RESET}`);

  let storeId: string | null = null;

  await test('GET /api/v1/stores → returns array', async () => {
    const body = await get('/api/v1/stores');
    assert(body.success === true, 'success flag missing');
    assert(Array.isArray(body.data), 'data should be array');

    if (body.data.length > 0) {
      storeId = body.data[0].id;
      console.log(`\n      ${CYAN}Found ${body.data.length} store${body.data.length === 1 ? '' : 's'}:${RESET}`);
      body.data.slice(0, 5).forEach((s: any, i: number) => {
        console.log(
          `      ${i + 1}. ${BOLD}${s.name}${RESET}` +
          `  open=${s.is_open ? GREEN + 'yes' + RESET : RED + 'no' + RESET}` +
          `  category=${s.app_categories?.name ?? '(none)'}` +
          `  products=${s._count?.products ?? 0}`
        );
      });
      if (body.data.length > 5) console.log(`      … and ${body.data.length - 5} more.`);
    } else {
      console.log(`\n      ${YELLOW}⚠ No stores in database yet.${RESET}`);
    }
  });

  await test('GET /api/v1/stores?isOpen=true → open stores only', async () => {
    const body = await get('/api/v1/stores?isOpen=true');
    assert(body.success === true, 'success flag missing');
    const allOpen = body.data.every((s: any) => s.is_open === true);
    assert(allOpen, 'Some stores in result are not open');
    console.log(`\n      ${CYAN}Open stores count:${RESET} ${body.data.length}`);
  });

  if (storeId) {
    await test(`GET /api/v1/stores/${storeId.slice(0,8)}… → full store with menu`, async () => {
      const body = await get(`/api/v1/stores/${storeId}`);
      assert(body.success === true, 'success flag missing');
      assert(body.data.id === storeId, 'ID mismatch');
      const s = body.data;
      console.log(`\n      ${CYAN}Store:${RESET}      ${s.name}`);
      console.log(`      ${CYAN}Address:${RESET}    ${s.address}`);
      console.log(`      ${CYAN}Location:${RESET}   ${s.latitude}, ${s.longitude}`);
      console.log(`      ${CYAN}Menu cats:${RESET}  ${s.menu_categories?.length ?? 0}`);
      if (s.menu_categories?.length > 0) {
        s.menu_categories.slice(0, 3).forEach((mc: any) => {
          const prods = mc.products_products_menu_category_idTomenu_categories?.length ?? 0;
          console.log(`        • ${mc.name} (${prods} product${prods === 1 ? '' : 's'})`);
        });
      }
    });

    await test(`GET /api/v1/stores/${storeId.slice(0,8)}…/menu-categories → menu list`, async () => {
      const body = await get(`/api/v1/stores/${storeId}/menu-categories`);
      assert(body.success === true, 'success flag missing');
      console.log(`\n      ${CYAN}Menu categories:${RESET} ${body.data.length}`);
    });

    await test(`GET /api/v1/stores/${storeId.slice(0,8)}…/products → product list`, async () => {
      const body = await get(`/api/v1/stores/${storeId}/products`);
      assert(body.success === true, 'success flag missing');
      console.log(`\n      ${CYAN}Products:${RESET} ${body.data.length}`);
      if (body.data.length > 0) {
        body.data.slice(0, 3).forEach((p: any) => {
          console.log(`        • ${p.name} — ${p.base_price} SAR  ${p.is_veg ? GREEN + '[veg]' + RESET : ''}`);
        });
      }
    });
  }
}

async function testAuthGuards() {
  console.log(`\n${BOLD}${YELLOW}══ Auth Guards (Protected Endpoints) ════════════════${RESET}`);

  await test('GET /api/v1/users (no token) → 401 Unauthorized', async () => {
    const res = await fetch(`${BASE}/api/v1/users`);
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });

  await test('GET /api/v1/orders (no token) → 401 Unauthorized', async () => {
    const res = await fetch(`${BASE}/api/v1/orders`);
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });

  await test('POST /api/v1/categories (no token) → 401 Unauthorized', async () => {
    const res = await fetch(`${BASE}/api/v1/categories`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'hacker category' }),
    });
    assert(res.status === 401, `Expected 401, got ${res.status}`);
  });
}

// ── Main ───────────────────────────────────────────────────────────────────
async function main() {
  console.log(`\n${BOLD}${CYAN}╔════════════════════════════════════════════════════════╗`);
  console.log(`║         AZOOMA API — Live Integration Tests            ║`);
  console.log(`║         Target: ${BASE.padEnd(38)} ║`);
  console.log(`╚════════════════════════════════════════════════════════╝${RESET}`);

  // Check server is reachable first
  try {
    await fetch(`${BASE}/health`);
  } catch {
    console.error(`\n${RED}${BOLD}❌ Cannot reach server at ${BASE}${RESET}`);
    console.error(`   Make sure 'npm run dev' is running in another terminal.\n`);
    process.exit(1);
  }

  await testHealth();
  await testCategories();
  await testStores();
  await testAuthGuards();

  // ── Summary ──────────────────────────────────────────────────────────────
  const total = passed + failed;
  const color = failed === 0 ? GREEN : RED;
  console.log(`\n${BOLD}${YELLOW}══ Results ═══════════════════════════════════════════${RESET}`);
  console.log(`  ${GREEN}Passed: ${passed}${RESET}  |  ${RED}Failed: ${failed}${RESET}  |  Total: ${total}`);
  if (failed === 0) {
    console.log(`\n  ${GREEN}${BOLD}✅ All tests passed! API is live and fetching real Supabase data.${RESET}\n`);
  } else {
    console.log(`\n  ${RED}${BOLD}❌ ${failed} test(s) failed. Check output above.${RESET}\n`);
    process.exit(1);
  }
}

main().catch((e) => {
  console.error(`\n${RED}Unexpected error:${RESET}`, e);
  process.exit(1);
});
