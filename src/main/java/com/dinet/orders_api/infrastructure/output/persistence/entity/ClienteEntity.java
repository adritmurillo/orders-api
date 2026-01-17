package com.dinet.orders_api.infrastructure.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ClienteEntity {
    @Id
    private String id; // CLI-111, CLI-112, etc

    @Column(name = "activo")
    private boolean activo;
}
