# GUIÓN PARA VIDEOMEMORIA

## REQUISITOS PREVIOS (instalar antes)

- **Java 17+** - Verificar: `java -version`
- **Maven 3.8+** - Verificar: `mvn -version`
- **Git Bash o Terminal** (para ejecutar comandos)
- **Navegador** (Chrome, Firefox, etc.)
- **Docker**: NO es necesario

---

## INSTALACIÓN

1. **Descomprimir** el ZIP en cualquier carpeta, ejemplo:
   - Windows: `C:\proyectos\books-microservices`
   - Linux/Mac: `~/proyectos/books-microservices`

2. La estructura debe quedar así:
```
books-microservices/
├── eureka-server/
├── api-gateway/
├── ms-books-catalogue/
├── ms-books-payments/
├── docs/
├── README.md
└── docker-compose.yml
```

---

## ANTES DE GRABAR - Preparar pantalla

1. **Cerrar** todo lo que no sea necesario
2. **Abrir**:
   - 1 navegador con `http://localhost:8761` (dará error, es normal)
   - 6 terminales/consolas (Git Bash, CMD, PowerShell, o terminal de IDE)
   - (Opcional) VS Code con el proyecto abierto

---

## PASO 1: Mostrar Eureka vacío

**En navegador:** Ir a `http://localhost:8761`
> Mostrará error "conexión rechazada" - es normal, aún no levantamos Eureka

---

## PASO 2: Levantar servicios (en 5 terminales, EN ESTE ORDEN)

**IMPORTANTE:** En cada terminal, primero navegar a la carpeta donde descomprimiste el ZIP.

### Terminal 1 - Eureka Server (PRIMERO)
```bash
cd eureka-server
mvn spring-boot:run
```
> Esperar mensaje: **"Started EurekaServerApplication"**
> Esto toma ~10-15 segundos

**En navegador:** Refrescar `http://localhost:8761`
> Ahora debe mostrar el dashboard de Eureka (sin servicios aún)

---

### Terminal 2 - Catalogue instancia 1 (puerto 8081)
```bash
cd ms-books-catalogue
mvn spring-boot:run
```
> Esperar mensaje: **"Started CatalogueApplication"** y **"Tomcat started on port 8081"**

---

### Terminal 3 - Catalogue instancia 2 (puerto 8083) - PARA DEMOSTRAR BALANCEO
```bash
cd ms-books-catalogue
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
```
> Esperar mensaje: **"Tomcat started on port 8083"**

---

### Terminal 4 - Payments (puerto 8082)
```bash
cd ms-books-payments
mvn spring-boot:run
```
> Esperar mensaje: **"Started PaymentsApplication"**

---

### Terminal 5 - Gateway (puerto 8090) - PUNTO DE ENTRADA
```bash
cd api-gateway
mvn spring-boot:run
```
> Esperar mensaje: **"Started ApiGatewayApplication"**

---

## PASO 3: Verificar en navegador

### Eureka Dashboard
**URL:** `http://localhost:8761`

> Mostrar que aparecen registrados:
> - **API-GATEWAY** (1 instancia)
> - **MS-BOOKS-CATALOGUE** (2 instancias - puertos 8081 y 8083)
> - **MS-BOOKS-PAYMENTS** (1 instancia)

### Swagger (opcional)
- Catalogue: `http://localhost:8081/swagger-ui.html`
- Payments: `http://localhost:8082/swagger-ui.html`

---

## PASO 4: Ejecutar comandos de demostración (Terminal 6)

### 1. Ver rutas del Gateway
```bash
curl http://localhost:8090/actuator/gateway/routes
```
> Muestra todas las rutas configuradas en el gateway

---

### 2. BALANCEO - Ejecutar 4 veces para ver alternancia
```bash
curl -s http://localhost:8090/payments/pedidos/balanceo-test
```
```bash
curl -s http://localhost:8090/payments/pedidos/balanceo-test
```
```bash
curl -s http://localhost:8090/payments/pedidos/balanceo-test
```
```bash
curl -s http://localhost:8090/payments/pedidos/balanceo-test
```
> **Resultado esperado:** Alterna entre `"port":8081` y `"port":8083` (Round-Robin)

