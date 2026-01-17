package com.dinet.orders_api.domain.ports.out;

import com.dinet.orders_api.domain.model.Pedido;

public interface PedidoRepositoryPort {
    Pedido save(Pedido pedido);
    boolean existsByNumeroPedido(String numeroPedido); // Para verificar si el numero de pedido ya existe
}
