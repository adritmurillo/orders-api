package com.dinet.orders_api.domain.ports.out;

import com.dinet.orders_api.domain.model.Pedido;

import java.util.List;
import java.util.Set;

public interface PedidoRepositoryPort {
    Pedido save(Pedido pedido);
    List<Pedido> saveAll(List<Pedido> pedidos);
    boolean existsByNumeroPedido(String numeroPedido);
    Set<String> existsByNumeroPedidoIn(Set<String> numerosPedido);

}
