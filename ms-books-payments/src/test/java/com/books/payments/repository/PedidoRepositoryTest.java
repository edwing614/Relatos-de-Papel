package com.books.payments.repository;

import com.books.payments.entity.DetallePedido;
import com.books.payments.entity.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PedidoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = Pedido.builder()
            .usuario(1L)
            .fechaPedido(LocalDateTime.now())
            .estado("C")
            .total(new BigDecimal("90000.00"))
            .emailContacto("test@test.com")
            .nombreContacto("Usuario Test")
            .detalles(new ArrayList<>())
            .build();

        DetallePedido detalle = DetallePedido.builder()
            .idLibro(1)
            .libroTitulo("Libro Test")
            .libroIsbn("978-TEST")
            .cantidad((short) 2)
            .precioUnitario(new BigDecimal("45000.00"))
            .subtotal(new BigDecimal("90000.00"))
            .build();

        pedido.addDetalle(detalle);
    }

    @Test
    void save_DeberiaGuardarPedidoConDetalles() {
        Pedido guardado = pedidoRepository.save(pedido);

        assertThat(guardado.getIdPedido()).isNotNull();
        assertThat(guardado.getDetalles()).hasSize(1);
        assertThat(guardado.getTotal()).isEqualByComparingTo(new BigDecimal("90000.00"));
    }

    @Test
    void findById_DeberiaRetornarPedidoConDetalles() {
        Pedido persistido = entityManager.persistAndFlush(pedido);

        Optional<Pedido> encontrado = pedidoRepository.findById(persistido.getIdPedido());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEstado()).isEqualTo("C");
        assertThat(encontrado.get().getDetalles()).hasSize(1);
    }

    @Test
    void findByUsuario_DeberiaRetornarPedidosDelUsuario() {
        entityManager.persistAndFlush(pedido);

        Pedido otroPedido = Pedido.builder()
            .usuario(1L)
            .fechaPedido(LocalDateTime.now())
            .estado("F")
            .total(new BigDecimal("50000.00"))
            .detalles(new ArrayList<>())
            .build();
        entityManager.persistAndFlush(otroPedido);

        Page<Pedido> pedidos = pedidoRepository.findByUsuario(1L, PageRequest.of(0, 10));

        assertThat(pedidos.getTotalElements()).isEqualTo(2);
    }

    @Test
    void findByEstado_DeberiaFiltrarPorEstado() {
        entityManager.persistAndFlush(pedido);

        Pedido pedidoFallido = Pedido.builder()
            .usuario(2L)
            .fechaPedido(LocalDateTime.now())
            .estado("F")
            .total(BigDecimal.ZERO)
            .failureReason("Stock insuficiente")
            .detalles(new ArrayList<>())
            .build();
        entityManager.persistAndFlush(pedidoFallido);

        Page<Pedido> completados = pedidoRepository.findByEstado("C", PageRequest.of(0, 10));
        Page<Pedido> fallidos = pedidoRepository.findByEstado("F", PageRequest.of(0, 10));

        assertThat(completados.getTotalElements()).isEqualTo(1);
        assertThat(fallidos.getTotalElements()).isEqualTo(1);
        assertThat(fallidos.getContent().get(0).getFailureReason()).isEqualTo("Stock insuficiente");
    }
}
