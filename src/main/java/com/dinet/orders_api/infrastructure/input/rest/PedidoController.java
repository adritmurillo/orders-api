package com.dinet.orders_api.infrastructure.input.rest;

import com.dinet.orders_api.application.dto.ResumenCargaDto;
import com.dinet.orders_api.application.ports.input.CargarPedidosUseCase;
import com.dinet.orders_api.domain.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos")
public class PedidoController {

    private final CargarPedidosUseCase cargarPedidosUseCase;

    @PostMapping(value = "/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cargar pedidos desde CSV")
    public ResponseEntity<ResumenCargaDto> cargarPedidos(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId
    ) {
        // Generar correlationId si no viene
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        log.info("Recibiendo archivo: {}, CorrelationId: {}", file.getOriginalFilename(), correlationId);

        // Validar archivo vacío
        if (file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío", correlationId);
        }

        // Validar extensión CSV
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("El archivo debe ser CSV", correlationId);
        }

        // Validar Idempotency-Key
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new BadRequestException("Falta el header Idempotency-Key", correlationId);
        }

        // Ejecutar caso de uso
        ResumenCargaDto resumen = cargarPedidosUseCase.ejecutar(file, idempotencyKey, correlationId);

        log.info("Carga completada. Procesados: {}, Guardados: {}, Errores: {}",
                resumen.getTotalProcesados(), resumen.getGuardados(), resumen.getConError());

        return ResponseEntity.ok(resumen);
    }

}