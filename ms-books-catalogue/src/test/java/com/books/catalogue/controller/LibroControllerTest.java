package com.books.catalogue.controller;

import com.books.catalogue.dto.*;
import com.books.catalogue.exception.*;
import com.books.catalogue.service.LibroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LibroController.class)
@Import(com.books.catalogue.exception.GlobalExceptionHandler.class)
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibroService libroService;

    private LibroDTO libroDTO;
    private CreateLibroRequest createRequest;

    @BeforeEach
    void setUp() {
        libroDTO = LibroDTO.builder()
            .idLibro(1)
            .codigo("LIB001")
            .codigoIsbn("978-0307474728")
            .titulo("Cien Años de Soledad")
            .precio(new BigDecimal("45000.00"))
            .visible(true)
            .stockDisponible(50)
            .autores(Collections.emptyList())
            .categorias(Collections.emptyList())
            .build();

        createRequest = CreateLibroRequest.builder()
            .codigo("LIB001")
            .codigoIsbn("978-0307474728")
            .titulo("Cien Años de Soledad")
            .precio(new BigDecimal("45000.00"))
            .visible(true)
            .build();
    }

    @Test
    void crear_DeberiaRetornar201() throws Exception {
        when(libroService.crear(any(CreateLibroRequest.class))).thenReturn(libroDTO);

        mockMvc.perform(post("/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idLibro").value(1))
            .andExpect(jsonPath("$.titulo").value("Cien Años de Soledad"));
    }

    @Test
    void crear_DeberiaRetornar400SiDatosInvalidos() throws Exception {
        CreateLibroRequest invalidRequest = CreateLibroRequest.builder()
            .codigo("") // código vacío - inválido
            .build();

        mockMvc.perform(post("/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPorId_DeberiaRetornar200() throws Exception {
        when(libroService.obtenerPorId(1, false)).thenReturn(libroDTO);

        mockMvc.perform(get("/libros/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idLibro").value(1))
            .andExpect(jsonPath("$.titulo").value("Cien Años de Soledad"));
    }

    @Test
    void obtenerPorId_DeberiaRetornar404SiNoExiste() throws Exception {
        when(libroService.obtenerPorId(999, false))
            .thenThrow(new ResourceNotFoundException("Libro no encontrado"));

        mockMvc.perform(get("/libros/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_DeberiaRetornar204() throws Exception {
        doNothing().when(libroService).eliminar(1);

        mockMvc.perform(delete("/libros/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void actualizar_DeberiaRetornar200() throws Exception {
        UpdateLibroRequest updateRequest = UpdateLibroRequest.builder()
            .titulo("Título Actualizado")
            .build();

        LibroDTO updated = LibroDTO.builder()
            .idLibro(1)
            .titulo("Título Actualizado")
            .build();

        when(libroService.actualizar(eq(1), any(UpdateLibroRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/libros/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Título Actualizado"));
    }
}
