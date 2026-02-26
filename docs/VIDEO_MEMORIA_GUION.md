# Guion Videomemoria — Actividad 3 (15 min)

## Minuto 0-2: Introducción y Arquitectura

- Presentarte y nombrar la asignatura
- Mostrar diagrama de arquitectura (docs/ACT3.md)
- Explicar los componentes:
  - Eureka (discovery), Gateway (entrada única + CORS), Catalogue (CRUD), Payments, **Search (nuevo con ES)**
  - Frontend React conectado SOLO al Gateway

## Minuto 2-4: Cambios en el Backend

- Mostrar brevemente el nuevo microservicio `ms-books-search`:
  - `LibroDocument.java` → mapping ES con `search_as_you_type`, `text`, `keyword`, `nested`
  - `SearchController.java` → 3 endpoints: `/search`, `/search/suggest`, `/search/reindex`
  - `LibroSearchServiceImpl.java` → queries ES nativas (multi_match, nested, aggregations)
- Mostrar la configuración CORS en `CorsConfig.java` del Gateway
- Mostrar la nueva ruta `/search/**` en `application.yml` del Gateway

## Minuto 4-6: Docker Compose

- Mostrar `docker-compose.yml` con 6 servicios + Elasticsearch + Kibana
- Mostrar los healthchecks y depends_on
- Ejecutar `docker compose up --build` (tener ya compilado para no esperar)
- Mostrar `docker ps` con todos los contenedores corriendo
- Mostrar Eureka Dashboard con los 4 microservicios registrados

## Minuto 6-8: Evidencia Backend Funcionando

Terminal con curl:
```bash
# 1. Verificar ES
curl http://localhost:9200

# 2. Reindexar
curl -X POST http://localhost:8090/search/search/reindex

# 3. Búsqueda full-text
curl "http://localhost:8090/search/search?q=Soledad"

# 4. Sugerencias typeahead
curl "http://localhost:8090/search/search/suggest?q=cie"

# 5. Facets con filtro
curl "http://localhost:8090/search/search?q=&categorias=Ficción&ratingMin=4"

# 6. CRUD vía gateway (verificar que sigue funcionando)
curl http://localhost:8090/catalogue/libros
```

## Minuto 8-11: Frontend Funcionando

- Abrir `http://localhost:3000`
- **Demo typeahead**: escribir "cie" → ver sugerencias desplegarse
- **Demo full-text**: buscar "novela misterio" → ver resultados
- **Demo facets**: marcar checkbox "Ficción" → resultados se refinan
- Marcar "Stephen King" como autor → solo sus libros
- Marcar rating 4+ → se filtran más
- Limpiar filtros → vuelven todos
- Mostrar paginación si hay suficientes resultados
- Mostrar botón "Reindexar" → funciona desde el frontend

## Minuto 11-13: Código Frontend Relevante

- Mostrar `api.js` → todas las llamadas pasan por `REACT_APP_API_BASE_URL` (Gateway)
- Mostrar `App.js`:
  - Typeahead con debounce de 250ms
  - Facets dinámicos que vienen del response de ES
  - Paginación
- Mostrar `.env` → `REACT_APP_API_BASE_URL=http://localhost:8090`

## Minuto 13-14: Despliegue Remoto (preparado)

- Mostrar docs de despliegue en `docs/ACT3.md`
- Explicar las 2 opciones: Vercel+Railway o Elastic Cloud
- Mostrar las variables de entorno necesarias
- (Si se desplegó) Abrir la URL pública y hacer una búsqueda

## Minuto 14-15: Conclusiones

- Resumen de lo logrado:
  - Integración completa FE ↔ BE via Gateway
  - Migración de búsqueda a Elasticsearch con search_as_you_type
  - 3 endpoints de búsqueda: full-text, suggest, facets
  - Docker Compose con todo el backend + ES
  - Frontend funcional con typeahead, resultados y facets
- Mejoras futuras posibles:
  - Sincronización automática catalogue → search (evento o scheduled)
  - Paginación con scroll en ES para grandes volúmenes
  - Auth con Spring Security
- Despedida
