package com.dinet.orders_api.application.dto;

import com.dinet.orders_api.domain.model.TipoError;
import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorLineaDto {
    private int numeroLinea;
    private String motivo;
    private TipoError tipo;

}
