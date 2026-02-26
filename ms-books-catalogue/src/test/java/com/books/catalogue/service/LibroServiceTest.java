package com.books.catalogue.service;

import com.books.catalogue.dto.*;
import com.books.catalogue.entity.*;
import com.books.catalogue.exception.*;
import com.books.catalogue.repository.*;
import com.books.catalogue.service.impl.LibroServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private LibroServiceImpl libroService;

    private Libro libro;
    private CreateLibroRequest createRequest;

    @BeforeEach
    void setUp() {
        libro = Libro.builder()
            .idLibro(1)
            .codigo("LIB001")
            .codigoIsbn("978-0307474728")
            .titulo("Cien Años de Soledad")
            .descripcion("Novela de García Márquez")
            .precio(new BigDecimal("45000.00"))
            .visible(true)
            .fechaPublicacion(LocalDate.of(1967, 5, 30))
            .rating(5)
            .autores(new HashSet<>())
            .categorias(new HashSet<>())
            .build();

        createRequest = CreateLibroRequest.builder()
            .codigo("LIB002")
            .codigoIsbn("978-1234567890")
            .titulo("Nuevo Libro")
            .precio(new BigDecimal("30000.00"))
            .visible(true)
            .rating(4)
            .build();
    }

    @Test
    void crear_DeberiaCrearLibroExitosamente() {
        when(libroRepository.existsByCodigoIsbn(anyString())).thenReturn(false);
        when(libroRepository.existsByCodigo(anyString())).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenAnswer(invocation -> {
            Libro l = invocation.getArgument(0);
            l.setIdLibro(2);
            return l;
        });
        when(inventarioRepository.findByIdLibro(anyInt())).thenReturn(Optional.empty());

        LibroDTO resultado = libroService.crear(createRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Nuevo Libro");
        verify(libroRepository).save(any(Libro.class));
    }

    @Test
    void crear_DeberiaLanzarExcepcionSiIsbnDuplicado() {
        when(libroRepository.existsByCodigoIsbn(anyString())).thenReturn(true);

        assertThatThrownBy(() -> libroService.crear(createRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("ISBN");
    }

    @Test
    void obtenerPorId_DeberiaRetornarLibroSiExiste() {
        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));
        when(inventarioRepository.findByIdLibro(1)).thenReturn(Optional.empty());

        LibroDTO resultado = libroService.obtenerPorId(1, false);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdLibro()).isEqualTo(1);
        assertThat(resultado.getTitulo()).isEqualTo("Cien Años de Soledad");
    }

    @Test
    void obtenerPorId_DeberiaLanzarExcepcionSiNoExiste() {
        when(libroRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libroService.obtenerPorId(999, false))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerPorId_DeberiaLanzarExcepcionSiNoVisibleYModoPublico() {
        libro.setVisible(false);
        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));

        assertThatThrownBy(() -> libroService.obtenerPorId(1, false))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void obtenerPorId_DeberiaRetornarLibroNoVisibleSiModoAdmin() {
        libro.setVisible(false);
        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));
        when(inventarioRepository.findByIdLibro(1)).thenReturn(Optional.empty());

        LibroDTO resultado = libroService.obtenerPorId(1, true);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getVisible()).isFalse();
    }

    @Test
    void verificarDisponibilidad_DeberiaRetornarTrueSiHayStock() {
        Inventario inventario = Inventario.builder()
            .idLibro(1)
            .cantidadDisponible(10)
            .build();

        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));
        when(inventarioRepository.findByIdLibro(1)).thenReturn(Optional.of(inventario));

        boolean resultado = libroService.verificarDisponibilidad(1, 5);

        assertThat(resultado).isTrue();
    }

    @Test
    void verificarDisponibilidad_DeberiaRetornarFalseSiNoHayStock() {
        Inventario inventario = Inventario.builder()
            .idLibro(1)
            .cantidadDisponible(2)
            .build();

        when(libroRepository.findById(1)).thenReturn(Optional.of(libro));
        when(inventarioRepository.findByIdLibro(1)).thenReturn(Optional.of(inventario));

        boolean resultado = libroService.verificarDisponibilidad(1, 5);

        assertThat(resultado).isFalse();
    }

    @Test
    void eliminar_DeberiaEliminarSiExiste() {
        when(libroRepository.existsById(1)).thenReturn(true);
        doNothing().when(libroRepository).deleteById(1);

        libroService.eliminar(1);

        verify(libroRepository).deleteById(1);
    }

    @Test
    void eliminar_DeberiaLanzarExcepcionSiNoExiste() {
        when(libroRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> libroService.eliminar(999))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
