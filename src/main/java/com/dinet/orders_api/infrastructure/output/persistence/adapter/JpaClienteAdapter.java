package com.dinet.orders_api.infrastructure.output.persistence.adapter;

import com.dinet.orders_api.domain.model.Cliente;
import com.dinet.orders_api.domain.ports.out.ClienteRepositoryPort;
import com.dinet.orders_api.infrastructure.output.persistence.mapper.ClienteMapper;
import com.dinet.orders_api.infrastructure.output.persistence.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaClienteAdapter implements ClienteRepositoryPort {
    private final ClienteRepository repo;
    private final ClienteMapper mapper;
    @Override
    public Optional<Cliente> findById(String id) {
        return repo.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Cliente> findAllActivos() {
        return repo.findByActivoTrue().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
