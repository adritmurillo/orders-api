package com.dinet.orders_api.domain.model;

public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    ENTREGADO;

    public static boolean esValido(String estado){
        if(estado == null || estado.trim().isEmpty()){
            return false;
        }
        try{
            EstadoPedido.valueOf(estado.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e){
            return false;
        }
    }
}
