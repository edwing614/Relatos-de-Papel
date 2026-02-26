package com.books.payments.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "usuario", nullable = false)
    private Long usuario;

    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @Column(name = "estado", nullable = false, length = 1)
    private String estado; // P=Pendiente, C=Completado, F=Fallido, X=Cancelado

    @Column(name = "total", nullable = false, precision = 13, scale = 2)
    private BigDecimal total;

    @Column(name = "email_contacto", length = 250)
    private String emailContacto;

    @Column(name = "nombre_contacto", length = 250)
    private String nombreContacto;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetallePedido> detalles = new ArrayList<>();

    public void addDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }
}
