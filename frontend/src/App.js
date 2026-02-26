import React, { useState, useEffect, useCallback, useRef } from 'react';
import { searchBooks, suggestBooks, reindexBooks, didYouMean } from './api';
import './App.css';

function App() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [facets, setFacets] = useState({});
  const [totalHits, setTotalHits] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [selectedAuthors, setSelectedAuthors] = useState([]);
  const [selectedRating, setSelectedRating] = useState(null);
  const [loading, setLoading] = useState(false);
  const [reindexing, setReindexing] = useState(false);
  const [error, setError] = useState(null);
  const [didYouMeanList, setDidYouMeanList] = useState([]);

  const suggestTimeout = useRef(null);
  const searchBoxRef = useRef(null);

  const doSearch = useCallback(async (pageNum = 0) => {
    setLoading(true);
    setError(null);
    try {
      const params = { q: query, page: pageNum, size: 10 };
      if (selectedCategories.length) params.categorias = selectedCategories;
      if (selectedAuthors.length) params.autores = selectedAuthors;
      if (selectedRating) params.ratingMin = selectedRating;

      const data = await searchBooks(params);
      setResults(data.results || []);
      setFacets(data.facets || {});
      setTotalHits(data.totalHits || 0);
      setTotalPages(data.totalPages || 0);
      setPage(pageNum);

      // Did-you-mean: si 0 resultados y hay query, pedir correcciones
      if ((data.totalHits || 0) === 0 && query.trim().length > 0) {
        try {
          const dym = await didYouMean(query.trim());
          setDidYouMeanList(dym.suggestions || []);
        } catch { setDidYouMeanList([]); }
      } else {
        setDidYouMeanList([]);
      }
    } catch (err) {
      setError('Error al buscar. Verifica que el backend esté corriendo.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [query, selectedCategories, selectedAuthors, selectedRating]);

  // Search on filter change
  useEffect(() => {
    doSearch(0);
  }, [selectedCategories, selectedAuthors, selectedRating]); // eslint-disable-line

  // Typeahead suggestions
  const handleInputChange = (e) => {
    const val = e.target.value;
    setQuery(val);

    if (suggestTimeout.current) clearTimeout(suggestTimeout.current);
    if (val.length >= 2) {
      suggestTimeout.current = setTimeout(async () => {
        try {
          const data = await suggestBooks(val);
          setSuggestions(data.suggestions || []);
          setShowSuggestions(true);
        } catch {
          setSuggestions([]);
        }
      }, 250);
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setShowSuggestions(false);
    doSearch(0);
  };

  const selectSuggestion = (titulo) => {
    setQuery(titulo);
    setShowSuggestions(false);
    setTimeout(() => doSearch(0), 50);
  };

  const toggleCategory = (cat) => {
    setSelectedCategories((prev) =>
      prev.includes(cat) ? prev.filter((c) => c !== cat) : [...prev, cat]
    );
  };

  const toggleAuthor = (aut) => {
    setSelectedAuthors((prev) =>
      prev.includes(aut) ? prev.filter((a) => a !== aut) : [...prev, aut]
    );
  };

  const handleReindex = async () => {
    setReindexing(true);
    try {
      const data = await reindexBooks();
      alert(`Reindexado: ${data.documentosIndexados} documentos`);
      doSearch(0);
    } catch (err) {
      alert('Error al reindexar: ' + (err.response?.data?.message || err.message));
    } finally {
      setReindexing(false);
    }
  };

  // Close suggestions on click outside
  useEffect(() => {
    const handler = (e) => {
      if (searchBoxRef.current && !searchBoxRef.current.contains(e.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const renderStars = (rating) => '★'.repeat(rating || 0) + '☆'.repeat(5 - (rating || 0));

  return (
    <div className="app">
      <header className="header">
        <h1>Relatos de Papel</h1>
        <p>Buscador de libros con Elasticsearch</p>
      </header>

      <main className="main">
        {/* Search Bar */}
        <div className="search-section" ref={searchBoxRef}>
          <form onSubmit={handleSubmit} className="search-form">
            <div className="search-input-wrapper">
              <input
                type="text"
                value={query}
                onChange={handleInputChange}
                placeholder="Buscar libros por título, autor, descripción..."
                className="search-input"
                onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
              />
              {showSuggestions && suggestions.length > 0 && (
                <ul className="suggestions">
                  {suggestions.map((s, i) => (
                    <li key={i} onClick={() => selectSuggestion(s.titulo)}>
                      <strong>{s.titulo}</strong>
                      {s.autores && <span className="suggest-author"> — {s.autores}</span>}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Buscando...' : 'Buscar'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={handleReindex} disabled={reindexing}>
              {reindexing ? 'Reindexando...' : 'Reindexar'}
            </button>
          </form>
        </div>

        <div className="content">
          {/* Sidebar Facets */}
          <aside className="sidebar">
            <h3>Filtros</h3>

            {/* Category Facets */}
            {facets.categorias && facets.categorias.length > 0 && (
              <div className="facet-group">
                <h4>Categorías</h4>
                {facets.categorias.map((f) => (
                  <label key={f.key} className="facet-item">
                    <input
                      type="checkbox"
                      checked={selectedCategories.includes(f.key)}
                      onChange={() => toggleCategory(f.key)}
                    />
                    <span>{f.key}</span>
                    <span className="facet-count">({f.count})</span>
                  </label>
                ))}
              </div>
            )}

            {/* Author Facets */}
            {facets.autores && facets.autores.length > 0 && (
              <div className="facet-group">
                <h4>Autores</h4>
                {facets.autores.map((f) => (
                  <label key={f.key} className="facet-item">
                    <input
                      type="checkbox"
                      checked={selectedAuthors.includes(f.key)}
                      onChange={() => toggleAuthor(f.key)}
                    />
                    <span>{f.key}</span>
                    <span className="facet-count">({f.count})</span>
                  </label>
                ))}
              </div>
            )}

            {/* Rating Filter */}
            {facets.ratings && facets.ratings.length > 0 && (
              <div className="facet-group">
                <h4>Rating mínimo</h4>
                {[5, 4, 3, 2, 1].map((r) => (
                  <label key={r} className="facet-item">
                    <input
                      type="radio"
                      name="rating"
                      checked={selectedRating === r}
                      onChange={() => setSelectedRating(selectedRating === r ? null : r)}
                    />
                    <span className="stars">{renderStars(r)}</span>
                  </label>
                ))}
                {selectedRating && (
                  <button className="btn-clear" onClick={() => setSelectedRating(null)}>
                    Limpiar rating
                  </button>
                )}
              </div>
            )}

            {(selectedCategories.length > 0 || selectedAuthors.length > 0 || selectedRating) && (
              <button
                className="btn btn-clear-all"
                onClick={() => {
                  setSelectedCategories([]);
                  setSelectedAuthors([]);
                  setSelectedRating(null);
                }}
              >
                Limpiar todos los filtros
              </button>
            )}
          </aside>

          {/* Results */}
          <section className="results">
            {error && <div className="error">{error}</div>}

            <div className="results-header">
              <span>{totalHits} resultado{totalHits !== 1 ? 's' : ''} encontrado{totalHits !== 1 ? 's' : ''}</span>
            </div>

            {results.length === 0 && !loading && (
              <div className="no-results">
                <p>No se encontraron libros. Intenta con otro término o haz clic en "Reindexar".</p>
                {didYouMeanList.length > 0 && (
                  <div className="did-you-mean">
                    <p>¿Quisiste decir...?</p>
                    <ul>
                      {didYouMeanList.map((s, i) => (
                        <li key={i}>
                          <button onClick={() => { setQuery(s); setTimeout(() => doSearch(0), 50); }}>
                            {s}
                          </button>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}

            <div className="results-grid">
              {results.map((libro) => (
                <div key={libro.idLibro} className="book-card">
                  <div className="book-header">
                    <h3>{libro.titulo}</h3>
                    <span className="book-rating">{renderStars(libro.rating)}</span>
                  </div>
                  <p className="book-authors">
                    {libro.autores?.map((a) => a.nombre).join(', ') || 'Autor desconocido'}
                  </p>
                  <p className="book-desc">
                    {libro.descripcion?.substring(0, 150)}
                    {libro.descripcion?.length > 150 ? '...' : ''}
                  </p>
                  <div className="book-meta">
                    <span className="book-price">
                      ${libro.precio?.toLocaleString('es-CO')}
                    </span>
                    <span className="book-isbn">ISBN: {libro.codigoIsbn || 'N/A'}</span>
                  </div>
                  <div className="book-tags">
                    {libro.categorias?.map((c) => (
                      <span key={c.idCategoria || c.nombre} className="tag">{c.nombre}</span>
                    ))}
                  </div>
                  <div className="book-stock">
                    Stock: {libro.stockDisponible ?? 'N/A'} unidades
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="pagination">
                <button
                  disabled={page === 0}
                  onClick={() => doSearch(page - 1)}
                  className="btn btn-sm"
                >
                  Anterior
                </button>
                <span>Página {page + 1} de {totalPages}</span>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => doSearch(page + 1)}
                  className="btn btn-sm"
                >
                  Siguiente
                </button>
              </div>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}

export default App;
