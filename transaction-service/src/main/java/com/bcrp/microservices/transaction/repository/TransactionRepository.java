package com.bcrp.microservices.transaction.repository;

import com.bcrp.microservices.transaction.entity.TransactionEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<TransactionEntity> {

    public Uni<TransactionEntity> findByReferencia(String referencia) {
        return find("referencia", referencia).firstResult();
    }

    public Uni<List<TransactionEntity>> findByEstado(String estado) {
        return list("estado", estado);
    }

    public Uni<List<TransactionEntity>> findByNumeroCuenta(String numeroCuenta) {
        return list("numeroCuenta", numeroCuenta);
    }

    public Uni<List<TransactionEntity>> findByTipo(String tipo) {
        return list("tipo", tipo);
    }
}