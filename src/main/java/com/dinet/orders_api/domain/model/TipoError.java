package com.dinet.orders_api.domain.model;

public enum TipoError {
    CLIENTE_NO_ENCONTRADO("Cliente ingresado no encontrado"),
    ZONA_INVALIDA("La zona ingresada no es valida"),
    FECHA_INVALIDA("La fecha ingresada no es valida"),
    ESTADO_INVALIDO("El estado ingresado no es valido"),
    DUPLICADO("El numero de pedido ya existe"),
    CADENA_FRIO_NO_SOPORTADA("La zona no tiene soporte de refrigeracion"),
    FORMATO_INVALIDO("El formato del campo es invalido"),
    NUMERO_PEDIDO_INVALIDO("El numero de pedido es invalido");

    private final String descripcion;

    TipoError(String descripcion){
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
