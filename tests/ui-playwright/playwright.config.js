// @ts-check
const { defineConfig } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',
  timeout: 60000,
  expect: { timeout: 10000 },
  fullyParallel: false,
  retries: 1,
  reporter: [
    ['list'],
    ['html', { outputFolder: '../../evidencias/playwright-report', open: 'never' }],
  ],
  use: {
    baseURL: process.env.SWAGGER_UI_URL || 'http://localhost:8081',
    trace: 'on-first-retry',
    screenshot: 'on',
    video: 'on-first-retry',
  },
});
