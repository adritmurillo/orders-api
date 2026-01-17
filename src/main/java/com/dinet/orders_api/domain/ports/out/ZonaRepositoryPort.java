package com.dinet.orders_api.domain.ports.out;

import com.dinet.orders_api.domain.model.Zona;

import java.util.Optional;

public interface ZonaRepositoryPort {
    Optional<Zona> findById(String id);
}
