# Explicación de las APIs - Books Microservices

## Arquitectura General

El sistema tiene 4 componentes:

```
Cliente (curl/navegador)
       │
       ▼
  API Gateway (:8090)  ← Punto único de entrada
       │
  ┌────┴────┐
  ▼         ▼
Catalogue  Payments   ← Se comunican entre sí via Eureka
:8081/:8083  :8082
  │         │
  ▼         ▼
 [H2]      [H2]      ← Bases de datos separadas
       │
  Eureka (:8761)      ← Registro de servicios
```

- **El cliente SIEMPRE habla con el Gateway (puerto 8090)**, nunca directamente con los microservicios
- **Payments habla con Catalogue** por nombre de servicio (no por IP) gracias a Eureka
- Cada microservicio tiene su propia base de datos H2 en memoria

---

## EUREKA SERVER (Puerto 8761)

**¿Qué hace?**
Es el registro de servicios. Cuando un microservicio se levanta, se registra aquí con su nombre y puerto. Así los demás servicios lo pueden encontrar sin saber su IP exacta.

**¿Por qué es importante?**
Sin Eureka, Payments tendría que saber la IP y puerto exacto de Catalogue. Con Eureka, solo necesita saber el nombre: `ms-books-catalogue`.

**Dashboard:** `http://localhost:8761`
Aquí se ven todos los servicios registrados y sus instancias.

---

## API GATEWAY (Puerto 8090)

**¿Qué hace?**
Es la puerta de entrada. Todas las peticiones del cliente llegan aquí y el Gateway las redirige al microservicio correcto.

**Rutas principales:**
- `/catalogue/**` → redirige a ms-books-catalogue
- `/payments/**` → redirige a ms-books-payments

**Transcripción POST:**
Algunos clientes (formularios HTML, ciertos frontends) solo pueden enviar POST. El Gateway puede convertir esas peticiones:
- `POST /gw/catalogue/libros/get/1` → se convierte en `GET /libros/1`
- `POST /gw/catalogue/libros/update/3` → se convierte en `PUT /libros/3`
- `POST /gw/catalogue/libros/patch/4` → se convierte en `PATCH /libros/4`
- `POST /gw/catalogue/libros/delete/5` → se convierte en `DELETE /libros/5`

Esto se logra con un filtro personalizado: `TranscribeMethodGatewayFilterFactory`

**Route Viewer:**
`GET /actuator/gateway/routes` → muestra todas las rutas configuradas

---

## MS-BOOKS-CATALOGUE (Puerto 8081 / 8083)

**¿Qué hace?**
Gestiona el catálogo de libros: CRUD completo, búsqueda combinada, inventario/stock.

### Entidades principales

**Libro:**
- idLibro, codigo, codigoIsbn (único), titulo, descripcion, precio, visible, fechaPublicacion, rating (1-5)
- Relación ManyToMany con Autor y Categoria

**Inventario:**
- idInventario, idLibro, cantidadDisponible, fechaActualizacion
- Tabla separada del libro para manejar el stock

**Autor:** idAutor, nombre, pais
**Categoria:** idCategoria, nombre, descripcion

### Endpoints públicos (via Gateway: `/catalogue/...`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/libros` | Listar libros con filtros (solo visibles) |
| GET | `/libros/{id}` | Obtener libro por ID (solo si visible) |
| GET | `/libros/isbn/{isbn}` | Obtener libro por ISBN |
| POST | `/libros` | Crear un libro nuevo |
| PUT | `/libros/{id}` | Actualizar libro completo |
| PATCH | `/libros/{id}` | Actualizar libro parcialmente |
| DELETE | `/libros/{id}` | Eliminar libro |
| POST | `/libros/search` | Búsqueda con body JSON |
| GET | `/categorias` | Listar categorías |
| GET | `/autores` | Listar autores |

### Búsqueda combinada (Specifications)

El endpoint `GET /libros` acepta múltiples filtros combinables:

| Parámetro | Tipo | Ejemplo | Descripción |
|-----------|------|---------|-------------|
| titulo | String | `?titulo=Cien` | Busca parcial en título |
| autor | String | `?autor=Gabriel` | Busca parcial en nombre de autor |
| categoria | String | `?categoria=Ficción` | Busca parcial en categoría |
| codigoIsbn | String | `?codigoIsbn=978-...` | Busca ISBN exacto |
| precioMin | Number | `?precioMin=20000` | Precio mínimo |
| precioMax | Number | `?precioMax=50000` | Precio máximo |
| ratingMin | Integer | `?ratingMin=4` | Rating mínimo (1-5) |
| ratingMax | Integer | `?ratingMax=5` | Rating máximo (1-5) |

**Se pueden combinar:** `?ratingMin=4&autor=Gabriel&precioMax=50000`

Esto se implementa con **JPA Specifications** que construyen la query dinámicamente según los filtros que se envíen.

### Endpoints internos (usados por Payments, no expuestos al cliente)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/libros/internal/{id}/disponibilidad?cantidad=X` | Verifica si el libro está disponible y tiene stock |
| POST | `/libros/internal/{id}/decrementar-stock?cantidad=X` | Resta stock después de una compra exitosa |

### Campo "visible"

- `visible=true` → el libro aparece en búsquedas públicas y se puede comprar
- `visible=false` → el libro NO aparece en búsquedas y NO se puede comprar
- Los endpoints públicos filtran automáticamente (solo muestran visible=true)

