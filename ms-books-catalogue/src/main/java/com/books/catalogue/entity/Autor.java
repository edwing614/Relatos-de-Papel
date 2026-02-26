package com.books.catalogue.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "autor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autor")
    private Integer idAutor;

    @Column(name = "nombre", nullable = false, length = 250)
    private String nombre;

    @Column(name = "pais", length = 250)
    private String pais;
}
