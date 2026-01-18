package com.dinet.orders_api.infrastructure.output.persistence.repository;

import com.dinet.orders_api.infrastructure.output.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity , UUID> {
    boolean existsByNumeroPedido(String numeroPedido);

    @Query("select p.numeroPedido from PedidoEntity p where p.numeroPedido in :numerosPedido")
    Set<String> findExistingNumeroPedido(@Param("numerosPedido") Set<String> numerosPedido);
}
