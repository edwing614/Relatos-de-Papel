package com.books.payments.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePedidoRequest {

    @NotNull(message = "El usuario es obligatorio")
    private Long usuario;

    @Email(message = "Email de contacto inválido")
    private String emailContacto;

    private String nombreContacto;

    @NotEmpty(message = "Debe incluir al menos un item")
    @Valid
    private List<ItemPedidoRequest> items;
}
