import http from 'k6/http';
import { check, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

// ============================================
// Relatos de Papel - Prueba de Carga
// Endpoint: GET /libros (listado/busqueda)
// ============================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const BOOKS_PATH = __ENV.BOOKS_PATH || '/libros';

export const options = {
  stages: [
    { duration: '10s', target: parseInt(__ENV.VUS) || 10 },
    { duration: __ENV.DURATION || '30s', target: parseInt(__ENV.VUS) || 10 },
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
  },
};

export default function () {
  // Escenario 1: Listar todos los libros
  const resList = http.get(`${BASE_URL}${BOOKS_PATH}`);
  check(resList, {
    'GET /libros status 200': (r) => r.status === 200,
    'response has content': (r) => {
      const body = JSON.parse(r.body);
      return body.content && body.content.length > 0;
    },
  });

  sleep(0.5);

  // Escenario 2: Obtener libro por ID (seed data, id=1)
  const resDetail = http.get(`${BASE_URL}${BOOKS_PATH}/1`);
  check(resDetail, {
    'GET /libros/1 status 200': (r) => r.status === 200,
    'libro has titulo': (r) => {
      const body = JSON.parse(r.body);
      return body.titulo !== undefined;
    },
  });

  sleep(0.5);

  // Escenario 3: Busqueda por query params
  const resSearch = http.get(`${BASE_URL}${BOOKS_PATH}?ratingMin=4&precioMax=100`);
  check(resSearch, {
    'GET /libros?search status 200': (r) => r.status === 200,
  });

  sleep(0.3);
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    '../../evidencias/k6-summary.json': JSON.stringify(data, null, 2),
  };
}