---

### 3. Búsqueda con filtro de rating
```bash
curl -s "http://localhost:8090/catalogue/libros?ratingMin=4&ratingMax=5"
```
> Muestra solo libros con rating entre 4 y 5

---

### 4. Transcripción POST → GET
```bash
curl -s -X POST http://localhost:8090/gw/catalogue/libros/get/1
```
> El gateway convierte POST a GET internamente

---

### 5. Transcripción POST → PUT (actualizar)
```bash
curl -s -X POST http://localhost:8090/gw/catalogue/libros/update/3 -H "Content-Type: application/json" -d "{\"titulo\": \"Don Quijote - Edicion Especial\", \"precio\": 48000}"
```
> Actualiza el libro 3

---

### 6. Transcripción POST → PATCH (actualización parcial)
```bash
curl -s -X POST http://localhost:8090/gw/catalogue/libros/patch/4 -H "Content-Type: application/json" -d "{\"precio\": 39000}"
```
> Solo actualiza el precio del libro 4

---

### 7. Transcripción POST → DELETE
```bash
curl -s -X POST http://localhost:8090/gw/catalogue/libros/delete/99
```
> Intenta eliminar libro 99 (da 404 porque no existe)

---

### 8. PEDIDO OK - Stock suficiente
```bash
curl -s -X POST http://localhost:8090/payments/pedidos -H "Content-Type: application/json" -d "{\"usuario\": 1, \"items\": [{\"idLibro\": 2, \"cantidad\": 2}]}"
```
> **Resultado:** `"estado":"C"` (Completado) - El libro 2 tiene stock suficiente

---

### 9. PEDIDO FAIL - Stock insuficiente
```bash
curl -s -X POST http://localhost:8090/payments/pedidos -H "Content-Type: application/json" -d "{\"usuario\": 1, \"items\": [{\"idLibro\": 10, \"cantidad\": 10}]}"
```
> **Resultado:** `"estado":"F"` (Fallido) - El libro 10 solo tiene 2 unidades

---

### 10. PEDIDO FAIL - Libro no visible
```bash
curl -s -X POST http://localhost:8090/payments/pedidos -H "Content-Type: application/json" -d "{\"usuario\": 1, \"items\": [{\"idLibro\": 9, \"cantidad\": 1}]}"
```
> **Resultado:** `"estado":"F"` (Fallido) - El libro 9 tiene visible=false

---

### 11. Listar todos los pedidos
```bash
curl -s http://localhost:8090/payments/pedidos
```
> Muestra los pedidos creados (OK y FAIL)

---

### 12. Búsqueda con POST y body JSON
```bash
curl -s -X POST http://localhost:8090/gw/catalogue/libros/search -H "Content-Type: application/json" -d "{\"ratingMin\": 5}"
```
> Busca libros con rating = 5

---

## PUNTOS CLAVE PARA MENCIONAR EN EL VIDEO

| Característica | Qué decir |
|----------------|-----------|
| **Eureka** | "Service Discovery - los microservicios se registran por nombre, no por IP" |
| **Gateway** | "Punto único de entrada en puerto 8090 - el cliente nunca habla directo con los microservicios" |
| **Balanceo** | "Hay 2 instancias de Catalogue y las peticiones se alternan automáticamente (Round-Robin)" |
| **Transcripción** | "El gateway puede convertir POST a GET, PUT, PATCH o DELETE para clientes limitados" |
| **Specifications** | "Búsqueda combinada con múltiples filtros: rating, autor, precio, categoría, etc." |
| **Validación compra** | "Payments valida contra Catalogue: verifica que el libro exista, esté visible y tenga stock" |
| **Estados pedido** | "C = Completado (éxito), F = Fallido (error de validación)" |
| **BDs separadas** | "Cada microservicio tiene su propia base de datos H2" |

---

## AL TERMINAR - Cerrar todo

En cada terminal presionar `Ctrl + C` para detener los servicios.

Orden sugerido para cerrar:
1. Gateway
2. Payments
3. Catalogue (ambas instancias)
4. Eureka
