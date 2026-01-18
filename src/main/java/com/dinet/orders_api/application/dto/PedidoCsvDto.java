package com.dinet.orders_api.application.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PedidoCsvDto {
    private String numeroPedido;
    private String clienteId;
    private String zonaId;
    private String fechaEntrega;
    private String estado;
    private String zonaEntrega;
    private String requiereRefrigeracion;

    private int numeroLinea;



}
