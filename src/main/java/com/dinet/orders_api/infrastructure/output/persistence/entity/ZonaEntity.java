package com.dinet.orders_api.infrastructure.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "zonas")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ZonaEntity {
    @Id
    private String id; // ZONA1, ZONA5, etc
    @Column(name = "soporte_refrigeracion")
    private boolean soporteRefrigeracion;
}
