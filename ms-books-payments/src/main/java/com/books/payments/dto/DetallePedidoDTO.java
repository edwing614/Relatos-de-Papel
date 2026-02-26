package com.books.payments.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoDTO {
    private Integer idDetalle;
    private Integer idLibro;
    private String libroTitulo;
    private String libroIsbn;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
