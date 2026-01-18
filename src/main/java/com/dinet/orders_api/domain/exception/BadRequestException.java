package com.dinet.orders_api.domain.exception;

public class BadRequestException extends RuntimeException {

    private final String correlationId;

    public BadRequestException(String message, String correlationId) {
        super(message);
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
