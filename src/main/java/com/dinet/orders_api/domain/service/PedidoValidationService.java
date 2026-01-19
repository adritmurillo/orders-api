package com.dinet.orders_api.domain.service;

import com.dinet.orders_api.application.dto.PedidoCsvDto;
import com.dinet.orders_api.domain.exception.ValidationException;
import com.dinet.orders_api.domain.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PedidoValidationService {

    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");

    public Pedido validarYConvertir(
            PedidoCsvDto dto,
            Map<String, Cliente> clientes,
            Map<String, Zona> zonas,
            Set<String> pedidosExistentes
    ) throws ValidationException {
        if (!dto.getNumeroPedido().matches("^[a-zA-Z0-9]+$")) {
            throw new ValidationException(
                    "El número de pedido debe ser alfanumérico",
                    TipoError.NUMERO_PEDIDO_INVALIDO
            );
        }

        // Validar duplicado
        if (pedidosExistentes.contains(dto.getNumeroPedido())) {
            throw new ValidationException(
                    "El pedido ya existe: " + dto.getNumeroPedido(),
                    TipoError.DUPLICADO
            );
        }

        // Validar cliente
        Cliente cliente = clientes.get(dto.getClienteId());
        if (cliente == null || !cliente.isActivo()) {
            throw new ValidationException(
                    "Cliente no encontrado o inactivo: " + dto.getClienteId(),
                    TipoError.CLIENTE_NO_ENCONTRADO
            );
        }

        // Validar zona
        Zona zona = zonas.get(dto.getZonaEntrega());
        if (zona == null) {
            throw new ValidationException(
                    "Zona no encontrada: " + dto.getZonaEntrega(),
                    TipoError.ZONA_INVALIDA
            );
        }

        // Validar fecha
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(dto.getFechaEntrega());
            LocalDate hoy = LocalDate.now(LIMA_ZONE);
            if (fecha.isBefore(hoy)) {
                throw new ValidationException(
                        "La fecha ya pasó: " + dto.getFechaEntrega(),
                        TipoError.FECHA_INVALIDA
                );
            }
        } catch (Exception e) {
            throw new ValidationException(
                    "Fecha inválida: " + dto.getFechaEntrega(),
                    TipoError.FECHA_INVALIDA
            );
        }

        // Validar estado
        if (!EstadoPedido.esValido(dto.getEstado())) {
            throw new ValidationException(
                    "Estado inválido: " + dto.getEstado(),
                    TipoError.ESTADO_INVALIDO
            );
        }

        // Validar refrigeración
        boolean necesitaFrio = Boolean.parseBoolean(dto.getRequiereRefrigeracion());
        if (necesitaFrio && !zona.isSoporteRefrigeracion()) {
            throw new ValidationException(
                    "La zona no soporta refrigeración",
                    TipoError.CADENA_FRIO_NO_SOPORTADA
            );
        }

        // Crear pedido
        Pedido pedido = new Pedido();
//        pedido.setId(UUID.randomUUID());
        pedido.setNumeroPedido(dto.getNumeroPedido());
        pedido.setClienteId(dto.getClienteId());
        pedido.setZonaId(dto.getZonaEntrega());
        pedido.setFechaEntrega(fecha);
        pedido.setEstado(dto.getEstado().toUpperCase());
        pedido.setRequiereRefrigeracion(necesitaFrio);

        return pedido;
    }
}