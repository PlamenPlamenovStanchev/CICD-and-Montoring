const { test, expect } = require('@playwright/test');

test('should display active tasks section', async ({ page }) => {
  await page.goto('http://localhost');
  
  const activeTasksHeading = page.locator('h2:has-text("Active Tasks")');
  await expect(activeTasksHeading).toBeVisible();
});

test('should have task list container', async ({ page }) => {
  await page.goto('http://localhost');
  
  const taskList = page.locator('#task-list');
  await expect(taskList).toBeVisible();
});

test('should display add task section heading', async ({ page }) => {
  await page.goto('http://localhost');
  
  const addTaskHeading = page.locator('h2:has-text("Add New Task")');
  await expect(addTaskHeading).toBeVisible();
});

test('should have feedback message element', async ({ page }) => {
  await page.goto('http://localhost');
  
  // Feedback element exists but might not be visible initially
  const feedback = page.locator('#api-feedback');
  expect(await feedback.count()).toBe(1);
});
