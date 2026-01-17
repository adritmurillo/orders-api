package com.dinet.orders_api.domain.ports.out;

import com.dinet.orders_api.domain.model.CargaIdempotencia;

import java.util.Optional;

public interface CargaIdempotenciaPort {
    CargaIdempotencia save(CargaIdempotencia cargaIdempotencia);
    Optional<CargaIdempotencia> findByArchivoHash(String archivoHash);  
}
