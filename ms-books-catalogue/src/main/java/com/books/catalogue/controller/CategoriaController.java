package com.books.catalogue.controller;

import com.books.catalogue.dto.CategoriaDTO;
import com.books.catalogue.entity.Categoria;
import com.books.catalogue.repository.CategoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "API para gestión de categorías")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    @GetMapping
    @Operation(summary = "Listar todas las categorías")
    public ResponseEntity<List<CategoriaDTO>> listar() {
        List<CategoriaDTO> categorias = categoriaRepository.findAll().stream()
            .map(c -> CategoriaDTO.builder()
                .idCategoria(c.getIdCategoria())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

    @PostMapping
    @Operation(summary = "Crear una categoría")
    public ResponseEntity<CategoriaDTO> crear(@RequestBody CategoriaDTO dto) {
        Categoria categoria = Categoria.builder()
            .nombre(dto.getNombre())
            .descripcion(dto.getDescripcion())
            .build();
        categoria = categoriaRepository.save(categoria);

        return ResponseEntity.status(HttpStatus.CREATED).body(CategoriaDTO.builder()
            .idCategoria(categoria.getIdCategoria())
            .nombre(categoria.getNombre())
            .descripcion(categoria.getDescripcion())
            .build());
    }
}
