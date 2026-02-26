package com.books.payments.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoDTO {
    private Long idPedido;
    private Long usuario;
    private LocalDateTime fechaPedido;
    private String estado;
    private String estadoDescripcion;
    private BigDecimal total;
    private String emailContacto;
    private String nombreContacto;
    private String failureReason;
    private List<DetallePedidoDTO> detalles;
}
