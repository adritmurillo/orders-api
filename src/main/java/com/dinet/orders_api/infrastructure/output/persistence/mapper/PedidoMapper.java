package com.dinet.orders_api.infrastructure.output.persistence.mapper;

import com.dinet.orders_api.domain.model.Pedido;
import com.dinet.orders_api.infrastructure.output.persistence.entity.PedidoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PedidoMapper {
    Pedido toDomain(PedidoEntity pedidoEntity);
    PedidoEntity toEntity(Pedido pedidoDomain);
}
