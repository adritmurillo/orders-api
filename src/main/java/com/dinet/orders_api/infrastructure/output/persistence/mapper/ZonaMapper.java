package com.dinet.orders_api.infrastructure.output.persistence.mapper;

import com.dinet.orders_api.domain.model.Zona;
import com.dinet.orders_api.infrastructure.output.persistence.entity.ZonaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ZonaMapper {
    ZonaEntity toEntity(Zona zonaDomain);
    Zona toDomain(ZonaEntity zonaEntity);
}
