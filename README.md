# Books Microservices - Relatos de Papel

Sistema de microservicios para gestión de catálogo de libros y pedidos/compras.

## Arquitectura

```
┌─────────────────┐
│     Cliente     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   API Gateway   │ :8090
│ (Spring Cloud)  │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌───────┐  ┌───────┐
│Catalog│  │Payment│
│:8081  │◄─│ :8082 │  (HTTP via Eureka con Load Balancing)
│:8083  │  │       │
└───┬───┘  └───┬───┘
    │          │
    ▼          ▼
  [H2/DB]    [H2/DB]

    ▲          ▲
    └────┬─────┘
         │
┌────────┴────────┐
│  Eureka Server  │ :8761
│ (Service Disc.) │
└─────────────────┘
```

## Proyectos

| Proyecto | Puerto | Descripción |
|----------|--------|-------------|
| `eureka-server` | 8761 | Service Discovery |
| `api-gateway` | 8090 | API Gateway (punto de entrada) |
| `ms-books-catalogue` | 8081, 8083 | Catálogo de libros (2 instancias para balanceo) |
| `ms-books-payments` | 8082 | Gestión de pedidos/compras |

## Requisitos

- Java 17+
- Maven 3.8+
- (Opcional) Docker y Docker Compose

## Ejecución Local

### 1. Clonar y compilar

```bash
cd books-microservices

# Compilar todos los proyectos
mvn clean package -DskipTests -f eureka-server/pom.xml
mvn clean package -DskipTests -f api-gateway/pom.xml
mvn clean package -DskipTests -f ms-books-catalogue/pom.xml
mvn clean package -DskipTests -f ms-books-payments/pom.xml
```

### 2. Iniciar servicios (en orden)

**Terminal 1 - Eureka Server:**
```bash
cd eureka-server && mvn spring-boot:run
```

**Terminal 2 - Catalogue Instancia 1 (puerto 8081):**
```bash
cd ms-books-catalogue && mvn spring-boot:run
```

**Terminal 3 - Catalogue Instancia 2 (puerto 8083) - Para balanceo:**
```bash
cd ms-books-catalogue && mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
```

**Terminal 4 - Payments Service:**
```bash
cd ms-books-payments && mvn spring-boot:run
```

**Terminal 5 - API Gateway:**
```bash
cd api-gateway && mvn spring-boot:run
```

### 3. Verificar

- Eureka Dashboard: http://localhost:8761
- Gateway: http://localhost:8090
- Catalogue Swagger: http://localhost:8081/swagger-ui.html
- Payments Swagger: http://localhost:8082/swagger-ui.html

## Balanceo de Carga (Load Balancing)

El sistema usa **Eureka + Spring Cloud LoadBalancer** para balancear las peticiones entre las instancias de `ms-books-catalogue`.

### Verificar instancias registradas
```bash
curl -s http://localhost:8761/eureka/apps/MS-BOOKS-CATALOGUE | grep -oP 'port[^>]*>\K[0-9]+'
# Salida esperada: 8081 y 8083
```

### Demostrar balanceo (Round Robin)
```bash
# Desde payments (usa WebClient con @LoadBalanced)
for i in 1 2 3 4 5 6; do
  curl -s http://localhost:8082/pedidos/balanceo-test
  echo ""
done

# Salida esperada: alternancia entre port:8081 y port:8083
```

## 12 Comandos cURL via Gateway (Puerto 8090)

### 1. Route Viewer del Gateway
```bash
curl http://localhost:8090/actuator/gateway/routes
```

### 2. Búsqueda con ratingMin/ratingMax
```bash
curl "http://localhost:8090/catalogue/libros?ratingMin=4&ratingMax=5"
```

### 3. POST→GET (transcripción real)
```bash
curl -X POST http://localhost:8090/gw/catalogue/libros/get/1
# POST se transcribe a GET /libros/1
```

### 4. POST→PUT (transcripción)
```bash
curl -X POST http://localhost:8090/gw/catalogue/libros/update/3 \
  -H "Content-Type: application/json" \
  -d '{"titulo": "Don Quijote - Edición Especial", "precio": 48000}'
```

### 5. POST→PATCH (transcripción)
```bash
curl -X POST http://localhost:8090/gw/catalogue/libros/patch/4 \
  -H "Content-Type: application/json" \
  -d '{"precio": 39000}'
```

### 6. POST→DELETE (transcripción)
```bash
curl -X POST http://localhost:8090/gw/catalogue/libros/delete/11
# HTTP 204 si existe, 404 si no existe
```

### 7. Crear pedido - CASO OK (stock suficiente)
```bash
curl -X POST http://localhost:8090/payments/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuario": 1, "items": [{"idLibro": 2, "cantidad": 2}]}'
# Respuesta: estado="C" (Completado)
```

