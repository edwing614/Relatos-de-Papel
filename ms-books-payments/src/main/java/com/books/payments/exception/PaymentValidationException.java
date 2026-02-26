package com.books.payments.exception;

import com.books.payments.dto.PedidoDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentValidationException extends RuntimeException {

    private final List<String> errors;
    private final PedidoDTO pedidoFallido;

    public PaymentValidationException(String message, List<String> errors, PedidoDTO pedidoFallido) {
        super(message);
        this.errors = errors;
        this.pedidoFallido = pedidoFallido;
    }
}
