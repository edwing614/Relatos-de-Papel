package com.books.catalogue.service.impl;

import com.books.catalogue.dto.*;
import com.books.catalogue.entity.*;
import com.books.catalogue.exception.*;
import com.books.catalogue.repository.*;
import com.books.catalogue.service.LibroService;
import com.books.catalogue.specification.LibroSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final InventarioRepository inventarioRepository;

    @Override
    public LibroDTO crear(CreateLibroRequest request) {
        // Validar ISBN único
        if (request.getCodigoIsbn() != null && libroRepository.existsByCodigoIsbn(request.getCodigoIsbn())) {
            throw new DuplicateResourceException("Ya existe un libro con el ISBN: " + request.getCodigoIsbn());
        }

        // Validar código único
        if (libroRepository.existsByCodigo(request.getCodigo())) {
            throw new DuplicateResourceException("Ya existe un libro con el código: " + request.getCodigo());
        }

        Libro libro = Libro.builder()
            .codigo(request.getCodigo())
            .codigoIsbn(request.getCodigoIsbn())
            .titulo(request.getTitulo())
            .descripcion(request.getDescripcion())
            .precio(request.getPrecio())
            .visible(request.getVisible())
            .fechaPublicacion(request.getFechaPublicacion())
            .rating(request.getRating())
            .autores(new HashSet<>())
            .categorias(new HashSet<>())
            .build();

        // Asignar autores
        if (request.getAutorIds() != null && !request.getAutorIds().isEmpty()) {
            List<Autor> autores = autorRepository.findAllById(request.getAutorIds());
            libro.setAutores(new HashSet<>(autores));
        }

        // Asignar categorías
        if (request.getCategoriaIds() != null && !request.getCategoriaIds().isEmpty()) {
            List<Categoria> categorias = categoriaRepository.findAllById(request.getCategoriaIds());
            libro.setCategorias(new HashSet<>(categorias));
        }

        libro = libroRepository.save(libro);

        // Crear inventario inicial
        if (request.getStockInicial() != null && request.getStockInicial() > 0) {
            Inventario inventario = Inventario.builder()
                .idLibro(libro.getIdLibro())
                .cantidadDisponible(request.getStockInicial())
                .fechaActualizacion(LocalDateTime.now())
                .build();
            inventarioRepository.save(inventario);
        }

        return toDTO(libro);
    }

    @Override
    @Transactional(readOnly = true)
    public LibroDTO obtenerPorId(Integer id, boolean incluirOcultos) {
        Libro libro = libroRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));

        if (!incluirOcultos && !libro.getVisible()) {
            throw new ResourceNotFoundException("Libro no encontrado con id: " + id);
        }

        return toDTO(libro);
    }

    @Override
    @Transactional(readOnly = true)
    public LibroDTO obtenerPorIsbn(String isbn, boolean incluirOcultos) {
        Libro libro = libroRepository.findByCodigoIsbn(isbn)
            .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ISBN: " + isbn));

        if (!incluirOcultos && !libro.getVisible()) {
            throw new ResourceNotFoundException("Libro no encontrado con ISBN: " + isbn);
        }

        return toDTO(libro);
    }

    @Override
    public LibroDTO actualizar(Integer id, UpdateLibroRequest request) {
        Libro libro = libroRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));

        // Validar ISBN único si se cambia
        if (request.getCodigoIsbn() != null && !request.getCodigoIsbn().equals(libro.getCodigoIsbn())) {
            if (libroRepository.existsByCodigoIsbn(request.getCodigoIsbn())) {
                throw new DuplicateResourceException("Ya existe un libro con el ISBN: " + request.getCodigoIsbn());
            }
        }

        // Actualización completa (PUT)
        if (request.getCodigo() != null) libro.setCodigo(request.getCodigo());
        if (request.getCodigoIsbn() != null) libro.setCodigoIsbn(request.getCodigoIsbn());
        if (request.getTitulo() != null) libro.setTitulo(request.getTitulo());
        libro.setDescripcion(request.getDescripcion());
        if (request.getPrecio() != null) libro.setPrecio(request.getPrecio());
        if (request.getVisible() != null) libro.setVisible(request.getVisible());
        libro.setFechaPublicacion(request.getFechaPublicacion());
        if (request.getRating() != null) libro.setRating(request.getRating());

        // Actualizar autores
        if (request.getAutorIds() != null) {
            List<Autor> autores = autorRepository.findAllById(request.getAutorIds());
            libro.setAutores(new HashSet<>(autores));
        }

        // Actualizar categorías
        if (request.getCategoriaIds() != null) {
            List<Categoria> categorias = categoriaRepository.findAllById(request.getCategoriaIds());
            libro.setCategorias(new HashSet<>(categorias));
        }

        libro = libroRepository.save(libro);
        return toDTO(libro);
    }

    @Override
    public LibroDTO actualizarParcial(Integer id, UpdateLibroRequest request) {
        Libro libro = libroRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + id));

        // Validar ISBN único si se cambia
        if (request.getCodigoIsbn() != null && !request.getCodigoIsbn().equals(libro.getCodigoIsbn())) {
            if (libroRepository.existsByCodigoIsbn(request.getCodigoIsbn())) {
                throw new DuplicateResourceException("Ya existe un libro con el ISBN: " + request.getCodigoIsbn());
            }
            libro.setCodigoIsbn(request.getCodigoIsbn());
        }

        // Actualización parcial (PATCH) - solo campos no nulos
        if (request.getCodigo() != null) libro.setCodigo(request.getCodigo());
        if (request.getTitulo() != null) libro.setTitulo(request.getTitulo());
        if (request.getDescripcion() != null) libro.setDescripcion(request.getDescripcion());
        if (request.getPrecio() != null) libro.setPrecio(request.getPrecio());
        if (request.getVisible() != null) libro.setVisible(request.getVisible());
        if (request.getFechaPublicacion() != null) libro.setFechaPublicacion(request.getFechaPublicacion());
        if (request.getRating() != null) libro.setRating(request.getRating());

        if (request.getAutorIds() != null) {
            List<Autor> autores = autorRepository.findAllById(request.getAutorIds());
            libro.setAutores(new HashSet<>(autores));
        }

        if (request.getCategoriaIds() != null) {
            List<Categoria> categorias = categoriaRepository.findAllById(request.getCategoriaIds());
            libro.setCategorias(new HashSet<>(categorias));
        }

        libro = libroRepository.save(libro);
        return toDTO(libro);
    }

    @Override
    public void eliminar(Integer id) {
        if (!libroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Libro no encontrado con id: " + id);
        }
        libroRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LibroDTO> buscar(LibroSearchCriteria criteria, Pageable pageable, boolean incluirOcultos) {
        Specification<Libro> spec = LibroSpecification.withCriteria(criteria);

        // Por defecto, solo mostrar visibles (modo público)
        if (!incluirOcultos) {
            if (criteria.getVisible() == null) {
                criteria.setVisible(true);
            }
            spec = spec.and(LibroSpecification.visibleOnly());
        }

        return libroRepository.findAll(spec, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Integer idLibro, Integer cantidad) {
        Libro libro = libroRepository.findById(idLibro)
            .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + idLibro));

        if (!libro.getVisible()) {
            return false;
        }

        return inventarioRepository.findByIdLibro(idLibro)
            .map(inv -> inv.getCantidadDisponible() >= cantidad)
            .orElse(false);
    }

    @Override
    public void decrementarStock(Integer idLibro, Integer cantidad) {
        int updated = inventarioRepository.decrementarStock(idLibro, cantidad);
        if (updated == 0) {
            throw new InsufficientStockException("Stock insuficiente para el libro: " + idLibro);
        }
    }

    private LibroDTO toDTO(Libro libro) {
        Integer stock = inventarioRepository.findByIdLibro(libro.getIdLibro())
            .map(Inventario::getCantidadDisponible)
            .orElse(0);

        return LibroDTO.builder()
            .idLibro(libro.getIdLibro())
            .codigo(libro.getCodigo())
            .codigoIsbn(libro.getCodigoIsbn())
            .titulo(libro.getTitulo())
            .descripcion(libro.getDescripcion())
            .precio(libro.getPrecio())
            .visible(libro.getVisible())
            .fechaPublicacion(libro.getFechaPublicacion())
            .rating(libro.getRating())
            .stockDisponible(stock)
            .autores(libro.getAutores().stream()
                .map(a -> AutorDTO.builder()
                    .idAutor(a.getIdAutor())
                    .nombre(a.getNombre())
                    .pais(a.getPais())
                    .build())
                .collect(Collectors.toList()))
            .categorias(libro.getCategorias().stream()
                .map(c -> CategoriaDTO.builder()
                    .idCategoria(c.getIdCategoria())
                    .nombre(c.getNombre())
                    .descripcion(c.getDescripcion())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
