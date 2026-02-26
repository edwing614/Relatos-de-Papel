# Diagnóstico Técnico - Relatos de Papel (books-microservices)

## Resumen Ejecutivo

- **Arquitectura de 4 microservicios** Java 17 + Spring Boot 3.2.0 + Spring Cloud 2023.0.0 (Eureka, Gateway, LoadBalancer)
- **Backend puro** (sin frontend web): toda interacción es vía API REST, documentada con Swagger/OpenAPI
- **Sin autenticación**: todos los endpoints son públicos, no hay Spring Security ni JWT
- **Base de datos H2 en memoria** (perfil local) con seed data de 10 libros, 8 autores y 8 categorías precargados automáticamente
- **Docker Compose disponible** para levantar los 4 servicios con un solo comando; sin dependencias externas (no CAPTCHA, no emails, no pagos reales)

---

## 1. Stack y Estructura

### Stack Tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.2.0 |
| Cloud | Spring Cloud | 2023.0.0 |
| Service Discovery | Netflix Eureka | - |
| API Gateway | Spring Cloud Gateway | - |
| Load Balancing | Spring Cloud LoadBalancer | Round-Robin |
| ORM | JPA / Hibernate | - |
| Documentación API | SpringDoc OpenAPI | 2.3.0 |
| Mapeo DTOs | MapStruct | 1.5.5 |
| Boilerplate | Lombok | - |
| Build | Maven | 3.8+ |
| Contenedores | Docker + Docker Compose | - |
| BD desarrollo | H2 (en memoria) | - |
| BD producción | MySQL 8.0 / PostgreSQL | - |

### Estructura del Repositorio

```
books-microservices/
├── eureka-server/              # Service Discovery (puerto 8761)
├── api-gateway/                # Gateway + enrutamiento (puerto 8090)
├── ms-books-catalogue/         # Catálogo de libros (puerto 8081/8083)
│   ├── controller/             # LibroController, AdminLibroController, AutorController, CategoriaController
│   ├── service/impl/           # LibroServiceImpl
│   ├── repository/             # LibroRepository, AutorRepository, CategoriaRepository, InventarioRepository
│   ├── entity/                 # Libro, Autor, Categoria, Inventario
│   ├── dto/                    # LibroDTO, CreateLibroRequest, UpdateLibroRequest, LibroSearchCriteria...
│   ├── specification/          # LibroSpecification (búsqueda dinámica JPA Criteria)
│   ├── exception/              # GlobalExceptionHandler (RFC 7807)
│   └── resources/data.sql      # Seed data
├── ms-books-payments/          # Pedidos/compras (puerto 8082)
│   ├── controller/             # PedidoController
│   ├── service/impl/           # PedidoServiceImpl
│   ├── client/                 # CatalogueClient (WebClient + Eureka)
│   ├── entity/                 # Pedido, DetallePedido
│   ├── dto/                    # PedidoDTO, CreatePedidoRequest, ItemPedidoRequest...
│   └── exception/              # GlobalExceptionHandler (RFC 7807)
├── docs/
│   ├── db-design.md
│   └── db-schema.sql
├── docker-compose.yml
├── README.md
├── EXPLICACION_APIS.md
└── DEMO_VIDEO.md
```

---

## 2. Cómo Levantar el Sistema

### Opción A: Docker Compose (recomendada)

```bash
cd /home/dark/projects/books-microservices
docker-compose up --build
```

**Orden automático (gestionado por `depends_on`):**
1. `eureka-server` (espera health check en `/actuator/health`)
2. `api-gateway` + `ms-books-catalogue` (esperan a Eureka)
3. `ms-books-payments` (espera a Eureka + Catalogue)

### Opción B: Manual (Maven)

```bash
# Terminal 1 - Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 2 - Catalogue (esperar a que Eureka esté UP)
cd ms-books-catalogue && mvn spring-boot:run

# Terminal 3 - Payments (esperar a que Catalogue esté UP)
cd ms-books-payments && mvn spring-boot:run

# Terminal 4 - Gateway
cd api-gateway && mvn spring-boot:run
```

### Puertos

| Servicio | Puerto | URL |
|---|---|---|
| Eureka Dashboard | 8761 | http://localhost:8761 |
| API Gateway | 8090 | http://localhost:8090 |
| Catalogue | 8081 | http://localhost:8081 |
| Catalogue (instancia 2) | 8083 | http://localhost:8083 |
| Payments | 8082 | http://localhost:8082 |
| Swagger Catalogue | 8081 | http://localhost:8081/swagger-ui.html |
| Swagger Payments | 8082 | http://localhost:8082/swagger-ui.html |
| H2 Console Catalogue | 8081 | http://localhost:8081/h2-console |
| H2 Console Payments | 8082 | http://localhost:8082/h2-console |

### Base de Datos

