package com.books.payments.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedidoRequest {

    @NotNull(message = "El ID del libro es obligatorio")
    private Integer idLibro;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    @Max(value = 100, message = "La cantidad máxima es 100")
    private Integer cantidad;
}
