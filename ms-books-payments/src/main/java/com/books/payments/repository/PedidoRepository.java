package com.books.payments.repository;

import com.books.payments.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findByUsuario(Long usuario, Pageable pageable);

    Page<Pedido> findByEstado(String estado, Pageable pageable);
}
