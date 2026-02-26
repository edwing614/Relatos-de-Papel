package com.books.catalogue.repository;

import com.books.catalogue.entity.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LibroRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LibroRepository libroRepository;

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = Libro.builder()
            .codigo("TEST001")
            .codigoIsbn("978-TEST-ISBN")
            .titulo("Libro de Prueba")
            .descripcion("Descripción de prueba")
            .precio(new BigDecimal("35000.00"))
            .visible(true)
            .fechaPublicacion(LocalDate.of(2023, 1, 15))
            .rating(4)
            .build();
    }

    @Test
    void findByCodigoIsbn_DeberiaRetornarLibro_CuandoExiste() {
        entityManager.persistAndFlush(libro);

        Optional<Libro> encontrado = libroRepository.findByCodigoIsbn("978-TEST-ISBN");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo("Libro de Prueba");
        assertThat(encontrado.get().getRating()).isEqualTo(4);
    }

    @Test
    void findByCodigoIsbn_DeberiaRetornarVacio_CuandoNoExiste() {
        Optional<Libro> encontrado = libroRepository.findByCodigoIsbn("ISBN-INEXISTENTE");

        assertThat(encontrado).isEmpty();
    }

    @Test
    void existsByCodigoIsbn_DeberiaRetornarTrue_CuandoExiste() {
        entityManager.persistAndFlush(libro);

        boolean existe = libroRepository.existsByCodigoIsbn("978-TEST-ISBN");

        assertThat(existe).isTrue();
    }

    @Test
    void existsByCodigo_DeberiaRetornarFalse_CuandoNoExiste() {
        boolean existe = libroRepository.existsByCodigo("CODIGO-INEXISTENTE");

        assertThat(existe).isFalse();
    }

    @Test
    void save_DeberiaGuardarLibroConRating() {
        libro.setRating(5);

        Libro guardado = libroRepository.save(libro);

        assertThat(guardado.getIdLibro()).isNotNull();
        assertThat(guardado.getRating()).isEqualTo(5);
    }

    @Test
    void findById_DeberiaRetornarLibroConTodosLosCampos() {
        Libro persistido = entityManager.persistAndFlush(libro);

        Optional<Libro> encontrado = libroRepository.findById(persistido.getIdLibro());

        assertThat(encontrado).isPresent();
        Libro l = encontrado.get();
        assertThat(l.getCodigo()).isEqualTo("TEST001");
        assertThat(l.getCodigoIsbn()).isEqualTo("978-TEST-ISBN");
        assertThat(l.getTitulo()).isEqualTo("Libro de Prueba");
        assertThat(l.getPrecio()).isEqualByComparingTo(new BigDecimal("35000.00"));
        assertThat(l.getVisible()).isTrue();
        assertThat(l.getRating()).isEqualTo(4);
    }
}
