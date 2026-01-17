package com.dinet.orders_api.domain.ports.out;

import com.dinet.orders_api.domain.model.Cliente;

import java.util.Optional;

public interface ClienteRepositoryPort {
    Optional<Cliente> findById(String id);
}
