package com.bcrp.microservices.transaction.dto;

import com.bcrp.microservices.transaction.entity.TransactionEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    public Long id;
    public String referencia;
    public BigDecimal monto;
    public String numeroCuenta;
    public String bancoOrigen;
    public String bancoDestino;
    public String tipo;
    public String estado;
    public String moneda;
    public String concepto;
    public LocalDateTime fechaCreacion;

    public TransactionDTO() {}

    public TransactionDTO(TransactionEntity entity) {
        this.id = entity.getId();
        this.referencia = entity.getReferencia();
        this.monto = entity.getMonto();
        this.numeroCuenta = entity.getNumeroCuenta();
        this.bancoOrigen = entity.getBancoOrigen();
        this.bancoDestino = entity.getBancoDestino();
        this.tipo = entity.getTipo();
        this.estado = entity.getEstado();
        this.moneda = entity.getMoneda();
        this.concepto = entity.getConcepto();
        this.fechaCreacion = entity.getFechaCreacion();
    }
}