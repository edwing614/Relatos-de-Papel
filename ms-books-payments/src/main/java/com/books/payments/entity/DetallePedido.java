package com.books.payments.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "id_libro", nullable = false)
    private Integer idLibro;

    @Column(name = "libro_titulo", length = 250)
    private String libroTitulo;

    @Column(name = "libro_isbn", length = 250)
    private String libroIsbn;

    @Column(name = "cantidad", nullable = false)
    private Short cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 13, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 13, scale = 2)
    private BigDecimal subtotal;
}
