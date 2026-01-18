package com.dinet.orders_api.domain.service;

import com.dinet.orders_api.application.dto.PedidoCsvDto;
import com.dinet.orders_api.domain.exception.ValidationException;
import com.dinet.orders_api.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class PedidoValidationService {
    private static final ZoneId LIMA_ZONE = ZoneId.of("America/Lima");
    private static final String ALFANUMERICO_REGEX = "^[a-zA-Z0-9]+$";

    public Pedido validarYConvertir(
            PedidoCsvDto dto,
            Map<String, Cliente> clientesMap,
            Map<String, Zona> zonasMap,
            Set<String> numerosPedidoExistentes
    ) throws ValidationException {

        if(!esAlfanumerico(dto.getNumeroPedido())){
            throw new ValidationException(
                    "Numero de pedido no es alfanumerico: " + dto.getNumeroPedido(),
                    TipoError.NUMERO_PEDIDO_INVALIDO
            );
        }

        if(numerosPedidoExistentes.contains(dto.getNumeroPedido())){
            throw new ValidationException(
                    "Numero de pedido duplicado: " + dto.getNumeroPedido(),
                    TipoError.DUPLICADO
            );
        }

        Cliente cliente = clientesMap.get(dto.getClienteId());
        if (cliente == null) {
            throw new ValidationException(
                    "Cliente no encontrado: " + dto.getClienteId(),
                    TipoError.CLIENTE_NO_ENCONTRADO
            );
        }

        if (!cliente.isActivo()){
            throw new ValidationException(
                    "Cliente no encontrado: " + dto.getClienteId(),
                    TipoError.CLIENTE_NO_ENCONTRADO
            );
        }


        Zona zona = zonasMap.get(dto.getZonaEntrega());
        if (zona == null){
            throw new ValidationException(
                    "Zona no encontrada: " + dto.getZonaEntrega(),
                    TipoError.ZONA_INVALIDA
            );
        }

        LocalDate fechaEntrega = validarFecha(dto.getFechaEntrega());

        if(!EstadoPedido.esValido(dto.getEstado())){
            throw new ValidationException(
                    "Estado de pedido invalido: " + dto.getEstado() + "Debe ser Pendiente, Entregado o Confirmado",
                    TipoError.ESTADO_INVALIDO
            );
        }

        boolean requiereRefrigeracion = parseBoolean(dto.getRequiereRefrigeracion());
        if(requiereRefrigeracion && !zona.isSoporteRefrigeracion()) {
            throw new ValidationException(
                    "La zona ingresada no soporte de refrigeracion: " + dto.getZonaId(),
                    TipoError.CADENA_FRIO_NO_SOPORTADA
            );
        }

        Pedido pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setNumeroPedido(dto.getNumeroPedido());
        pedido.setClienteId(dto.getClienteId().trim());
        pedido.setZonaId(dto.getZonaEntrega().trim());
        pedido.setFechaEntrega(fechaEntrega);
        pedido.setEstado(dto.getEstado().trim().toUpperCase());
        pedido.setRequiereRefrigeracion(requiereRefrigeracion);

        return pedido;
    }

    private boolean esAlfanumerico(String valor){
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }
        return valor.trim().matches(ALFANUMERICO_REGEX);
    }

    private LocalDate validarFecha(String fechaStr) throws ValidationException{
        try {
            LocalDate fecha = LocalDate.parse(fechaStr.trim());
            LocalDate hoy = LocalDate.now(LIMA_ZONE);

            if (fecha.isBefore(hoy)){
                throw new ValidationException(
                        "La fecha de entrega ya pasó: " + fechaStr,
                        TipoError.FECHA_INVALIDA
                );
            }

            return fecha;

        } catch (DateTimeException e){
            throw new ValidationException(
                    "Formato de fecha invalido: " + fechaStr + ". Se espera formato AAAA-MM-DD",
                    TipoError.FECHA_INVALIDA
            );
        }
    }

    private boolean parseBoolean(String valor) throws ValidationException{
        if (valor == null || valor.trim().isEmpty()){
            return false;
        }
        String valorMinusc = valor.trim().toLowerCase();

        if ("true".equals(valorMinusc)){
            return true;
        } else if ("false".equals(valorMinusc)) {
            return false;
        } else {
            throw new ValidationException(
                    "Valor invalido: " + valor + ". Ingrese bien 'true' o 'false'",
                    TipoError.FORMATO_INVALIDO
            );
        }
    }

}
