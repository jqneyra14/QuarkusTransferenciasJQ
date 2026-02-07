package com.bcrp.microservices.account.dto;
import com.bcrp.microservices.account.entity.AccountEntity;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AccountDTO {

    public Long id;

    @NotBlank
    @Size(min = 10, max = 20)
    public String numero;

    @NotNull
    public Long customerId;

    @NotNull
    @DecimalMin(value = "0.0")
    public BigDecimal saldo;

    @NotBlank
    public String moneda;

    @NotBlank
    public String tipoCuenta;

    public Boolean activo;

    public AccountDTO() {}

    public AccountDTO(AccountEntity account) {
        this.id = account.getId();
        this.numero = account.getNumero();
        this.customerId = account.getCustomerId();
        this.saldo = account.getSaldo();
        this.moneda = account.getMoneda();
        this.tipoCuenta = account.getTipoCuenta();
        this.activo = account.getActivo();
    }

    public AccountEntity toEntity() {
        AccountEntity account = new AccountEntity();
        account.setId(this.id);
        account.setNumero(this.numero);
        account.setCustomerId(this.customerId);
        account.setSaldo(this.saldo != null ? this.saldo : BigDecimal.ZERO);
        account.setMoneda(this.moneda != null ? this.moneda : "PEN");
        account.setTipoCuenta(this.tipoCuenta);
        account.setActivo(this.activo != null ? this.activo : true);
        return account;
    }
}