package com.books.catalogue.controller;

import com.books.catalogue.dto.*;
import com.books.catalogue.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin/libros")
@RequiredArgsConstructor
@Tag(name = "Admin - Libros", description = "API administrativa para gestión de libros (incluye ocultos)")
public class AdminLibroController {

    private final LibroService libroService;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener libro por ID (admin)", description = "Obtiene cualquier libro, incluyendo no visibles")
    public ResponseEntity<LibroDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(libroService.obtenerPorId(id, true));
    }

    @GetMapping
    @Operation(summary = "Buscar libros (admin)", description = "Búsqueda que incluye libros no visibles")
    public ResponseEntity<Page<LibroDTO>> buscar(
        @RequestParam(required = false) String titulo,
        @RequestParam(required = false) String autor,
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) String codigoIsbn,
        @RequestParam(required = false) BigDecimal precioMin,
        @RequestParam(required = false) BigDecimal precioMax,
        @RequestParam(required = false) Boolean visible,
        @RequestParam(required = false) LocalDate fechaPublicacionDesde,
        @RequestParam(required = false) LocalDate fechaPublicacionHasta,
        @PageableDefault(size = 10, sort = "titulo") Pageable pageable
    ) {
        LibroSearchCriteria criteria = LibroSearchCriteria.builder()
            .titulo(titulo)
            .autor(autor)
            .categoria(categoria)
            .codigoIsbn(codigoIsbn)
            .precioMin(precioMin)
            .precioMax(precioMax)
            .visible(visible)
            .fechaPublicacionDesde(fechaPublicacionDesde)
            .fechaPublicacionHasta(fechaPublicacionHasta)
            .build();

        return ResponseEntity.ok(libroService.buscar(criteria, pageable, true));
    }
}
