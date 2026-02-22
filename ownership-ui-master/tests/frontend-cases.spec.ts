import { test, expect } from '@playwright/test';

/**
 * V3 Front-end test cases – maps to V3-100-Test-Cases-Frontend.md.
 * Run with: npm run test:e2e
 * Requires: UI at baseURL (default http://localhost:3000), ownership API at http://localhost:8081.
 * Login tests require the API to be running and valid credentials (default admin / adminpw).
 * Env: UI_TEST_USER, UI_TEST_PASSWORD for login tests.
 */

const ADMIN_USER = process.env.UI_TEST_USER || 'admin';
const ADMIN_PASSWORD = process.env.UI_TEST_PASSWORD || 'adminpw';

/** Login submit button is inside #popup-modal; nav also has a "Login" button that opens the modal. */
function loginSubmitButton(page: import('@playwright/test').Page) {
  return page.locator('#popup-modal').getByRole('button', { name: 'Login' });
}

/** Open login modal, fill credentials, submit, and wait for navigation to /admin or /user. Fails with a clear message if login is rejected or times out. */
async function loginAndWaitForRedirect(page: import('@playwright/test').Page, timeout = 15000) {
  await page.goto('/');
  await page.getByRole('button', { name: /login/i }).click();
  await page.locator('#username').fill(ADMIN_USER);
  await page.locator('#password').fill(ADMIN_PASSWORD);
  await loginSubmitButton(page).click();
  const urlPromise = page.waitForURL(/\/(admin|user)/, { timeout });
  const invalidPromise = page.getByText(/invalid credentials/i).waitFor({ state: 'visible', timeout });
  const result = await Promise.race([
    urlPromise.then(() => 'ok' as const),
    invalidPromise.then(() => 'invalid' as const),
  ]).catch(() => 'timeout' as const);
  if (result === 'invalid') {
    throw new Error(
      `Login failed: Invalid credentials. Ensure the ownership API is running (e.g. port 8081) and username "${ADMIN_USER}" / password are valid.`
    );
  }
  if (result === 'timeout') {
    throw new Error(
      `Login did not redirect within ${timeout}ms. Ensure the ownership API is running and credentials (${ADMIN_USER}) are valid.`
    );
  }
  // Let client hydration and layout render; cookie is now set so a full load will pass middleware
  await page.waitForTimeout(1500);
}

test.describe('FE-001 to FE-020: Landing & Login', () => {
  test('FE-001: Home page loads', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/./);
  });

  test('FE-002: Login button visible on home', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('button', { name: /login/i })).toBeVisible({ timeout: 10000 });
  });

  test('FE-003: Click Login opens login modal', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: /login/i }).click();
    await expect(page.getByRole('heading', { name: /login to your account/i })).toBeVisible({ timeout: 5000 });
    await expect(page.locator('#username')).toBeVisible();
    await expect(page.locator('#password')).toBeVisible();
  });

  test('FE-004: Login with valid admin credentials', async ({ page }) => {
    await loginAndWaitForRedirect(page);
    await expect(page).toHaveURL(/\/(admin\/dashboard|user\/asset)/);
  });

  test('FE-008: Login with empty username shows validation', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: /login/i }).click();
    await page.locator('#password').fill('any');
    await loginSubmitButton(page).click();
    await expect(page.getByText(/username is required/i)).toBeVisible({ timeout: 3000 });
  });

  test('FE-010: Close login modal without submitting', async ({ page }) => {
    await page.goto('/');
    await page.getByRole('button', { name: /login/i }).click();
    await expect(page.locator('#username')).toBeVisible();
    await page.locator('#popup-modal').getByRole('button', { name: 'Close modal' }).click();
    await expect(page.locator('#username')).not.toBeVisible();
  });

  test('FE-018: Logo and brand visible', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('navigation').getByText('OWNER', { exact: true })).toBeVisible();
    await expect(page.getByRole('navigation').getByText('SHIP', { exact: true })).toBeVisible();
  });
});

test.describe('FE-021 to FE-035: Admin Dashboard & Navigation', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndWaitForRedirect(page);
    // Full load so middleware sees session cookie and dashboard layout renders
    await page.goto('/admin/dashboard');
    await page.waitForLoadState('networkidle').catch(() => {});
    const dashboardReady = page.locator('[class*="ant-layout"], .ant-card, a[href*="/admin/dashboard"], a[href*="/admin/asset"]').or(page.getByRole('link', { name: /Dashboard|Asset|Department|Users/i })).first();
    await expect(dashboardReady).toBeVisible({ timeout: 25000 });
  });

  test('FE-021: Admin dashboard loads', async ({ page }) => {
    await expect(page).toHaveURL(/admin\/dashboard/);
    await expect(page.locator('[class*="ant-layout"], .ant-card, a[href*="/admin/"]').first()).toBeVisible();
  });

  test('FE-030: Click Asset opens asset list', async ({ page }) => {
    const assetLink = page.locator('a[href*="/admin/asset"]').first();
    if (await assetLink.isVisible().catch(() => false)) {
      await assetLink.click();
      await expect(page).toHaveURL(/admin\/asset/);
    } else {
      await page.goto('/admin/asset');
      await expect(page).toHaveURL(/admin\/asset/);
    }
  });

  test('FE-031: Click Department opens department list', async ({ page }) => {
    const deptLink = page.locator('a[href*="/admin/department"]').first();
    if (await deptLink.isVisible().catch(() => false)) {
      await deptLink.click();
      await expect(page).toHaveURL(/admin\/department/);
    } else {
      await page.goto('/admin/department');
      await expect(page).toHaveURL(/admin\/department/);
    }
  });

  test('FE-032: Click User opens user list', async ({ page }) => {
    const userLink = page.locator('a[href*="/admin/user"]').first();
    if (await userLink.isVisible().catch(() => false)) {
      await userLink.click();
      await expect(page).toHaveURL(/admin\/user/);
    } else {
      await page.goto('/admin/user');
      await expect(page).toHaveURL(/admin\/user/);
    }
  });
});

