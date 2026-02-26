package com.books.catalogue.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLibroRequest {

    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;

    @Size(max = 250, message = "El ISBN no puede exceder 250 caracteres")
    private String codigoIsbn;

    @Size(max = 250, message = "El título no puede exceder 250 caracteres")
    private String titulo;

    private String descripcion;

    @DecimalMin(value = "0.00", message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;

    private Boolean visible;

    private LocalDate fechaPublicacion;

    @Min(value = 1, message = "El rating mínimo es 1")
    @Max(value = 5, message = "El rating máximo es 5")
    private Integer rating;

    private List<Integer> autorIds;

    private List<Short> categoriaIds;
}
