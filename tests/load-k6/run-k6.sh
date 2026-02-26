#!/bin/bash
# ===========================================
# Relatos de Papel - Ejecutar prueba de carga k6
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
EVIDENCIAS_DIR="$PROJECT_ROOT/evidencias"

mkdir -p "$EVIDENCIAS_DIR"

BASE_URL="${BASE_URL:-http://localhost:8081}"
VUS="${VUS:-10}"
DURATION="${DURATION:-30s}"

echo "=== Ejecutando prueba de carga k6 ==="
echo "Base URL: $BASE_URL"
echo "VUs: $VUS"
echo "Duración: $DURATION"
echo ""

k6 run \
  -e BASE_URL="$BASE_URL" \
  -e VUS="$VUS" \
  -e DURATION="$DURATION" \
  "$SCRIPT_DIR/books_search_loadtest.js"

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  echo ""
  echo "=== Prueba de carga EXITOSA ==="
  echo "Reporte JSON: $EVIDENCIAS_DIR/k6-summary.json"
  echo "Reporte HTML: $EVIDENCIAS_DIR/k6-report.html"
else
  echo ""
  echo "=== Prueba de carga FALLIDA (exit code: $EXIT_CODE) ==="
fi

exit $EXIT_CODE
