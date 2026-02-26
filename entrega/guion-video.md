# Guion de Video - Relatos de Papel: Pruebas Automatizadas
**Duración estimada:** 10 minutos

---

## MINUTO 0:00 - 1:00 | Introducción

**Narración:**
> "Bienvenidos. En este video demostraremos la implementación completa de pruebas automatizadas para el sistema Relatos de Papel, un backend de microservicios para gestión de un catálogo de libros."

**Pantalla:** Mostrar el README.md del proyecto.

> "El sistema consta de 4 microservicios: Eureka Server para Service Discovery, API Gateway, un servicio de Catálogo de libros y uno de Pagos/Pedidos. Está construido con Java 17, Spring Boot 3.2 y Spring Cloud."

**Pantalla:** Mostrar diagrama de arquitectura de la presentación.

> "Implementamos 4 tipos de pruebas con 4 plataformas distintas: Postman con Newman para API, k6 para carga, Karate para aceptación BDD, y Playwright para UI funcional sobre Swagger."

---

## MINUTO 1:00 - 2:30 | Levantar el Sistema

**Narración:**
> "Primero levantamos todo el stack con Docker Compose."

**Comando en terminal:**
```bash
cd /home/dark/projects/books-microservices
docker-compose up --build -d
```

**Pantalla:** Mostrar los contenedores creándose.

> "Docker Compose orquesta el orden: primero Eureka, luego Gateway y Catalogue, y finalmente Payments que depende de Catalogue."

**Esperar y verificar:**
```bash
curl -s http://localhost:8081/actuator/health | jq .
curl -s http://localhost:8081/libros | jq '.content | length'
```

> "El servicio está UP y tenemos 8 libros precargados como seed data."

**Pantalla:** Abrir http://localhost:8761 (Eureka Dashboard) y mostrar los servicios registrados.

---

## MINUTO 2:30 - 3:00 | Estructura del Proyecto de Tests

**Narración:**
> "Veamos la estructura de las pruebas."

**Comando:**
```bash
tree tests/ evidencias/ --dirsfirst -L 2
```

> "Tenemos 4 carpetas: acceptance-karate, ui-playwright, api-postman y load-k6. Todas generan reportes en la carpeta evidencias."

---

## MINUTO 3:00 - 5:00 | Postman + Newman (API REST)

**Narración:**
> "Empezamos con las pruebas de API usando Newman, el runner de línea de comandos de Postman."

**Pantalla:** Mostrar brevemente la colección JSON (estructura de 9 requests).

> "La colección implementa un flujo CRUD completo: health check, listar, crear, obtener por ID, actualizar con PUT, verificar, parchear con PATCH, eliminar y confirmar la eliminación con un 404."

**Comando:**
```bash
bash tests/api-postman/run-newman.sh
```

**Pantalla:** Mostrar la ejecución de Newman en terminal con los 9 tests pasando.

> "Todos los tests pasan. El flujo crea un libro con código único basado en timestamp, lo modifica y lo elimina, dejando la base de datos limpia."

**Pantalla:** Abrir `evidencias/newman-report.html` en el navegador.

> "El reporte HTML muestra cada request con su response, tiempos de ejecución y assertions."

---

## MINUTO 5:00 - 6:30 | k6 (Prueba de Carga)

**Narración:**
> "Ahora la prueba de carga con k6."

**Pantalla:** Mostrar brevemente el script `books_search_loadtest.js`.

> "El script simula 10 usuarios virtuales durante 30 segundos, ejecutando tres escenarios por iteración: listar libros, obtener detalle y búsqueda filtrada. Los thresholds exigen menos del 1% de errores y p95 menor a 2 segundos."

**Comando:**
```bash
bash tests/load-k6/run-k6.sh
```

**Pantalla:** Mostrar la ejecución de k6 con las barras de progreso y el summary final.

> "Vemos que los thresholds se cumplen. El p95 está muy por debajo de los 2 segundos y la tasa de error es 0%."

**Pantalla:** Abrir `evidencias/k6-report.html`.

> "El reporte HTML muestra gráficas de latencia, throughput y distribución de tiempos de respuesta."

---

## MINUTO 6:30 - 8:00 | Karate (Aceptación BDD)

**Narración:**
> "Para las pruebas de aceptación usamos Karate, un framework BDD para APIs."

**Pantalla:** Mostrar el archivo `libro-crud.feature`.

> "El feature file describe en lenguaje natural un viaje completo: verificar salud del servicio, listar libros existentes, crear uno nuevo, consultarlo, actualizarlo con PUT, verificar el cambio, parchear con PATCH, eliminar y confirmar que ya no existe."

**Comando:**
```bash
bash tests/acceptance-karate/run-karate.sh
```

**Pantalla:** Mostrar la ejecución de Maven con los tests corriendo.

> "Karate ejecuta cada paso del scenario y valida las respuestas automáticamente."

**Pantalla:** Abrir el reporte HTML de Karate en `evidencias/karate-report/`.

> "El reporte muestra cada paso Given/When/Then con el detalle de request y response."

---

## MINUTO 8:00 - 9:30 | Playwright (UI Funcional)

**Narración:**
> "Finalmente, pruebas de UI funcional con Playwright sobre Swagger UI."

**Pantalla:** Mostrar el archivo `swagger-ui.spec.js`.

> "Dado que el proyecto es backend puro, usamos Swagger UI como interfaz web. El primer test verifica que Swagger UI carga correctamente. El segundo ejecuta un GET /libros usando Try it out y Execute, y valida que la respuesta sea 200."

**Comando:**
```bash
bash tests/ui-playwright/run-playwright.sh
```

**Pantalla:** Mostrar la ejecución de Playwright.

> "Playwright abre un navegador headless, interactúa con Swagger UI y toma screenshots como evidencia."

**Pantalla:** Abrir `evidencias/playwright-report/index.html` y los screenshots.

> "El reporte incluye los screenshots capturados durante la ejecución."

---

## MINUTO 9:30 - 10:00 | Cierre

**Narración:**
> "En resumen, implementamos 4 tipos de pruebas con 4 plataformas distintas:"

**Pantalla:** Mostrar la estructura de evidencias completa.

```
evidencias/
├── newman-report.html       → 9 tests CRUD API
├── k6-summary.json          → Métricas de rendimiento
├── k6-report.html           → Gráficas de carga
├── karate-report/           → Reporte BDD paso a paso
└── playwright-report/       → Screenshots + reporte UI
```

> "Todo corre localmente con Docker Compose, es parametrizable vía variables de entorno y genera reportes HTML automáticos. Gracias por su atención."

**Pantalla:** Mostrar terminal con todos los reportes generados.
