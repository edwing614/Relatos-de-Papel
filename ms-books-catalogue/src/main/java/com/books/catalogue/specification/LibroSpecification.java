package com.books.catalogue.specification;

import com.books.catalogue.dto.LibroSearchCriteria;
import com.books.catalogue.entity.Autor;
import com.books.catalogue.entity.Categoria;
import com.books.catalogue.entity.Libro;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LibroSpecification {

    public static Specification<Libro> withCriteria(LibroSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Título (búsqueda parcial)
            if (criteria.getTitulo() != null && !criteria.getTitulo().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("titulo")),
                    "%" + criteria.getTitulo().toLowerCase() + "%"));
            }

            // Autor (búsqueda por nombre del autor)
            if (criteria.getAutor() != null && !criteria.getAutor().isBlank()) {
                Join<Libro, Autor> autorJoin = root.join("autores", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(autorJoin.get("nombre")),
                    "%" + criteria.getAutor().toLowerCase() + "%"));
            }

            // Categoría (búsqueda por nombre de la categoría)
            if (criteria.getCategoria() != null && !criteria.getCategoria().isBlank()) {
                Join<Libro, Categoria> categoriaJoin = root.join("categorias", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(categoriaJoin.get("nombre")),
                    "%" + criteria.getCategoria().toLowerCase() + "%"));
            }

            // ISBN exacto
            if (criteria.getCodigoIsbn() != null && !criteria.getCodigoIsbn().isBlank()) {
                predicates.add(cb.equal(root.get("codigoIsbn"), criteria.getCodigoIsbn()));
            }

            // Precio mínimo
            if (criteria.getPrecioMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), criteria.getPrecioMin()));
            }

            // Precio máximo
            if (criteria.getPrecioMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), criteria.getPrecioMax()));
            }

            // Visible
            if (criteria.getVisible() != null) {
                predicates.add(cb.equal(root.get("visible"), criteria.getVisible()));
            }

            // Fecha publicación desde
            if (criteria.getFechaPublicacionDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaPublicacion"),
                    criteria.getFechaPublicacionDesde()));
            }

            // Fecha publicación hasta
            if (criteria.getFechaPublicacionHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaPublicacion"),
                    criteria.getFechaPublicacionHasta()));
            }

            // Rating mínimo
            if (criteria.getRatingMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), criteria.getRatingMin()));
            }

            // Rating máximo
            if (criteria.getRatingMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), criteria.getRatingMax()));
            }

            // Eliminar duplicados si hay joins
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Libro> visibleOnly() {
        return (root, query, cb) -> cb.equal(root.get("visible"), true);
    }
}
