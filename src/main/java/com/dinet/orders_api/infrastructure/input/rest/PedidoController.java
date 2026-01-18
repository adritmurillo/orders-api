package com.dinet.orders_api.infrastructure.input.rest;


import com.dinet.orders_api.application.dto.ResumenCargaDto;
import com.dinet.orders_api.application.ports.input.CargarPedidosUseCase;
import com.dinet.orders_api.domain.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/pedidos")
public class PedidoController {
    private final CargarPedidosUseCase cargar;

    @PostMapping(value = "/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumenCargaDto> cargarPedidos(
            @RequestPart("file")MultipartFile file,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
            ){
        if (correlationId == null || correlationId.trim().isEmpty()){
            correlationId = UUID.randomUUID().toString();
        }

        log.info("Solicitud recibida - Cargar pedidos. CorrelationId: {}, Nombre archivo: {}, Tamaño archivo: {} bytes",
                correlationId, file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()){
            log.warn("Archivo vacio recibido. CorrelationId: {}", correlationId);
            throw new BadRequestException("El archivo no debe estar vacio", correlationId);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")){
            log.warn("No se pudo recibir el archivo. CorrelationId: {}. Nombre de archivo: {}", correlationId, fileName);
            throw new BadRequestException("El archivo debe ser de tipo .csv", correlationId);
        }

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()){
            log.warn("Idempotency-Key faltante o vacio. CorrelationId: {}", correlationId);
            throw new BadRequestException("El encabezado Idempotency-Key es obligatorio", correlationId);
        }

        ResumenCargaDto resumen = cargar.ejecutar(file, idempotencyKey, correlationId);

        return ResponseEntity.ok(resumen);
    }

}
