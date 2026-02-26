# Actividad 3 - Relatos de Papel
## Estrategia de Pruebas con 4 Plataformas

---

## Slide 1: Portada

**Relatos de Papel - books-microservices**
Actividad 3: Implementación de pruebas automatizadas
4 plataformas | 4 tipos de prueba | Cobertura completa

---

## Slide 2: Arquitectura del Sistema

```
                    ┌─────────────┐
                    │   Eureka    │
                    │  :8761      │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────┴──────┐ ┌──┴───────┐ ┌──┴───────────┐
       │ API Gateway │ │Catalogue │ │  Payments    │
       │   :8080     │ │  :8081   │ │    :8082     │
       └─────────────┘ └──────────┘ └──────────────┘
```

- Java 17 + Spring Boot 3.2 + Spring Cloud 2023
- H2 en memoria (perfil local) con seed data
- Sin autenticación | Sin frontend | Docker Compose

---

## Slide 3: Estrategia de Testing - 4 Plataformas

| Tipo de Prueba | Plataforma | Objetivo |
|---|---|---|
| Aceptación (BDD) | **Karate** | Validar flujo completo CRUD en lenguaje natural |
| UI funcional | **Playwright** | Verificar Swagger UI carga y ejecuta endpoints |
| API REST (sistema) | **Postman + Newman** | CRUD completo con assertions automáticas |
| Carga | **k6** | Rendimiento bajo concurrencia (10-50 VUs) |

---

## Slide 4: Postman + Newman - Pruebas API

**Colección:** RelatosDePapel-CRUD-Books

**Flujo de 9 pasos:**
1. Health Check del servicio
2. Listar libros (seed data)
3. Crear libro nuevo (POST)
4. Obtener por ID (GET)
5. Actualizar completo (PUT)
6. Verificar actualización (GET)
7. Actualización parcial (PATCH)
8. Eliminar (DELETE)
9. Confirmar eliminación (404)

**Evidencia:** `evidencias/newman-report.html`

---

## Slide 5: k6 - Prueba de Carga

**Script:** books_search_loadtest.js

**Escenarios por iteración:**
- GET /libros (listado paginado)
- GET /libros/1 (detalle)
- GET /libros?ratingMin=4&precioMax=100 (búsqueda filtrada)

**Configuración:**
- Ramp-up: 10s → 10 VUs
- Sostenido: 30s a 10 VUs
- Ramp-down: 5s → 0

**Thresholds:**
- http_req_failed < 1%
- p(95) < 2000ms

**Evidencia:** `evidencias/k6-report.html`

---

## Slide 6: Karate - Pruebas de Aceptación BDD

**Feature:** libro-crud.feature

```gherkin
Scenario: Viaje completo CRUD
  Given servicio activo
  When creo un libro
  Then lo consulto y valido campos
  When lo actualizo (PUT)
  Then verifico el cambio
  When lo parcheo (PATCH)
  Then solo cambió el campo parcheado
  When lo elimino (DELETE)
  Then confirmo 404
```

**Evidencia:** `evidencias/karate-report/`

---

## Slide 7: Playwright - Pruebas UI Funcionales

**Target:** Swagger UI del servicio Catalogue

**Tests:**
1. Swagger UI carga correctamente (título + endpoints visibles)
2. Ejecutar GET /libros desde UI (Try it out → Execute → verificar 200)

**Screenshots:** Capturados automáticamente como evidencia

**Evidencia:** `evidencias/playwright-report/index.html`

---

## Slide 8: Resultados y Evidencias

```
evidencias/
├── newman-report.html          → 9 tests API, CRUD completo
├── k6-summary.json             → Métricas de rendimiento
├── k6-report.html              → Gráficas de carga
├── karate-report/              → Reporte BDD con pasos
└── playwright-report/          → Screenshots + video
```

---

## Slide 9: Conclusiones

- **4 plataformas** distintas cubriendo aceptación, UI, API y carga
- **Entidad Libro** como eje central del testing CRUD
- **Sin dependencias externas** (no auth, no CAPTCHA, no emails)
- **Docker Compose** levanta todo el stack en un comando
- **Reportes HTML** generados automáticamente para evidencia
- **Parametrizable** vía variables de entorno (URLs, VUs, duración)

---

## Slide 10: Demostración en Vivo

1. `docker-compose up --build -d`
2. `bash tests/api-postman/run-newman.sh`
3. `bash tests/load-k6/run-k6.sh`
4. `bash tests/acceptance-karate/run-karate.sh`
5. `bash tests/ui-playwright/run-playwright.sh`
6. Abrir reportes en `./evidencias/`
