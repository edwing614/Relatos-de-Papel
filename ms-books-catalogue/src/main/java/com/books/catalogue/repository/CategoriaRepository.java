package com.books.catalogue.repository;

import com.books.catalogue.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Short> {

    Optional<Categoria> findByNombre(String nombre);
}
