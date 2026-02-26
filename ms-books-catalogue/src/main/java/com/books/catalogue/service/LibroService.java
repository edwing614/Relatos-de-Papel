package com.books.catalogue.service;

import com.books.catalogue.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibroService {

    LibroDTO crear(CreateLibroRequest request);

    LibroDTO obtenerPorId(Integer id, boolean incluirOcultos);

    LibroDTO obtenerPorIsbn(String isbn, boolean incluirOcultos);

    LibroDTO actualizar(Integer id, UpdateLibroRequest request);

    LibroDTO actualizarParcial(Integer id, UpdateLibroRequest request);

    void eliminar(Integer id);

    Page<LibroDTO> buscar(LibroSearchCriteria criteria, Pageable pageable, boolean incluirOcultos);

    boolean verificarDisponibilidad(Integer idLibro, Integer cantidad);

    void decrementarStock(Integer idLibro, Integer cantidad);
}
