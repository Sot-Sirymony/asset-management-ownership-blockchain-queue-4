import { defineConfig, devices } from '@playwright/test';

/**
 * V3 smoke and regression tests.
 * Run with: npm run test:e2e
 * Start the app first: npm run dev (default http://localhost:3000), or from repo root: ./start-api-frontend.sh
 */
export default defineConfig({
  testDir: './tests',
  globalSetup: './tests/global-setup.ts',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'list',
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'], viewport: { width: 1920, height: 1080 } } },
  ],
  timeout: 60000,
});
