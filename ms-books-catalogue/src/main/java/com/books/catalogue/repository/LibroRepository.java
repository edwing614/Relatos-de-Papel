package com.books.catalogue.repository;

import com.books.catalogue.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Integer>, JpaSpecificationExecutor<Libro> {

    Optional<Libro> findByCodigoIsbn(String codigoIsbn);

    Optional<Libro> findByCodigo(String codigo);

    boolean existsByCodigoIsbn(String codigoIsbn);

    boolean existsByCodigo(String codigo);
}
