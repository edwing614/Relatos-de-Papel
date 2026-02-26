package com.books.payments.client;

import com.books.payments.dto.LibroDTO;
import com.books.payments.dto.DisponibilidadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Cliente HTTP para comunicarse con ms-books-catalogue usando Service Discovery.
 * Usa WebClient con @LoadBalanced para resolver el servicio por nombre.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogueClient {

    private final WebClient.Builder webClientBuilder;

    private static final String CATALOGUE_SERVICE = "http://ms-books-catalogue";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    /**
     * Verifica la disponibilidad de un libro.
     */
    public DisponibilidadResponse verificarDisponibilidad(Integer idLibro, Integer cantidad) {
        log.info("Verificando disponibilidad del libro {} con cantidad {}", idLibro, cantidad);

        try {
            return webClientBuilder.build()
                .get()
                .uri(CATALOGUE_SERVICE + "/libros/internal/{id}/disponibilidad?cantidad={cantidad}",
                    idLibro, cantidad)
                .retrieve()
                .bodyToMono(DisponibilidadResponse.class)
                .timeout(TIMEOUT)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Libro {} no encontrado", idLibro);
            return DisponibilidadResponse.builder()
                .disponible(false)
                .build();
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad del libro {}: {}", idLibro, e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de catálogo", e);
        }
    }

    /**
     * Obtiene información de un libro.
     */
    public LibroDTO obtenerLibro(Integer idLibro) {
        log.info("Obteniendo información del libro {}", idLibro);

        try {
            return webClientBuilder.build()
                .get()
                .uri(CATALOGUE_SERVICE + "/libros/{id}", idLibro)
                .retrieve()
                .bodyToMono(LibroDTO.class)
                .timeout(TIMEOUT)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Libro {} no encontrado", idLibro);
            return null;
        } catch (Exception e) {
            log.error("Error al obtener libro {}: {}", idLibro, e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de catálogo", e);
        }
    }

    /**
     * Decrementa el stock de un libro después de una compra exitosa.
     */
    public void decrementarStock(Integer idLibro, Integer cantidad) {
        log.info("Decrementando stock del libro {} en {}", idLibro, cantidad);

        try {
            webClientBuilder.build()
                .post()
                .uri(CATALOGUE_SERVICE + "/libros/internal/{id}/decrementar-stock?cantidad={cantidad}",
                    idLibro, cantidad)
                .retrieve()
                .toBodilessEntity()
                .timeout(TIMEOUT)
                .block();
        } catch (Exception e) {
            log.error("Error al decrementar stock del libro {}: {}", idLibro, e.getMessage());
            // No lanzamos excepción para no afectar el pedido ya creado
        }
    }

    /**
     * Obtiene info de la instancia de catalogue que atiende (para demostrar balanceo).
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerInfoInstancia() {
        log.info("Consultando instancia de catalogue (balanceo)");
        try {
            return webClientBuilder.build()
                .get()
                .uri(CATALOGUE_SERVICE + "/libros/info")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(TIMEOUT)
                .block();
        } catch (Exception e) {
            log.error("Error al obtener info de instancia: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