### Campo "rating"

- Valor entre 1 y 5
- Validado con `@Min(1)` y `@Max(5)` en el DTO
- Se puede filtrar en búsqueda con ratingMin y ratingMax

---

## MS-BOOKS-PAYMENTS (Puerto 8082)

**¿Qué hace?**
Gestiona pedidos/compras de libros. Valida contra Catalogue que los libros existan, estén visibles y tengan stock.

### Entidades

**Pedido:**
- idPedido, usuario, fechaPedido, estado, total, emailContacto, nombreContacto, failureReason
- Estados: "C" (Completado), "F" (Fallido), "P" (Pendiente), "X" (Cancelado)

**DetallePedido:**
- idDetalle, idLibro, libroTitulo, libroIsbn, cantidad, precioUnitario, subtotal

### Endpoints (via Gateway: `/payments/...`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/pedidos` | Crear un pedido nuevo |
| GET | `/pedidos` | Listar todos los pedidos (paginado) |
| GET | `/pedidos/{id}` | Obtener pedido por ID |
| GET | `/pedidos/usuario/{id}` | Listar pedidos de un usuario |
| GET | `/pedidos/balanceo-test` | Test de balanceo (muestra qué instancia de Catalogue respondió) |

### Flujo de compra (paso a paso)

```
1. Cliente envía POST /pedidos con:
   { "usuario": 1, "items": [{"idLibro": 2, "cantidad": 2}] }

2. Payments recibe la petición

3. Para CADA item del pedido:
   a. Llama a Catalogue via Eureka: GET http://ms-books-catalogue/libros/internal/2/disponibilidad?cantidad=2
   b. Catalogue responde: { "disponible": true/false, "libro": {...} }
   c. Payments verifica:
      - ¿El libro existe? → Si no, FAIL
      - ¿visible = true? → Si no, FAIL
      - ¿stock >= cantidad? → Si no, FAIL

4. Si TODO OK:
   - Guarda pedido con estado = "C" (Completado)
   - Calcula total (precio × cantidad)
   - Llama a Catalogue para decrementar stock
   - Devuelve HTTP 201

5. Si ALGO FALLA:
   - Guarda pedido con estado = "F" (Fallido)
   - Guarda el motivo en failureReason
   - Devuelve HTTP 400 con detalle del error
```

### Casos de prueba

| Caso | Libro | Qué pasa | Resultado |
|------|-------|----------|-----------|
| OK | Libro 2 (stock=30) | Stock suficiente | estado="C" |
| FAIL | Libro 10 (stock=2) | Pedir 10 unidades, solo hay 2 | estado="F", "stock insuficiente" |
| FAIL | Libro 9 (visible=false) | Libro oculto | estado="F", "no disponible" |

### Comunicación con Catalogue

**¿Cómo se conectan?**
- Payments usa `WebClient` con `@LoadBalanced`
- La URL es `http://ms-books-catalogue/...` (nombre de servicio, NO IP)
- Eureka resuelve el nombre a la IP:puerto real
- Si hay 2 instancias, las alterna automáticamente (Round-Robin)

Esto está implementado en `CatalogueClient.java`

---

## BALANCEO DE CARGA

**¿Qué es?**
Si levantamos 2 instancias de Catalogue (puerto 8081 y 8083), Eureka + LoadBalancer distribuyen las peticiones entre ambas automáticamente.

**¿Cómo se demuestra?**
El endpoint `/pedidos/balanceo-test` llama a Catalogue y devuelve qué instancia respondió:
```
Llamada 1: {"port": 8081}
Llamada 2: {"port": 8083}
Llamada 3: {"port": 8081}
Llamada 4: {"port": 8083}
```

Esto es **Round-Robin**: una petición para cada instancia, alternando.

---

## MANEJO DE ERRORES

Se usa **Problem Details (RFC 7807)** para errores consistentes:

```json
{
  "type": "https://api.books.com/errors/not-found",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "Libro no encontrado con id: 99",
  "instance": "/libros/99",
  "timestamp": "2026-02-06T..."
}
```

| Error | HTTP | Cuándo |
|-------|------|--------|
| not-found | 404 | Libro o pedido no existe |
| duplicate | 409 | ISBN o código duplicado |
| insufficient-stock | 400 | Stock insuficiente |
| validation | 400 | Datos inválidos |
| payment-validation | 400 | Error en validación de compra |

---

## DATOS DE PRUEBA PRECARGADOS

El sistema arranca con datos de ejemplo en memoria (H2):

**10 Libros** con autores y categorías:
1. Cien Años de Soledad (rating 5, stock 50)
2. Ficciones (rating 5, stock 30)
3. Don Quijote (rating 5, stock 40)
4. La Casa de los Espíritus (rating 4, stock 25)
5. La Ciudad y los Perros (rating 4, stock 20)
6. Rayuela (rating 4, stock 35)
7. El Alquimista (rating 3, stock 45)
8. It (rating 3, stock 30)
9. **Libro invisible** (visible=false, stock 15) → Para probar FAIL
10. **Libro con poco stock** (rating 2, stock 2) → Para probar FAIL

**8 Autores:** García Márquez, Borges, Cervantes, Allende, Vargas Llosa, Cortázar, Coelho, King

**5 Categorías:** Ficción, Ciencia Ficción, Historia, Fantasía, Terror
