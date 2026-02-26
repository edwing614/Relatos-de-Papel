package com.books.catalogue.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaDTO {
    private Short idCategoria;
    private String nombre;
    private String descripcion;
}
