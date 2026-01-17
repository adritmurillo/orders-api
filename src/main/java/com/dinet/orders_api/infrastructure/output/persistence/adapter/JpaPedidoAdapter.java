package com.dinet.orders_api.infrastructure.output.persistence.adapter;

import com.dinet.orders_api.domain.model.Pedido;
import com.dinet.orders_api.domain.ports.out.PedidoRepositoryPort;
import com.dinet.orders_api.infrastructure.output.persistence.entity.PedidoEntity;
import com.dinet.orders_api.infrastructure.output.persistence.mapper.PedidoMapper;
import com.dinet.orders_api.infrastructure.output.persistence.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaPedidoAdapter implements PedidoRepositoryPort {
    private final PedidoRepository repo;
    private final PedidoMapper mapper;

    @Override
    public Pedido save(Pedido pedido) {
        PedidoEntity entity = mapper.toEntity(pedido);
        PedidoEntity savedEntity = repo.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByNumeroPedido(String numeroPedido) {
        return repo.existsByNumeroPedido(numeroPedido);
    }
}
