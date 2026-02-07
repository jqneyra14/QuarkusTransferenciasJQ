package com.bcrp.microservices.transaction.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AbonoDTO {

    @NotBlank
    public String numeroCuenta;

    @NotNull
    @DecimalMin(value = "0.01")
    public BigDecimal monto;

    @NotBlank
    public String bancoOrigen;

    @NotBlank
    public String referencia;

    public String moneda = "PEN";
    public String concepto;
}