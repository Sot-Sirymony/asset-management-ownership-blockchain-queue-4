#!/usr/bin/env node
/**
 * V3 API performance measurement – capture baseline and regression data.
 *
 * Usage:
 *   node scripts/api-perf-measure.js [baseUrl]
 *
 * Env:
 *   API_BASE_URL  Base URL (default http://localhost:8081)
 *   API_USER      Login username (default admin)
 *   API_PASSWORD  Login password (default adminpw)
 *   PERF_ITERATIONS  Requests per endpoint (default 10)
 *
 * Output: table of endpoint, min/avg/p95/max ms and error count for pasting into
 * V3-Test-And-Performance-Plan.md baseline table.
 */

const BASE_URL = process.env.API_BASE_URL || process.argv[2] || 'http://localhost:8081';
const USER = process.env.API_USER || 'admin';
const PASSWORD = process.env.API_PASSWORD || 'adminpw';
const ITERATIONS = parseInt(process.env.PERF_ITERATIONS || '10', 10);

function p95(arr) {
  if (arr.length === 0) return 0;
  const sorted = [...arr].sort((a, b) => a - b);
  const idx = Math.ceil(0.95 * sorted.length) - 1;
  return sorted[Math.max(0, idx)];
}

async function measure(name, fn) {
  const times = [];
  let errors = 0;
  for (let i = 0; i < ITERATIONS; i++) {
    const start = performance.now();
    try {
      await fn();
    } catch (e) {
      errors++;
    }
    times.push(performance.now() - start);
  }
  const valid = times.filter(() => true);
  return {
    name,
    n: valid.length,
    errors,
    min: valid.length ? Math.min(...valid) : 0,
    max: valid.length ? Math.max(...valid) : 0,
    avg: valid.length ? valid.reduce((a, b) => a + b, 0) / valid.length : 0,
    p95: p95(valid),
  };
}

async function main() {
  let token = null;

  const login = async () => {
    const res = await fetch(`${BASE_URL}/rest/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: USER, password: PASSWORD }),
    });
    if (!res.ok) throw new Error(`Login ${res.status}`);
    const data = await res.json();
    token = data?.payload?.token || data?.token;
    if (!token) throw new Error('No token in response');
  };

  const get = async (path) => {
    const res = await fetch(`${BASE_URL}${path}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    await res.text();
  };

  console.log(`Base URL: ${BASE_URL}  Iterations: ${ITERATIONS}\n`);

  const results = [];

  results.push(await measure('POST /rest/auth/login', login));
  await login();

  results.push(await measure('GET /api/v1/user/getAllAsset', () => get('/api/v1/user/getAllAsset')));
  results.push(await measure('GET /api/v1/admin/assetRequest', () => get('/api/v1/admin/assetRequest')));
  results.push(await measure('GET /api/v1/user/getAllIssue', () => get('/api/v1/user/getAllIssue')));
  results.push(await measure('GET /api/v1/admin/dashboard', () => get('/api/v1/admin/dashboard')));
  results.push(await measure('GET /api/v1/admin/department', () => get('/api/v1/admin/department')));
  results.push(await measure('GET /api/v1/getAllHistory', () => get('/api/v1/getAllHistory')));

  console.log('| Endpoint | min (ms) | avg (ms) | p95 (ms) | max (ms) | errors |');
  console.log('|----------|----------|----------|----------|----------|--------|');
  for (const r of results) {
    console.log(
      `| ${r.name} | ${r.min.toFixed(0)} | ${r.avg.toFixed(0)} | ${r.p95.toFixed(0)} | ${r.max.toFixed(0)} | ${r.errors} |`
    );
  }
  console.log('\nCopy the table above into V3-Test-And-Performance-Plan.md Section 3 (Performance baselines).');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
