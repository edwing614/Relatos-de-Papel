package com.books.catalogue.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadResponse {
    private boolean disponible;
    private LibroDTO libro;
}
