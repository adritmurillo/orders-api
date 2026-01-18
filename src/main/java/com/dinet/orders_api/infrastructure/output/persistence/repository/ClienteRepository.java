package com.dinet.orders_api.infrastructure.output.persistence.repository;

import com.dinet.orders_api.infrastructure.output.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, String> {
    List<ClienteEntity> findByActivoTrue();
}
