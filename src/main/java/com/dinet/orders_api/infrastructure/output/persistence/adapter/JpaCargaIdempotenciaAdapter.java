package com.dinet.orders_api.infrastructure.output.persistence.adapter;

import com.dinet.orders_api.domain.model.CargaIdempotencia;
import com.dinet.orders_api.domain.ports.out.CargaIdempotenciaPort;
import com.dinet.orders_api.infrastructure.output.persistence.entity.CargaIdempotenciaEntity;
import com.dinet.orders_api.infrastructure.output.persistence.mapper.CargaIdempotenciaMapper;
import com.dinet.orders_api.infrastructure.output.persistence.repository.CargaIdempotenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaCargaIdempotenciaAdapter implements CargaIdempotenciaPort {
    private final CargaIdempotenciaRepository repo;
    private final CargaIdempotenciaMapper mapper;

    @Override
    public CargaIdempotencia save(CargaIdempotencia cargaIdempotencia) {
        CargaIdempotenciaEntity entity = mapper.toEntity(cargaIdempotencia);
        CargaIdempotenciaEntity savedEntity = repo.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CargaIdempotencia> findByArchivoHash(String archivoHash) {
        return repo.findByArchivoHash(archivoHash).map(mapper :: toDomain);
    }

    @Override
    public Optional<CargaIdempotencia> findByIdempotencyKeyAndArchivoHash(String idempotencyKey, String archivoHash) {
        return repo.findByIdempotencyKeyAndArchivoHash(idempotencyKey, archivoHash).map(mapper :: toDomain);
    }
}
