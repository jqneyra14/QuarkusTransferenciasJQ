package com.bcrp.microservices.transaction.dto;

public class DetalleCompletoDTO {

    public TransactionDTO transaccion;
    public Object cuenta; // AccountDTO
    public Object cliente; // CustomerDTO

    public DetalleCompletoDTO() {}

    public DetalleCompletoDTO(TransactionDTO transaccion, Object cuenta, Object cliente) {
        this.transaccion = transaccion;
        this.cuenta = cuenta;
        this.cliente = cliente;
    }
}