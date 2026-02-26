package com.books.catalogue.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorDTO {
    private Integer idAutor;
    private String nombre;
    private String pais;
}
