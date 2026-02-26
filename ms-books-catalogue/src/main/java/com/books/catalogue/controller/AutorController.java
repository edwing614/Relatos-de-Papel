package com.books.catalogue.controller;

import com.books.catalogue.dto.AutorDTO;
import com.books.catalogue.entity.Autor;
import com.books.catalogue.repository.AutorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/autores")
@RequiredArgsConstructor
@Tag(name = "Autores", description = "API para gestión de autores")
public class AutorController {

    private final AutorRepository autorRepository;

    @GetMapping
    @Operation(summary = "Listar todos los autores")
    public ResponseEntity<List<AutorDTO>> listar() {
        List<AutorDTO> autores = autorRepository.findAll().stream()
            .map(a -> AutorDTO.builder()
                .idAutor(a.getIdAutor())
                .nombre(a.getNombre())
                .pais(a.getPais())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(autores);
    }

    @PostMapping
    @Operation(summary = "Crear un autor")
    public ResponseEntity<AutorDTO> crear(@RequestBody AutorDTO dto) {
        Autor autor = Autor.builder()
            .nombre(dto.getNombre())
            .pais(dto.getPais())
            .build();
        autor = autorRepository.save(autor);

        return ResponseEntity.status(HttpStatus.CREATED).body(AutorDTO.builder()
            .idAutor(autor.getIdAutor())
            .nombre(autor.getNombre())
            .pais(autor.getPais())
            .build());
    }
}
