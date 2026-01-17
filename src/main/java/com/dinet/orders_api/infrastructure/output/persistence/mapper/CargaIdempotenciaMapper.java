package com.dinet.orders_api.infrastructure.output.persistence.mapper;

import com.dinet.orders_api.domain.model.CargaIdempotencia;
import com.dinet.orders_api.infrastructure.output.persistence.entity.CargaIdempotenciaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CargaIdempotenciaMapper {
    CargaIdempotenciaEntity toEntity(CargaIdempotencia cargaIdempotencia);
    CargaIdempotencia toDomain(CargaIdempotenciaEntity cargaIdempotenciaEntity);
}
