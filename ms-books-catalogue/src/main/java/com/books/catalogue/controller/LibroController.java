package com.books.catalogue.controller;

import com.books.catalogue.dto.*;
import com.books.catalogue.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/libros")
@RequiredArgsConstructor
@Tag(name = "Libros", description = "API para gestión del catálogo de libros")
public class LibroController {

    private final LibroService libroService;

    @Value("${server.port}")
    private int serverPort;

    @PostMapping
    @Operation(summary = "Crear un nuevo libro", description = "Crea un nuevo libro en el catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Libro creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "ISBN o código duplicado")
    })
    public ResponseEntity<LibroDTO> crear(@Valid @RequestBody CreateLibroRequest request) {
        LibroDTO libro = libroService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(libro);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener libro por ID", description = "Obtiene un libro por su ID (solo visibles)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Libro encontrado"),
        @ApiResponse(responseCode = "404", description = "Libro no encontrado")
    })
    public ResponseEntity<LibroDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(libroService.obtenerPorId(id, false));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Obtener libro por ISBN", description = "Obtiene un libro por su ISBN (solo visibles)")
    public ResponseEntity<LibroDTO> obtenerPorIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(libroService.obtenerPorIsbn(isbn, false));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar libro completo", description = "Reemplaza todos los datos del libro")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Libro actualizado"),
        @ApiResponse(responseCode = "404", description = "Libro no encontrado"),
        @ApiResponse(responseCode = "409", description = "ISBN duplicado")
    })
    public ResponseEntity<LibroDTO> actualizar(
        @PathVariable Integer id,
        @Valid @RequestBody UpdateLibroRequest request
    ) {
        return ResponseEntity.ok(libroService.actualizar(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar libro parcialmente", description = "Actualiza solo los campos enviados")
    public ResponseEntity<LibroDTO> actualizarParcial(
        @PathVariable Integer id,
        @RequestBody UpdateLibroRequest request
    ) {
        return ResponseEntity.ok(libroService.actualizarParcial(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar libro", description = "Elimina un libro del catálogo")
    @ApiResponse(responseCode = "204", description = "Libro eliminado")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @Operation(summary = "Buscar libros (POST)", description = "Búsqueda combinada via POST body para transcripción de gateway")
    public ResponseEntity<Page<LibroDTO>> buscarPost(
        @RequestBody LibroSearchCriteria criteria,
        @PageableDefault(size = 10, sort = "titulo") Pageable pageable
    ) {
        return ResponseEntity.ok(libroService.buscar(criteria, pageable, false));
    }

    @GetMapping
    @Operation(summary = "Buscar libros", description = "Búsqueda combinada de libros con múltiples filtros. Por defecto solo muestra libros visibles.")
    public ResponseEntity<Page<LibroDTO>> buscar(
        @Parameter(description = "Filtrar por título (búsqueda parcial)")
        @RequestParam(required = false) String titulo,

        @Parameter(description = "Filtrar por nombre de autor (búsqueda parcial)")
        @RequestParam(required = false) String autor,

        @Parameter(description = "Filtrar por nombre de categoría (búsqueda parcial)")
        @RequestParam(required = false) String categoria,

        @Parameter(description = "Filtrar por ISBN exacto")
        @RequestParam(required = false) String codigoIsbn,

        @Parameter(description = "Precio mínimo")
        @RequestParam(required = false) BigDecimal precioMin,

        @Parameter(description = "Precio máximo")
        @RequestParam(required = false) BigDecimal precioMax,

        @Parameter(description = "Fecha publicación desde")
        @RequestParam(required = false) LocalDate fechaPublicacionDesde,

        @Parameter(description = "Fecha publicación hasta")
        @RequestParam(required = false) LocalDate fechaPublicacionHasta,

        @Parameter(description = "Rating mínimo (1-5)")
        @RequestParam(required = false) Integer ratingMin,

        @Parameter(description = "Rating máximo (1-5)")
        @RequestParam(required = false) Integer ratingMax,

        @PageableDefault(size = 10, sort = "titulo") Pageable pageable
    ) {
        LibroSearchCriteria criteria = LibroSearchCriteria.builder()
            .titulo(titulo)
            .autor(autor)
            .categoria(categoria)
            .codigoIsbn(codigoIsbn)
            .precioMin(precioMin)
            .precioMax(precioMax)
            .fechaPublicacionDesde(fechaPublicacionDesde)
            .fechaPublicacionHasta(fechaPublicacionHasta)
            .ratingMin(ratingMin)
            .ratingMax(ratingMax)
            .build();

        return ResponseEntity.ok(libroService.buscar(criteria, pageable, false));
    }

    // === Endpoint interno para ms-books-payments ===

    @GetMapping("/internal/{id}/disponibilidad")
    @Operation(summary = "Verificar disponibilidad (interno)", description = "Usado por ms-books-payments para validar stock")
    public ResponseEntity<DisponibilidadResponse> verificarDisponibilidad(
        @PathVariable Integer id,
        @RequestParam Integer cantidad
    ) {
        boolean disponible = libroService.verificarDisponibilidad(id, cantidad);
        LibroDTO libro = null;
        try {
            libro = libroService.obtenerPorId(id, true);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(DisponibilidadResponse.builder()
            .disponible(disponible)
            .libro(libro)
            .build());
    }

    @PostMapping("/internal/{id}/decrementar-stock")
    @Operation(summary = "Decrementar stock (interno)", description = "Usado por ms-books-payments después de una compra exitosa")
    public ResponseEntity<Void> decrementarStock(
        @PathVariable Integer id,
        @RequestParam Integer cantidad
    ) {
        libroService.decrementarStock(id, cantidad);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    @Operation(summary = "Info de instancia", description = "Devuelve info de la instancia para demostrar balanceo")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "service", "ms-books-catalogue",
            "port", serverPort,
            "instance", "catalogue-" + serverPort
        ));
    }
}
