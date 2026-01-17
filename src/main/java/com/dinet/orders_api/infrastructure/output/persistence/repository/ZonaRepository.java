package com.dinet.orders_api.infrastructure.output.persistence.repository;


import com.dinet.orders_api.infrastructure.output.persistence.entity.ZonaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZonaRepository extends JpaRepository<ZonaEntity, String> {
}
