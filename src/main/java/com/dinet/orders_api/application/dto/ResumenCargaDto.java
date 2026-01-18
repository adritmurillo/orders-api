package com.dinet.orders_api.application.dto;

import com.dinet.orders_api.domain.model.TipoError;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResumenCargaDto {
    private int totalProcesados;
    private int conError;
    private int guardados;
    private String correlationId;

    @Builder.Default
    private List<ErrorLineaDto> detalleErrores = new ArrayList<>();

    @Builder.Default
    private Map<TipoError, Integer> erroresAgrupados = new HashMap<>();

    public void incrementarProcesados(){
        this.totalProcesados++;
    }

    public void agregarExito(){
        this.guardados++;
    }

    public void agregarError(int numeroLinea, String motivo, TipoError tipo){
        this.conError++;
        ErrorLineaDto error = ErrorLineaDto.builder()
                .numeroLinea(numeroLinea)
                .motivo(motivo)
                .tipo(tipo)
                .build();

        this.detalleErrores.add(error);
        this.erroresAgrupados.merge(tipo, 1, Integer :: sum);

    }

}
