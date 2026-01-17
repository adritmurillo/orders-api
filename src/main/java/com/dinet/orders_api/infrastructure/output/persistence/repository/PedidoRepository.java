package com.dinet.orders_api.infrastructure.output.persistence.repository;

import com.dinet.orders_api.infrastructure.output.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity , UUID> {
    boolean existsByNumeroPedido(String numeroPedido);
}
