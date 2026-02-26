package com.books.payments.service;

import com.books.payments.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoService {

    PedidoDTO crear(CreatePedidoRequest request);

    PedidoDTO obtenerPorId(Long id);

    Page<PedidoDTO> listar(Pageable pageable);

    Page<PedidoDTO> listarPorUsuario(Long usuario, Pageable pageable);
}
