package com.books.payments.service;

import com.books.payments.client.CatalogueClient;
import com.books.payments.dto.*;
import com.books.payments.entity.Pedido;
import com.books.payments.exception.*;
import com.books.payments.repository.PedidoRepository;
import com.books.payments.service.impl.PedidoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CatalogueClient catalogueClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private CreatePedidoRequest createRequest;
    private LibroDTO libroDTO;
    private DisponibilidadResponse disponibilidadOk;

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
            .build();

        disponibilidadOk = DisponibilidadResponse.builder()
            .disponible(true)
            .libro(libroDTO)
            .build();

        createRequest = CreatePedidoRequest.builder()
            .usuario(1L)
            .emailContacto("test@test.com")
            .nombreContacto("Test User")
            .items(List.of(
                ItemPedidoRequest.builder()
                    .idLibro(1)
                    .cantidad(2)
                    .build()
            ))
            .build();
    }

    @Test
    void crear_DeberiaCrearPedidoExitosamente() {
        when(catalogueClient.verificarDisponibilidad(1, 2)).thenReturn(disponibilidadOk);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setIdPedido(1L);
            return p;
        });
        doNothing().when(catalogueClient).decrementarStock(anyInt(), anyInt());

        PedidoDTO resultado = pedidoService.crear(createRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo("C"); // Completado
        assertThat(resultado.getTotal()).isEqualByComparingTo(new BigDecimal("90000.00")); // 45000 * 2
        verify(catalogueClient).decrementarStock(1, 2);
    }

    @Test
    void crear_DeberiaFallarSiLibroNoDisponible() {
        DisponibilidadResponse noDisponible = DisponibilidadResponse.builder()
            .disponible(false)
            .libro(null)
            .build();

        when(catalogueClient.verificarDisponibilidad(1, 2)).thenReturn(noDisponible);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setIdPedido(1L);
            return p;
        });

        assertThatThrownBy(() -> pedidoService.crear(createRequest))
            .isInstanceOf(PaymentValidationException.class);
    }

    @Test
    void crear_DeberiaFallarSiLibroNoVisible() {
        libroDTO.setVisible(false);
        DisponibilidadResponse disponiblePeroOculto = DisponibilidadResponse.builder()
            .disponible(true)
            .libro(libroDTO)
            .build();

        when(catalogueClient.verificarDisponibilidad(1, 2)).thenReturn(disponiblePeroOculto);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido p = invocation.getArgument(0);
            p.setIdPedido(1L);
            return p;
        });

        assertThatThrownBy(() -> pedidoService.crear(createRequest))
            .isInstanceOf(PaymentValidationException.class);
    }

    @Test
    void obtenerPorId_DeberiaRetornarPedido() {
        Pedido pedido = Pedido.builder()
            .idPedido(1L)
            .usuario(1L)
            .estado("C")
            .total(new BigDecimal("90000.00"))
            .fechaPedido(LocalDateTime.now())
            .detalles(new ArrayList<>())
            .build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoDTO resultado = pedidoService.obtenerPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getIdPedido()).isEqualTo(1L);
        assertThat(resultado.getEstado()).isEqualTo("C");
    }

    @Test
    void obtenerPorId_DeberiaLanzarExcepcionSiNoExiste() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPorId(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
