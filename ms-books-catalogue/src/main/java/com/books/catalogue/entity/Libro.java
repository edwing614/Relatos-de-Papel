package com.books.catalogue.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "libro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Integer idLibro;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "codigo_isbn", length = 250, unique = true)
    private String codigoIsbn;

    @Column(name = "titulo", nullable = false, length = 250)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 13, scale = 2)
    private BigDecimal precio;

    @Column(name = "visible", nullable = false)
    private Boolean visible;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Column(name = "rating")
    private Integer rating;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "libro_autor",
        joinColumns = @JoinColumn(name = "id_libro"),
        inverseJoinColumns = @JoinColumn(name = "id_autor")
    )
    @Builder.Default
    private Set<Autor> autores = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "libro_categoria",
        joinColumns = @JoinColumn(name = "id_libro"),
        inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    @Builder.Default
    private Set<Categoria> categorias = new HashSet<>();
}