### 8. Crear pedido - CASO FAIL (stock insuficiente)
```bash
curl -X POST http://localhost:8090/payments/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuario": 1, "items": [{"idLibro": 10, "cantidad": 10}]}'
# Respuesta: estado="F" (Fallido), libro 10 solo tiene 2 unidades
```

### 9. Crear pedido - CASO FAIL (libro no visible)
```bash
curl -X POST http://localhost:8090/payments/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuario": 1, "items": [{"idLibro": 9, "cantidad": 1}]}'
# Respuesta: estado="F" (Fallido), libro 9 tiene visible=false
```

### 10. Test de balanceo (alternancia entre instancias)
```bash
for i in 1 2 3 4; do curl -s http://localhost:8090/payments/pedidos/balanceo-test; echo ""; done
# Salida: alternancia port:8081 / port:8083
```

### 11. Búsqueda combinada POST (body JSON)
```bash
curl -X POST http://localhost:8090/gw/catalogue/libros/search \
  -H "Content-Type: application/json" \
  -d '{"ratingMin": 4, "autor": "Gabriel"}'
```

### 12. Listar pedidos via Gateway
```bash
curl http://localhost:8090/payments/pedidos
```

## Transcripción POST (Gateway)

El gateway transcribe peticiones POST a otros métodos HTTP para clientes que solo soportan POST:

| Endpoint POST | Método Final | Destino |
|---------------|--------------|---------|
| `/gw/catalogue/libros/get/{id}` | **GET** | `/libros/{id}` |
| `/gw/catalogue/libros/search` | POST | `/libros/search` |
| `/gw/catalogue/libros/delete/{id}` | **DELETE** | `/libros/{id}` |
| `/gw/catalogue/libros/update/{id}` | **PUT** | `/libros/{id}` |
| `/gw/catalogue/libros/patch/{id}` | **PATCH** | `/libros/{id}` |
| `/gw/payments/create` | POST | `/pedidos` |

## Validación de Stock - Flujo

Cuando se crea un pedido en `ms-books-payments`:

1. Recibe solicitud con items (idLibro, cantidad)
2. Para cada item, llama a `ms-books-catalogue` via Eureka
3. Verifica: libro existe, visible=true, stock >= cantidad
4. Si OK → Guarda con estado "C" y decrementa stock
5. Si FAIL → Guarda con estado "F" y `failure_reason`

**Datos de prueba:**
- Libros 1-8: Stock suficiente (20-100 unidades)
- Libro 9: visible=false (no disponible)
- Libro 10: Solo 2 unidades (para probar stock insuficiente)

## Ejecución con Docker Compose

```bash
docker-compose up --build
```

## Tests

```bash
# Tests de catalogue (26 tests)
cd ms-books-catalogue && mvn test

# Tests de payments (9 tests)
cd ms-books-payments && mvn test
```

### Tests incluidos:

**ms-books-catalogue:**
- `LibroServiceTest` - Tests unitarios del servicio
- `LibroControllerTest` - Tests de controller (MockMvc)
- `LibroRepositoryTest` - Tests de repositorio (@DataJpaTest)
- `InventarioRepositoryTest` - Tests de repositorio (@DataJpaTest)

**ms-books-payments:**
- `PedidoServiceTest` - Tests unitarios del servicio
- `PedidoRepositoryTest` - Tests de repositorio (@DataJpaTest)

## Estructura del Proyecto

```
books-microservices/
├── eureka-server/
├── api-gateway/
│   └── filter/TranscribeMethodGatewayFilterFactory.java
├── ms-books-catalogue/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── specification/LibroSpecification.java
├── ms-books-payments/
│   ├── client/CatalogueClient.java  (WebClient + @LoadBalanced)
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   └── service/
├── docs/
│   ├── db-schema.sql
│   └── db-design.md
├── docker-compose.yml
└── README.md
```

## Características

- [x] Eureka Server para Service Discovery
- [x] Load Balancing entre instancias (Round Robin)
- [x] API Gateway con Spring Cloud Gateway
- [x] Transcripción POST -> GET/PUT/PATCH/DELETE
- [x] CRUD completo de libros
- [x] Búsqueda combinada con Specifications (incluye rating)
- [x] Campo rating (1-5) con validación
- [x] Control de visibilidad de libros
- [x] Gestión de inventario/stock
- [x] Creación de pedidos con validación contra catálogo
- [x] Validación de stock con casos OK y FAIL
- [x] Comunicación entre microservicios via Eureka
- [x] Documentación OpenAPI/Swagger
- [x] Manejo de errores con Problem Details (RFC 7807)
- [x] Tests unitarios, de controller y de repositorio
- [x] Perfiles para H2/MySQL/PostgreSQL
