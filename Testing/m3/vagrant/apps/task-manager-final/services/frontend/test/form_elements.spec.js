const { test, expect } = require('@playwright/test');

test('should fill title input field', async ({ page }) => {
  await page.goto('http://localhost');
  
  const titleInput = page.locator('#title');
  await titleInput.fill('Test Task Title');
  
  await expect(titleInput).toHaveValue('Test Task Title');
});

test('should select priority from dropdown', async ({ page }) => {
  await page.goto('http://localhost');
  
  const prioritySelect = page.locator('#priority');
  await prioritySelect.selectOption('High');
  
  await expect(prioritySelect).toHaveValue('High');
});

test('should fill date input field', async ({ page }) => {
  await page.goto('http://localhost');
  
  const dateInput = page.locator('#date');
  await dateInput.fill('2026-12-25');
  
  await expect(dateInput).toHaveValue('2026-12-25');
});

test('should have both priority options available', async ({ page }) => {
  await page.goto('http://localhost');
  
  const prioritySelect = page.locator('#priority');
  const options = await prioritySelect.locator('option').allTextContents();
  
  expect(options).toContain('Low');
  expect(options).toContain('High');
});
