package com.dinet.orders_api.infrastructure.output.persistence.repository;

import com.dinet.orders_api.infrastructure.output.persistence.entity.CargaIdempotenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CargaIdempotenciaRepository extends JpaRepository<CargaIdempotenciaEntity, UUID> {
    Optional<CargaIdempotenciaEntity> findByArchivoHash(String archivoHash);
}
