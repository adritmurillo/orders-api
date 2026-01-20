package com.dinet.orders_api.domain.service;

import com.dinet.orders_api.application.dto.PedidoCsvDto;
import com.dinet.orders_api.domain.exception.ValidationException;
import com.dinet.orders_api.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PedidoValidationServiceTest {

    private PedidoValidationService validacion;
    private Map<String, Cliente> clientes;
    private Map<String, Zona> zonas;
    private Set<String> existentes;

    @BeforeEach
    public void setUp(){
        validacion = new PedidoValidationService();

        clientes = new HashMap<>();
        clientes.put("CLI-001", new Cliente("CLI-001", true));
        clientes.put("CLI-002", new Cliente("CLI-002", false));

        zonas = new HashMap<>();
        zonas.put("ZONA1", new Zona("ZONA1", true));
        zonas.put("ZONA2", new Zona("ZONA2", false));

        existentes = new HashSet<>();
        existentes.add("P999");


    }

    @Test
    public void debeValidarPedidoValido() throws ValidationException {
        PedidoCsvDto dto = crearDtoValido();
        Pedido resultado = validacion.validarYConvertir(dto, clientes, zonas, existentes);

        assertNotNull(resultado);
        assertEquals("P001", resultado.getNumeroPedido());
        assertEquals("CLI-001", resultado.getClienteId());
        assertEquals("ZONA1", resultado.getZonaId());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertTrue(resultado.isRequiereRefrigeracion());
    }

    @Test
    public void debeRechazarNumeroPedidoNoAlfanumerico(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setNumeroPedido("P-001-INVALIDO");

        ValidationException ex = assertThrows(
                ValidationException.class, () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.NUMERO_PEDIDO_INVALIDO, ex.getTipoError());

    }

    @Test
    public void debeRechazarDuplicados(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setNumeroPedido("P999");

        ValidationException ex = assertThrows(
                ValidationException.class, () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.DUPLICADO, ex.getTipoError());

    }

    @Test
    public void debeRechazarClienteNoEncontrado(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setClienteId("CLI-999");

        ValidationException ex = assertThrows(
                ValidationException.class, () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.CLIENTE_NO_ENCONTRADO, ex.getTipoError());
    }

    @Test
    public void debeRechazarClienteInactivo(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setClienteId("CLI-002");

        ValidationException ex = assertThrows(
                ValidationException.class, () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.CLIENTE_NO_ENCONTRADO, ex.getTipoError());
    }

    @Test
    public void debeRechazarZonaNoEncontrada(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setZonaEntrega("ZONA999");

        ValidationException ex = assertThrows(
                ValidationException.class, () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.ZONA_INVALIDA, ex.getTipoError());
    }

    @Test
    public void debeRechazarFechaPasada(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setFechaEntrega("2020-01-01");

        ValidationException ex = assertThrows(
                ValidationException.class, () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.FECHA_INVALIDA, ex.getTipoError());
    }

    @Test
    public void debeRechazarFechaFormatoInvalido(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setFechaEntrega("01-01-2025");

        ValidationException ex = assertThrows(
                ValidationException.class, () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );
        assertEquals(TipoError.FECHA_INVALIDA, ex.getTipoError());
    }

    @Test
    public void debeRechazarEstadoInvalido() {
        PedidoCsvDto dto = crearDtoValido();
        dto.setEstado("ESTADO_INVALIDO");
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.ESTADO_INVALIDO, ex.getTipoError());
    }

    @Test
    public void debeRechazarRefrigeracionNoSoporte(){
        PedidoCsvDto dto = crearDtoValido();
        dto.setZonaEntrega("ZONA2");
        dto.setRequiereRefrigeracion("true");

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> validacion.validarYConvertir(dto, clientes, zonas, existentes)
        );

        assertEquals(TipoError.CADENA_FRIO_NO_SOPORTADA, ex.getTipoError());
    }

    @Test
    public void debePermitirPedidoSinRefrigeracionEnZonaSinSoporte() throws ValidationException {
        PedidoCsvDto dto = crearDtoValido();
        dto.setZonaEntrega("ZONA2");
        dto.setRequiereRefrigeracion("false");

        Pedido resultado = validacion.validarYConvertir(dto, clientes, zonas, existentes);

        assertNotNull(resultado);
        assertFalse(resultado.isRequiereRefrigeracion());
    }


    private PedidoCsvDto crearDtoValido(){
        String fecha = LocalDate.now().plusDays(30).toString();
        return PedidoCsvDto.builder()
                .numeroPedido("P001")
                .clienteId("CLI-001")
                .fechaEntrega(fecha)
                .estado("PENDIENTE")
                .zonaEntrega("ZONA1")
                .requiereRefrigeracion("true")
                .numeroLinea(1)
                .build();
    }
}
