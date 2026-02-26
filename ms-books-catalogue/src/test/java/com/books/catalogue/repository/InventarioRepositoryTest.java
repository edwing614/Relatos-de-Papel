package com.books.catalogue.repository;

import com.books.catalogue.entity.Inventario;
import com.books.catalogue.entity.Libro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InventarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventarioRepository inventarioRepository;

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = Libro.builder()
            .codigo("INV001")
            .codigoIsbn("978-INV-TEST")
            .titulo("Libro para Inventario")
            .precio(new BigDecimal("20000.00"))
            .visible(true)
            .rating(3)
            .build();
        libro = entityManager.persistAndFlush(libro);
    }

    @Test
    void findByIdLibro_DeberiaRetornarInventario_CuandoExiste() {
        Inventario inventario = Inventario.builder()
            .idLibro(libro.getIdLibro())
            .cantidadDisponible(50)
            .fechaActualizacion(LocalDateTime.now())
            .build();
        entityManager.persistAndFlush(inventario);

        Optional<Inventario> encontrado = inventarioRepository.findByIdLibro(libro.getIdLibro());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getCantidadDisponible()).isEqualTo(50);
    }

    @Test
    void findByIdLibro_DeberiaRetornarVacio_CuandoNoExiste() {
        Optional<Inventario> encontrado = inventarioRepository.findByIdLibro(9999);

        assertThat(encontrado).isEmpty();
    }

    @Test
    void decrementarStock_DeberiaDecrementarCantidad_CuandoHaySuficienteStock() {
        Inventario inventario = Inventario.builder()
            .idLibro(libro.getIdLibro())
            .cantidadDisponible(10)
            .fechaActualizacion(LocalDateTime.now())
            .build();
        entityManager.persistAndFlush(inventario);
        entityManager.clear();

        int updated = inventarioRepository.decrementarStock(libro.getIdLibro(), 3);

        assertThat(updated).isEqualTo(1);

        Optional<Inventario> actualizado = inventarioRepository.findByIdLibro(libro.getIdLibro());
        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getCantidadDisponible()).isEqualTo(7);
    }

    @Test
    void decrementarStock_DeberiaRetornarCero_CuandoNoHaySuficienteStock() {
        Inventario inventario = Inventario.builder()
            .idLibro(libro.getIdLibro())
            .cantidadDisponible(5)
            .fechaActualizacion(LocalDateTime.now())
            .build();
        entityManager.persistAndFlush(inventario);

        int updated = inventarioRepository.decrementarStock(libro.getIdLibro(), 10);

        assertThat(updated).isEqualTo(0);
    }
}
