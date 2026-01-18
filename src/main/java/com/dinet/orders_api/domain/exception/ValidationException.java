package com.dinet.orders_api.domain.exception;


import com.dinet.orders_api.domain.model.TipoError;
import lombok.Getter;

@Getter
public class ValidationException extends Exception {
    private final TipoError tipoError;

    public ValidationException(String mensaje, TipoError tipoError) {
        super(mensaje);
        this.tipoError = tipoError;
    }
}
