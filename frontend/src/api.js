import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8090';

const api = axios.create({ baseURL: API_BASE });

// --- Search (ms-books-search via gateway) ---
export async function searchBooks(params) {
  const { data } = await api.get('/search/search', { params });
  return data;
}

export async function suggestBooks(q) {
  const { data } = await api.get('/search/search/suggest', { params: { q, maxResults: 6 } });
  return data;
}

export async function reindexBooks() {
  const { data } = await api.post('/search/search/reindex');
  return data;
}

export async function didYouMean(q) {
  const { data } = await api.get('/search/search/didyoumean', { params: { q, maxResults: 5 } });
  return data;
}

// --- Catalogue (ms-books-catalogue via gateway) ---
export async function getLibros(params) {
  const { data } = await api.get('/catalogue/libros', { params });
  return data;
}

export async function getLibroById(id) {
  const { data } = await api.get(`/catalogue/libros/${id}`);
  return data;
}
