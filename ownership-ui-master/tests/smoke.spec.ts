import { test, expect } from '@playwright/test';

/**
 * V3 smoke tests – critical flows for release validation.
 * Ensures home loads and login entry point is present.
 */

test.describe('V3 Smoke', () => {
  test('home page loads and shows login entry', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/./);
    await expect(page.getByRole('button', { name: /login/i })).toBeVisible({ timeout: 10000 });
  });

  test('home page has main content', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('body')).toBeVisible();
  });
});
