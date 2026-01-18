package com.dinet.orders_api.application.ports.input;

import com.dinet.orders_api.application.dto.ResumenCargaDto;
import org.springframework.web.multipart.MultipartFile;

public interface CargarPedidosUseCase {
    ResumenCargaDto ejecutar(MultipartFile archivo, String idempotencyKey, String correlationId);
}
