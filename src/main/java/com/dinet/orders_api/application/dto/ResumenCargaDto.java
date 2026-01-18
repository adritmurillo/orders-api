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

    @Builder.Default
    private List<ErrorLineaDto> detalleErrores = new ArrayList<>();

    @Builder.Default
    private Map<TipoError, Integer> erroresAgrupados = new HashMap<>();

    public void agregarExito(){
        this.guardados++;
        this.totalProcesados++;
    }

    public void agregarError(int numeroLinea, String motivo, TipoError tipo){
        this.conError++;
        this.totalProcesados++;

        ErrorLineaDto error = ErrorLineaDto.builder()
                .numeroLinea(numeroLinea)
                .motivo(motivo)
                .tipo(tipo)
                .build();

        this.detalleErrores.add(error);

        if(this.erroresAgrupados.containsKey(tipo)){
            int cantidadActual = this.erroresAgrupados.get(tipo);
            this.erroresAgrupados.put(tipo, cantidadActual + 1);
        } else {
            this.erroresAgrupados.put(tipo, 1);
        }

    }




}
