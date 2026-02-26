package com.books.payments.service.impl;

import com.books.payments.client.CatalogueClient;
import com.books.payments.dto.*;
import com.books.payments.entity.*;
import com.books.payments.exception.*;
import com.books.payments.repository.PedidoRepository;
import com.books.payments.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CatalogueClient catalogueClient;

    @Override
    public PedidoDTO crear(CreatePedidoRequest request) {
        log.info("Creando pedido para usuario {}", request.getUsuario());

        List<String> errores = new ArrayList<>();
        List<ValidatedItem> itemsValidados = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Validar cada item contra el catálogo
        for (ItemPedidoRequest item : request.getItems()) {
            try {
                DisponibilidadResponse disponibilidad = catalogueClient.verificarDisponibilidad(
                    item.getIdLibro(), item.getCantidad());

                if (!disponibilidad.isDisponible()) {
                    errores.add("Libro " + item.getIdLibro() + ": no disponible o stock insuficiente");
                    continue;
                }

                LibroDTO libro = disponibilidad.getLibro();
                if (libro == null) {
                    errores.add("Libro " + item.getIdLibro() + ": no encontrado");
                    continue;
                }

                if (!libro.getVisible()) {
                    errores.add("Libro " + item.getIdLibro() + ": no está disponible para venta");
                    continue;
                }

                BigDecimal subtotal = libro.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
                total = total.add(subtotal);

                itemsValidados.add(ValidatedItem.builder()
                    .idLibro(libro.getIdLibro())
                    .titulo(libro.getTitulo())
                    .isbn(libro.getCodigoIsbn())
                    .cantidad(item.getCantidad())
                    .precioUnitario(libro.getPrecio())
                    .subtotal(subtotal)
                    .build());

            } catch (Exception e) {
                log.error("Error validando libro {}: {}", item.getIdLibro(), e.getMessage());
                errores.add("Libro " + item.getIdLibro() + ": error de validación - " + e.getMessage());
            }
        }

        // Crear el pedido
        Pedido pedido = Pedido.builder()
            .usuario(request.getUsuario())
            .fechaPedido(LocalDateTime.now())
            .emailContacto(request.getEmailContacto())
            .nombreContacto(request.getNombreContacto())
            .total(total)
            .detalles(new ArrayList<>())
            .build();

        // Si hay errores, guardar como FALLIDO
        if (!errores.isEmpty()) {
            pedido.setEstado("F"); // Fallido
            pedido.setFailureReason(String.join("; ", errores));
            pedido = pedidoRepository.save(pedido);

            log.warn("Pedido {} creado con estado FALLIDO: {}", pedido.getIdPedido(), pedido.getFailureReason());

            throw new PaymentValidationException(
                "Validación fallida para el pedido",
                errores,
                toDTO(pedido)
            );
        }

        // Pedido exitoso
        pedido.setEstado("C"); // Completado

        // Agregar detalles
        for (ValidatedItem item : itemsValidados) {
            DetallePedido detalle = DetallePedido.builder()
                .idLibro(item.getIdLibro())
                .libroTitulo(item.getTitulo())
                .libroIsbn(item.getIsbn())
                .cantidad(item.getCantidad().shortValue())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();
            pedido.addDetalle(detalle);
        }

        pedido = pedidoRepository.save(pedido);
        log.info("Pedido {} creado exitosamente con total {}", pedido.getIdPedido(), pedido.getTotal());

        // Decrementar stock en catálogo
        for (ValidatedItem item : itemsValidados) {
            try {
                catalogueClient.decrementarStock(item.getIdLibro(), item.getCantidad());
            } catch (Exception e) {
                log.warn("No se pudo decrementar stock del libro {}: {}",
                    item.getIdLibro(), e.getMessage());
            }
        }

        return toDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoDTO obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        return toDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listar(Pageable pageable) {
        return pedidoRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoDTO> listarPorUsuario(Long usuario, Pageable pageable) {
        return pedidoRepository.findByUsuario(usuario, pageable).map(this::toDTO);
    }

    private PedidoDTO toDTO(Pedido pedido) {
        return PedidoDTO.builder()
            .idPedido(pedido.getIdPedido())
            .usuario(pedido.getUsuario())
            .fechaPedido(pedido.getFechaPedido())
            .estado(pedido.getEstado())
            .estadoDescripcion(getEstadoDescripcion(pedido.getEstado()))
            .total(pedido.getTotal())
            .emailContacto(pedido.getEmailContacto())
            .nombreContacto(pedido.getNombreContacto())
            .failureReason(pedido.getFailureReason())
            .detalles(pedido.getDetalles().stream()
                .map(d -> DetallePedidoDTO.builder()
                    .idDetalle(d.getIdDetalle())
                    .idLibro(d.getIdLibro())
                    .libroTitulo(d.getLibroTitulo())
                    .libroIsbn(d.getLibroIsbn())
                    .cantidad(d.getCantidad().intValue())
                    .precioUnitario(d.getPrecioUnitario())
                    .subtotal(d.getSubtotal())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    private String getEstadoDescripcion(String estado) {
        return switch (estado) {
            case "P" -> "Pendiente";
            case "C" -> "Completado";
            case "F" -> "Fallido";
            case "X" -> "Cancelado";
            default -> "Desconocido";
        };
    }

    @lombok.Data
    @lombok.Builder
    private static class ValidatedItem {
        private Integer idLibro;
        private String titulo;
        private String isbn;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
