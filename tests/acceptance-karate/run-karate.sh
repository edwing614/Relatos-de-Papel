#!/bin/bash
# ===========================================
# Relatos de Papel - Ejecutar pruebas Karate
# ===========================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
EVIDENCIAS_DIR="$PROJECT_ROOT/evidencias/karate-report"

mkdir -p "$EVIDENCIAS_DIR"

BASE_URL="${BASE_URL:-http://localhost:8081}"

echo "=== Ejecutando pruebas de aceptación Karate ==="
echo "Base URL: $BASE_URL"
echo ""

cd "$SCRIPT_DIR"
mvn clean test -DbaseUrl="$BASE_URL" -Dkarate.options="--output $EVIDENCIAS_DIR"

EXIT_CODE=$?

# Copiar reportes HTML generados por Karate
if [ -d "$SCRIPT_DIR/target/karate-reports" ]; then
  cp -r "$SCRIPT_DIR/target/karate-reports/"* "$EVIDENCIAS_DIR/" 2>/dev/null
  echo ""
  echo "Reportes copiados a: $EVIDENCIAS_DIR"
fi

if [ $EXIT_CODE -eq 0 ]; then
  echo "=== Pruebas Karate EXITOSAS ==="
else
  echo "=== Pruebas Karate FALLARON (exit code: $EXIT_CODE) ==="
fi

exit $EXIT_CODE