- **Perfil `local` (por defecto):** H2 en memoria, se crea y destruye automáticamente. No requiere instalación.
- **Perfil `mysql`:** Requiere MySQL corriendo en localhost:3306.
- **Perfil `postgres`:** Requiere PostgreSQL corriendo en localhost:5432.
- **Migraciones:** No hay Flyway/Liquibase. Hibernate genera tablas con `ddl-auto: create-drop` (local).
- **Seed data:** Se carga automáticamente desde `data.sql` en cada reinicio (perfil local).

---

## 3. Variables de Entorno Requeridas

### Docker Compose (configuradas internamente)

| Variable | Servicio | Descripción |
|---|---|---|
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | gateway, catalogue, payments | URL del servidor Eureka |
| `SPRING_PROFILES_ACTIVE` | catalogue, payments | Perfil activo (local/mysql/postgres) |

### Propiedades de Aplicación (por servicio)

| Variable | Valor por defecto |
|---|---|
| `server.port` | 8761 / 8090 / 8081 / 8082 |
| `spring.application.name` | eureka-server / api-gateway / ms-books-catalogue / ms-books-payments |
| `spring.profiles.active` | local |
| `spring.datasource.url` | jdbc:h2:mem:{dbname} (local) |
| `spring.datasource.username` | sa (local) |
| `spring.datasource.password` | (vacío en local) |
| `catalogue.service.name` | ms-books-catalogue (solo payments) |

> **Nota:** No hay archivos `.env`. No se detectaron secretos sensibles; las credenciales de BD están hardcodeadas en `application.yml` para perfiles dev (root/root para MySQL, postgres/postgres para PostgreSQL).

---

## 4. API REST

### Autenticación

**No hay autenticación.** No se usa Spring Security, JWT, API keys ni OAuth2. Todos los endpoints son públicos.

### Documentación OpenAPI/Swagger

- Catalogue: http://localhost:8081/swagger-ui.html (`/api-docs` para JSON)
- Payments: http://localhost:8082/swagger-ui.html (`/api-docs` para JSON)
- Todos los controladores usan anotaciones `@Tag`, `@Operation`, `@ApiResponses`

### Endpoints Principales

#### Catálogo - Libros (`/libros`) → vía Gateway: `/catalogue/libros`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/libros` | Crear libro |
| `GET` | `/libros` | Buscar libros (query params + paginación) |
| `GET` | `/libros/{id}` | Obtener libro por ID (solo visibles) |
| `GET` | `/libros/isbn/{isbn}` | Obtener libro por ISBN |
| `POST` | `/libros/search` | Búsqueda avanzada (body JSON) |
| `PUT` | `/libros/{id}` | Actualizar libro completo |
| `PATCH` | `/libros/{id}` | Actualizar libro parcial |
| `DELETE` | `/libros/{id}` | Eliminar libro |

#### Catálogo - Admin (`/admin/libros`) → vía Gateway: `/catalogue/admin/libros`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/admin/libros` | Buscar libros (incluye no visibles) |
| `GET` | `/admin/libros/{id}` | Obtener cualquier libro por ID |

#### Catálogo - Autores y Categorías

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/autores` | Listar autores |
| `POST` | `/autores` | Crear autor |
| `GET` | `/categorias` | Listar categorías |
| `POST` | `/categorias` | Crear categoría |

#### Catálogo - Internos (usados por Payments)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/libros/internal/{id}/disponibilidad?cantidad=N` | Verificar disponibilidad |
| `POST` | `/libros/internal/{id}/decrementar-stock?cantidad=N` | Decrementar stock |
| `GET` | `/libros/info` | Info de instancia (balanceo) |

#### Pagos - Pedidos (`/pedidos`) → vía Gateway: `/payments/pedidos`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/pedidos` | Crear pedido (valida stock vía Catalogue) |
| `GET` | `/pedidos` | Listar pedidos (paginado) |
| `GET` | `/pedidos/{id}` | Obtener pedido por ID |
| `GET` | `/pedidos/usuario/{usuario}` | Pedidos por usuario |
| `GET` | `/pedidos/balanceo-test` | Test de load balancing |

#### Gateway - Transcripción POST

| Ruta POST Gateway | Se convierte a |
|---|---|
| `POST /gw/catalogue/libros/get/{id}` | `GET /libros/{id}` |
| `POST /gw/catalogue/libros/update/{id}` | `PUT /libros/{id}` |
| `POST /gw/catalogue/libros/patch/{id}` | `PATCH /libros/{id}` |
| `POST /gw/catalogue/libros/delete/{id}` | `DELETE /libros/{id}` |
| `POST /gw/catalogue/libros/search` | `POST /libros/search` |
| `POST /gw/payments/create` | `POST /pedidos` |

### Entidad CRUD Recomendada para Pruebas: **Libro**

Flujo completo disponible:
1. `POST /catalogue/libros` → Crear
2. `GET /catalogue/libros/{id}` → Leer
3. `PUT /catalogue/libros/{id}` → Actualizar
4. `DELETE /catalogue/libros/{id}` → Eliminar
5. `GET /catalogue/libros` → Verificar eliminación

Validaciones activas: código requerido, título requerido, ISBN único, rating 1-5, precio >= 0.

### Manejo de Errores

