package com.bcrp.microservices.audit.repository;

import com.bcrp.microservices.audit.entity.AuditEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AuditRepository implements PanacheRepository<AuditEntity> {

    public Uni<List<AuditEntity>> findByEvento(String evento) {
        return list("evento", evento);
    }

    public Uni<List<AuditEntity>> findByReferencia(String referencia) {
        return list("referencia", referencia);
    }

    public Uni<List<AuditEntity>> findByNumeroCuenta(String numeroCuenta) {
        return list("numeroCuenta", numeroCuenta);
    }
}