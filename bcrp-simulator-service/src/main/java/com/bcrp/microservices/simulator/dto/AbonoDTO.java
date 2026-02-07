package com.bcrp.microservices.simulator.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AbonoDTO {

    @NotBlank(message = "El número de cuenta es requerido")
    public String numeroCuenta;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    public BigDecimal monto;

    @NotBlank(message = "El banco origen es requerido")
    public String bancoOrigen;

    @NotBlank(message = "La referencia es requerida")
    public String referencia;

    public String moneda = "PEN";

    public LocalDateTime fechaOperacion = LocalDateTime.now();

    public String concepto;

    // Constructores
    public AbonoDTO() {}

    public AbonoDTO(String numeroCuenta, BigDecimal monto, String bancoOrigen, String referencia) {
        this.numeroCuenta = numeroCuenta;
        this.monto = monto;
        this.bancoOrigen = bancoOrigen;
        this.referencia = referencia;
    }
}