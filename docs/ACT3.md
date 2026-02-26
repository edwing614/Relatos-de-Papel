# Actividad 3 — Integración FE/BE, Elasticsearch y Docker

## Arquitectura Final

```
┌─────────────┐       ┌──────────────────┐       ┌────────────────────┐
│  Frontend    │──────>│  API Gateway     │──────>│ ms-books-catalogue │
│  React       │ :3000 │  :8090           │       │ :8081 (H2)         │
│              │       │                  │──────>│ ms-books-payments  │
│  - Búsqueda  │       │  CORS habilitado │       │ :8082 (H2)         │
│  - Typeahead │       │                  │──────>│ ms-books-search    │
│  - Facets    │       │  Eureka Client   │       │ :8083 (ES)         │
└─────────────┘       └────────┬─────────┘       └────────────────────┘
                               │
                    ┌──────────┴──────────┐
                    │  Eureka Server      │
                    │  :8761              │
                    └─────────────────────┘

                    ┌─────────────────────┐
                    │  Elasticsearch      │──── Kibana :5601
                    │  :9200 (single-node)│
                    └─────────────────────┘
```

### Rutas del Gateway

| Ruta Gateway              | Servicio destino       | Descripción                    |
|---------------------------|------------------------|--------------------------------|
| `/catalogue/**`           | ms-books-catalogue     | CRUD de libros                 |
| `/payments/**`            | ms-books-payments      | Gestión de pedidos             |
| `/search/**`              | ms-books-search        | Búsqueda ES (nuevo)            |
| `/gw/catalogue/libros/*`  | ms-books-catalogue     | Transcripción POST→GET/PUT/DEL |

## Pasos para Correr en Local

### Prerrequisitos
- Docker y Docker Compose instalados
- Node.js 18+ y npm
- (Opcional) Java 17 y Maven si quieres compilar fuera de Docker

### 1. Levantar todo el backend con Docker

```bash
cd books-microservices
docker compose up --build
```

Esto levanta: Elasticsearch, Kibana, Eureka, Gateway, Catalogue, Payments y Search.

Espera hasta ver en los logs que todos los servicios se registren en Eureka (~2 min).

### 2. Verificar que los servicios están arriba

```bash
# Eureka Dashboard
open http://localhost:8761

# Elasticsearch
curl http://localhost:9200

# Gateway health
curl http://localhost:8090/actuator/health

# Catálogo (vía gateway)
curl http://localhost:8090/catalogue/libros

# Search (vía gateway)
curl http://localhost:8090/search/search?q=
```

### 3. Reindexar libros en Elasticsearch

```bash
curl -X POST http://localhost:8090/search/search/reindex
```

Respuesta esperada:
```json
{
  "message": "Reindexación completada",
  "indexedCount": 9,
  "tookMs": 1234,
  "indexName": "libros",
  "timestamp": "2026-02-25T18:49:00Z"
}
```

### 4. Levantar el Frontend

```bash
cd frontend
npm install
npm start
```

Se abre en `http://localhost:3000`. La app busca directamente contra el Gateway en `:8090`.

### 5. Probar la aplicación

1. Al abrir la página, se muestran todos los libros indexados
2. Escribe en la caja de búsqueda → aparecen sugerencias (typeahead)
3. Haz clic en "Buscar" → resultados full-text
4. Usa los checkboxes de la barra lateral para filtrar por Categoría, Autor o Rating
5. Busca con un typo (ej: "soledda") → aparece bloque "¿Quisiste decir...?"
6. Haz clic en la sugerencia → se ejecuta la búsqueda correcta

## Endpoints de Búsqueda (ms-books-search)

### Búsqueda Full-Text con Facets

```
GET /search?q={texto}&categorias={cat1}&autores={aut1}&ratingMin={n}&page={p}&size={s}
```

Ejemplo:
```bash
curl "http://localhost:8090/search/search?q=Soledad"
```

Respuesta:
```json
{
  "results": [ { "idLibro": 1, "titulo": "Cien Años de Soledad", ... } ],
  "totalHits": 1,
  "page": 0,
  "size": 10,
  "totalPages": 1,
  "facets": {
    "categorias": [ { "key": "Ficción", "count": 1 }, { "key": "Fantasía", "count": 1 } ],
    "autores": [ { "key": "Gabriel García Márquez", "count": 1 } ],
    "ratings": [ { "key": "5", "count": 1 } ]
  }
}
```

### Sugerencias / Autocomplete

```
GET /search/suggest?q={texto}&maxResults={n}
```

Ejemplo:
```bash
curl "http://localhost:8090/search/search/suggest?q=cie"
```

