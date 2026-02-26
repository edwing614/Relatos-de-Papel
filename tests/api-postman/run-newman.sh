#!/bin/bash
# ===========================================
# Relatos de Papel - Ejecutar pruebas Newman
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
EVIDENCIAS_DIR="$PROJECT_ROOT/evidencias"

mkdir -p "$EVIDENCIAS_DIR"

echo "=== Ejecutando colección Postman con Newman ==="
echo "Directorio de evidencias: $EVIDENCIAS_DIR"

newman run "$SCRIPT_DIR/RelatosDePapel-CRUD-Books.postman_collection.json" \
  -e "$SCRIPT_DIR/local.postman_environment.json" \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export "$EVIDENCIAS_DIR/newman-report.html" \
  --reporter-htmlextra-title "Relatos de Papel - CRUD Books Report" \
  --color on

EXIT_CODE=$?

if [ $EXIT_CODE -eq 0 ]; then
  echo ""
  echo "=== Pruebas Newman EXITOSAS ==="
  echo "Reporte HTML: $EVIDENCIAS_DIR/newman-report.html"
else
  echo ""
  echo "=== Pruebas Newman FALLARON (exit code: $EXIT_CODE) ==="
  echo "Revisa el reporte: $EVIDENCIAS_DIR/newman-report.html"
fi

exit $EXIT_CODE