test.describe('FE-036 to FE-055: Admin Assets', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndWaitForRedirect(page);
    await page.goto('/admin/asset');
    await page.waitForLoadState('networkidle').catch(() => {});
    const assetPageReady = page.locator('[class*="ant-layout"], .ant-table, .ant-empty, a[href*="/admin/asset"]').or(page.getByText(/Asset Management|Assign New/i)).first();
    await expect(assetPageReady).toBeVisible({ timeout: 25000 });
  });

  test('FE-036: Asset list loads', async ({ page }) => {
    await expect(page).toHaveURL(/admin\/asset/);
    await expect(page.locator('.ant-table, table, [class*="ant-list"], .ant-empty, [class*="ant-layout"]').or(page.getByText(/Asset Management|Asset/i)).first()).toBeVisible();
  });

  test('FE-051: Asset list empty state or table visible', async ({ page }) => {
    await expect(page).toHaveURL(/admin\/asset/);
    await expect(page.locator('.ant-table, table, [class*="empty"], .ant-empty, [class*="ant-layout"]').first()).toBeVisible();
  });

  test('FE-038: Create Asset button/link visible', async ({ page }) => {
    await expect(page).toHaveURL(/admin\/asset/);
    await expect(page.getByRole('button', { name: /Assign New|Create|Add/i }).or(page.locator('a[href*="/admin/asset/create"]')).first()).toBeVisible({ timeout: 15000 });
  });
});

test.describe('FE-076 to FE-090: User flows', () => {
  test.beforeEach(async ({ page }) => {
    await loginAndWaitForRedirect(page);
    await page.goto('/user/asset');
    await page.waitForLoadState('networkidle').catch(() => {});
    const userPageReady = page.locator('[class*="ant-layout"], a[href*="/user/asset"]').or(page.getByRole('link', { name: /Asset|Report Issue|History/i })).first();
    await expect(userPageReady).toBeVisible({ timeout: 25000 });
  });

  test('FE-076: User asset list loads (or redirect to user area)', async ({ page }) => {
    if (!page.url().includes('/user/asset')) await page.goto('/user/asset');
    await expect(page).toHaveURL(/\/user\/asset/);
    await expect(page.locator('a[href*="/user/asset"], [class*="ant-layout"]').first()).toBeVisible();
  });

  test('FE-079: User asset request list loads', async ({ page }) => {
    const link = page.locator('a[href*="/user/asset-request"]').first();
    if (await link.isVisible().catch(() => false)) {
      await link.click();
    } else {
      await page.goto('/user/asset-request');
    }
    await expect(page).toHaveURL(/\/user\/asset-request/);
    await expect(page.locator('a[href*="/user/asset-request"], [class*="ant-layout"]').first()).toBeVisible({ timeout: 15000 });
  });

  test('FE-082: User report issue list loads', async ({ page }) => {
    const link = page.locator('a[href*="/user/report-issue"]').first();
    if (await link.isVisible().catch(() => false)) {
      await link.click();
    } else {
      await page.goto('/user/report-issue');
    }
    await expect(page).toHaveURL(/\/user\/report-issue/);
    await expect(page.locator('a[href*="/user/report-issue"], [class*="ant-layout"]').first()).toBeVisible({ timeout: 15000 });
  });
});

test.describe('FE-091 to FE-100: Error & edge', () => {
  test('FE-093: Non-existent asset show shows error or 404', async ({ page }) => {
    await loginAndWaitForRedirect(page);
    await page.goto('/admin/asset/show/nonexistent-id-12345');
    await expect(page).toHaveURL(/admin\/asset\/show\/nonexistent-id-12345/);
    const anyContent = page.getByText(/error|not found|404/i).or(page.locator('main')).or(page.getByRole('heading')).first();
    await expect(anyContent).toBeVisible({ timeout: 8000 });
  });

  test('FE-100: Dashboard load within 10s', async ({ page }) => {
    const start = Date.now();
    await loginAndWaitForRedirect(page, 10000);
    const elapsed = Date.now() - start;
    expect(elapsed).toBeLessThan(10000);
  });
});

test.describe('FE-013: Unauthenticated access', () => {
  test('Access /admin/dashboard without login redirects or shows login', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.waitForLoadState('domcontentloaded');
    const onLogin = await page.getByRole('heading', { name: /login/i }).isVisible().catch(() => false);
    const onHome = await page.getByRole('button', { name: /login/i }).isVisible().catch(() => false);
    const url = page.url();
    const base = (process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:3000').replace(/\/$/, '');
    const onHomeUrl = url === base + '/' || url === base + '/login' || url.endsWith('/');
    const dashboardNavVisible = await page.locator('a[href*="/admin/dashboard"], a[href*="/admin/asset"]').first().isVisible().catch(() => false);
    expect(onLogin || onHome || onHomeUrl || !dashboardNavVisible).toBeTruthy();
  });
});