Respuesta:
```json
{
  "suggestions": [
    { "idLibro": 1, "titulo": "Cien Años de Soledad", "autores": "Gabriel García Márquez", "score": 5.2 }
  ]
}
```

### Búsqueda con Facets (filtros combinados)

```bash
# Filtrar por categoría "Ficción" y rating mínimo 4
curl "http://localhost:8090/search/search?q=&categorias=Ficción&ratingMin=4"

# Filtrar por autor específico
curl "http://localhost:8090/search/search?q=&autores=Stephen%20King"

# Combinado: texto + categoría + rango de precio
curl "http://localhost:8090/search/search?q=novela&categorias=Misterio&precioMin=30000&precioMax=60000"
```

### Correcciones / Did-you-mean

```
GET /search/didyoumean?q={texto_con_typo}&maxResults={n}
```

Ejemplo:
```bash
curl "http://localhost:8090/search/search/didyoumean?q=soledda"
```

Respuesta:
```json
{
  "q": "soledda",
  "suggestions": ["Cien Años de Soledad"]
}
```

Usa fuzzy matching sobre `titulo.suggest` y `descripcion.suggest` (campos con analyzer standard para preservar los tokens originales). El FE llama este endpoint cuando una búsqueda devuelve 0 resultados y muestra un bloque "¿Quisiste decir...?" con links clickeables.

### Reindexar

```bash
curl -X POST http://localhost:8090/search/search/reindex
```

Respuesta mejorada con métricas:
```json
{
  "message": "Reindexación completada",
  "indexedCount": 9,
  "tookMs": 1200,
  "indexName": "libros",
  "timestamp": "2026-02-25T18:49:00Z"
}
```

## Mapping de Elasticsearch

| Campo              | Tipo ES             | Uso                           |
|--------------------|---------------------|-------------------------------|
| `titulo`           | search_as_you_type  | Full-text + autocompletado    |
| `descripcion`      | text                | Full-text con analyzer español|
| `autores.nombre`   | text + keyword      | Full-text + facets exactos    |
| `categorias.nombre`| keyword + text      | Facets exactos + búsqueda     |
| `precio`           | scaled_float        | Filtros por rango             |
| `rating`           | integer             | Filtros + facets              |
| `fechaPublicacion` | date                | Filtros por rango             |
| `codigoIsbn`       | keyword             | Búsqueda exacta              |
| `visible`          | boolean             | Filtro (solo visibles)        |

## Notas de CORS

- CORS se configura en el **API Gateway** (`CorsConfig.java`) — NO en cada microservicio.
- Orígenes permitidos: `http://localhost:*`, `http://127.0.0.1:*`, `https://*.vercel.app`
- El frontend SIEMPRE pasa por el Gateway (`:8090`), nunca llama directo a los microservicios.

## Despliegue Remoto

### Opción 1: Frontend en Vercel + Backend en Railway

1. **Frontend → Vercel**
   - Conectar el repo a Vercel
   - Directorio raíz: `frontend/`
   - Variable de entorno: `REACT_APP_API_BASE_URL=https://tu-gateway.railway.app`
   - Build command: `npm run build`
   - Output: `build/`

2. **Backend → Railway**
   - Crear un proyecto en Railway con los 5 servicios (Eureka, Gateway, Catalogue, Payments, Search)
   - Usar los Dockerfiles existentes
   - Configurar variables de entorno:
     - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server.railway.internal:8761/eureka/`
     - `SPRING_ELASTICSEARCH_URIS=http://elasticsearch.railway.internal:9200`
   - Railway ofrece Elasticsearch como plugin

### Opción 2: Elastic Cloud para Elasticsearch

1. Crear cluster en [Elastic Cloud](https://cloud.elastic.co) (free trial)
2. Obtener URL y API key
3. Configurar en ms-books-search:
   ```
   SPRING_ELASTICSEARCH_URIS=https://mi-cluster.es.cloud.es.io:9243
   ```
4. El resto de servicios puede estar en Railway/Render

### Variables necesarias para remoto

```env
# Gateway (público)
SERVER_PORT=8090

# Todos los microservicios
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-host:8761/eureka/

# Solo ms-books-search
SPRING_ELASTICSEARCH_URIS=https://host-elasticsearch:9200

# Frontend
REACT_APP_API_BASE_URL=https://url-publica-del-gateway
```

## Comandos Rápidos

```bash
# Levantar todo el backend
docker compose up --build

# Levantar solo Elasticsearch + Search
docker compose up elasticsearch ms-books-search

# Ver logs de un servicio
docker compose logs -f ms-books-search

# Parar todo
docker compose down

# Parar todo y borrar volúmenes (reset ES data)
docker compose down -v

# Frontend
cd frontend && npm start
```
