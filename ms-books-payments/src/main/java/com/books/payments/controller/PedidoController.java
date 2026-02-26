package com.books.payments.controller;

import com.books.payments.client.CatalogueClient;
import com.books.payments.dto.*;
import com.books.payments.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "API para gestión de pedidos/compras de libros")
public class PedidoController {

    private final PedidoService pedidoService;
    private final CatalogueClient catalogueClient;

    @PostMapping
    @Operation(
        summary = "Crear un nuevo pedido",
        description = "Crea un pedido validando disponibilidad y stock contra el servicio de catálogo vía Eureka"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación - algunos libros no disponibles")
    })
    public ResponseEntity<PedidoDTO> crear(@Valid @RequestBody CreatePedidoRequest request) {
        PedidoDTO pedido = pedidoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID", description = "Obtiene los detalles de un pedido específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<PedidoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos", description = "Lista paginada de todos los pedidos")
    public ResponseEntity<Page<PedidoDTO>> listar(
        @PageableDefault(size = 10, sort = "fechaPedido") Pageable pageable
    ) {
        return ResponseEntity.ok(pedidoService.listar(pageable));
    }

    @GetMapping("/usuario/{usuario}")
    @Operation(summary = "Listar pedidos por usuario", description = "Lista paginada de pedidos de un usuario específico")
    public ResponseEntity<Page<PedidoDTO>> listarPorUsuario(
        @PathVariable Long usuario,
        @PageableDefault(size = 10, sort = "fechaPedido") Pageable pageable
    ) {
        return ResponseEntity.ok(pedidoService.listarPorUsuario(usuario, pageable));
    }

    @GetMapping("/balanceo-test")
    @Operation(
        summary = "Test de balanceo",
        description = "Llama a catalogue via Eureka para demostrar que el balanceo funciona (muestra qué instancia atendió)"
    )
    public ResponseEntity<Map<String, Object>> testBalanceo() {
        return ResponseEntity.ok(catalogueClient.obtenerInfoInstancia());
    }
}
