package com.books.catalogue.repository;

import com.books.catalogue.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    @Query("SELECT i FROM Inventario i WHERE i.idLibro = :idLibro")
    Optional<Inventario> findByIdLibro(@Param("idLibro") Integer idLibro);

    @Modifying
    @Query("UPDATE Inventario i SET i.cantidadDisponible = i.cantidadDisponible - :cantidad WHERE i.idLibro = :idLibro AND i.cantidadDisponible >= :cantidad")
    int decrementarStock(@Param("idLibro") Integer idLibro, @Param("cantidad") Integer cantidad);
}
