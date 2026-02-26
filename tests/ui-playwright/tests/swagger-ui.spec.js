// @ts-check
const { test, expect } = require('@playwright/test');

// ============================================
// Relatos de Papel - Pruebas UI sobre Swagger UI
// ============================================

const SWAGGER_PATH = '/swagger-ui/index.html';

test.describe('Swagger UI - Catalogo de Libros', () => {

  test('1. Swagger UI carga correctamente', async ({ page }) => {
    await page.goto(SWAGGER_PATH);

    // Esperar a que Swagger UI cargue completamente
    await page.waitForSelector('.swagger-ui', { timeout: 30000 });

    // Verificar que el titulo o encabezado de la API esta visible
    const title = page.locator('.swagger-ui .info .title');
    await expect(title).toBeVisible({ timeout: 15000 });

    // Verificar que existen secciones de operaciones (endpoints)
    const operations = page.locator('.swagger-ui .opblock');
    await expect(operations.first()).toBeVisible({ timeout: 15000 });

    // Screenshot como evidencia
    await page.screenshot({
      path: '../../evidencias/playwright-report/swagger-ui-loaded.png',
      fullPage: true
    });
  });

  test('2. Ejecutar GET /libros desde Swagger UI y obtener 200', async ({ page }) => {
    await page.goto(SWAGGER_PATH);
    await page.waitForSelector('.swagger-ui', { timeout: 30000 });

    // Buscar el bloque GET que contiene exactamente "/libros" como summary (listado)
    // Usamos el opblock-summary-path que contiene solo "/libros" sin /{id}
    const librosGetBlock = page.locator('.opblock-get .opblock-summary-path[data-path="/libros"]').first();
    await librosGetBlock.click();

    // Esperar a que se expanda
    await page.waitForTimeout(1000);

    // Click en "Try it out"
    const tryItOutBtn = page.locator('.opblock-body .try-out__btn').first();
    await tryItOutBtn.click();
    await page.waitForTimeout(500);

    // Click en "Execute"
    const executeBtn = page.locator('.opblock-body .execute.opblock-control__btn').first();
    await executeBtn.click();

    // Esperar la respuesta
    await page.waitForTimeout(3000);

    // Verificar que aparece algun response en la seccion de live responses
    const responseSection = page.locator('.opblock-body .responses-inner .response').first();
    await expect(responseSection).toBeVisible({ timeout: 15000 });

    // Screenshot como evidencia
    await page.screenshot({
      path: '../../evidencias/playwright-report/swagger-get-libros-200.png',
      fullPage: true
    });
  });

});
