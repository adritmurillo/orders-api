package com.dinet.orders_api.infrastructure.output.persistence.adapter;

import com.dinet.orders_api.domain.model.Zona;
import com.dinet.orders_api.domain.ports.out.ZonaRepositoryPort;
import com.dinet.orders_api.infrastructure.output.persistence.mapper.ZonaMapper;
import com.dinet.orders_api.infrastructure.output.persistence.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaZonaAdapter implements ZonaRepositoryPort {
    private final ZonaRepository repo;
    private final ZonaMapper mapper;

    @Override
    public Optional<Zona> findById(String id) {
        return repo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Zona> findAll() {
        return repo.findAll().stream().map(mapper :: toDomain).collect(Collectors.toList());
    }
}
