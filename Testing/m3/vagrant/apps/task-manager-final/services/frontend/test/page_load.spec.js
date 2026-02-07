const { test, expect } = require('@playwright/test');

test('should load the page successfully', async ({ page }) => {
  await page.goto('http://localhost');
  
  // Check page title
  await expect(page).toHaveTitle('Task Tracker Pro');
});

test('should display main heading', async ({ page }) => {
  await page.goto('http://localhost');
  
  const heading = page.locator('h1');
  await expect(heading).toBeVisible();
  await expect(heading).toContainText('Microservice Task Tracker');
});

test('should display system status section', async ({ page }) => {
  await page.goto('http://localhost');
  
  // Check if System Status card exists
  const statusCard = page.locator('text=System Status');
  await expect(statusCard).toBeVisible();
});

test('should display task form', async ({ page }) => {
  await page.goto('http://localhost');
  
  // Check if form elements exist
  await expect(page.locator('#title')).toBeVisible();
  await expect(page.locator('#priority')).toBeVisible();
  await expect(page.locator('#date')).toBeVisible();
  await expect(page.locator('button:has-text("Create Task")')).toBeVisible();
});
