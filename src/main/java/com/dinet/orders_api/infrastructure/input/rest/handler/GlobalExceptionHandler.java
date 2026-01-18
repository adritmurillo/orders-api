package com.dinet.orders_api.infrastructure.input.rest.handler;

import com.dinet.orders_api.domain.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MissingRequestHeaderException;
import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            WebRequest request
    ) {
        log.warn("Bad Request: {} - CorrelationId: {}", ex.getMessage(), ex.getCorrelationId());

        ErrorResponse error = new ErrorResponse(
                "BAD_REQUEST",
                ex.getMessage(),
                null,
                ex.getCorrelationId(),
                LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            WebRequest request
    ) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Archivo demasiado grande - CorrelationId: {}", correlationId);

        ErrorResponse error = new ErrorResponse(
                "FILE_TOO_LARGE",
                "El archivo excede el tamaño máximo permitido",
                null,
                correlationId,
                LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(
            MissingRequestHeaderException ex,
            WebRequest request
    ) {
        String correlationId = UUID.randomUUID().toString();
        log.warn("Header faltante: {} - CorrelationId: {}", ex.getHeaderName(), correlationId);

        ErrorResponse error = new ErrorResponse(
                "MISSING_HEADER",
                "Header obligatorio faltante: " + ex.getHeaderName(),
                null,
                correlationId,
                LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Error inesperado - CorrelationId: {}", correlationId, ex);

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "Error interno del servidor",
                new String[]{ex.getMessage()},
                correlationId,
                LocalDateTime.now().toString()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    public record ErrorResponse(
            String code,
            String message,
            String[] details,
            String correlationId,
            String timestamp
    ) {}
}
