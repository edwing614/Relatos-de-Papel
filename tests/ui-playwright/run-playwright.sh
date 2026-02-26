#!/bin/bash
# ===========================================
# Relatos de Papel - Ejecutar pruebas Playwright
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
EVIDENCIAS_DIR="$PROJECT_ROOT/evidencias/playwright-report"

mkdir -p "$EVIDENCIAS_DIR"

export SWAGGER_UI_URL="${SWAGGER_UI_URL:-http://localhost:8081}"

echo "=== Ejecutando pruebas UI Playwright ==="
echo "Swagger UI URL: $SWAGGER_UI_URL"
echo ""

cd "$SCRIPT_DIR"

# Instalar dependencias si no existen
if [ ! -d "node_modules" ]; then
  echo "Instalando dependencias..."
  npm install
  npx playwright install chromium
fi

npx playwright test

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  echo ""
  echo "=== Pruebas Playwright EXITOSAS ==="
  echo "Reporte HTML: $EVIDENCIAS_DIR/index.html"
else
  echo ""
  echo "=== Pruebas Playwright FALLARON (exit code: $EXIT_CODE) ==="
  echo "Reporte HTML: $EVIDENCIAS_DIR/index.html"
fi

exit $EXIT_CODE
