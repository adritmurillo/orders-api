package com.dinet.orders_api.infrastructure.output.persistence.mapper;

import com.dinet.orders_api.domain.model.Cliente;
import com.dinet.orders_api.infrastructure.output.persistence.entity.ClienteEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClienteMapper {
    ClienteEntity toEntity(Cliente clienteDomain);
    Cliente toDomain(ClienteEntity clienteEntity);
}
