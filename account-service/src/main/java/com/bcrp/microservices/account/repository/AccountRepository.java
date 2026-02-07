package com.bcrp.microservices.account.repository;


import com.bcrp.microservices.account.entity.AccountEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class AccountRepository implements PanacheRepository<AccountEntity> {

    public Uni<AccountEntity> findByNumero(String numero) {
        return find("numero", numero).firstResult();
    }

    public Uni<List<AccountEntity>> findByCustomerId(Long customerId) {
        return list("customerId = ?1 AND activo = true", customerId);
    }

    public Uni<List<AccountEntity>> findActiveAccounts() {
        return list("activo", true);
    }

    // Operaciones de negocio
    public Uni<Boolean> acreditar(Long accountId, BigDecimal monto) {
        return findById(accountId)
                .onItem().ifNotNull().transformToUni(account -> {
                    account.setSaldo(account.getSaldo().add(monto));
                    account.preUpdate();
                    return persist(account).map(a -> true);
                })
                .onItem().ifNull().continueWith(false);
    }

    public Uni<Boolean> debitar(Long accountId, BigDecimal monto) {
        return findById(accountId)
                .onItem().ifNotNull().transformToUni(account -> {
                    if (account.getSaldo().compareTo(monto) < 0) {
                        return Uni.createFrom().item(false); // Saldo insuficiente
                    }
                    account.setSaldo(account.getSaldo().subtract(monto));
                    account.preUpdate();
                    return persist(account).map(a -> true);
                })
                .onItem().ifNull().continueWith(false);
    }

    public Uni<Boolean> validarSaldo(Long accountId, BigDecimal monto) {
        return findById(accountId)
                .map(account -> account != null &&
                        account.getActivo() &&
                        account.getSaldo().compareTo(monto) >= 0);
    }
}