Usa **Problem Details (RFC 7807)**:
```json
{
  "type": "https://api.books.com/errors/not-found",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "Libro no encontrado con id: 99",
  "instance": "/libros/99",
  "timestamp": "2026-02-20T..."
}
```

Excepciones custom: `ResourceNotFoundException` (404), `DuplicateResourceException` (409), `InsufficientStockException` (400), `PaymentValidationException` (400).

---

## 5. UI Web (Rutas y Flujos)

### No hay frontend web

El proyecto es **backend puro**. No existe aplicación web con UI. Las interfaces disponibles son:

- **Swagger UI** (http://localhost:8081/swagger-ui.html y http://localhost:8082/swagger-ui.html)
- **Eureka Dashboard** (http://localhost:8761)
- **H2 Console** (http://localhost:8081/h2-console)
- **Gateway Routes** (http://localhost:8090/actuator/gateway/routes)

### Flujo E2E recomendado (vía API)

1. `GET /catalogue/libros` → Listar catálogo
2. `GET /catalogue/libros/{id}` → Ver detalle de un libro
3. `POST /payments/pedidos` → Crear pedido con items
4. `GET /payments/pedidos/{id}` → Verificar pedido creado
5. `GET /catalogue/libros/{id}` → Verificar que el stock decrementó

### Formularios recomendados para pruebas (vía Swagger UI)

1. **Crear Libro** (`POST /libros`) — validaciones: código requerido, título requerido, ISBN único, rating 1-5
2. **Crear Pedido** (`POST /pedidos`) — validaciones: usuario requerido, items requeridos, stock suficiente, libro visible
3. **Búsqueda Avanzada** (`POST /libros/search`) — filtros combinados: título, autor, categoría, rango de precio, rango de rating, fechas

---

## 6. Datos de Prueba

### Seed Data Existente

**Archivo:** `ms-books-catalogue/src/main/resources/data.sql`

Se carga automáticamente al iniciar con perfil `local`. Incluye:

| Tipo | Cantidad | Detalles |
|---|---|---|
| Categorías | 8 | Ficción, No Ficción, Ciencia Ficción, Fantasía, Romance, Misterio, Historia, Tecnología |
| Autores | 8 | García Márquez, Borges, Allende, Vargas Llosa, Cortázar, Zafón, Coelho, King |
| Libros | 10 | 8 visibles + 1 oculto (id=9) + 1 con stock bajo de 2 (id=10) |
| Inventario | 10 | Stock entre 2 y 100 unidades |
| Relaciones libro-autor | 10 | Asociaciones ManyToMany |
| Relaciones libro-categoría | 12 | Asociaciones ManyToMany |

### Libros útiles para pruebas de fallo

- **ID 9** ("Libro Oculto de Prueba"): `visible=false` → pedidos contra este libro fallan
- **ID 10** ("Libro Stock Bajo"): `stock=2` → pedidos de 3+ unidades fallan por stock insuficiente

### Cómo crear datos rápidamente

- **Reiniciar servicio:** Al usar H2 (perfil local), reiniciar `ms-books-catalogue` recarga toda la seed data
- **Vía API:** `POST /catalogue/libros`, `POST /catalogue/autores`, `POST /catalogue/categorias`
- **No hay scripts adicionales** de fixtures o factories

---

## 7. Bloqueadores y Recomendaciones

### Bloqueadores Potenciales

| Riesgo | Nivel | Detalle |
|---|---|---|
| Sin frontend | Alto | No hay UI web para pruebas E2E con Cypress/Playwright. Solo se puede probar Swagger UI o construir un frontend de prueba |
| H2 volátil | Medio | Con perfil `local`, la BD se destruye al reiniciar. Los datos de prueba no persisten entre ejecuciones |
| Orden de arranque | Medio | Los servicios deben levantarse en orden: Eureka → Catalogue → Payments → Gateway. Docker Compose lo maneja, pero manual requiere esperar |
| Sin autenticación | Bajo | Simplifica las pruebas (no hay tokens que gestionar), pero no hay flujos de auth que probar |
| Puertos fijos | Bajo | Los puertos están hardcodeados (8761, 8090, 8081, 8082). Conflictos si hay otros servicios corriendo |

### Recomendaciones para Modo Test

1. **Usar Docker Compose** para levantar todo el stack antes de correr pruebas
2. **Pruebas API con Postman/Newman y k6** son las más valiosas dado que es backend puro
3. **Para Cypress/Playwright:** se podrían probar Swagger UI o Eureka Dashboard, pero el valor sería bajo. Considerar si realmente se necesita E2E de UI
4. **Crear un perfil `test`** en `application.yml` con configuración específica para pruebas automatizadas
5. **Reiniciar servicios entre suites** para garantizar estado limpio de datos (o usar endpoint de reset)
6. **k6 es ideal** para probar el load balancing entre las 2 instancias de Catalogue
7. **Postman/Newman** es ideal para validar el flujo completo: crear libro → crear pedido → verificar stock

---

*Diagnóstico técnico - Febrero 2026*
