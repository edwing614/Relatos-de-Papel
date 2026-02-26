# Relatos de Papel - Guía de Testing

## Prerrequisitos

| Herramienta | Versión mínima | Instalación |
|---|---|---|
| Docker + Docker Compose | 20.x+ | `sudo apt install docker.io docker-compose` |
| Node.js + npm | 18+ | `nvm install 18` |
| Newman | 6+ | `npm install -g newman newman-reporter-htmlextra` |
| k6 | 0.47+ | [https://k6.io/docs/get-started/installation/](https://k6.io/docs/get-started/installation/) |
| Maven | 3.8+ | `sudo apt install maven` |
| Java JDK | 17+ | `sudo apt install openjdk-17-jdk` |
| Playwright | 1.40+ | Se instala automáticamente con `npm install` |

---

## 1. Levantar el Stack

```bash
cd /home/dark/projects/books-microservices
docker-compose up --build -d
```

Esperar ~60 segundos a que todos los servicios estén UP. Verificar:

```bash
# Health checks
curl -s http://localhost:8761/actuator/health | jq .status  # Eureka
curl -s http://localhost:8081/actuator/health | jq .status  # Catalogue
curl -s http://localhost:8082/actuator/health | jq .status  # Payments

# Verificar seed data
curl -s http://localhost:8081/libros | jq '.content | length'  # Debe ser >= 8
```

### Puertos del sistema

| Servicio | Puerto |
|---|---|
| Eureka Server | 8761 |
| API Gateway | 8090 (host) → 8080 (container) |
| Catalogue | 8081 |
| Payments | 8082 |
| Swagger UI (Catalogue) | http://localhost:8081/swagger-ui/index.html |
| Swagger UI (Payments) | http://localhost:8082/swagger-ui/index.html |

---

## 2. Ejecutar las Suites de Prueba

### A) Pruebas de Aceptación - Karate (BDD)

```bash
cd tests/acceptance-karate
bash run-karate.sh
```

O manualmente:
```bash
cd tests/acceptance-karate
mvn clean test -DbaseUrl=http://localhost:8081
```

**Reporte:** `./evidencias/karate-report/`

---

### B) Pruebas UI - Playwright (sobre Swagger UI)

```bash
cd tests/ui-playwright
bash run-playwright.sh
```

O manualmente:
```bash
cd tests/ui-playwright
npm install
npx playwright install chromium
SWAGGER_UI_URL=http://localhost:8081 npx playwright test
```

**Reporte:** `./evidencias/playwright-report/index.html`

---

### C) Pruebas API - Postman + Newman

```bash
# Instalar Newman (si no está)
npm install -g newman newman-reporter-htmlextra

# Ejecutar
bash tests/api-postman/run-newman.sh
```

O manualmente:
```bash
newman run tests/api-postman/RelatosDePapel-CRUD-Books.postman_collection.json \
  -e tests/api-postman/local.postman_environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export evidencias/newman-report.html
```

**Reporte:** `./evidencias/newman-report.html`

---

### D) Pruebas de Carga - k6

```bash
bash tests/load-k6/run-k6.sh
```

O manualmente:
```bash
k6 run \
  -e BASE_URL=http://localhost:8081 \
  -e VUS=10 \
  -e DURATION=30s \
  tests/load-k6/books_search_loadtest.js
```

Personalizar la carga:
```bash
# Más agresivo
VUS=50 DURATION=60s bash tests/load-k6/run-k6.sh

# Suave
VUS=5 DURATION=15s bash tests/load-k6/run-k6.sh
```

**Reportes:** `./evidencias/k6-summary.json` y `./evidencias/k6-report.html`

---

## 3. Ubicación de Reportes

```
evidencias/
├── newman-report.html          # Postman/Newman - CRUD completo
├── k6-summary.json             # k6 - Métricas de carga (JSON)
├── k6-report.html              # k6 - Reporte visual de carga
├── karate-report/              # Karate - Reporte BDD
│   └── karate-summary.html
└── playwright-report/          # Playwright - Reporte UI
    ├── index.html
    ├── swagger-ui-loaded.png
    └── swagger-get-libros-200.png
```

---

## 4. Ejecutar Todo de una Vez

```bash
cd /home/dark/projects/books-microservices

# 1. Levantar stack
docker-compose up --build -d
sleep 60

# 2. Verificar servicios
curl -sf http://localhost:8081/actuator/health || echo "Catalogue no ready"

# 3. Newman (API)
bash tests/api-postman/run-newman.sh

# 4. k6 (Carga)
bash tests/load-k6/run-k6.sh

# 5. Karate (Aceptación)
bash tests/acceptance-karate/run-karate.sh

# 6. Playwright (UI)
bash tests/ui-playwright/run-playwright.sh

echo "=== Todos los reportes en ./evidencias/ ==="
```

---

## 5. Troubleshooting

### Puertos ocupados
```bash
# Verificar qué usa el puerto
lsof -i :8081
# Matar proceso
kill -9 $(lsof -t -i :8081)
```

### Servicios no listos
```bash
# Esperar a que Catalogue esté UP
until curl -sf http://localhost:8081/actuator/health > /dev/null; do
  echo "Esperando Catalogue..."
  sleep 5
done
```

### Docker sin recursos
```bash
# Limpiar contenedores e imágenes antiguas
docker system prune -f
```

### Newman no encuentra el reporter
```bash
npm install -g newman-reporter-htmlextra
```

### Playwright no abre navegador (WSL)
```bash
# Instalar dependencias del sistema
npx playwright install-deps chromium
```

### Karate falla compilando
```bash
# Verificar Java y Maven
java -version   # Debe ser 17+
mvn -version    # Debe ser 3.8+
```

### Datos contaminados entre pruebas
```bash
# Reiniciar servicios para limpiar H2
docker-compose restart ms-books-catalogue ms-books-payments
sleep 15
```
