# Comandos de Demo — Relatos de Papel (Actividad 3)

## 1. Levantar todo el backend

```bash
docker compose up --build -d
```

Esperar ~2 min a que todos los servicios se registren en Eureka.

```bash
# Verificar contenedores
docker compose ps

# Verificar Eureka (deben aparecer 4 servicios)
curl http://localhost:8761/eureka/apps | grep "<name>"
```

## 2. Reindexar libros en Elasticsearch

```bash
curl -s -X POST 'http://localhost:8090/search/search/reindex' | python3 -m json.tool
```

Respuesta esperada:
```json
{
  "message": "Reindexación completada",
  "indexedCount": 9,
  "tookMs": 1234,
  "indexName": "libros",
  "timestamp": "2026-02-25T..."
}
```

## 3. Búsqueda full-text

```bash
# Búsqueda simple
curl -s 'http://localhost:8090/search/search?q=Soledad' | python3 -m json.tool

# Búsqueda por autor
curl -s 'http://localhost:8090/search/search?q=Garcia+Marquez' | python3 -m json.tool

# Búsqueda con typo (fuzziness=AUTO lo tolera)
curl -s 'http://localhost:8090/search/search?q=cien+anios' | python3 -m json.tool
```

## 4. Sugerencias / Typeahead

```bash
curl -s 'http://localhost:8090/search/search/suggest?q=cie' | python3 -m json.tool
curl -s 'http://localhost:8090/search/search/suggest?q=ray' | python3 -m json.tool
```

## 5. Did-you-mean / Correcciones

```bash
# Typo en "soledad"
curl -s 'http://localhost:8090/search/search/didyoumean?q=soledda' | python3 -m json.tool

# Typo en "sombra"
curl -s 'http://localhost:8090/search/search/didyoumean?q=sonbra' | python3 -m json.tool

# Typo en "ficciones"
curl -s 'http://localhost:8090/search/search/didyoumean?q=ficcciones' | python3 -m json.tool
```

## 6. Facets con filtros combinados

```bash
# Solo categoría "Ficción"
curl -s 'http://localhost:8090/search/search?q=&categorias=Ficci%C3%B3n' | python3 -m json.tool

# Categoría + rating mínimo 4
curl -s 'http://localhost:8090/search/search?q=&categorias=Ficci%C3%B3n&ratingMin=4' | python3 -m json.tool

# Autor específico
curl -s 'http://localhost:8090/search/search?q=&autores=Stephen+King' | python3 -m json.tool

# Rango de precio
curl -s 'http://localhost:8090/search/search?q=novela&precioMin=30000&precioMax=50000' | python3 -m json.tool
```

## 7. Frontend

```bash
cd frontend
npm install
PORT=3002 npm start
```

Abrir http://localhost:3002 y demostrar:
1. **Typeahead**: escribir "cie" → aparecen sugerencias
2. **Full-text**: buscar "novela misterio" → resultados
3. **Facets**: marcar checkboxes de categoría/autor/rating
4. **Did-you-mean**: buscar "soledda" → 0 resultados → aparece "¿Quisiste decir...?"
5. **Click en sugerencia** → re-ejecuta búsqueda correcta
6. **Reindexar**: botón "Reindexar" funciona

## 8. Verificar servicios individuales

```bash
# Elasticsearch
curl -s http://localhost:9200 | python3 -m json.tool

# Kibana
# Abrir http://localhost:5601

# Gateway health
curl -s http://localhost:8090/actuator/health

# Catálogo vía gateway
curl -s 'http://localhost:8090/catalogue/libros?size=2' | python3 -m json.tool

# Swagger de Search
# Abrir http://localhost:8083/swagger-ui/index.html
```
